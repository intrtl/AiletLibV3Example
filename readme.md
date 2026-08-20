![Latest Release](https://img.shields.io/badge/latest%20release-4.20.4-brightgreen)

# Integrating the Ailet library

The Ailet library embeds visit shooting, reports, and synchronization into your Android app.

API classes: `Ailet`, `AiletClient`. Maven package: `com.ailet.android:lib`.

To call Ailet without the library, use [integration via Android Intent](../intents/readme.md).

- [Scenario example](#scenario-example)
- [What you need](#what-you-need)
  - [How to create a GitHub personal access token](#how-to-create-a-github-personal-access-token)
  - [How to add the Maven repository](#how-to-add-the-maven-repository)
  - [How to add dependencies](#how-to-add-dependencies)
  - [ProGuard rules](#proguard-rules)
- [How to initialize the library](#how-to-initialize-the-library)
- [How to call methods](#how-to-call-methods)
- [On-device recognition (Palomna)](#on-device-recognition-palomna)
- [Method reference](#method-reference)
  - [getServers()](#getservers)
  - [init()](#init)
  - [start()](#start)
  - [getReports()](#getreports)
  - [showSummaryReport()](#showsummaryreport)
  - [setPortal()](#setportal)
  - [requestSyncCatalogs()](#requestsynccatalogs)
  - [showVisit()](#showvisit)
  - [finishVisit()](#finishvisit)
  - [logout()](#logout)
  - [getTotalSyncStat()](#gettotalsyncstat)
  - [syncPalomna()](#syncpalomna)
- [Broadcast message](#broadcast-message)
- [Migrating from IntRtl](#migrating-from-intrtl)
- [Report example](#report-example)
- [Known issues](#known-issues)
  - [Gradle 8.x and obfuscation](#gradle-8x-and-obfuscation)

## Scenario example

To run a visit and get a report:

1. Create a GitHub personal access token with the `read:packages` scope.
2. Add the Maven repository and the `com.ailet.android:lib` dependency.
3. Call `Ailet.initialize` in the `Application` class.
4. Call `init()` through `Ailet.getClient()`.
5. Call `start()` and take photos.
6. Wait for the report-ready broadcast or call `getReports()`.

## What you need

- An initial authorization token. The Ailet team issues it.
- A GitHub account that can read `intrtl/IRLib` packages.

See the current library version in the [version list](https://github.com/intrtl/IRLib/packages/1361609/versions).

### How to create a GitHub personal access token

To download Ailet packages from GitHub Packages:

1. Open GitHub.
2. Click your avatar in the top-right corner.
3. Select **Settings**.
4. Open **Developer settings**.
5. Open **Personal access tokens** → **Tokens (classic)**.
6. Click **Generate new token**.
7. Enable the `read:packages` scope.
8. Click **Generate token**.
9. Save the token: GitHub shows it once.

Do not commit the token to the repository. Put the login and token in `gradle.properties` or environment variables.

### How to add the Maven repository

To let Gradle download the Ailet library, add the repository in one of these ways.

**Option 1.** Add the repository in `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url 'https://maven.pkg.github.com/intrtl/irlib'
            credentials {
                username 'your GitHub username'
                password 'personal GitHub access token'
            }
        }
    }
}
```

**Option 2.** Add the repository in the root `build.gradle`:

```groovy
allprojects {
    repositories {
        maven {
            url 'https://maven.pkg.github.com/intrtl/irlib'
            credentials {
                username 'your GitHub username'
                password 'personal GitHub access token'
            }
        }
    }
}
```

### How to add dependencies

To add the library, put the dependency in the module `build.gradle`. Use a version from the [version list](https://github.com/intrtl/IRLib/packages/1361609/versions):

```groovy
implementation "com.ailet.android:lib:1.0.0"
```

A dynamic `+` version pulls the latest package and can break the build with no code change. For production builds, pin a specific version.

To enable the tech support module, add a dependency with the same version as the library:

```groovy
implementation "com.ailet.android:lib-feature-techsupport-intercom:1.0.0"
```

### ProGuard rules

To keep obfuscation from breaking the library, add this to `proguard-rules.pro`:

```proguard
-keep class com.ailet.** { *; }
-keep class com.ailet.lib3.** { *; }
-keep interface com.ailet.lib3.** { *; }
-keep enum com.ailet.lib3.** { *; }

-keepclassmembers class com.ailet.lib3.** {
    *** *(...);
}

-keepnames class com.ailet.lib3.** { *; }
-keepattributes *Annotation*
-keepattributes Signature

-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep interface com.google.gson.TypeAdapter
-keep interface com.google.gson.JsonSerializer
-keep interface com.google.gson.JsonDeserializer

-dontwarn com.ailet.lib3.**
```

## How to initialize the library

To start work, call `Ailet.initialize` in your `Application` subclass:

```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val features = setOf<AiletFeature>(
            DefaultStockCameraFeature(),
            IntercomTechSupportManager(this),
            HostAppInstallInfoProviderFeature(
                this,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                AiletLibInstallInfo
            )
        )

        val accessToken = "..."

        Ailet.initialize(this, accessToken, features)
    }
}
```

Modules in `features` are optional:

- `DefaultStockCameraFeature` — stock camera;
- `IntercomTechSupportManager` — tech support;
- `HostAppInstallInfoProviderFeature` — build identity for diagnostics.

After `initialize`, call methods through `Ailet.getClient()`.

To keep the Ailet camera screen from closing right after it opens when camera permission is already granted, add this to `features`:

```kotlin
DefaultAiletPermissionsFeature(
    excludedPermissions = setOf(AiletPermissionsFeature.Exclude.CAMERA)
)
```

## How to call methods

A method call returns `AiletCall`. Then run it asynchronously with `execute()` or synchronously with `executeBlocking()`.

Asynchronous call:

```kotlin
Ailet.getClient()
    .setPortal(portalName)
    .execute({ result ->
        when (result) {
            // handle the result
        }
    }, { throwable ->
        // handle the error
    })
```

Synchronous call. You choose the execution thread:

```kotlin
val result = Ailet.getClient()
    .setPortal(portalName)
    .executeBlocking()
```

In Java, parameters have no default values. Pass every argument explicitly:

```java
Ailet.getClient().init(
    "login",
    "password",
    null,
    false,
    null,
    false
).execute(
    result -> {
        // handle the result
        return null;
    },
    throwable -> {
        // handle the error
        return null;
    },
    () -> {
        // call completed
        return null;
    }
);
```

## On-device recognition (Palomna)

Some Ailet builds can recognize photos on the device. If your build does not include this, skip the “Palomna build” notes.

To download models, classes, and catalogs in advance, call [`syncPalomna()`](#syncpalomna).

## Method reference

| Method | What it does |
| --- | --- |
| [`init`](#init) | Authorizes the user, starts the library, and downloads catalogs |
| [`getServers`](#getservers) | Returns the list of available portals |
| [`start`](#start) | Starts visit shooting |
| [`getReports`](#getreports) | Returns a visit report |
| [`showSummaryReport`](#showsummaryreport) | Opens a summary visit report |
| [`setPortal`](#setportal) | Sets the active portal |
| [`requestSyncCatalogs`](#requestsynccatalogs) | Downloads catalogs |
| [`showVisit`](#showvisit) | Opens visit photos |
| [`finishVisit`](#finishvisit) | Finishes the visit |
| [`logout`](#logout) | Signs the user out |
| [`getTotalSyncStat`](#gettotalsyncstat) | Returns visit synchronization stats |
| [`syncPalomna`](#syncpalomna) | Downloads models and catalogs for on-device recognition |

To [migrate from `IntRtl`](#migrating-from-intrtl), use `AiletClient`.

### getServers()

`getServers()` returns a list of `AiletServer` servers. Call it only in multiportal mode. Then pass the selected server to `init()`.

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `login` | `String` | Yes | — | User login in Ailet |
| `password` | `String` | Yes | — | User password in Ailet |
| `externalUserId` | `String` | No | `null` | External user ID |

**Errors**

| Error | Description |
| --- | --- |
| `BackendApiException` | Server error with an [HTTP status](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) |

### init()

`init()` authorizes the user, starts the library, and downloads catalogs. It can also start the synchronization service.

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `login` | `String` | Yes | — | User login in Ailet |
| `password` | `String` | Yes | — | User password in Ailet |
| `externalUserId` | `String` | No | `null` | External user ID |
| `multiPortalMode` | `Boolean` | No | `true` | Enable multiportal mode |
| `server` | `AiletServer` | No | `null` | Server to sign in to |
| `isNeedSyncCatalogs` | `Boolean` | No | `true` | Sync catalogs on sign-in |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `DataInconsistencyException` | `Current auth state data is null` | Authentication data is invalid |
| `IllegalStateException` | `Inconsistency! server is null` | Server is empty |
| `IllegalStateException` | `No portals available` | No portals available |
| `BackendApiException` | — | Server error with an [HTTP status](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) |
| Other exception types | — | Internal library error. Contact support |

### start()

`start()` starts shooting for a visit.

> **On-device (Palomna build):** if models and classes are downloaded, photos are processed on the device when there is no internet.

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `storeId` | `AiletMethodStart.StoreId` | Yes | — | External store ID |
| `externalVisitId` | `String` | No | `null` | External visit ID |
| `sceneGroupId` | `Int` | No | `null` | Scene group ID |
| `taskId` | `String` | No | `null` | External task ID |
| `visitType` | `String` | No | `null` | Visit type (`before`, `after`) |
| `visitUuid` | `String` | No | `null` | Internal visit ID |
| `retailTaskIterationUuid` | `String` | No | `null` | Iteration ID (retail) |
| `retailTaskId` | `String` | No | `null` | Task ID (retail) |
| `retailTaskActionId` | `String` | No | `null` | Action ID (retail) |
| `sceneTypes` | `List` | No | `listOf()` | List of scene types |
| `launchConfig` | `LaunchConfig` | No | `AiletMethodStart.LaunchConfig()` | Launch configuration |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `Throwable` | `Uneditable(historical) visit` | The visit is finished and cannot be edited |
| `IllegalStateException` | `Inconsistent AiletClient state: (Unknown, Warning, Error)` | Inconsistent client state |
| `Throwable` | `Unauthorized` | User is not authorized |

### getReports()

`getReports()` returns a visit report as JSON. See the [report format](#report-example).

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `externalVisitId` | `String` | Yes | — | External visit ID |
| `taskId` | `String` | No | `null` | External task ID |
| `visitType` | `String` | No | `null` | Visit type (`before`, `after`) |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `AiletException` | `Visit with externalId [externalId] is not found` | Visit with this ID was not found |
| `OnDeviceNotAvailableException` | `On-device recognition not available` | **(Palomna build)** On-device recognition is unavailable: no network and models are not downloaded |

> **On-device (Palomna build):** the `result` and `report.result` fields also include:
> - `source`: `"online"` if every photo was recognized on the server, or `"on-device"` if at least one photo was recognized only locally;
> - `completed_on_device`: number of photos recognized on-device.

### showSummaryReport()

`showSummaryReport()` opens the summary visit report screen.

> **On-device (Palomna build):** the screen shows on-device recognition data.

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `externalVisitId` | `String` | Yes | — | External visit ID |
| `taskId` | `String` | No | `null` | External task ID |
| `visitType` | `String` | No | `null` | Visit type (`before`, `after`) |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `Throwable` | `Unauthorized` | User is not authorized |
| `IllegalArgumentException` | `No store for externalId [externalId]` | No store with this external ID |
| `IllegalArgumentException` | `No store for storeId [storeId]` | No store with this ID |
| `IllegalArgumentException` | `No visit for summary report request $param found` | Visit for the summary report was not found |
| `IllegalArgumentException` | `Incorrect historical visit params` | Invalid parameters for a finished visit |
| `BackendApiException` | — | Server error with an [HTTP status](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) |
| `RuntimeException` | `No visit/Offline` | No visit, the device is offline |

### setPortal()

`setPortal()` sets the current portal in multiportal mode.

> **On-device (Palomna build):** when you switch portals, on-device recognition settings for that portal are saved and applied. After switching, call `syncPalomna()` if models for the portal are not downloaded yet.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `portalName` | `String` | Yes | Portal ID |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `Throwable` | `Unauthorized` | User is not authorized |
| `IllegalArgumentException` | `No [server] found in local portals list` | Server was not found in the local portal list |
| `AiletException` | `no [server] in servers list` | Server is not in the list |

### requestSyncCatalogs()

`requestSyncCatalogs()` downloads catalogs for the selected portal in multiportal mode.

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `syncMode` | `AiletMethodSyncCatalogs.SyncMode` | No | `AiletMethodSyncCatalogs.SyncMode.EAGER` | `EAGER` — all catalogs. `SOFT` — required catalogs only |
| `strategy` | `AiletMethodSyncCatalogs.Strategy` | No | `AiletMethodSyncCatalogs.Strategy.Schedule` | `Schedule` — queue the download. `SyncRightNow` — sync immediately |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `Throwable` | `Unauthorized` | User is not authorized |

To download catalogs for every portal:

```kotlin
Ailet.getClient()
    .getServers(
        "login",
        "password",
        "external_user_id"
    )
    .execute({ result ->
        runBlocking {
            result.servers.forEach { server ->
                Ailet.getClient().setPortal(
                    portalName = server.name
                ).executeBlocking()

                Ailet.getClient().requestSyncCatalogs(
                    syncMode = AiletMethodSyncCatalogs.SyncMode.EAGER,
                    strategy = AiletMethodSyncCatalogs.Strategy.SyncRightNow
                ).executeBlocking()
            }
        }
        // actions after all catalogs are downloaded
    }, { throwable ->
        // handle the error
    })
```

### showVisit()

`showVisit()` opens the visit photo screen. The first photo opens.

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `externalVisitId` | `String` | Yes | — | External visit ID |
| `taskId` | `String` | No | `null` | External task ID |
| `visitType` | `String` | No | `null` | Visit type (`before`, `after`) |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `Throwable` | `Unauthorized` | User is not authorized |
| `IllegalArgumentException` | `No visit with id: [visitId]` | Visit was not found |
| `IndexOutOfBoundsException` | `No photos in visit with id: [$visitId]` | The visit has no photos |
| `RuntimeException` | `No visit/Offline` | No local visit, the device is offline. The visit cannot be checked on the server |
| `BackendApiException` | — | Server error with an [HTTP status](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) |

### finishVisit()

`finishVisit()` closes the visit. After that the visit is view-only.

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `externalVisitId` | `String` | Yes | — | External visit ID |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `Throwable` | `Unauthorized` | User is not authorized |
| `AiletException` | `Visit with externalId [externalVisitId] is already finished` | The visit is already finished |
| `AiletException` | `Visit with externalId [externalVisitId] is not found` | Visit was not found |

### logout()

`logout()` signs the user out and clears service data.

> **On-device (Palomna build):** if on-device recognition is enabled in mobile settings, the library deletes downloaded models, classes, and catalogs. This happens only if the next `init()` runs for a different user.

### getTotalSyncStat()

Available in version 4.17.3 and later. The new `source` and `completed_on_device` fields exist in the Palomna build.

`getTotalSyncStat()` returns photo statistics and starts the synchronization service if it is stopped. The result is a JSON string.

> **On-device (Palomna build):** each `items` element and `total_stat` also include `source` (`online` / `on-device`) and `completed_on_device`.

**Response example**

```json
{
    "items": [
        {
            "visit_id": "3",
            "source": "online",
            "visit_external_id": "156r459",
            "total_photos": 10,
            "sent_photos": 10,
            "completed_photos": 10,
            "completed_on_device": 2,
            "code": "RESULT_OK",
            "code_int": 1,
            "message": "Processed successfully"
        },
        {
            "visit_id": "2",
            "source": "on-device",
            "visit_external_id": "156r46",
            "total_photos": 10,
            "sent_photos": 1,
            "completed_photos": 0,
            "completed_on_device": 9,
            "code": "IN_PROGRESS",
            "code_int": 16,
            "message": "Synchronization in progress"
        }
    ],
    "total_stat": {
        "total_photos": 20,
        "sent_photos": 11,
        "completed_photos": 10,
        "completed_on_device": 11,
        "current_problem": "Error sending photos to the server"
    }
}
```

### syncPalomna()

`syncPalomna()` downloads and updates models, classes, and catalogs for on-device recognition without internet.

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `externalIds` | `List<String>` | No | `listOf()` | External store IDs |
| `storeIds` | `List<Int>` | No | `listOf()` | Internal store IDs in the library |
| `useMobile` | `Boolean` | No | `false` | Allow synchronization over mobile network |
| `isAutoUpdate` | `Boolean` | No | `false` | Update models without asking the user |

**Errors**

| Error | Error text | Description |
| --- | --- | --- |
| `OnDeviceNotAvailableException` | `On-device not available` | On-device recognition is disabled in settings |
| `OnDeviceDownloadMobileException` | `Cant download via mobile network` | Download over mobile network is forbidden (`useMobile = false`) |
| `OnDeviceNoStoreException` | `Store not found` | Store was not found |
| `Throwable` | `Unauthorized` | User is not authorized |

When the Palomna download status changes, the library sends a broadcast with `intent.action = SYNC_PALOMNA_STATE`.

| Extra | Description |
| --- | --- |
| `dataSetsProgress` | Model download progress |
| `matricesProgress` | Matrix download progress |
| `matricesTypesProgress` | Matrix type download progress |
| `metricsProgress` | Metric download progress |
| `imagesProgress` | Image download progress |

## Broadcast message

When the library receives all visit data, it sends a broadcast with `intent.action = com.ailet.app.BROADCAST_WIDGETS_RECEIVED` or `com.ailet.russia.BROADCAST_WIDGETS_RECEIVED`.

On Android 13 and later, set the export flag: the message comes from the library as a separate component.

To handle the message:

```kotlin
broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        parseBroadcastMessage(intent)
    }
}

val intentFilter = IntentFilter(IR_BROADCAST_V3)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
} else {
    registerReceiver(broadcastReceiver, intentFilter)
}

private const val NOT_SET = "not set"
private const val VISIT_ID = "visit_id"
private const val INTERNAL_VISIT_ID = "internal_visit_id"
private const val STORE_ID = "store_id"
private const val TASK_ID = "task_id"
private const val TOTAL_PHOTOS = "total_photos"
private const val COMPLETED_PHOTOS = "completed_photos"
private const val COMPLETED_ON_DEVICE = "completed_on_device"
private const val SOURCE = "source"
private const val RESULT = "result"

private fun parseBroadcastMessage(intent: Intent) {
    val extras = intent.extras
    val visitId = extras?.getString(VISIT_ID, NOT_SET)
    val internalVisitId = extras?.getString(INTERNAL_VISIT_ID, NOT_SET)
    val storeId = extras?.getString(STORE_ID, NOT_SET)
    val taskId = extras?.getString(TASK_ID, NOT_SET)
    val totalPhotos = extras?.getInt(TOTAL_PHOTOS, 0)
    val completedPhotos = extras?.getInt(COMPLETED_PHOTOS, 0)
    val completedOnDevice = extras?.getInt(COMPLETED_ON_DEVICE, 0)
    val source = extras?.getString(SOURCE, "online")
    val result = extras?.getString(RESULT, null)

    result?.let { uriString ->
        try {
            val fileFromUri = readFromUri(Uri.parse(uriString))
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
```

`IR_BROADCAST_V3` is the action constant in your project. Use `com.ailet.app.BROADCAST_WIDGETS_RECEIVED` or `com.ailet.russia.BROADCAST_WIDGETS_RECEIVED` depending on the build.

| Parameter | Type | Description |
| --- | --- | --- |
| `internal_visit_id` | `String` | Internal visit ID in Ailet |
| `visit_id` | `String` | Visit ID |
| `store_id` | `String` | Store ID |
| `user_id` | `String` | User ID in Ailet |
| `total_photos` | `Int` | Number of photos in the visit |
| `completed_photos` | `Int` | Number of processed photos |
| `completed_on_device` | `Int` | **(Palomna build)** Number of photos recognized on-device |
| `source` | `String` | **(Palomna build)** Data source (`online` / `on-device`) |
| `result` | `String` | `Uri` of the report file |

## Migrating from IntRtl

From version 3.0 the `IntRtl` client class is deprecated. Use `AiletClient`.

The new client methods match the [legacy client methods](https://github.com/intrtl/AiletLibraryExamples/blob/master/Android/IrLibExample/readme.md#методы).

Each deprecated `IntRtl` method has `ReplaceWith` in the `Deprecated` annotation. Android Studio can replace the old call with a new one from the hint.

Differences in the new client:

1. Methods do not block the thread. The call returns `AiletCall`. Then use `execute()` or `executeBlocking()`.
2. With `executeBlocking()`, you choose the execution thread.

Before version 3.0.0:

```kotlin
client.setPortal(portalName)
```

From version 3.0.0:

```kotlin
Ailet.getClient()
    .setPortal(portalName)
    .execute({ result ->
        when (result) {
            // handle the result
        }
    }, { throwable ->
        // handle the error
    })
```

## Report example

The full JSON is in [report_exaple.json](./report_exaple.json).

## Known issues

### Gradle 8.x and obfuscation

To keep a Gradle 8.x build from failing during obfuscation, check the rules in [ProGuard rules](#proguard-rules). Gradle 8.x needs a keep rule for the `com.ailet.**` package.
