# Dependency Injection

## Framework

The project uses **Koin 4.2.2** with the **Koin Compiler Plugin 1.2.1** (`io.insert-koin.compiler.plugin`) for compile-time annotation processing. Versions live in `gradle/libs.versions.toml` (`koin`, `koin-plugin`).

Key dependencies (bundled as `libs.bundles.koin` / `kointest` / `koinandroidtest`):
- `koin-core` — core DI container
- `koin-android` — Android integration
- `koin-annotations` — annotation support
- `koin-androidx-compose` — Jetpack Compose integration
- `koin-androidx-workmanager` — WorkManager worker injection
- `koin-test-junit5` — testing framework
- `koin-android-test`, `koin-test` — Android test utilities

---

## Module Architecture

Modules follow a 3-tier hierarchy (see `settings.gradle.kts` for the full list):

```
app/
├── core/             # Foundational infrastructure
│   ├── contracts     # Shared interfaces (AppInfo, ConfigInfo, Router, ConnectionManager, etc.)
│   ├── data          # Shared models, destinations, DI qualifier constants
│   ├── localprefs    # Preference storage (+ prefs, data submodules)
│   ├── utils
│   ├── router
│   ├── httpclient
│   ├── vpnconnect
│   ├── vpnlauncher
│   ├── portforwarding
│   ├── payments
│   ├── regions
│   └── obfuscator
│
├── capabilities/     # Cross-cutting concerns
│   ├── shareevents (KPI/analytics)
│   ├── notifications
│   ├── csi
│   ├── networkmanagement
│   ├── featureflags
│   ├── location
│   ├── snooze
│   ├── buildconfig
│   ├── ui, sharedui
│
└── features/         # User-facing screens (19 modules)
    ├── login, signup, splash
    ├── connection
    ├── settings, profile
    ├── vpnregionselection, obfuscationregionselection
    ├── dedicatedip
    └── ...
```

---

## Initialization

`App.kt` bootstraps Koin using the `@KoinApplication` annotation and the compiler plugin's `startKoin<App>`:

```kotlin
@KoinApplication(modules = [AppModule::class])
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<App> {
            androidContext(this@App)
            workManagerFactory()
        }
    }
}
```

`AppModule` (`app/src/main/java/com/kape/vpn/di/AppModule.kt`) is the root module. It:
- Includes every core/capability/feature module (~35) via `@Module(includes = [...])`, including the flavor-specific ones (`PaymentsModule`, `SignupBillingModule`, `FlavorLocationModule`)
- Uses `@ComponentScan` to auto-discover components in the `com.kape.vpn` and `com.kape.obfuscator` packages
- Directly provides top-level singletons: `AndroidAccountAPI`, `KPIAPI`, `CSIAPI`, `RegionsAPI`, `AppInfo`, `DeviceInfo`, `ConfigInfo`, `LicenceReader`, `CertificatePinningClient`, `PortForwardingApi`, coroutine dispatchers/scope, Intent/PendingIntent builders, and notification-related managers

---

## Module Declaration Pattern

Bindings are declared as explicit provider methods on an `@Module` class:

```kotlin
@Module
@ComponentScan("com.kape.vpnconnect.worker")
class VpnConnectModule {
    @Singleton(binds = [UsageProvider::class])
    fun provideUsageProvider(context: Context): UsageProvider = UsageProviderImpl(context)

    @Singleton([ConnectionStatusProvider::class])
    fun provideConnectionStatusProvider(
        connectionValues: Map<ConnectionStatus, String>,
        notificationHandler: NotificationHandler,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): ConnectionStatusProvider = ConnectionStatusProviderImpl(connectionValues, notificationHandler, ioScope)
}
```

ViewModels are registered with `@KoinViewModel` provider methods (or class annotations). A few classes (e.g. `ConnectionManagerImpl`) are `KoinComponent`s that `inject()` their dependencies lazily.

Use `@Named(DI.SOME_CONSTANT)` qualifiers where multiple bindings of the same type exist. The constants live in `DI` in `core/data/src/main/java/com/kape/data/Constants.kt` (e.g. `DI.IO_DISPATCHER`, `DI.MAIN_DISPATCHER`, `DI.IO_SCOPE`, `DI.AUTOMATION_PENDING_INTENT`, `DI.VPN_LAUNCHER`).

---

## `core/contracts` Module

This module defines shared interfaces that cut across layers, enforcing proper dependency inversion. Features depend on contracts, not concrete classes.

| Interface | Purpose |
|-----------|---------|
| `AppInfo` | Build metadata (version, package name, etc.) |
| `ConfigInfo` | App configuration — certificate, user agent, update URL |
| `DeviceInfo` | Device characteristics |
| `Router` | Navigation bus |
| `AuthenticationDataSource` | Login/logout operations |
| `IsUserLoggedInUseCase`, `LogoutUseCase` | Shared auth use cases |
| `ConnectionManager` | Connect / disconnect / reconnect the VPN |
| `ConnectionInfoProvider`, `ConnectionStatusProvider` | Observable VPN connection state |
| `UsageProvider` | Data usage |
| `KpiDataSource` | Analytics event tracking |
| `NetworkManager` | Network operations |
| `MetaEndpoints` | Endpoint configuration |
| `LicenceReader`, `GetAppLatestVersion`, `ScreenElementProvider` | Misc. shared services |

