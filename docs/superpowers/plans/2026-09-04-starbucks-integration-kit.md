# Starbucks Support Tools Integration Kit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Package the Starbucks-specific support-tools wiring as one copy-ready directory while reducing existing host edits to one Gradle apply line and one authority-resolver call.

**Architecture:** `app-support-tools` owns a canonical, value-free integration kit. Its Gradle script registers the copied Java/debug manifest sources and generates a debug-only JSON resource from the host-owned `uri.properties`; Starbucks consumes a local copy of the directory and retains only the two explicit integration hooks.

**Tech Stack:** Android Gradle Plugin 8.7.2, Gradle Groovy DSL, Java 11-compatible host adapter code, Android `ContentProvider`, JUnit 4, Gradle fixture tasks

**Spec:** `docs/superpowers/specs/2026-09-04-starbucks-integration-kit-design.md`

## Global Constraints

- Execute this plan with a single agent, in task order.
- Work on the `app-support-tools` `dev` branch.
- Never run `git add`, `git commit`, or any history/index/remote-changing Git command in `starbucks_android`.
- Preserve all pre-existing Starbucks staged, unstaged, and untracked changes.
- The canonical kit contains property key names only, never Starbucks URL values, credentials, tokens, or authentication data.
- Generated JSON exists only below `starbucks_android/app/build/` and is never copied into the canonical kit.
- Apart from dependency/module connection, existing Starbucks files retain only one `apply from` line and one `NewUriAuthorityResolver.resolve(authority)` call.
- Release variants contain no provider, generated environment JSON, support-tools runtime dependency, UI, or initialization behavior.
- Run only task-specific tests during implementation; run the broader debug/release verification once at the end.

## File Structure

### Canonical files in app-support-tools

- Create `integrations/starbucks/support-tools-integration/support-tools-integration.gradle`: property-key mapping, validation, JSON generation, Android source/manifest/resource registration.
- Create `integrations/starbucks/support-tools-integration/src/main/java/com/starbucks/co2/constants/NewUriAuthorityResolver.java`: dependency-free identity-fallback registry used by all host variants.
- Create `integrations/starbucks/support-tools-integration/src/debug/java/com/starbucks/co2/debug/StarbucksSupportToolsEnvironmentProvider.java`: debug-only support-tools initialization.
- Create `integrations/starbucks/support-tools-integration/src/debug/AndroidManifest.xml`: non-exported provider registration.
- Create `integrations/starbucks/support-tools-integration/README.md`: copy and two-line integration instructions, dependency alternatives, security/release notes.
- Create `integrations/starbucks/verification/settings.gradle`: isolated fixture settings.
- Create `integrations/starbucks/verification/build.gradle`: executable checks for valid generation, missing/blank properties, mapping uniqueness, kit layout, and sensitive-value absence.
- Create `integrations/starbucks/verification/fixtures/valid-uri.properties`: placeholder-only complete input.
- Create `integrations/starbucks/verification/fixtures/missing-uri.properties`: placeholder-only input missing one required key.
- Create `integrations/starbucks/verification/fixtures/blank-uri.properties`: placeholder-only input with one blank required key.

### Local-only Starbucks changes

- Create `support-tools-integration/` by copying the canonical kit directory.
- Modify `app/build.gradle`: remove inline generated-resource paths and JSON task; add the one-line external script application.
- Modify `app/src/main/java/com/starbucks/co2/constants/NewURI.java`: retain its existing single resolver call unchanged.
- Delete `app/src/main/java/com/starbucks/co2/constants/NewUriAuthorityResolver.java`: replaced by the copied main source directory.
- Delete `app/src/debug/java/com/starbucks/co2/debug/StarbucksSupportToolsEnvironmentProvider.java`: replaced by the copied debug source directory.
- Delete `app/src/debug/AndroidManifest.xml`: replaced by the copied debug manifest, provided that it contains no unrelated pre-existing entries.

---

### Task 1: Extract and Test the Gradle Configuration Generator

