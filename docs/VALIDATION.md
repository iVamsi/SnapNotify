# Consumer validation

## Checks and their limits

The existing CI workflow runs `check`, including unit tests, lint, and the demo release assembly. The demo's normal release configuration does not enable minification.

The separate Consumer smoke checks workflow tests four combinations:

| Dependency | Debug | Release with R8 |
| --- | --- | --- |
| Local project source | Assemble demo | Assemble minified demo |
| Maven Central 1.2.0 | Assemble demo | Assemble minified demo |

The init script enables release minification only for this verification invocation. Normal local demo builds are unchanged. Published mode substitutes the demo's project dependency with the fixed Maven coordinate and removes file-based dependency repositories so a Maven Local artifact cannot satisfy that dependency.

Published mode checks the released 1.2.0 artifact, not the source under review. Source mode checks the working checkout, not publication metadata. Neither check publishes artifacts or needs publishing credentials.

## Run locally

Use Java 21 and an Android SDK installation containing platform 37. From the repository root:

```bash
bash ./gradlew -I scripts/consumer-smoke.init.gradle -PconsumerDependency=source :SnapNotifyDemo:assembleRelease
bash ./gradlew -I scripts/consumer-smoke.init.gradle -PconsumerDependency=published :SnapNotifyDemo:assembleRelease
```

Replace `assembleRelease` with `assembleDebug` for debug verification. Run `bash ./gradlew check` separately for the existing test and lint tasks.

## Compatibility evidence

The workflow uses the checked-in dependency catalog and Gradle wrapper with Java 21. It does not vary Kotlin, Compose, AGP, Java, or Android runtime versions. A green result establishes only assembly success for those resolved build inputs; it is not a broad version-support matrix.

The repository currently declares AGP 9.4.0, Compose compiler plugin 2.4.20, Compose BOM 2026.09.00, and compile SDK 37. Published dependencies may impose additional version constraints. Record resolved versions when investigating a failure.

These checks do not install or launch the application, test runtime reflection, verify snackbar behavior on devices, or establish accessibility correctness. Release runtime tests and a wider version matrix remain separate work.

## Maintaining the checks

When releasing a new version, update the Maven coordinate in `scripts/consumer-smoke.init.gradle` and this document after the artifact is available. Do not silently fall back to project source if artifact resolution fails. Review R8 diagnostics uploaded by release jobs when shrinking fails.

This validation setup must pass CI before any new compatibility claim is added to the README.