---

## Key Singletons by Category

**Authentication & Accounts**
- `AndroidAccountAPI` — PIA account API (`mobile-shared-account`)
- `AuthenticationDataSourceImpl` — implements `AuthenticationDataSource`
- `LoginUseCase`, `GetUserLoggedInUseCase`, `LogoutUseCase`

**VPN Connection**
- `ConnectionManager` → `ConnectionManagerImpl` — drives `PiaService` (KAPE platform SDK)
- `ConnectionStatusProvider`, `ConnectionInfoProvider` — connection state
- `ConnectionDataSource`, `ClientStateDataSource`
- `ConnectionPrefs` — connection preferences storage

**Analytics & Logging**
- `KPIAPI`, `CSIAPI`
- `KpiDataSourceImpl` — implements `KpiDataSource`
- `SubmitKpiEventUseCase`

**Network & Regions**
- `RegionsAPI` — region metadata
- `VpnRegionRepository`, `RegionListProvider`

**Preferences**
- Multiple `*Prefs` singletons from `core/localprefs/prefs`: `SettingsPrefs`, `ConnectionPrefs`, `CsiPrefs`, `ConsentPrefs`, etc.

---

## Testing

Unit tests use JUnit5 + `mockk`. Most tests construct the class under test directly with mocks rather than loading Koin modules:

```kotlin
@BeforeEach
fun setUp() {
    source = KpiDataSourceImpl(api, eventGenerator)
}
```

Classes that resolve dependencies through `KoinComponent` (e.g. `ConnectionManagerImpl`) are tested by starting Koin with a small module of mocks (`startKoin { modules(...) }` / `stopKoin()`), with the test implementing `KoinTest`.

---

## Flavor-Specific Bindings

Product flavors (dimension `provider`): `google`, `amazon`, `noinapp`, `meta`, `fdroid`.

### Modules with flavor-specific implementations

Flavor differences are isolated in **flavor-specific Koin modules**: the same `@Module` class name exists in each flavor source set with different bindings, and `AppModule` includes it by name.

| Module | Source sets | Flavored `@Module` |
|--------|-------------|--------------------|
| `core/payments` | `src/google`; `src/nogms` (amazon, noinapp, meta, fdroid) | `PaymentsModule` |
| `features/signup` | `src/google`; `src/amazon`; `src/nogms` (noinapp, meta, fdroid) | `SignupBillingModule` (flavor-agnostic bindings stay in `SignupModule` in `src/main`) |
| `app` | `src/gms` (google, noinapp); `src/nogms` (amazon, meta, fdroid) | `FlavorLocationModule` |

**Rules:**
- Register flavor-specific classes via **explicit `@Singleton` provider methods** in the flavored `@Module`. Do **not** rely on class-level `@Singleton` annotations — the Koin compiler plugin does not reliably auto-discover them across flavor source sets in multi-module builds.
- Every flavor's version of the module must have the same class name and package so `AppModule` compiles for all flavors.
- Non-billing flavors provide no-op implementations (e.g. `NoOpPaymentIssueHandler`, `NoOpSignupBillingHandler`).

```kotlin
// features/signup/src/google/.../di/SignupBillingModule.kt
@Module
class SignupBillingModule {
    @Singleton(binds = [SignupDataSource::class])
    fun provideSignupDataSource(
        api: AndroidAccountAPI,
        eventGenerator: KpiEventGenerator,
        submitKpiEventUseCase: SubmitKpiEventUseCase,
    ): SignupDataSource = SignupDataSourceImpl(api, eventGenerator, submitKpiEventUseCase)
}

// features/signup/src/nogms/.../di/SignupBillingModule.kt
@Module
class SignupBillingModule {
    @Singleton(binds = [SignupBillingHandler::class])
    fun provideSignupBillingHandler(): SignupBillingHandler = NoOpSignupBillingHandler(NO_IN_APP_SUBSCRIPTIONS)
}
```

### Compile safety

Koin compile-time safety is disabled in the Koin modules (it can't verify definitions that only exist in some flavors, or across module boundaries):

```kotlin
koinCompiler {
    compileSafety = false
}
```

### Flavor dimension propagation

Gradle requires that any module depending (directly or transitively) on a module with `productFlavors` must also declare the same flavor dimension and flavors, so variant matching can resolve correctly.

The following library modules declare `flavorDimensions.add("provider")` with all five flavors:

| Module | Reason |
|--------|--------|
| `core/payments` | Has flavor-specific source sets |
| `features/signup` | Has flavor-specific source sets; depends on `core/payments` and `features/login` |
| `features/login` | Depends on `core/payments` |
| `features/dedicatedip` | Depends on `core/payments` |
| `features/connection` | Depends on `core/payments` and `features/dedicatedip` |
| `features/customization` | Depends on `features/connection` |
| `features/splash` | Depends on `features/signup` |
| `features/tvwelcome` | Depends on `features/signup` |

Any new module that adds a direct or transitive dependency on `core/payments` or `features/signup` must also add the flavor dimension declaration.

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
- Annotation-driven module generation via the compiler plugin
- Better IDE support and autocomplete
- Significantly less boilerplate in `App.kt`
- Cleaner module dependency declarations
- Simplified test setup and isolation