**Files:**
- Create: `integrations/starbucks/support-tools-integration/support-tools-integration.gradle`
- Create: `integrations/starbucks/verification/settings.gradle`
- Create: `integrations/starbucks/verification/build.gradle`
- Create: `integrations/starbucks/verification/fixtures/valid-uri.properties`
- Create: `integrations/starbucks/verification/fixtures/missing-uri.properties`
- Create: `integrations/starbucks/verification/fixtures/blank-uri.properties`

**Interfaces:**
- Consumes: a Java `Properties` instance whose keys match the Starbucks-owned names listed below.
- Produces: `ext.supportToolsBuildEnvironmentConfig(Properties)` returning a map with `schemaVersion: 1` and `services`; Android debug tasks named `generate<Variant>SupportToolsEnvironment`; `support_tools_environments.json` below the variant's generated `res/raw` directory.

- [ ] **Step 1: Create fixture inputs containing placeholders only**

Use non-routable placeholder authorities such as `msr-dev.example.invalid`. The valid fixture must define every unique key in this mapping:

```groovy
def supportToolsServices = [
    [id: 'msr', dev: 'msrDevUri', stg: 'msrStgUri', real: 'msrRealUri', aliases: ['msrTestUri', 'msrDrUri']],
    [id: 'web', dev: 'webDevUri', stg: 'webStgUri', real: 'webRealUri', aliases: ['webTestUri', 'webDrUri']],
    [id: 'auth', dev: 'authDevUri', stg: 'authStgUri', real: 'authRealUri', aliases: ['authTestUri', 'authDrUri']],
    [id: 'auth-manage', dev: 'authManageDevUri', stg: 'authManageStgUri', real: 'authManageRealUri', aliases: ['authManageTestUri', 'authManageDrUri']],
    [id: 'voc', dev: 'vocDevUri', stg: 'vocStgUri', real: 'vocRealUri', aliases: []],
    [id: 'log', dev: 'logDevUri', stg: 'logDevUri', real: 'logRealUri', aliases: []],
    [id: 'on-demand', dev: 'onDemandDevUri', stg: 'onDemandStgUri', real: 'onDemandRealUri', aliases: ['onDemandTestUri', 'onDemandDrUri']],
    [id: 'gift-deliver', dev: 'giftDeliverDevUri', stg: 'giftDeliverStgUri', real: 'giftDeliverRealUri', aliases: ['giftDeliverTestUri', 'giftDeliverDrUri']],
    [id: 'gift-api', dev: 'giftDeliverApiDevUri', stg: 'giftDeliverApiStgUri', real: 'giftDeliverApiRealUri', aliases: []],
    [id: 'online-store', dev: 'onlineStoreDevUri', stg: 'onlineStoreStgUri', real: 'onlineStoreRealUri', aliases: []],
    [id: 'biz-mall', dev: 'bizMallDevUri', stg: 'bizMallStgUri', real: 'bizMallRealUri', aliases: []],
    [id: 'payment', dev: 'paymentDevUri', stg: 'paymentStgUri', real: 'paymentRealUri', aliases: ['paymentTestUri', 'paymentDrUri']],
    [id: 'web-external', dev: 'webExternalDevUri', stg: 'webExternalStgUri', real: 'webExternalRealUri', aliases: ['webExternalTestUri', 'webExternalDrUri']],
    [id: 'frequency', dev: 'webFrequencyDevUri', stg: 'webFrequencyStgUri', real: 'webFrequencyRealUri', aliases: []],
    [id: 'frequency-defect', dev: 'webFrequencyDefectReportDevUri', stg: 'webFrequencyDefectReportStgUri', real: 'webFrequencyDefectReportRealUri', aliases: []],
]
```

The missing fixture omits `msrRealUri`; the blank fixture includes `msrRealUri=`.

- [ ] **Step 2: Write failing Gradle fixture checks**

The fixture build applies the integration script without an Android plugin and calls its pure configuration closure. Define this failure-capturing helper, then register one aggregate `verifyIntegrationScript` task whose actions assert:

