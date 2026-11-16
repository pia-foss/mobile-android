# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Private Internet Access (PIA) Android VPN client. Written in Kotlin with Jetpack Compose UI, targeting Android 7+ (min SDK 24, compile/target SDK 37, JVM 21).

## Common Commands

```bash
# Lint (required before all builds in CI)
./gradlew ktlintCheck

# Unit tests (CI runs both: flavored modules + non-flavored modules)
./gradlew testGoogleDebugUnitTest testDebugUnitTest

# Build debug APK
./gradlew assembleGoogleDebug

# Instrumentation tests (requires running emulator)
./gradlew :app:connectedGoogleDebugAndroidTest

# Check library versions for updates
python3 check_versions.py
```

Product flavors (dimension `provider`): `google`, `amazon`, `noinapp`, `meta`, `fdroid`. Substitute `Google` in task names for other flavors (e.g., `assembleAmazonDebug`, `assembleFdroidRelease`).

Flavors are split by Google Play Services availability via shared source dirs:
- `google`, `noinapp` → `src/gms/java` (app module)
- `amazon`, `meta`, `fdroid` → `src/nogms/java` (no-op / non-GMS implementations)

Region JSON assets (`app/src/main/assets/*-regions.json`) are committed to the repo, not fetched at build time — this keeps builds (including F-Droid's network-less reproducible build) deterministic from a given commit/tag. Refresh them from `serverlist.piaservers.net` by running `./gradlew updateRegionsInformation` and commit the resulting diff when bumping the version.

## Required Environment Variables

- `PIA_VALID_USERNAME`, `PIA_VALID_PASSWORD`, `PIA_VALID_DIP_TOKEN` — required for instrumentation tests (passed as instrumentation runner arguments)

No credentials are needed to resolve dependencies — all KAPE libraries come from JitPack (see below).

## Architecture

Three-tier module hierarchy (see `settings.gradle.kts` for the full list):

**Core** — foundational infrastructure (no UI):
- `core/contracts` — shared interfaces (AppInfo, Router, ConnectionManager, AuthenticationDataSource, etc.) enforcing dependency inversion
- `core/data` — shared models, including navigation destinations (`ComposeDestination`)
- `core/vpnconnect` — VPN connection management, built on the KAPE platform SDK (`PiaService`, WireGuard/AmneziaWG authenticators)
- `core/httpclient` — HTTP with certificate pinning (Ktor + OkHttp)
- `core/localprefs` (+ `prefs`, `data` submodules) — encrypted preference storage and the `*Prefs` wrappers
- `core/payments` — in-app purchase handling; **has flavor source sets** (google / nogms)
- `core/regions` — server lists, latencies (`VpnRegionRepository`)
- `core/router` — navigation bus + Navigation 3 back stack helpers
- `core/portforwarding`, `core/obfuscator`, `core/vpnlauncher`, `core/utils`

**Capabilities** — cross-cutting concerns:
- `capabilities/ui` — shared Compose components, Material3 theme, fonts, string resources
- `capabilities/sharedui` — shared UI pieces (e.g., tiles)
- `capabilities/shareevents` — KPI/analytics event tracking
- `capabilities/featureflags` — feature flag management
- `capabilities/csi` — customer support log collection/submission
- `capabilities/networkmanagement` — network rules/IP table management
- `capabilities/notifications`, `capabilities/location`, `capabilities/snooze`, `capabilities/buildconfig`

**Features** — user-facing screens (19 modules, each self-contained with ViewModels, use cases, DI):
- `features/connection` — main VPN connection screen
- `features/login`, `features/signup` — authentication; signup **has flavor source sets** (amazon / google / nogms)
- `features/settings`, `features/profile`, `features/vpnregionselection`, `features/dedicatedip`, `features/tvwelcome`, etc.

**App module** (`app/`) — orchestrator: single `MainActivity`, Navigation 3 `NavDisplay`, Koin DI root (`App.kt` with `@KoinApplication`).

## Clean Architecture

Each feature module and core module follows a layered structure. The dependency rule flows inward: UI → Domain → Data, with `core/contracts` providing the shared interfaces that cross module boundaries.

### Layers within a module

**Presentation** (`ui/`):
- Composable screens, split into `mobile/` and `tv/` subpackages where a TV variant exists
- A single ViewModel per feature (`@KoinViewModel`), exposing state as `StateFlow<ScreenState>` and individual `StateFlow`s for reactive providers
- UI collects state via `collectAsStateWithLifecycle()`; events are plain function calls on the ViewModel

**Domain** (`domain/`):
- Use cases encapsulate business logic — named `<Verb><Noun>UseCase` (e.g., `IsVpnProfileInstalledUseCase`, `GetLogsUseCase`)
- Data source interfaces live here (e.g., `ConnectionDataSource`), not in the data layer — keeping the domain independent of implementation
- Cross-feature logic lives in core modules, often behind a `core/contracts` interface (e.g., `ConnectionManager`)

**Data** (`data/`):
- Implements the domain's data source interfaces (e.g., `ConnectionDataSourceImpl`)
- Repositories coordinate multiple data sources and handle caching (e.g., `VpnRegionRepository` in `core/regions`)
- Prefs classes (`ConnectionPrefs`, `SettingsPrefs`, etc.) live in `core/localprefs/prefs`

### Real data flow example: connecting to VPN

```
ConnectionScreen.kt
  └─ viewModel.onConnectionButtonClicked()
       └─ ConnectionManager.connect(server, ...)          [core/contracts interface]
            └─ ConnectionManagerImpl                      [core/vpnconnect/domain]
                 ├─ connectionPrefs / connectionInfoProvider updates
                 ├─ startShadowsocks()                    [core/obfuscator]
                 └─ PiaService.startVpn(excludedApps)     [core/vpnconnect/platformsdk → KAPE platform-sdk]
```

State propagates back via `ConnectionStatusProviderImpl` / `ConnectionInfoProviderImpl` (`core/vpnconnect/utils`), which expose `StateFlow<ConnectionStatus>` observed by the ViewModel.

### Navigation

Uses **Jetpack Navigation 3**. `Router` (defined in `core/contracts`, implemented in `core/router/RouterImpl`) is a thin `StateFlow`-based bus:
- ViewModels call `router.updateDestination(SomeDestination)` or `router.navigateBack()`; composables can use `LocalNavigator`
- `MainActivity` observes `router.getNavigationState()` / `getBackState()` and applies them to a `MutableList<NavKey>` back stack via `navigateTo()` / `navigateBack()` (`core/router/BackStackNavigation.kt`), rendered by `NavDisplay`
- Destinations are `ComposeDestination` objects in `core/data` (e.g., `Connection`, `VpnRegionSelection`, `HelpSettings`); each carries `DestinationNavOptions` (`None`, `PopUpTo`, `ClearAll`) replacing Nav2's `popUpTo`

### ViewModel state pattern

```kotlin
// Single state object for the screen
private val _state = MutableStateFlow(defaultState)
val state: StateFlow<ScreenState> = _state

// Direct flows from providers (not copied into _state)
val connectionStatus = connectionInfoProvider.status  // StateFlow<ConnectionStatus>

// Event handlers launch coroutines
fun onSomeAction() = viewModelScope.launch(ioDispatcher) { ... }
```

## Dependency Injection

Uses **Koin 4.2.x** with the **Koin compiler plugin** (`io.insert-koin.compiler.plugin`) and annotations:
- Annotations: `@Module`, `@Singleton`, `@KoinViewModel`, `@Factory`
- The DI root is `App.kt`; see `docs/dependency-injection.md` for full reference
- Modules set `compileSafety = false` in their `koinCompiler` config

**Flavor-specific DI rules** (important):
- Modules with product flavors (`core/payments`, `features/signup`) cannot use class-level `@Singleton`; must use explicit `@Singleton` provider methods
- Any module that transitively depends on `core/payments` or `features/signup` must declare `flavorDimensions.add("provider")` and the same five flavors in its `build.gradle.kts`

## Key Technical Details

- **UI**: Jetpack Compose + Material3; Compose for TV (`androidx.tv`) for TV screens
- **Navigation**: Navigation 3 (`androidx.navigation3`)
- **Async**: Kotlin Coroutines throughout; no RxJava
- **Networking**: Ktor client with OkHttp engine
- **Testing**: JUnit5 (android-junit5 plugin) + MockK + Koin Test; instrumented tests use `ANDROIDX_TEST_ORCHESTRATOR`
- **Linting**: ktlint Gradle plugin (`org.jlleitschuh.gradle.ktlint`); CI blocks all builds if ktlint fails
- **Versions**: managed in `gradle/libs.versions.toml`
- **Configuration plugin** (`configuration/`, id `"configuration"`): applied to all modules, centralizes Android SDK/JVM settings (`Config` in `ConfigurationPlugin.kt`) — do not override compile/target SDK or JVM settings in individual modules

## KAPE Libraries

Resolved from JitPack (`com.github.pia-foss:*`), versions in `gradle/libs.versions.toml`:
- `platform-sdk` — VPN platform SDK (service, WireGuard, connection status)
- `mobile-shared-account` — authentication API
- `mobile-shared-regions` — region metadata
- `mobile-shared-kpi` — analytics
- `mobile-shared-csi` — customer support logging
- `mobile-android-vpn-manager:openvpn` — OpenVPN
- `mobile-android-obfuscation-proxy` — Shadowsocks obfuscation
