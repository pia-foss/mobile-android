# Dependency Injection

## Framework

The project uses **Koin 4.2** with the **Koin compiler plugin** (`io.insert-koin.compiler.plugin`), configured with annotations. Versions live in `gradle/libs.versions.toml` (`koin`, `koinCompiler`).

The convention plugins in `configuration/` apply the compiler plugin to every module (`pia.android.library` and `pia.android.application`), so a module only adds the Koin libraries it uses:

```kotlin
implementation(platform(libs.koin.bom))
implementation(libs.koin.core)
implementation(libs.koin.annotations)
```

**Compile safety is on.** The compiler plugin checks at build time that every dependency of every definition can be resolved, for every flavor. A missing binding is a build error, not a crash at runtime.

---

## Initialization

`App.kt` starts Koin from the generated root module:

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

`workManagerFactory()` lets Koin create the `@KoinWorker` workers (port forwarding, snooze), which is why the app manifest removes WorkManager's default initializer.

`AppModule` (`app/src/main/java/com/kape/vpn/di/AppModule.kt`) is the root:
- It includes every module's `@Module` class via `@Module(includes = [...])`.
- It scans `com.kape.vpn` with `@ComponentScan`.
- It provides what only the app knows: the pinned certificate, the user agent, the update URL, the VPN notification's `PendingIntent`, and the `AppInfo` and `DeviceInfo` implementations built from `BuildConfig` and `Build`.

---

## Module declaration pattern

Each module has one `@Module` class that scans its own package:

```kotlin
@Module
@ComponentScan("com.kape.vpnregionselection")
class VpnRegionSelectionModule
```

Classes declare themselves with class-level annotations:

```kotlin
@Factory
class ObserveVpnRegionsUseCase(...)

@Singleton(binds = [AccountDetailsRepository::class])
internal class ApiAccountDetailsRepository(...) : AccountDetailsRepository

@KoinViewModel
class VpnRegionSelectionViewModel(...) : ViewModel()
```

- **`@Factory`** for use cases and other stateless classes.
- **`@Singleton(binds = [Interface::class])`** for repositories, data sources and anything holding state. Bind the interface, so other code depends on it rather than the implementation, which can stay `internal`.
- **`@KoinViewModel`** for ViewModels, which Compose screens get with `koinViewModel()`.

Use provider functions on the `@Module` class only for objects the module doesn't own, such as library APIs built with a builder (`AndroidAccountAPI`, `RegionsAPI`, `CSIAPI`, `KPIAPI`).

Android components the system creates (services, receivers, the tile and widget) can't take constructor parameters, so they use `KoinComponent` and `inject()`/`get()`. Keep their logic in injected use cases.

### Qualifiers

When several bindings share a type, they're told apart with `@Named` and the constants in `Qualifiers` (`core/contracts/src/main/java/com/kape/contracts/di/Qualifiers.kt`):

| Qualifier | Type | Provided by |
|-----------|------|-------------|
| `IO_DISPATCHER` | `CoroutineDispatcher` | `core/utils` |
| `MAIN_DISPATCHER` | `CoroutineDispatcher` | `core/utils` |
| `APP_SCOPE` | `CoroutineScope` | `core/utils` |
| `CERTIFICATE` | `String` | `app` |
| `USER_AGENT` | `String` | `app` |
| `UPDATE_URL` | `String` | `app` |
| `VPN_NOTIFICATION_INTENT` | `PendingIntent` | `app` |

Use `APP_SCOPE` for work that must outlive a screen, such as connecting, so leaving the screen doesn't cancel it.

---

## `core/contracts`

The interfaces that cross module boundaries live in `core/contracts`, so modules depend on them rather than on each other. Each is implemented by exactly one module, for example:

| Interface | Implemented in |
|-----------|----------------|
| `Router` | `core/router` |
| `ConnectionManager`, `ConnectionInfoProvider`, `ConnectionStatusSource`, `UsageProvider` | `core/vpnconnect` |
| `IsUserLoggedInUseCase`, `LogoutUseCase` | `features/login` |
| `StorePurchaseHistory` | `features/login` (per store) |
| `SubscriptionBilling` | `features/signup` (per store) |
| `AppUpdateStatus` | `features/splash` |
| `DedicatedIpHome` | `features/dedicatedip` |
| `SnoozeStatus` | `capabilities/snooze` |
| `UsageEventsControl`, `ConnectionEvents`, `PurchaseEvents` | `capabilities/shareevents` |
| `PermissionStatus`, `NetworkStatus` | `core/utils` |
| `AutomationServiceLauncher` | `features/automation` |
| `AppInfo`, `DeviceInfo` | `app` |

When a feature needs something from another feature, add a narrow interface here instead of a dependency between the two features. Composing two features' UI, such as the side menu inside the connection screen, happens in `app`'s navigation graph.

---

## Store flavors

Product flavors (dimension `provider`): `google`, `amazon`, `noinapp`, `meta`, `fdroid`. A module gets them by applying the `pia.android.flavors` convention plugin. Only modules with store-specific code or a flavored dependency need it; the rest work with every flavor.

Flavor-specific code lives in flavor source sets. Several flavors can share one source directory with `shareFlavorSources(directory, flavors)`, named after what those flavors have in common:

| Module | Source sets |
|--------|-------------|
| `features/signup` | `google`, `amazon`, `websiteonly` (noinapp, meta, fdroid) |
| `features/login` | `google`, `nostore` (amazon, noinapp, meta, fdroid) |
| `features/dedicatedip` | `google`, `nostore` (amazon, noinapp, meta, fdroid) |
| `capabilities/location` | `playservices` (google, noinapp), `noplayservices` (amazon, meta, fdroid) |

`core/payments` (Google Play Billing) isn't flavored. Modules depend on it from their `google` flavor only (`"googleImplementation"(project(":core:payments"))`), so other builds don't ship the Play Billing library.

### Flavor-specific bindings

Each flavor source set has a `@Module` class with **the same name and package**, and the module's main `@Module` includes it by that name:

```kotlin
// features/signup/src/main/.../di/SignupModule.kt
@Module(includes = [StoreModule::class])
@ComponentScan("com.kape.signup")
class SignupModule

// features/signup/src/websiteonly/.../di/StoreModule.kt
@Module
class StoreModule {
    @Singleton(binds = [SubscriptionStore::class, SubscriptionBilling::class])
    fun provideStore(): NoInAppSubscriptionStore = NoInAppSubscriptionStore(offerInstead = StoreOffer.WebsiteOnly)
}
```

The main module's `@ComponentScan` also finds class-level annotations in the flavor source sets, since they share its package. Stores without a feature get no-op implementations rather than no binding, so compile safety passes for every flavor.

---

## Testing

Unit tests use JUnit 5 and MockK, and construct the class under test directly with fakes or mocks instead of starting Koin:

```kotlin
private val viewModel = PermissionsViewModel(FakePermissionStatus(), RecordingRouter())
```

Keep logic out of `KoinComponent` classes (services, receivers) and in use cases, so it can be tested this way.