```groovy
def captureFailure = { Class<? extends Throwable> expectedType, Closure action ->
    try {
        action.call()
        throw new AssertionError("Expected ${expectedType.simpleName}")
    } catch (Throwable failure) {
        assert expectedType.isInstance(failure)
        failure
    }
}

def valid = supportToolsBuildEnvironmentConfig(loadFixture('valid-uri.properties'))
assert valid.schemaVersion == 1
assert valid.services*.id.size() == 15
assert valid.services*.id.toSet().size() == 15
assert valid.services.find { it.id == 'msr' }.targets.keySet() == ['DEV', 'STG', 'REAL'] as Set
assert valid.services.find { it.id == 'log' }.targets.STG == 'log-dev.example.invalid'

def missing = captureFailure(GradleException) {
    supportToolsBuildEnvironmentConfig(loadFixture('missing-uri.properties'))
}
assert missing.message == 'Missing or blank uri.properties key: msrRealUri'

def blank = captureFailure(GradleException) {
    supportToolsBuildEnvironmentConfig(loadFixture('blank-uri.properties'))
}
assert blank.message == 'Missing or blank uri.properties key: msrRealUri'
```

The fixture must write the valid map with `groovy.json.JsonOutput`, parse it with `groovy.json.JsonSlurper`, and assert the same schema/service count after the round trip.

- [ ] **Step 3: Run the fixture and confirm it fails before the script exists**

Run:

```bash
./gradlew -p integrations/starbucks/verification verifyIntegrationScript
```

Expected: `FAILURE` because `../support-tools-integration/support-tools-integration.gradle` or `supportToolsBuildEnvironmentConfig` does not exist.

- [ ] **Step 4: Implement the pure mapping and validation closure**

In `support-tools-integration.gradle`, define the exact service list from Step 1 and expose:

```groovy
ext.supportToolsBuildEnvironmentConfig = { Properties properties ->
    def ids = supportToolsServices*.id
    if (ids.size() != ids.toSet().size()) {
        throw new GradleException('Duplicate support-tools service id')
    }
    def requiredKeys = supportToolsServices.collectMany { service ->
        [service.dev, service.stg, service.real] + service.aliases
    }.toSet()
    requiredKeys.each { key ->
        def value = properties.getProperty(key)
        if (value == null || value.trim().isEmpty()) {
            throw new GradleException("Missing or blank uri.properties key: ${key}")
        }
    }
    [
        schemaVersion: 1,
        services: supportToolsServices.collect { service ->
            def keys = ([service.dev, service.stg, service.real] + service.aliases).unique()
            [
                id: service.id,
                sourceAuthorities: keys.collect { properties.getProperty(it) }.unique(),
                targets: [
                    DEV: properties.getProperty(service.dev),
                    STG: properties.getProperty(service.stg),
                    REAL: properties.getProperty(service.real),
                ],
            ]
        },
    ]
}
```

Do not include any URL value in the script.

- [ ] **Step 5: Add Android application wiring guarded by the plugin**

Use `plugins.withId('com.android.application')` so the pure fixture can apply the script without Android. Inside the callback:

```groovy
def kitDir = buildscript.sourceFile.parentFile
android.sourceSets.main.java.srcDir(new File(kitDir, 'src/main/java'))
android.sourceSets.debug.java.srcDir(new File(kitDir, 'src/debug/java'))
android.sourceSets.debug.manifest.srcFile(new File(kitDir, 'src/debug/AndroidManifest.xml'))

android.applicationVariants.all { variant ->
    if (variant.buildType.name != 'debug') return
    def output = layout.buildDirectory.file(
        "generated/supportTools/${variant.name}/res/raw/support_tools_environments.json")
    android.sourceSets.maybeCreate(variant.name).res.srcDir(
        "$buildDir/generated/supportTools/${variant.name}/res")
    def task = tasks.register("generate${variant.name.capitalize()}SupportToolsEnvironment") {
        def input = rootProject.file('uri.properties')
        inputs.file(input)
        outputs.file(output)
        doLast {
            if (!input.isFile()) {
                throw new GradleException("Missing uri.properties: ${input}")
            }
            def properties = new Properties()
            input.withInputStream { properties.load(it) }
            def config = supportToolsBuildEnvironmentConfig(properties)
            def destination = output.get().asFile
            destination.parentFile.mkdirs()
            destination.setText(
                groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(config)),
                'UTF-8')
        }
    }
    variant.preBuildProvider.configure { dependsOn(task) }
    variant.mergeResourcesProvider.configure { dependsOn(task) }
}
```

