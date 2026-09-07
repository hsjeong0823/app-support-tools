# Starbucks Support Tools Integration Kit Design

## Purpose

Provide Starbucks with a copy-ready integration kit for `support-tools`. The kit keeps host-specific integration code in one directory and limits edits to existing Starbucks files to one Gradle application line and one runtime resolver call, excluding the separate library dependency and module connection.

The integration kit must not contain or commit Starbucks URL values, credentials, tokens, or other authentication data. It may contain the names of properties already owned by Starbucks, such as `msrDevUri`.

## Source of Truth and Installation Shape

The canonical kit is committed in `app-support-tools` at:

```text
integrations/starbucks/support-tools-integration/
```

For local validation or host adoption, the directory is copied as a unit to the Starbucks project root:

```text
starbucks_android/
├── support-tools-integration/
├── app/
└── ...
```

The Starbucks copy is local-only during this work and must not be added to its Git index or history. The canonical kit in `app-support-tools` contains no environment values and is safe to commit.

## Components

```text
support-tools-integration/
├── support-tools-integration.gradle
├── src/
│   ├── main/java/com/starbucks/co2/constants/
│   │   └── NewUriAuthorityResolver.java
│   └── debug/
│       ├── AndroidManifest.xml
│       └── java/com/starbucks/co2/debug/
│           └── StarbucksSupportToolsEnvironmentProvider.java
└── README.md
```

### Gradle integration script

`support-tools-integration.gradle` owns all build-time integration:

- registers the kit's main and debug source directories with the Android application module;
- registers the debug-only manifest;
- reads the host-owned `uri.properties` only while generating a debug variant;
- maps logical service names and runtime environments to property key names;
- generates `support_tools_environments.json` below AGP's application-module path `build/generated/res/generate<Variant>SupportToolsEnvironment/raw/`;
- registers that generated resource directory only for debug variants;
- wires generation into the relevant Android resource task;
- does not add anything to release variants.

The application module applies the script with one line:

```groovy
apply from: "$rootDir/support-tools-integration/support-tools-integration.gradle"
```

Library dependency and source-module or published-artifact configuration remain explicit host setup because they vary by delivery method.

### Runtime resolver

`NewUriAuthorityResolver` is compiled as part of the host application's main source set. It provides a dependency-free, process-local authority resolver with identity fallback. The existing `NewURI.Builder.build()` path calls it once at the final authority resolution point.

When support-tools has not installed a runtime mapping, or a service/environment is unknown, the resolver returns the original build-time authority unchanged.

### Debug environment provider

`StarbucksSupportToolsEnvironmentProvider` is included only in debug variants through the kit's debug source set and manifest. It reads the generated raw resource, supplies the host-specific environment mapping to support-tools, and relies on the library for environment selection and launcher restart behavior.

No session, cookie, token, or credential is copied into the provider or generated configuration.

## Data Flow

1. A Starbucks debug build starts generation for its selected variant.
2. The Gradle script reads property values from the existing Starbucks `uri.properties` file.
3. The script resolves its checked-in service/environment key mapping and validates every required property.
4. It writes the resolved values to a JSON resource under `app/build/`.
5. The debug-only provider loads that resource at runtime and registers the mapping with support-tools.
6. After an environment is selected, the single resolver call in `NewURI` replaces matching authorities.
7. Missing runtime mappings fall back to the authority selected by the existing Starbucks build configuration.

The generated JSON contains host URL values, but it is a derived debug build artifact under the already ignored `build/` directory. It is never stored in the canonical integration kit or committed by this work.

## Release Isolation

Release variants must contain none of the following:

- `StarbucksSupportToolsEnvironmentProvider` or its manifest registration;
- `support_tools_environments.json`;
- a runtime dependency on support-tools;
- support-tools UI or initialization behavior.

`NewUriAuthorityResolver` remains dependency-free and returns its input unless a debug provider installs a mapping. Its presence therefore does not require the support-tools library and does not change release behavior.

## Failure Behavior

- If `uri.properties` is missing for a debug build, generation fails with a message naming the expected file.
- If a required property is missing or blank, generation fails with a message naming the property key but never printing its value.
- Duplicate logical service/environment entries fail generation rather than silently overwriting data.
- Unknown services, environments, and unmapped authorities return the original authority at runtime instead of crashing the host.
- Release builds do not read or validate integration properties because the integration is debug-only.

## Security and Repository Rules

- The canonical kit contains property key names only, never property values.
- Tests and documentation use placeholders, not real Starbucks hosts or credentials.
- Error output names keys but does not echo values.
- Generated JSON remains below `app/build/` and must not be copied back into the kit.
- Starbucks changes remain unstaged and uncommitted during local verification.
- Existing Starbucks-owned URL data and repository history are outside this migration and remain untouched.

## Migration

The migration removes integration code currently distributed across Starbucks locations and replaces it with the copied directory:

- remove the JSON generation and generated-resource registration block from `app/build.gradle`;
- remove the separately placed resolver, provider, and provider manifest content after equivalent files are available through the copied kit;
- add the single `apply from` line to `app/build.gradle`;
- retain one resolver call in `NewURI.Builder.build()`;
- retain explicit support-tools dependency/module setup appropriate to local source or published artifact use.

## Verification

### app-support-tools

- verify the canonical kit contains every documented file;
- test service/environment mapping, JSON generation, missing-file handling, and missing/blank-key handling;
- scan tracked integration files and fixtures to ensure no URL values, tokens, or credentials were introduced;
- run the existing support-tools unit tests affected by the integration.

### Starbucks local validation

- copy the canonical kit to the Starbucks root and build representative debug variants;
- verify DEV, STG, and REAL selection changes representative `NewURI` authorities as expected;
- build a representative release variant;
- inspect the release dependency graph, merged manifest, resources, and APK to confirm support-tools, the provider, and generated JSON are absent;
- use read-only Git commands to confirm the exact local Starbucks diff and that nothing is staged.

## Acceptance Criteria

- A new workstation can integrate the host-specific portion by copying one directory.
- Existing Starbucks code changes, apart from library connection, are limited to one Gradle apply line and one resolver call.
- No real URL, credential, token, or authentication value is committed to app-support-tools or the integration kit.
- Debug environment switching continues to work for the approved service mappings.
- Release behavior and contents remain unchanged.
- Starbucks Git index, history, and remote are not modified.
