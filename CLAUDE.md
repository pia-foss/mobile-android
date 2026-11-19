# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Private Internet Access (PIA) Android VPN client, a Clean Architecture rewrite of `mobile-android`. Kotlin with Jetpack Compose (and Compose for TV), targeting Android 7+ (min SDK 24, compile/target SDK 37, JVM 21).

## Common Commands

```bash
# Lint (CI blocks all builds if it fails). ktlintFormat fixes most issues.
./gradlew ktlintCheck

# Unit tests: flavored modules + non-flavored modules
./gradlew testGoogleDebugUnitTest testDebugUnitTest

# Aggregated unit test coverage across all modules (Kover). Runs the googleDebug/debug unit tests;
# report in build/reports/kover/htmlCoverage/index.html (XML: :koverXmlReportCoverage). Keep the leading `:`, or every module also writes its own report
./gradlew :koverHtmlReportCoverage

# Build all five flavors' debug APKs
./gradlew assembleDebug

# End-to-end tests (needs a device/emulator and PIA_VALID_* credentials)
./gradlew :app:connectedNoinappDebugAndroidTest
```

Product flavors (dimension `provider`): `google`, `amazon`, `noinapp`, `meta`, `fdroid`. Substitute the flavor in task names (e.g. `assembleAmazonDebug`, `assembleFdroidRelease`).

After changing dependencies, regenerate `gradle/verification-metadata.xml` (see README, "Dependency Verification").

Region JSON assets (`app/src/main/assets/*-regions.json`) are committed so builds are deterministic. Refresh them with `./gradlew updateRegionsInformation` when bumping the version.

## Required Environment Variables

- `PIA_VALID_USERNAME`, `PIA_VALID_PASSWORD`, `PIA_VALID_DIP_TOKEN`: only for the end-to-end tests (passed as instrumentation runner arguments).

No credentials are needed to resolve dependencies; the KAPE libraries come from JitPack.

## Build Configuration

Modules apply convention plugins from the `configuration/` included build instead of repeating plugin blocks:

- `pia.android.library`: Android library, ktlint, serialization, Koin compiler, JUnit 5
- `pia.android.library.compose`: the above plus Compose
- `pia.android.feature`: the above plus `capabilities:ui` (Compose, Navigation 3, router, contracts, Koin)
- `pia.android.application`: the app module, including the store flavors
- `pia.android.flavors`: the five store flavors, for libraries with store-specific code or a flavored dependency

SDK and JVM settings live in `Config` (`configuration/src/main/kotlin/ConfigurationPlugin.kt`). Don't override them in modules. Library versions, bundles and plugin IDs live in `gradle/libs.versions.toml`.

A module whose name matches a library's namespace uses a `.core` suffix (e.g. `com.kape.regions.core`, `com.kape.account.core`).

## Architecture

**Core** (`core/`): no UI.
- `contracts`: interfaces shared across modules (`Router`, `ConnectionManager`, `AppInfo`, ...) and the Koin `Qualifiers`
- `network`: certificate-pinned Ktor clients (`PinnedHttpClientFactory`); the only module that exposes Ktor, so only modules making HTTP calls depend on it
- `data`: shared models and navigation destinations (`ComposeDestination`)
- `account` (also the authentication shared by `login` and `signup`), `regions`, `payments` (Google Play only), `vpnconnect` (KAPE platform SDK), `localprefs` (`api`: `*Store` interfaces; `prefs`: the DataStore-backed `*Prefs` implementing them; `data`: models), `router`, `obfuscator`, `portforwarding`, `vpnlauncher`, `utils`

**Capabilities** (`capabilities/`): cross-cutting services.
- `ui` (design system, strings), `sharedui` (connection tiles), `appbar`, `rating` (rating prompt and dialogs), `csi`, `shareevents`, `location` (flavored: play services / none), `networkmanagement`, `snooze`, `featureflags`, `buildconfig`

**Features** (`features/`): one module per user-facing feature (`connection`, `login`, `signup`, `settings`, `vpnregionselection`, `dedicatedip`, `automation`, `widget`, ...).

**App** (`app/`): `MainActivity`, the navigation graph (`main/AppNavigation.kt`), the root Koin module, and system entry points (`system/`: boot and update receivers, Quick Settings tile; `shortcuts/`).

### Rules

- Features don't depend on other features. When one needs something from another, add a narrow interface to `core/contracts` and implement it in the owning module. UI composition across features (e.g. the side menu inside the connection screen) happens in `app`.
- Inside a module: `ui/` (screens, ViewModels) → `domain/` (use cases, and the repository/data source interfaces they need) → `data/` (implementations). The domain layer never depends on data classes or libraries.
- Domain code and ViewModels use the `*Store` interfaces from `core/localprefs/api`, never the `*Prefs` classes. A store lists only what those layers use, so add a member to it (and `override` in the `*Prefs`) when you need one.
- Use cases are named `<Verb><Noun>UseCase` and are `@Factory`. Prefer one ViewModel per screen (or small screen group), exposing `StateFlow`s collected with `collectAsStateWithLifecycle()`.
- Work that must outlive a screen (connecting, saving before navigating back) runs in the `@Named(Qualifiers.APP_SCOPE)` scope, or finishes before `router.navigateBack()`: leaving a screen cancels its `viewModelScope`.

### Pitfalls

- The `*Prefs`/`*Store` `StateFlow`s use `SharingStarted.WhileSubscribed`, so their `.value` is a default until something collects them. In one-off reads use the `...Now()` suspend functions (add one if missing), never `.value`.
- System components (services, receivers, tile, widget) are `KoinComponent`s; keep their logic in injected use cases so it can be unit tested.
- The account library needs its `AccountContentProvider`, declared in `core/account`'s manifest. Without it, login tokens are only kept in memory.

### Navigation

Navigation 3. ViewModels call `router.updateDestination(SomeDestination)` / `router.navigateBack()`; `MainActivity` applies them to the back stack shown by `NavDisplay`. Destinations are in `core/data` (`ComposeDestination`) with `DestinationNavOptions` (`None`, `PopUpTo`, `ClearAll`). TV and phones have separate graphs in `AppNavigation.kt`; a destination without a screen on the current device type shows `UnsupportedDestinationScreen`.

## Dependency Injection

Koin 4.2 with the Koin compiler plugin and annotations (`@Module`, `@ComponentScan`, `@Factory`, `@Singleton(binds = [...])`, `@KoinViewModel`, `@KoinWorker`). **Compile safety is on**, so a missing binding fails the build for the affected flavor. Each module has one `@Module` scanning its package, included by `AppModule`. Flavor-specific bindings use a `@Module` with the same name in each flavor source set. See `docs/dependency-injection.md`.

## Testing and Style

- Unit tests: JUnit 5 + MockK + `kotlinx-coroutines-test`. Construct the class under test directly; don't start Koin.
- End-to-end tests (`app/src/androidTest/java/e2e`): UI Automator, driven by test tags exposed as resource IDs. Keep existing `testTag`s when changing screens.
- ktlint: Kotlin files end without a trailing newline; imports are sorted lexicographically. Comments explain edge cases or non-obvious reasons, not what the code already says.

## KAPE Libraries

Resolved from JitPack (`com.github.pia-foss:*`), versions in `gradle/libs.versions.toml`: `platform-sdk` (VPN), `mobile-shared-account`, `mobile-shared-regions`, `mobile-shared-kpi`, `mobile-shared-csi`, `mobile-android-vpn-manager:openvpn`, `mobile-android-obfuscation-proxy`.