Assert during the focused Starbucks build that `buildscript.sourceFile` resolves to the copied `support-tools-integration.gradle`; do not hardcode a workstation path.

- [ ] **Step 6: Run the focused fixture**

Run:

```bash
./gradlew -p integrations/starbucks/verification verifyIntegrationScript
```

Expected: `BUILD SUCCESSFUL`; all valid, missing, blank, uniqueness, and JSON round-trip assertions pass.

- [ ] **Step 7: Commit Task 1 in app-support-tools only**

```bash
git add integrations/starbucks/support-tools-integration/support-tools-integration.gradle integrations/starbucks/verification
git commit -m "feat: extract Starbucks environment generation"
```

### Task 2: Package the Host Adapter and Installation Contract

**Files:**
- Create: `integrations/starbucks/support-tools-integration/src/main/java/com/starbucks/co2/constants/NewUriAuthorityResolver.java`
- Create: `integrations/starbucks/support-tools-integration/src/debug/java/com/starbucks/co2/debug/StarbucksSupportToolsEnvironmentProvider.java`
- Create: `integrations/starbucks/support-tools-integration/src/debug/AndroidManifest.xml`
- Create: `integrations/starbucks/support-tools-integration/README.md`
- Modify: `integrations/starbucks/verification/build.gradle`

**Interfaces:**
- Consumes: `SupportTools.loadEnvironmentConfig(Context, int)` and `SupportTools.resolveAuthority(Context, String)`; generated `R.raw.support_tools_environments`.
- Produces: `NewUriAuthorityResolver.install(Resolver)` and `NewUriAuthorityResolver.resolve(String)`; debug provider authority `${applicationId}.supporttools-environment`.

- [ ] **Step 1: Add failing kit contract checks**

Extend `verifyIntegrationScript` to check that all four distributable files exist, the Java packages are exact, the provider manifest is non-exported, and no tracked kit text contains a real URL-like value. Permit only the Android XML namespace literal:

```groovy
def allowedUrl = 'http://schemas.android.com/apk/res/android'
kitDir.eachFileRecurse { file ->
    if (file.isFile()) {
        file.eachLine { line ->
            def sanitized = line.replace(allowedUrl, '')
            assert !(sanitized =~ /https?:\/\//)
            assert !(sanitized =~ /(?i)(password|access[_-]?token|client[_-]?secret)\s*[=:]/)
        }
    }
}
```

Run:

```bash
./gradlew -p integrations/starbucks/verification verifyIntegrationScript
```

Expected: `FAILURE` listing the missing resolver, provider, manifest, or README.

- [ ] **Step 2: Move the existing resolver implementation into the canonical kit**

Create the resolver with this public API and behavior:

```java
public final class NewUriAuthorityResolver {
    public interface Resolver { String resolve(String originalAuthority); }
    private static final Resolver IDENTITY = value -> value;
    private static volatile Resolver resolver = IDENTITY;
    private NewUriAuthorityResolver() {}
    public static void install(Resolver value) {
        resolver = value != null ? value : IDENTITY;
    }
    public static String resolve(String originalAuthority) {
        try {
            String resolved = resolver.resolve(originalAuthority);
            return resolved != null ? resolved : originalAuthority;
        } catch (Throwable ignored) {
            return originalAuthority;
        }
    }
}
```

Keep package `com.starbucks.co2.constants` and do not import support-tools.

- [ ] **Step 3: Move the existing debug provider and manifest into the canonical kit**

Keep package `com.starbucks.co2.debug`. In `onCreate()`, load `R.raw.support_tools_environments`; only after success install:

