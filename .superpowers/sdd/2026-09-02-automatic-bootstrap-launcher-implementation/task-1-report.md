# Task 1 report: Establish the supported build toolchain

## Status

Implemented and committed as `5ec4d623e7c57c6717c1512894321720ae518555` (`build: align Gradle wrapper with AGP`).

## Changes

- Updated the Gradle wrapper to the reproducible Gradle 8.9 binary distribution and the required SHA-256 checksum.
- Updated the sample app's `compileSdk` from 34 to 35.
- Left `support-tools` compile SDK, both modules' min SDK and target SDK values, and all other wrapper properties unchanged.
- Did not modify `starbucks_android`.

## Verification

- `./gradlew --version`: passed; reports Gradle 8.9.
- `./gradlew :support-tools:testDebugUnitTest :support-tools:assembleDebug :app:assembleDebug :app:assembleRelease`: attempted. Support-tools unit tests and debug AAR assembly completed, but the build stopped at `:app:compileDebugKotlin` because the pre-existing sample source references unresolved `_root_ide_package_` in `app/src/main/java/com/hsjeong/supporttools/sample/ui/MainActivity.kt`.
- Final self-review confirmed the commit contains only the two requested files and the worktree is clean.

## Concern

The baseline sample app compilation failure is outside this task's two-file scope. Fixing it would require changing `MainActivity.kt`, which was explicitly not requested.

## Follow-up baseline fix and verification

Per the acceptance ruling, the sample launcher reference in `app/src/main/java/com/hsjeong/supporttools/sample/ui/MainActivity.kt` was corrected to call the already-imported `SupportToolsActivity.start(this)` directly. This is a minimal sample-only fix; no Starbucks files were modified.

The exact command `./gradlew :support-tools:testDebugUnitTest :support-tools:assembleDebug :app:assembleDebug :app:assembleRelease` was rerun. The support-tools unit test, support-tools debug assembly, app debug assembly, and app release compilation/package steps completed, but the command still fails at `:support-tools:lintVitalAnalyzeRelease` due an existing Android Lint `IncompatibleClassChangeError` involving `androidx.lifecycle.lint.NonNullableMutableLiveDataDetector` and `org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall`. No task-scoped lint configuration was changed to mask this toolchain/dependency failure.

## Investigation: `lintVitalAnalyzeRelease` failure (systematic-debugging Phases 1–3)

### Phase 1 — reproduction and evidence

- Reproduced consistently with `./gradlew :support-tools:lintVitalAnalyzeRelease --stacktrace`; it exits 1 at `:support-tools:lintVitalAnalyzeRelease`.
- The full cause chain is Gradle `TaskExecutionException` → worker `WorkExecutionException` → lint `RuntimeException`. The innermost message is `Found class org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall, but interface was expected`.
- The stack identifies `androidx.lifecycle.lint.NonNullableMutableLiveDataDetector` (`NonNullableMutableLiveDataDetector.kt:140`) while visiting `SupportTools.kt`; this is a detector/classpath linkage error, not a source compilation error.
- The same lint task run with the cached pre-change Gradle 9.1.0 binary fails with the identical `KaCallableMemberCall`/`NonNullableMutableLiveDataDetector` error. Therefore changing the wrapper from 9.1.0 to 8.9 is not the trigger or resolution.

### Phase 2 — dependency and configuration pattern

- The declared toolchain is AGP `8.7.2` and Kotlin Gradle plugin `1.9.25`.
- `support-tools` declares `androidx.lifecycle:lifecycle-process:2.10.0`; resolved release runtime dependencies align the lifecycle family to `2.10.0`.
- The resolved graph upgrades the Kotlin runtime from the plugin's requested `org.jetbrains.kotlin:kotlin-stdlib:1.9.25` to `2.0.21` (shown as `1.9.25 -> 2.0.21`). Lifecycle 2.10.0 and related dependencies also resolve Kotlin stdlib/common at `2.0.21`.
- This creates a mixed Kotlin toolchain: Kotlin compiler/plugin `1.9.25`, Kotlin analysis/lint integration encountering Kotlin 2.0.21 APIs, and the lifecycle lint detector compiled against an incompatible `KaCallableMemberCall` shape. The older Gradle 9.1 comparison preserves the same AGP/Kotlin/lifecycle declarations and reproduces the failure.

### Phase 3 — hypothesis and smallest proposed test

