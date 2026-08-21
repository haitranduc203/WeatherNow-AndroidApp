# WeatherNow Fresher Readiness Fixes Design

## Goal

Bring WeatherNow to a credible Android Fresher portfolio standard by removing demo-only behavior from production flows, making location and offline behavior truthful, fixing security and coroutine/threading defects, and aligning the README with the implementation.

## Scope

The implementation covers exactly six review findings:

1. Favorites cards use real repository/cache weather data rather than city-name-based constants.
2. “Use my location” uses Android location services with permission and fallback handling.
3. The test notification receiver is unavailable to other apps in release builds.
4. Coroutine cancellation is preserved through the remote data source and repository layers.
5. Room access is asynchronous and `allowMainThreadQueries()` is removed.
6. README claims accurately describe the finished implementation and portfolio maturity.

Unrelated visual redesign, Hilt adoption, multi-module migration, and dependency upgrades are out of scope.

## Architecture and Data Flow

### Favorites weather

`FavoritesViewModel` continues to observe the active location and saved favorites. For each distinct location it loads current and daily weather through `WeatherRepository`, combines successful results into `FavoriteItemUiModel`, and exposes loading or recoverable error state without fabricated values. Cached repository results remain valid display data. Removing or adding favorites cancels obsolete location work so stale results do not overwrite the current list.

### Device location

Introduce an Android-backed location data source owned by the data layer. It uses `FusedLocationProviderClient`, preferring a recent last-known location and requesting a current balanced-power location when necessary. The repository resolves coordinates to a displayable `WeatherLocation`; if no usable fix is available it returns a typed error rather than pretending Hanoi is the device location.

The Search screen owns the permission request through Activity Result APIs. Outcomes are explicit:

- Permission granted and fix available: navigate to or display the detected coordinates.
- Permission denied: keep the user on Search and show an actionable message.
- GPS/provider unavailable or timeout: show an error and offer manual search.
- No permission request occurs merely because the app launches.

Hanoi remains the default home location for first launch, but it is labeled as the default and never presented as detected GPS.

### Security

`WeatherTestNotificationReceiver` is removed from the main manifest and declared only in the debug source set with `android:exported="false"`. Release manifests contain no test actions or externally triggerable test receiver.

### Cancellation and errors

Remote calls must immediately rethrow `CancellationException` before mapping other failures. ViewModels must not swallow cancellation. User-visible operations expose a stable loading/success/error state; unexpected exceptions may be logged or mapped but not replaced with plausible fake weather.

### Room threading

All one-shot DAO operations become `suspend`; observed data remains `Flow`. Production database construction removes `allowMainThreadQueries()`. Repository implementations call DAO APIs from suspending code and preserve cancellation. Cache clearing and cache-size reads run asynchronously through ViewModel scope.

`fallbackToDestructiveMigration()` may remain for this version-1 database because no migration is currently required; schema export/migration policy is outside this six-item scope.

## Testing Strategy

Development follows red-green TDD for behavior that can be tested locally:

- Favorites tests prove values come from repository results and cover partial/error results.
- Location repository tests cover available fix, denied/unavailable result, and no Hanoi masquerading as device location.
- Remote data source tests prove cancellation is rethrown unchanged.
- DAO/repository tests compile and pass with suspend APIs and cover cache/favorite/recent-search operations.
- Manifest verification proves the release manifest omits the test receiver.
- Existing 76 unit tests remain passing or are updated only where behavior intentionally changes.

Automated verification runs unit tests, Android lint, debug build, and release build.

## Physical Device Acceptance Test

After automated checks, install the debug APK on the USB-connected Android device and verify:

1. Fresh launch does not request notification or location permission without a related user action.
2. Tapping “Use my location” requests location permission at the correct time.
3. Granting permission returns the device’s approximate real coordinates and loads weather.
4. Denying permission displays an actionable message and manual search remains usable.
5. Favorites show API/cache-derived values, not fixed Tokyo/Paris/Hanoi constants.
6. Airplane mode displays cached weather or a truthful error; it never fabricates data.
7. Clearing cache, rotating the device, and background/foreground transitions do not crash.
8. Notification permission and background preference flows behave correctly on the connected OS version.

Release manifest inspection is authoritative for the debug-only receiver because a debug receiver is intentionally unavailable in release.

## README Positioning

README will describe the app as a Fresher portfolio project, list verified capabilities, and avoid “production-ready” or “strict Clean Architecture” claims. It will accurately document default Hanoi behavior, real device location, cache limitations, test counts from the latest run, and the CI tasks actually configured. Claims about permissions and background threading must match code.

## Completion Criteria

- No production weather UI uses city-name-based hardcoded measurements.
- “Use my location” invokes real Android location services and handles denial/unavailability.
- Release manifest contains no exported test receiver or test broadcast actions.
- Cancellation propagates without conversion to a network error.
- Production Room configuration rejects main-thread access and all tests pass with asynchronous DAOs.
- README matches the verified implementation.
- Automated suite/builds pass and the physical-device acceptance test is recorded with device/OS details and any limitations.
