# SDD ledger — plan: /Users/hwasoojeong/Desktop/codex/android_tools/plans/2026-09-02-automatic-bootstrap-launcher-implementation.md

Worktree: /Users/hwasoojeong/Desktop/hsjeong/study/app-support-tools/.worktrees/automatic-bootstrap-launcher
Branch: feature/automatic-bootstrap-launcher
Merge base: 8316c4f
Spec: /Users/hwasoojeong/Desktop/codex/android_tools/design/2026-09-02-minimal-host-integration-design.md

## Setup evidence

- Baseline attempt 1 failed because the worktree had no git-ignored local.properties.
- Added worktree-only local.properties with sdk.dir=/Users/hwasoojeong/Library/Android/sdk.
- Baseline attempt 2 reached Android compilation. support-tools compiled, then app:checkDebugAarMetadata failed because sample compileSdk 34 is lower than the dependencies' required API 35.
- User approved changing only the sample compileSdk from 34 to 35. support-tools remains 35 and starbucks_android remains untouched.
- adb devices reported no connected devices.

## Preflight rulings

- Ruling: Add sample app compileSdk 35 to Task 1 — required to establish the plan's buildable baseline; if wrong, the sample could diverge from its intended minimum host SDK, but API 35 matches the library and AGP 8.7 support ceiling.
- Ruling: With no connected device, Tasks 3, 4, 5, and 6 must compile instrumentation test APKs but cannot claim connected tests pass — if wrong, runtime Provider or launcher behavior may remain unverified until a device is available.
- Ruling: Task 1 includes the minimal correction of the sample's pre-existing invalid `_root_ide_package_...SupportToolsActivity.start(this)` reference to `SupportToolsActivity.start(this)` — Task 1's acceptance criteria require sample debug/release builds to establish a usable baseline; Task 5 will later remove this direct library UI reference for release isolation.

## Preflight consistency scan

| Task(s) | Producer / consumer relationship | Finding |
|---|---|---|
| Task 1 self | Wrapper 8.9 and sample compileSdk 35 must make the listed baseline tasks build | Internally consistent after the approved compileSdk ruling. |
| Task 2 self | Failing tests define InitializationGate and volume shortcut API implemented later in the same task | Internally consistent; test packages can access Kotlin internal declarations in the same module. |
| Task 3 self | Provider consumes SupportTools.initialize and isInitializedForTests from Task 2 | Internally consistent; runtime connected assertion cannot execute without a device. |
| Task 4 self | Launcher test consumes SupportToolsActivity exposed by the manifest change | Internally consistent; debug test APK compilation remains available without a device. |
| Task 5 self | Sample release removes its source reference before changing implementation to debugImplementation | Internally consistent and preserves release compilation. |
| Task 6 self | Verification consumes all earlier artifacts and commits | Internally consistent; connected result must be reported as not run. |
| Tasks 1 → 5 | Both modify app/build.gradle.kts | No conflict: Task 1 changes compileSdk; Task 5 changes only the support-tools dependency configuration and an unused import. |
| Tasks 2 → 3 | Task 2 produces initialization gate and internal status; Task 3 invokes and tests them | No conflict. |
| Tasks 3 → 4 | Both modify support-tools/src/main/AndroidManifest.xml | No conflict: Provider and launcher declarations are additive. |
| Tasks 4 → 5 | Launcher test requires the library on the debug test classpath; Task 5 narrows the dependency to debugImplementation | No conflict: Android instrumentation tests target debug and retain the library dependency. |
| Tasks 1–5 → 6 | Task 6 verifies the complete phase | No conflict; final diff range must cover five implementation commits after the setup commit. |

## Task status