Hypothesis: the root cause is the incompatible combination of lifecycle `2.10.0` (which brings Kotlin 2.0.21-era analysis/runtime types) with the project Kotlin plugin `1.9.25` under AGP `8.7.2`; the lifecycle lint detector then links against a different `KaCallableMemberCall` binary shape and crashes. The evidence supports a dependency/toolchain mismatch rather than Gradle wrapper behavior.

Smallest proposed test: in a disposable working copy or temporary Gradle init-script dependency substitution, resolve the lifecycle family to the last pre-2.10 line already compatible with Kotlin 1.9.x (candidate `2.9.2`) and rerun only `:support-tools:lintVitalAnalyzeRelease`. A passing lint task would confirm the version-mismatch hypothesis; if it still crashes, next isolate the detector by temporarily disabling `NullSafeMutableLiveData` for the same task. No project files were changed for this investigation.

## RED→GREEN test: lifecycle 2.9.2

- Changed only the active `support-tools/build.gradle.kts` dependency literal from `androidx.lifecycle:lifecycle-process:2.10.0` to `2.9.2`.
- Ran `./gradlew :support-tools:lintVitalAnalyzeRelease --stacktrace`.
- Result: **RED**. The task failed with the same `Found class org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall, but interface was expected` error in `androidx.lifecycle.lint.NonNullableMutableLiveDataDetector`.
- Reverted the experimental dependency literal to `androidx.lifecycle:lifecycle-process:2.10.0` immediately, as instructed. No production dependency change was committed.
- Because the focused test remained red, the full Task 1 verification was not run and no second fix was attempted.

## RED→GREEN test: lifecycle 2.8.3

- Changed only the active `support-tools/build.gradle.kts` dependency literal from `androidx.lifecycle:lifecycle-process:2.10.0` to `2.8.3`.
- `./gradlew :support-tools:lintVitalAnalyzeRelease --stacktrace` passed (`BUILD SUCCESSFUL`). This is the focused GREEN result.
- `./gradlew :support-tools:dependencyInsight --dependency androidx.lifecycle:lifecycle-livedata-core --configuration releaseRuntimeClasspath` selected `androidx.lifecycle:lifecycle-livedata-core:2.8.3`; the report shows lifecycle-process, runtime, livedata, viewmodel, and related artifacts all at 2.8.3, with conflict resolution against older 2.x requests.
- Full verification was then run: `./gradlew --version` reported Gradle 8.9. The requested aggregate build reached support-tools unit tests, support-tools debug assembly, app debug assembly, app release compilation and packaging, and support-tools release lint successfully.
- The aggregate command still exits RED at `:app:lintVitalAnalyzeRelease` with the same `KaCallableMemberCall` / `NonNullableMutableLiveDataDetector` ICCE. The app's own version-catalog `androidx.lifecycle:lifecycle-runtime-ktx` remains at 2.10.0, so the support-tools-only correction does not change the app release lint classpath.
- The 2.8.3 support-tools correction is retained and committed separately; no Starbucks files were touched.

## Follow-up investigation: why lifecycle 2.9.2 stayed RED

The cached metadata explains the unchanged failure. `lifecycle-process:2.9.2` is a real selected artifact (its POM declares `lifecycle-runtime:[2.9.2]` and the lifecycle family constraints at `2.9.2`), but its own dependency metadata requires `org.jetbrains.kotlin:kotlin-stdlib:2.0.21`. The `2.10.0` POM likewise requires Kotlin stdlib `2.0.21`. Thus changing only `lifecycle-process` from 2.10.0 to 2.9.2 does not remove the Kotlin 2.0.21 runtime from the lint analysis classpath, which is consistent with the identical ICCE.

For comparison, cached `lifecycle-process:2.8.3` metadata declares lifecycle runtime dependencies at `2.8.3` and Kotlin stdlib `1.8.22`; it is the first available cached family that does not request Kotlin 2.0.x. The 2.9.2 module metadata was produced by Gradle 8.13 and the 2.10.0 metadata by Gradle 9.1.0, but the POM dependency declarations independently show the relevant Kotlin requirement.

New single hypothesis: lifecycle 2.9.2 remained incompatible because it also requires Kotlin 2.0.21; the problematic boundary is the lifecycle 2.9.x+ family versus this project’s Kotlin plugin 1.9.25, not specifically the 2.10.0 patch line.

Smallest next test: use a disposable dependency substitution to resolve the complete lifecycle family to `2.8.3` (not just `lifecycle-process`) and rerun only `:support-tools:lintVitalAnalyzeRelease`. A green result would confirm the first-compatible-family hypothesis; a red result would show the detector/lint classpath is incompatible independently of lifecycle version. No production files were changed during this follow-up investigation; only this report is modified.
