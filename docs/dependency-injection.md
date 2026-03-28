# Dependency Injection

## Framework

The project uses **Koin 4.2.0** with the **Koin Compiler Plugin 0.4.1** for compile-time annotation processing.

Key dependencies:
- `koin-core` — core DI container
- `koin-android` — Android integration
- `koin-annotations` — annotation support
- `koin-compose` — Jetpack Compose integration
- `koin-test-junit5` — testing framework
- `koin-android-test` — Android test utilities

---

## Module Architecture

Modules follow a 3-tier hierarchy:

```
app/
├── core/           # Foundational infrastructure
│   ├── contracts   # Shared interfaces (AppInfo, ConfigInfo, Router, etc.)
│   ├── localprefs  # Preference storage
│   ├── utils
│   ├── router
│   ├── httpclient
│   ├── vpnconnect
│   ├── vpnlauncher
│   ├── payments
│   ├── regions
│   └── obfuscator
│
├── capabilities/   # Cross-cutting concerns
│   ├── shareevents (KPI/analytics)
│   ├── notifications
│   ├── csi
│   ├── networkmanagement
│   ├── featureflags
│   ├── location
│   └── snooze
│
└── features/       # User-facing screens
    ├── login, signup, splash
    ├── connection
    ├── settings, profile
    ├── vpnregionselection
    ├── dedicatedip
    └── ... (20+ more)
```

---

## Initialization

`App.kt` bootstraps Koin using the `@KoinApplication` annotation:

```kotlin
@KoinApplication(modules = [AppModule::class])
class App : Application() {
    override fun onCreate() {
        startKoin<App> {
            androidContext(this@App)
        }
    }
}
```

`AppModule` (`app/src/main/java/com/kape/vpn/di/AppModule.kt`) is the root module. It:
- Includes all 23 feature/capability modules via `@Module(includes = [...])`
- Uses `@ComponentScan` to auto-discover components in the `com.kape.vpn` and `com.kape.obfuscator` packages
- Directly provides top-level singletons: `AndroidAccountAPI`, `KPIAPI`, `CSIAPI`, `RegionsAPI`, `VPNManagerAPI`, `AppInfo`, `ConfigInfo`, Intent builders, and notification builders

---

## Module Declaration Pattern

```kotlin
@Module(includes = [PrefsModule::class, UtilsModule::class])
class ConnectionModule {

    @Singleton
    fun provideConnectionManager(prefs: ConnectionPrefs): ConnectionManager =
        ConnectionManagerImpl(prefs)

    @Singleton(binds = [ConnectionDataSource::class])
    fun provideDataSource(impl: ConnectionDataSourceImpl): ConnectionDataSource = impl

    @KoinViewModel
    fun provideViewModel(useCase: ConnectionUseCase): ConnectionViewModel =
        ConnectionViewModel(useCase)
}
```

Use `@Named(DI.SOME_CONSTANT)` qualifiers where multiple implementations of the same type exist (e.g. `@Named(DI.ALARM_MANAGER)`, `@Named(DI.AUTOMATION_PENDING_INTENT)`).

---

## `core/contracts` Module

This module defines shared interfaces that cut across layers, enforcing proper dependency inversion. Features depend on contracts, not concrete classes.

| Interface | Purpose |
|-----------|---------|
| `AppInfo` | Build metadata (version, package name, etc.) |
| `ConfigInfo` | App configuration — certificate, user agent, update URL |
| `Router` | Navigation interface |
| `AuthenticationDataSource` | Login/logout operations |
| `KpiDataSource` | Analytics event tracking |
| `NetworkManager` | Network operations |
| `MetaEndpoints` | Endpoint configuration |

---

## Key Singletons by Category

**Authentication & Accounts**
- `AndroidAccountAPI` — PIA account API
- `AuthenticationDataSourceImpl` — implements `AuthenticationDataSource`
- `LoginUseCase`, `LogoutUseCase`, `GetUserLoggedInUseCase`

**VPN Connection**
- `VPNManagerAPI` — VPN connection manager
- `ConnectionManager` — connection state management
- `ConnectionUseCase`, `ConnectionConfigurationUseCase`
- `ConnectionPrefs` — connection preferences storage

**Analytics & Logging**
- `KPIAPI`, `CSIAPI`
- `KpiDataSourceImpl` — implements `KpiDataSource`
- `SubmitKpiEventUseCase`

**Network & Regions**
- `RegionsAPI` — region metadata
- `RegionListProvider` — region utilities

**Preferences**
- Multiple `*Prefs` singletons: `SettingsPrefs`, `ConnectionPrefs`, `CsiPrefs`, etc.

---

## Testing

Tests use `KoinTest` + JUnit5 + `mockk`. Dependencies are injected directly as mocks rather than loading heavyweight module functions:

```kotlin
@BeforeEach
fun setUp() {
    stopKoin()
    startKoin {}
    source = KpiDataSourceImpl(mockk(), mockk(), mockk())
}
```

Test classes extend or implement `KoinTest` for access to Koin test utilities.

---

## Migration: DSL to Annotations (KM-15137)

The project migrated from manual DSL-based module functions to annotation-based Koin modules.

| | Before | After |
|---|---|---|
| Module declaration | `fun appModule()` | `@Module class AppModule` |
| App initialization | Manual `startKoin { modules(...) }` with 30+ imports | `@KoinApplication(modules = [AppModule::class])` on `App` |
| ViewModel registration | Factory functions returning instances | `@KoinViewModel` annotation |
| Module dependencies | Function parameters e.g. `vpnConnectModule(appModule)` | `@Module(includes = [...])` |

**Benefits of the new approach:**
- Compile-time safety through annotation processing
- Better IDE support and autocomplete
- Significantly less boilerplate in `App.kt`
- Cleaner module dependency declarations
- Simplified test setup and isolation