- Task 1: complete (`8316c4f..1add6c3`), independent review clean. Verified Gradle 8.9 and successful `:support-tools:testDebugUnitTest`, `:support-tools:assembleDebug`, `:app:assembleDebug`, and `:app:assembleRelease`. Lifecycle was aligned to 2.8.3 because 2.9.x/2.10.0 lint detectors are binary-incompatible with the project's AGP 8.7.2/Kotlin 1.9.25 toolchain.
- Task 2: paused before implementation at user request due usage limits. The Task 2 agent was interrupted before any file edits, test run, report, or commit. Worktree is clean at `1add6c3`. Resume by dispatching a fresh Task 2 implementer from `task-2-brief.md` and start with the RED unit tests.
- Ruling: Task 2 replaces the plan's single all-or-nothing gate around three irreversible registrations with per-side-effect idempotent boundaries — the design's binding guarantees require both retry after a partial failure and no duplicate successful registrations; if wrong, the added coordination may be more internal structure than necessary.
- Ruling: Task 2 permits the smallest necessary change to `ScreenNameOverlayManager.kt` and covering tests — registration must not depend on the first call's feature value so later configuration can activate it; if wrong, this widens Task 2 beyond the plan's original five-file list.
- Task 2: fix round 1/5 (2 addressed, 1 open — installed `DebugKeyCallback` still ignores later disablement because the predicate is checked only at installation; commits `983ab02..e68f654`).
- Task 2: fix round 2/5 (0 fully addressed, 1 open — immediate disablement works, but forwarding reconstructs `KeyEvent` and loses repeat/metadata; commits `e68f654..63c75f3`).
- Task 2: fix round 3/5 (forwarding regression addressed, 1 open — shared key handler still invokes `onTriggered` instead of owning only state and consume/forward decisions; commits `63c75f3..d96ea09`).
- Task 2: fix round 4/5 (1 addressed, 0 open — key handler is decision-only and original Android events are preserved; commits `d96ea09..20dadd2`).
- Task 2: complete (commits `1add6c3..20dadd2`, scoped re-review clean).
- Task 3: complete (commits `20dadd2..f587e74`, review clean). Instrumentation APK and merged manifests verified; connected runtime tests not run because no device/ADB is available.
- Task 4: complete (commits `f587e74..badd1d9`, review clean). Instrumentation APK and merged debug manifest show two launcher entries; connected runtime test not run because no device/ADB is available.
- Task 5: complete (commits `badd1d9..f24eb64`, review clean). Debug retains Support Tools; release APK/manifest excludes Support Tools classes, components, FileProvider, and development permission. Connected runtime tests not run because no device/ADB is available.
- Ruling: Task 6 scope verification uses merge-base range `8316c4f..HEAD`, not the plan's `HEAD~5..HEAD` — review-fix and prerequisite commits make a five-commit window incomplete; if wrong, the verification includes extra setup changes that should have been excluded.
- Task 6: fix round 1/5 (3 evidence findings addressed, 0 open — complete DEX namespace, variant dependency boundary, exact artifact commands/results; no code commits).
- Task 6: complete (verification report scoped re-review clean; HEAD remains `f24eb64`).
- Final whole-branch review: paused at user request after dispatch because usage was at 2%; reviewer was interrupted before returning a verdict. No source, test, config, commit, or Git-state change occurred.
- Final whole-branch review: resumed on 2026-09-03 from clean `feature/automatic-bootstrap-launcher` HEAD `f24eb64`; restart with a fresh reviewer using `review-8316c4f..f24eb64.diff`.
- Final whole-branch review: with fixes — Important: `DeepLinkManager.setDeepLinkList` still injects a host-specific URL instead of retaining only caller-provided links. Minor: `InitializationGateTest` lacks real contention coverage for at-most-once execution and failure/retry behavior.
- Final fix: complete — removed the host-specific deep-link default and added caller-only regression plus deterministic initialization-gate contention/failure-retry coverage; required unit and aggregate Gradle verification passed.
- Final fix scoped re-review: clean — both final-review findings addressed; no Critical, Important, or Minor breakage introduced in `f24eb64..fe15c6c`.
