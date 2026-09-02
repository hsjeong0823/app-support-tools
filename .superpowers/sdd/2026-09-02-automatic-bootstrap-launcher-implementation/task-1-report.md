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
