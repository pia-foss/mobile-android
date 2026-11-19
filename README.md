[![PIA logo][pia-image]][pia-url]

# Private Internet Access
Private Internet Access is the world's leading consumer VPN service. At Private Internet Access we believe in unfettered access for all, and as a firm supporter of the open source ecosystem we have made the decision to open source our VPN clients. For more information about the PIA service, please visit our website [privateinternetaccess.com](https://privateinternetaccess.com).

# Android Application
A re-write of the PIA Android application. Written in Kotlin with Jetpack Compose, following Clean Architecture.

## Installation

### Requirements
 - Git (latest)
 - Android Studio (Stable channel)
 - JDK 21
 - ADB installed
 - Android 7+ (min SDK 24)

Please use these instructions to install Git on your computer if it is not already installed: [Installing Git](https://gist.github.com/derhuerst/1b15ff4652a867391f03)

Please use these instructions to install and download Android Studio on your computer if it is not already installed: [Android Studio Download Link](https://developer.android.com/studio/index.html)

#### Download Codebase
Using the terminal:

`git clone https://github.com/xvpn/kp_vpn_android *folder-name*`

Type in what folder you want to put in without the ** or use a graphical interface like Android Studio's to clone the repo.

No credentials are needed to resolve dependencies: the KAPE libraries come from JitPack.

#### Building

>Once the project is cloned, the project will build once opened in Android Studio. The first build downloads all dependencies and can take a while; later builds are incremental. Once completed, the app will be able to be run on a device connected to the computer or an emulator running on your computer.

The app has five store flavors: `google`, `amazon`, `noinapp` (website), `meta` and `fdroid`. For example:

```bash
./gradlew assembleGoogleDebug
./gradlew ktlintCheck
./gradlew testGoogleDebugUnitTest testDebugUnitTest
```

## Documentation

#### Architecture
The app follows Clean Architecture. Each module is split into `ui` (Compose screens and ViewModels), `domain` (use cases and the interfaces they depend on) and `data` (implementations of those interfaces). Modules talk to each other through the interfaces in `core/contracts`, so features don't depend on each other.

Code structure via modules:

* `app` - the application: `MainActivity`, the navigation graph, the root Koin module, and system entry points (boot and update receivers, the Quick Settings tile, launcher shortcuts).
* `configuration` - an included build with the Gradle convention plugins (`pia.android.library`, `pia.android.library.compose`, `pia.android.feature`, `pia.android.application`, `pia.android.flavors`) and the shared SDK/JVM settings.
* `core` - foundational modules without UI.
  * `contracts` - the interfaces shared across modules, such as `Router`, `ConnectionManager` and `AppInfo`, plus the Koin qualifiers.
  * `data` - shared models and the navigation destinations.
  * `account` - the PIA account API and account details.
  * `localprefs` - the encrypted preference stores (`prefs`) and their data classes (`data`).
  * `payments` - Google Play billing. Only Google builds include it.
  * `regions` - the VPN and Shadowsocks server lists, latencies and the optimal server.
  * `router` - navigation on top of Navigation 3.
  * `vpnconnect` - establishes the VPN connection, using the KAPE platform SDK.
  * `obfuscator`, `portforwarding`, `vpnlauncher`, `utils`.
* `capabilities` - cross-cutting services used by several features.
  * `ui` - the design system: shared Compose elements, themes, fonts and all string resources.
  * `sharedui` - the connection screen's tiles.
  * `csi` - customer support diagnostics: collecting and sending logs.
  * `shareevents` - anonymous usage events (KPI), only while the user allows sharing them.
  * `location` - location permissions and mock location. Has separate Google Play Services and non-GMS implementations.
  * `networkmanagement` - the per-network automation rules.
  * `snooze`, `featureflags`, `buildconfig`.
* `features` - one module per user-facing feature, such as `connection`, `login`, `signup`, `settings`, `vpnregionselection`, `dedicatedip`, `automation` and `widget`.

See [docs/dependency-injection.md](/docs/dependency-injection.md) for how dependency injection and store flavors are set up.

### Contributing

By contributing to this project you are agreeing to the terms stated in the Contributor License Agreement (CLA) [here](/CLA.rst).

For more details please see [CONTRIBUTING](/CONTRIBUTING.md).

Issues and Pull Requests should use these templates: [ISSUE](/.github/ISSUE_TEMPLATE.md) and [PULL REQUEST](/.github/PULL_REQUEST_TEMPLATE.md).

### Testing

Unit tests use JUnit 5 and MockK:

```bash
./gradlew testGoogleDebugUnitTest testDebugUnitTest
```

The end-to-end tests in `app/src/androidTest` drive the app with UI Automator on a device or emulator. They need valid credentials in the environment variables `PIA_VALID_USERNAME`, `PIA_VALID_PASSWORD` and `PIA_VALID_DIP_TOKEN`:

```bash
./gradlew :app:connectedNoinappDebugAndroidTest
```

### Builds

To trigger a build for testing along with all other builds from the latest main, push a branch with branch name starting with `rc`.

The region lists in `app/src/main/assets/*-regions.json` are committed rather than fetched at build time, so builds (including F-Droid's network-less reproducible build) are deterministic for a given commit. Refresh them with `./gradlew updateRegionsInformation` and commit the result when bumping the version.

### Test Coverage

Unit test coverage is measured with [Kover](https://github.com/Kotlin/kotlinx-kover). Every module gets it from the convention plugins, and the root project merges them into one report. Keep the leading `:` so only the root task runs; without it Gradle also writes a separate report in every module:

```bash
./gradlew :koverHtmlReportCoverage   # build/reports/kover/htmlCoverage/index.html
./gradlew :koverXmlReportCoverage    # build/reports/kover/reportCoverage.xml (JaCoCo format, for CI tools)
```

Flavored modules are measured on the `google` flavor (`googleDebug`), the others on `debug`. UI isn't unit tested for now, so `@Composable` functions, the design system theme, the navigation entries and the Glance widget UI are excluded (ViewModels are still measured), along with generated code (`BuildConfig`, `R`, Compose singletons). The filters live in the root `build.gradle.kts`.

### Dependency Verification

`gradle/verification-metadata.xml` pins every resolved dependency by SHA-256 content hash, so a tampered or substituted artifact fails the build before any code executes. CI regenerates this file and fails if it diverges from the committed version, but only runs that check when a push/PR touches a dependency-related file (`gradle/libs.versions.toml`, `settings.gradle.kts`, the `configuration/` build scripts and convention plugins, `gradle/verification-metadata.xml`, or `gradle/wrapper/gradle-wrapper.properties`) or adds a `libs.` catalog reference to any `.gradle.kts` file — see `detect-dependency-changes` in `.github/workflows/assembleDebug.yml`.

Whenever you bump a dependency version, regenerate it locally and commit the result. Include the tasks CI actually runs, not just `help` — some tools (e.g. ktlint) resolve their own tool classpath lazily, only when their task executes, so `help` alone won't capture it:

```bash
./gradlew --write-verification-metadata sha256 help ktlintCheck testGoogleDebugUnitTest testDebugUnitTest koverHtmlReportCoverage koverXmlReportCoverage assembleGoogleDebug
```

Release builds (`assembleAmazonRelease`, `assembleMetaRelease`, `assembleNoinappRelease`, `assembleFdroidRelease`, `bundleGoogleRelease`) resolve R8 and other tools the debug build doesn't, so run them too when a change affects them.

Review the diff in `gradle/verification-metadata.xml` before committing to confirm only the expected components changed, then commit it alongside your dependency bump.

If a build fails with `Dependency verification failed`, `--write-verification-metadata` often won't fix it even when you add the failing task to the command above — some files (a BOM's own `.pom`, or a `*-parent.pom` pulled in via Maven's parent-POM inheritance walk) are only fetched by Gradle's normal verified resolution path, not by write-metadata mode, no matter which task you run. In that case, add the missing hash by hand:

1. Note the exact `<group>:<name>:<version>` and filename (`.pom`/`.module`/classified `.jar`) from the error.
2. Get the file: check `~/.gradle/caches/modules-2/files-2.1/<group>/<name>/<version>/` first (Gradle may have already fetched it for a different resolution path); otherwise download it from `https://repo1.maven.org/maven2/<group-as-path>/<name>/<version>/<filename>` (Google-hosted artifacts, e.g. `com.android.tools.build`, live at `https://dl.google.com/android/maven2/...` instead — check for a `.sha256` sidecar at the same URL to cross-verify).
3. Hash it (`shasum -a 256 <file>`) and add the `<component>`/`<artifact>`/`<sha256>` entry by hand next to its sibling versions in `gradle/verification-metadata.xml`.

Note: this metadata is regenerated on macOS, while CI runs on Linux. A handful of AGP build tools (currently just `aapt2`) publish a separate, differently-hashed jar per OS (`-osx`, `-linux`, `-windows` classifiers) — regenerating locally only ever captures the macOS one, so the Linux one always needs the manual step above. If you hit `Dependency verification failed` naming a `-linux.jar`, that's this case.

This is expected to happen occasionally as new tasks/flavors get exercised for the first time — it's not a sign the approach is broken.

### License

This project is licensed under the [MIT (Expat) license](https://choosealicense.com/licenses/mit/), which can be found [here](/LICENSE).

### Acknowledgements

This product includes software developed by the OpenSSL Project for use in the OpenSSL Toolkit. (https://www.openssl.org/)

© 2002-2017 OpenVPN Inc. - OpenVPN is a registered trademark of OpenVPN Inc.


<!-- Markdown link & img dfn's -->
[pia-image]: https://assets-cms.privateinternetaccess.com/img/frontend/pia_menu_logo_light.svg
[pia-url]: https://www.privateinternetaccess.com/
[wiki]: https://en.wikipedia.org/wiki/Private_Internet_Access