```java
Context appContext = context.getApplicationContext();
NewUriAuthorityResolver.install(
        original -> SupportTools.resolveAuthority(appContext, original));
```

The manifest entry remains:

```xml
<provider
    android:name="com.starbucks.co2.debug.StarbucksSupportToolsEnvironmentProvider"
    android:authorities="${applicationId}.supporttools-environment"
    android:exported="false"
    android:initOrder="1000" />
```

- [ ] **Step 4: Write copy-first installation documentation**

Document these exact host actions:

1. Copy `support-tools-integration/` to the Starbucks project root.
2. Add `apply from: "$rootDir/support-tools-integration/support-tools-integration.gradle"` to `app/build.gradle` after the Android configuration.
3. Change the final authority append in `NewURI.Builder.build()` to `uri.append(NewUriAuthorityResolver.resolve(authority));`.
4. Add either the local source-module `debugImplementation project(':support-tools')` setup or the eventual published debug dependency.

State that actual values remain in `uri.properties`, generated JSON is debug-only under `app/build/`, and the kit must never contain generated output.

- [ ] **Step 5: Run contract and existing environment tests**

Run:

```bash
./gradlew -p integrations/starbucks/verification verifyIntegrationScript
./gradlew :support-tools:testDebugUnitTest --tests '*EnvironmentConfigParserTest' --tests '*EnvironmentAuthorityResolverTest'
```

Expected: both commands succeed; the verification output contains no property values.

- [ ] **Step 6: Commit Task 2 in app-support-tools only**

```bash
git add integrations/starbucks
git commit -m "feat: add copy-ready Starbucks integration kit"
```

### Task 3: Replace the Scattered Starbucks Integration Locally

**Files:**
- Create locally: `starbucks_android/support-tools-integration/` copied from the canonical kit.
- Modify locally: `starbucks_android/app/build.gradle`
- Verify unchanged hook: `starbucks_android/app/src/main/java/com/starbucks/co2/constants/NewURI.java`
- Delete locally: `starbucks_android/app/src/main/java/com/starbucks/co2/constants/NewUriAuthorityResolver.java`
- Delete locally: `starbucks_android/app/src/debug/java/com/starbucks/co2/debug/StarbucksSupportToolsEnvironmentProvider.java`
- Delete locally: `starbucks_android/app/src/debug/AndroidManifest.xml`

**Interfaces:**
- Consumes: canonical directory completed in Tasks 1-2; existing local `:support-tools` module connection and `debugImplementation` dependency.
- Produces: a local Starbucks build using only the copied kit plus the two approved host hooks.

- [ ] **Step 1: Record the Starbucks baseline with read-only Git commands**

Run:

```bash
git status --short
git diff -- app/build.gradle app/src/main/java/com/starbucks/co2/constants/NewURI.java app/src/main/java/com/starbucks/co2/constants/NewUriAuthorityResolver.java app/src/debug build.gradle settings.gradle
git diff --cached --name-only
```

Save the output outside the repository for comparison. Do not stage anything.

- [ ] **Step 2: Copy the canonical directory as one unit**

Copy:

```text
app-support-tools/integrations/starbucks/support-tools-integration/
```

to:

```text
starbucks_android/support-tools-integration/
```

Exclude no distributable file and do not copy `integrations/starbucks/verification/`.

- [ ] **Step 3: Slim `app/build.gradle` to one integration line**

Remove the five `devDebug`/`realDebug`/`_testDebug`/`stgDebug`/`drDebug` generated `res.srcDir` entries from the existing `android.sourceSets` block. Remove the complete inline `android.applicationVariants.all` support-tools JSON generation block. Add exactly:

```groovy
apply from: "$rootDir/support-tools-integration/support-tools-integration.gradle"
```

Retain `debugImplementation project(':support-tools')` and all unrelated host configuration unchanged.

- [ ] **Step 4: Remove superseded scattered files and inspect the resolver hook**

Delete only the old standalone resolver, provider, and debug manifest listed above. Before deleting the manifest, verify it contains only the support-tools provider. Confirm `NewURI.java` still contains exactly one functional integration change:

