# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Private Internet Access (PIA) Android VPN client. Written in Kotlin with Jetpack Compose UI, targeting Android 7+ (min SDK 24, compile/target SDK 36).

## Common Commands

```bash
# Lint (required before all builds in CI)
./gradlew ktlintCheck

# Unit tests (google flavor)
./gradlew testGoogleDebugUnitTest

# Build debug APK
./gradlew assembleGoogleDebug

# Instrumentation tests (requires running emulator)
./gradlew :app:connectedGoogleDebugAndroidTest

# Check library versions for updates
python3 check_versions.py
```

Product flavors: `google`, `amazon`, `noinapp`, `meta`. Substitute `Google` in task names for other flavors (e.g., `assembleAmazonDebug`).

Region JSON assets are auto-fetched from `serverlist.piaservers.net` before every build via the `fetchRegionsInformation` pre-build task.

## Required Environment Variables

- `GITHUB_USERNAME` + `GITHUB_TOKEN` — access to private KAPE Maven packages (required to resolve dependencies)
- `PIA_VALID_USERNAME`, `PIA_VALID_PASSWORD`, `PIA_VALID_DIP_TOKEN` — required for instrumentation tests (injected into `BuildConfig` at debug build time)

## Architecture

Three-tier module hierarchy:

**Core** — foundational infrastructure (no UI):
- `core/contracts` — shared interfaces (AppInfo, Router, AuthenticationDataSource, etc.) enforcing dependency inversion
- `core/vpnconnect` — VPN connection state and logic
- `core/httpclient` — HTTP with certificate pinning (Ktor + OkHttp)
- `core/localprefs` — encrypted SharedPreferences storage
- `core/payments` — in-app purchase handling; **has flavor source sets** (google/amazon/noinapp/meta)
- `core/router` — navigation abstraction

**Capabilities** — cross-cutting concerns:
- `capabilities/ui` — shared Compose components, Material3 theme, fonts, string resources
- `capabilities/shareevents` — KPI/analytics event tracking
- `capabilities/featureflags` — feature flag management
- `capabilities/csi` — customer support log collection/submission
- `capabilities/networkmanagement` — network rules/IP table management

**Features** — user-facing screens (19 modules, each self-contained with ViewModels, use cases, DI):
- `features/connection` — main VPN connection screen; **has flavor source sets**
- `features/login`, `features/signup` — authentication; signup **has flavor source sets**
- `features/settings`, `features/profile`, `features/vpnregionselection`, `features/dedicatedip`, etc.

**App module** (`app/`) — orchestrator: single `MainActivity`, navigation host, Koin DI root (`App.kt` with `@KoinApplication`).

## Clean Architecture

Each feature module and core module follows a strict layered structure. The dependency rule flows inward: UI → Domain → Data, with `core/contracts` providing the shared interfaces that cross module boundaries.

### Layers within a module

**Presentation** (`ui/`):
- Composable screens split into `mobile/` and `tv/` subpackages
- A single ViewModel per feature (`@KoinViewModel`), exposing state as `StateFlow<ScreenState>` and individual `StateFlow`s for reactive providers
- UI collects state via `collectAsStateWithLifecycle()`; events are plain function calls on the ViewModel

**Domain** (`domain/`):
- Use cases are the only business logic entry point — named `<Verb><Noun>UseCase` (e.g., `StartConnectionUseCase`, `ReconnectUseCase`)
- Data source interfaces live here (e.g., `ConnectionDataSource`), not in the data layer — keeping the domain independent of implementation
- Some simple features (e.g., `features/settings`) define use cases locally; cross-feature logic lives in core modules

**Data** (`data/`):
- Implements the domain's data source interfaces (e.g., `ConnectionDataSourceImpl`)
- Repositories coordinate multiple data sources and handle caching (e.g., `VpnRegionRepository` in `core/regions`)
- Prefs classes (`ConnectionPrefs`, `SettingsPrefs`, etc.) are thin wrappers over `core/localprefs`

### Real data flow example: connecting to VPN

```
ConnectionScreen.kt
  └─ viewModel.onConnectionButtonClicked()
       └─ StartConnectionUseCase(server)                  [core/vpnconnect/domain]
            ├─ ConnectionConfigurationUseCase              [core/contracts → core/vpnconnect]
            └─ ConnectionDataSource.startConnection()     [core/vpnconnect/domain interface]
                 └─ ConnectionDataSourceImpl              [core/vpnconnect/data]
                      └─ connectionApi.startConnection()  [KAPE vpnmanager library]
```

State propagates back via `ConnectionInfoProviderImpl` which holds a `StateFlow<VpnConnectionStatus>` observed by the ViewModel.

### Navigation

`Router` (defined in `core/contracts`, implemented in `core/router/RouterImpl`) is a thin `StateFlow`-based bus:
- ViewModels call `router.updateDestination(SomeDestination)` or `router.navigateBack()`
- `MainActivity` (app module) observes `router.getNavigationState()` and drives the Compose `NavHost`
- Destinations are sealed/data objects (e.g., `Connection`, `VpnRegionSelection`, `HelpSettings`)

### ViewModel state pattern

```kotlin
// Single state object for the screen
private val _state = MutableStateFlow(defaultState)
val state: StateFlow<ScreenState> = _state

// Direct flows from providers (not copied into _state)
val connectionState = connectionInfoProvider.connectionState  // StateFlow<VpnConnectionStatus>

// Event handlers always launch coroutines
fun onSomeAction() = viewModelScope.launch { ... }
```

### Repository pattern

Used when a feature needs coordinated access to multiple data sources or caching:
- `VpnRegionRepository` (`core/regions`) — fetches server lists, latencies, port configs; injected into ViewModels that need region data
- Use cases call repositories; repositories call data sources

## Dependency Injection

Uses **Koin 4.2.0** with annotation-based configuration:
- Annotations: `@Module`, `@Singleton`, `@KoinViewModel`, `@Factory`
- The DI root is `App.kt`; see `docs/dependency-injection.md` for full reference

**Flavor-specific DI rules** (important):
- Modules with product flavors (`core/payments`, `features/signup`) cannot use class-level `@Singleton`; must use explicit `@Singleton` provider methods
- Must set `compileSafety = false` in Koin compiler config for those modules
- Any module that transitively depends on `core/payments` or `features/signup` must declare `flavorDimensions.add("provider")` in its `build.gradle.kts`

## Key Technical Details

- **UI**: Jetpack Compose + Material3; TV variants exist for some screens (`features/tvwelcome`)
- **Async**: Kotlin Coroutines throughout; no RxJava
- **Networking**: Ktor client with OkHttp engine
- **Testing**: JUnit5 + MockK + Koin Test; instrumented tests use `ANDROIDX_TEST_ORCHESTRATOR`
- **Linting**: ktlint 11.5.1; CI blocks all builds if ktlint fails
- **Configuration plugin** (`id: "configuration"`): applied to all modules, centralizes Android SDK/JVM settings — do not override compile/target SDK or JVM settings in individual modules

## Private KAPE Libraries

Resolved from GitHub Maven packages (`maven.pkg.github.com/pia-foss/`):
- `com.kape.android:account-android` — authentication API
- `com.kape.android:vpnmanager` — VPN connection manager
- `com.kape.android:regions-android` — region metadata
- `com.kape.android:kpi-android` — analytics
- `com.kape.android:csi-android` — customer support logging