```java
uri.append(NewUriAuthorityResolver.resolve(authority));
```

- [ ] **Step 5: Run focused debug compilation and generation**

Run from Starbucks:

```bash
sh ./gradlew :app:assembleDevDebug
```

Expected: `BUILD SUCCESSFUL`; `app/build/generated/supportTools/devDebug/res/raw/support_tools_environments.json` exists; the generated JSON parses with schema version 1 and 15 services.

- [ ] **Step 6: Confirm the local-only diff and zero staged changes**

Run:

```bash
git status --short
git diff --check
git diff --cached --name-only
```

Expected: the copied directory and intended host edits are unstaged; cached output is unchanged from the baseline; no Git write command is run.

Do not commit Task 3 because all Starbucks changes are local verification only.

### Task 4: Verify Runtime Switching, Release Isolation, and Security

**Files:**
- Verify: `app-support-tools/integrations/starbucks/support-tools-integration/`
- Verify locally: `starbucks_android/support-tools-integration/`
- Verify locally: Starbucks generated outputs, merged manifests, dependency reports, and APKs.

**Interfaces:**
- Consumes: completed canonical kit and local Starbucks integration.
- Produces: fresh evidence that copy-first debug integration works and release output remains isolated.

- [ ] **Step 1: Verify all three runtime targets in debug**

Use the already established emulator smoke-test procedure. For DEV, STG, and REAL:

1. save the support-tools environment preference;
2. launch/restart the Starbucks host launcher;
3. compare representative `NewURI` authorities for `msr`, `web`, `auth`, `payment`, and `frequency` against the generated JSON target;
4. confirm the launcher is a Starbucks activity, not a support-tools activity.

Expected: every representative authority equals the selected target and no session/token/cookie-clearing behavior is introduced.

- [ ] **Step 2: Run the final app-support-tools checks once**

Run:

```bash
./gradlew -p integrations/starbucks/verification verifyIntegrationScript
./gradlew test assembleDebug assembleRelease
```

Expected: all tasks succeed.

- [ ] **Step 3: Run the final Starbucks debug/release builds once**

Run:

```bash
sh ./gradlew :app:assembleDevDebug :app:assembleRealRelease
sh ./gradlew :app:dependencyInsight --configuration realReleaseRuntimeClasspath --dependency support-tools
```

Expected: both variants build; dependency insight reports no matching release dependency.

- [ ] **Step 4: Inspect release artifacts**

Inspect the merged RealRelease manifest, merged resources, and APK contents. Assert that none contains:

```text
StarbucksSupportToolsEnvironmentProvider
support_tools_environments.json
com/hsjeong/supporttools
```

Also confirm the debug merged manifest contains the provider and the debug APK contains `res/raw/support_tools_environments.json`.

- [ ] **Step 5: Scan the canonical Git diff for protected values**

Run in app-support-tools:

```bash
git diff origin/dev...HEAD -- integrations/starbucks
git grep -n -E 'https?://|password[[:space:]]*[=:]|access[_-]?token[[:space:]]*[=:]|client[_-]?secret[[:space:]]*[=:]' -- integrations/starbucks
```

Expected: no URL values or credential assignments. The sole permitted URL-like literal is the Android manifest namespace `http://schemas.android.com/apk/res/android`.

- [ ] **Step 6: Perform the final Starbucks Git protection check**

Run only read-only commands:

```bash
git status --short
git diff --stat
git diff --cached --name-only
```

Expected: only approved local integration changes are new relative to the recorded baseline, and no new staged entry exists. Report pre-existing Starbucks changes separately from changes made by this plan.

- [ ] **Step 7: Report completion without committing Starbucks**

Report:

- app-support-tools commits and verification results;
- the canonical kit path;
- the two required host hooks and separate dependency/module connection;
- exact local-only Starbucks changes;
- release-isolation evidence;
- security scan result and the fact that generated URL values remain only in ignored build output;
- any remaining delivery decision between local source dependency and a published library artifact.
