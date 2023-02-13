# Integrating the Ailet Library v.3 for Android into your project

- [Integrating the Ailet Library v.3 for Android into your project](#integrating-the-ailet-library-v3-for-android-into-your-project)
  - [1. Connecting the Ailet Lib module using Maven (GitHub)](#1-connecting-the-ailet-lib-module-using-maven-github)
    - [1.1. Create a GitHub personal access token](#11-create-a-github-personal-access-token)
    - [1.2. Add the Ailet repository to the project](#12-add-the-ailet-repository-to-the-project)
    - [1.3. Add the following two dependencies to ``build.gradle`` file of the module](#13-add-the-following-two-dependencies-to-buildgradle-file-of-the-module)
  - [2. Usage](#2-usage)
    - [2.1. Initialization](#21-initialization)
    - [2.2. Usage](#22-usage)
  - [3. Methods](#3-methods)
    - [3.1 The getServers() method. List of available servers](#31-the-getservers-method-list-of-available-servers)
    - [3.2 The init() method. Initialization of the library](#32-the-init-method-initialization-of-the-library)
    - [3.3 The start() method. The beginning of the visit](#33-the-start-method-the-beginning-of-the-visit)
    - [3.4 The getReports() method. Getting a report on the visit](#34-the-getreports-method-getting-a-report-on-the-visit)
    - [3.5 The showSummaryReport() method. Displaying a summary report on the visit](#35-the-showsummaryreport-method-displaying-a-summary-report-on-the-visit)
    - [3.6 The setPortal() method. Choosing the active portal](#36-the-setportal-method-choosing-the-active-portal)
  - [4. Sample report](#4-sample-report)

## 1. Connecting the Ailet Lib module using Maven (GitHub)

Here and further we will call the Ailet Library Module as *the Ailet Lib* or simply *the module*.

### 1.1. Create a GitHub personal access token

- In the upper right corner of any GitHub page, click on your profile photo, and then click **Settings**.
- In the left sidebar, select **Developer settings**
- In the left sidebar, select **Personal access tokens**, and then click **Generate new token** to create a new token.
- Set scope ``read:packages``

### 1.2. Add the Ailet repository to the project

**Option 1** (basic). Add the repository to the root ``build.gradle``:

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

**Option 2** (using ``settings.gradle`` and ``DependencyResolutionManagement``). Add the repository to ``settings.gradle``:

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

### 1.3. Add the following two dependencies to ``build.gradle`` file of the module

```groovy
def ailetLibVersion = '3.7.1'
// the Ailet Lib module
implementation "com.ailet.android:lib:$ailetLibVersion"
// optional: the technical support module
implementation "com.ailet.android:lib-feature-techsupport-intercom:$ailetLibVersion"
```

## 2. Usage

### 2.1. Initialization

Before you start, you need to initialize the ``Ailet`` object in your ``Application`` heir:

```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // modules of the library's optional functionality
        val features = setOf<AiletFeature>(
                DefaultStockCameraFeature(), // standard camera module
                IntercomTechSupportManager(this), // technical support module
                HostAppInstallInfoProviderFeature(
                        this,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                        AiletLibInstallInfo
                ) // identification module (will help in diagnosing problems)
        )

        // initial authorization token provided by the Ailet team
        val accessToken = "..."
        
        // initializing the Ailet Lib with your token and selected modules
        Ailet.initialize(this, accessToken, features)
    }
}
```

### 2.2. Usage

After initialization, a single library client ``AiletClient`` becomes available to you. Now, you can use it to call library's methods:

```kotlin
Ailet.getClient()
```

## 3. Methods

Since version 3.0, the ``IntRtl`` library client class has been marked as deprecated. 
Instead, you need to use an instance of ``AiletClient`` class.
The methods of the new client conceptually correspond to
[methods](https://github.com/intrtl/AiletLibraryExamples/blob/master/Android/IrLibExample/readme_en.md) of the deprecated one:

Method  | Description
--- | ---
[init]() | Initializing the library, authorizing the user, and loading catalogs.
[start]() | Starting the visit.
[getReports]() | Returning the report on the specified visit.
[showSummaryReport]() | Summary report on the specified visit.
[setPortal]() | Choosing a portal to work with.

For the convenience of switching to a new client, ``replaceWith`` blocks have been added to the Deprecated annotation of each ``IntRtl`` method, allowing you to automatically replace the old method with a new one using Android Studio prompts.

However, there are several significant differences between the old and new clients:

1. The client's methods are no longer blocking. Calling each of them returns an object ``AiletCall``, which, in turn, can be executed either synchronously using the ``executeBlocking()`` method, or asynchronously using the ``execute()`` method.

    Up to version 3.0.0:

    ```kotlin
    client.setPortal(portalName)
    ```

    Starting from version 3.0.0:

    ```kotlin
    Ailet.getClient()
            .setPortal(portalName)
            .execute({ result -> 
                when(result) {
                    // result processing
                }
            }, { throwable -> 
                // error handling
            })
    ```
2. Blocking execution of methods is also possible, but in this case the responsibility for choosing the correct execution flow falls on the library user.

    ```kotlin
    val result = Ailet.getClient()
            .setPortal(portalName)
            .executeBlocking()
    ```

### 3.1 The getServers() method. List of available servers

The method returns a list of servers (AiletServer) that can be used in ``init`` method. The method is optional and is only necessary for the multiport mode.

Parameter | Type | Description | Required | By default
---------|-----|----------|:---------:|:-----------------:
login           |String      | The user's login in the Ailet system.      | + | 
password        |String      | The user's password in the Ailet system.     | + | 
externalUserId  |String      | External user ID (user ID from the external system). | | null 

### 3.2 The init() method. Initialization of the library

This method is responsible for authorizing the user in the library, as well as for initializing the Ailet Lib itself and loading catalogs necessary for the module to work.

Parameter | Type | Description | Required | By default
---------|-----|----------|:---------:|:-----------------:
login           |String      | The user's login in the Ailet system.      | + | 
password        |String      | The user's password in the Ailet system.     | + | 
externalUserId  |String      | External user ID (user ID from the external system). | | null  
multiPortalMode |Boolean     | Multiportality support.            | | true 
server          |AiletServer | The server to which you are logging in.     | | null 
isNeedSyncCatalogs|Boolean | Flag for the need to synchronize catalogs.  | | true


### 3.3 The start() method. The beginning of the visit

The method starts photoshooting screen during the visit.

Parameter | Type | Description | Required | By default
---------|-----|----------|:-:|:-:
storeId         |AiletMethodStart.StoreId      | External identifier of the point of sale.        | + | 
externalVisitId |String      | External visit ID.     |  | null
sceneGroupId    |Int         | ID of the scene group. |  | null 
taskId          |String      | External task ID.      |  | null 
visitType       |String      | Type of visit (before/after merchandising).         | | null 

### 3.4 The getReports() method. Getting a report on the visit

The method returns a report on the visit in the `json` format ([see example]()).

Parameter | Type | Description | Required | By default
---------|-----|----------|:-:|:-:
externalVisitId |String | External visit ID | + | 
taskId          |String | External task ID. |   | null 
visitType       |String | Type of visit (before/after merchandising).  | | null 

### 3.5 The showSummaryReport() method. Displaying a summary report on the visit

The method opens the screen for viewing the summary report on the visit.

Parameter | Type | Description | Required | By default
---------|-----|----------|:-:|:-:
externalVisitId |String | External visit ID | + |  
taskId          |String | External task ID. |   | null 
visitType       |String | Type of visit (before/after merchandising). | | null 

### 3.6 The setPortal() method. Choosing the active portal

The method is used to install the current portal in multi-portal mode.

Parameter | Type | Description | Required 
---------|-----|----------|:-:
portalName | String |Portal ID | + 


## 4. Sample report

```json
{
    "task_id": "67",
    "photosCounter": 4,
    "scenesCounter": 2,
    "notDetectedPhotosCounter": 0,
    "notDetectedScenesCounter": 0,
    "local_visit_id": "e4ef7672014924-def3dccc",
    "visit_id": "2",
    "status": "RESULT_OK",
    "result": {
        "visit_id": "2",
        "total_photos": 0,
        "sended_photos": 0,
        "code": "RESULT_OK",
        "codeInt": 1,
        "message": "Successfully processed"
    },
    "photos": {
        "e4ef7672014924-def3dccc-PHOTO-000001": {
            "error": {
                "code": "RESULT_OK",
                "codeInt": 1,
                "message": "Successfully processed"
            },
            "products": [
                {
                    "product_id": "00fc4c31-a332-4a6b-b219-6dceb80e245d",
                    "facing": 1,
                    "facing_group": 1,
                    "price": 0,
                    "price_type": 0,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Village house, Cream pit.erased.20%, Tetra, .480"
                },
                {
                    "product_id": "147f7d0e-35c3-4edb-aba1-e31084406494",
                    "facing": 4,
                    "facing_group": 0,
                    "price": 14.99,
                    "price_type": 1,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Ermigurt 3,2%, Cup, .100"
                }
            ],
            "scene_type": "Warm shelf",
            "scene_id": "e4ef7672014924-def3dccc-SCENE-000001",
            "image_path": "/data/user/0/com.intrtl.lib2test/files/files/1/19/19f/19f2/19f2e/19f2e39d0991e36585bad2ea58ee0e0f.jpg",
            "image_url": "https://dairy-demo.intrtl.com/api/photo_raw/2023/01/17/e4ef7672014924-def3dccc/2023-01-17-15-44-30-2885-o.jpg",
            "task_id": "67"
        },
        "e4ef7672014924-def3dccc-PHOTO-000002": {
            "error": {
                "code": "RESULT_OK",
                "codeInt": 1,
                "message": "Successfully processed"
            },
            "products": [
                {
                    "product_id": "00fc4c31-a332-4a6b-b219-6dceb80e245d",
                    "facing": 2,
                    "facing_group": 2,
                    "price": 0,
                    "price_type": 0,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Village house, Cream pit.erased.20%, Tetra, .480"
                },
                {
                    "product_id": "147f7d0e-35c3-4edb-aba1-e31084406494",
                    "facing": 6,
                    "facing_group": 2,
                    "price": 14.99,
                    "price_type": 1,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Ermigurt 3,2%, Cup, .100"
                }
            ],
            "scene_type": "Cold shelf",
            "scene_id": "e4ef7672014924-def3dccc-SCENE-000002",
            "image_path": "/data/user/0/com.intrtl.lib2test/files/files/4/4c/4c9/4c9d/4c9d1/4c9d1afd71269a5e08791f963938e5f3.jpg",
            "image_url": "https://dairy-demo.intrtl.com/api/photo_raw/2023/01/17/e4ef7672014924-def3dccc/2023-01-17-15-44-47-4064-o.jpg",
            "task_id": "67"
        },
        "e4ef7672014924-def3dccc-PHOTO-000004": {
            "error": {
                "code": "RESULT_OK",
                "codeInt": 1,
                "message": "Successfully processed"
            },
            "products": [
                {
                    "product_id": "6156f4da52105-4578",
                    "facing": 1,
                    "facing_group": 0,
                    "price": 59.9,
                    "price_type": 1,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Fruttis, cup, .115"
                },
                {
                    "product_id": "6156f545c20df-5885",
                    "facing": 1,
                    "facing_group": 1,
                    "price": 28.99,
                    "price_type": 0,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Fruttis 8% , cup, .115"
                }
            ],
            "scene_type": "Cold shelf",
            "scene_id": "e4ef7672014924-def3dccc-SCENE-000002",
            "image_path": "/data/user/0/com.intrtl.lib2test/files/files/7/76/767/767c/767c3/767c32d09dc3617bb1c049378ccdb7b7.jpg",
            "image_url": "https://dairy-demo.intrtl.com/api/photo_raw/2023/01/17/e4ef7672014924-def3dccc/2023-01-17-16-37-46-9543-o.jpg",
            "task_id": "67"
        },
        "e4ef7672014924-def3dccc-PHOTO-000005": {
            "error": {
                "code": "RESULT_OK",
                "codeInt": 1,
                "message": "Successfully processed"
            },
            "products": [],
            "scene_type": "Cold shelf",
            "scene_id": "e4ef7672014924-def3dccc-SCENE-000002",
            "image_path": "/data/user/0/com.intrtl.lib2test/files/files/7/7d/7da/7da2/7da27/7da27907aa428fbe3979764876eec411.jpg",
            "image_url": "https://dairy-demo.intrtl.com/api/photo_raw/2023/01/17/e4ef7672014924-def3dccc/2023-01-17-16-44-31-8028-o.jpg",
            "task_id": "67"
        }
    },
    "assortment_achievement": [
        {
            "brand_id": "de548597-55c3-49bf-8d17-9a29c86929b1",
            "brand_name": "Actimel",
            "id": "7e132a93-703d-11e7-a5c2-000d3a250e47",
            "facing_fact": 0,
            "facing_plan": 1,
            "facing_real": 0,
            "price": 0,
            "price_type": 0,
            "name": "Actimel, cherry, bottle .600",
            "product_category_id": "5e5236ee77ac4-7319",
            "category_name": "OTHER_H"
        },
        {
            "brand_id": "de548597-55c3-49bf-8d17-9a29c86929b1",
            "brand_name": "Actimel",
            "id": "6796ad60-50e9-465b-ae6c-3a31a15f3d19",
            "facing_fact": 0,
            "facing_plan": 1,
            "facing_real": 0,
            "price": 0,
            "price_type": 0,
            "name": "Actimel,cherry, bottle .100",
            "product_category_id": "5e5236ee77ac4-7319",
            "category_name": "OTHER_H"
        }
    ],
    "assortment_achievement_by_metrics": [
        {
            "products": [
                {
                    "brand_id": "de548597-55c3-49bf-8d17-9a29c86929b1",
                    "brand_name": "Actimel",
                    "id": "7e132a93-703d-11e7-a5c2-000d3a250e47",
                    "facing_fact": 0,
                    "facing_plan": 1,
                    "facing_real": 0,
                    "price": 0,
                    "price_type": 0,
                    "name": "Actimel, cherry, bottle .600",
                    "product_category_id": "5e5236ee77ac4-7319",
                    "category_name": "OTHER_H"
                },
                {
                    "brand_id": "de548597-55c3-49bf-8d17-9a29c86929b1",
                    "brand_name": "Actimel",
                    "id": "6796ad60-50e9-465b-ae6c-3a31a15f3d19",
                    "facing_fact": 0,
                    "facing_plan": 1,
                    "facing_real": 0,
                    "price": 0,
                    "price_type": 0,
                    "name": "Actimel, cherry, bottle .100",
                    "product_category_id": "5e5236ee77ac4-7319",
                    "category_name": "OTHER_H"
                }
            ],
            "assortment_achievement_name": "General"
        }
    ],
    "share_shelf": {
        "share_shelf_by_visit": [
            {
                "plan": 0,
                "percent": 0,
                "value": 0,
                "value_previous": 0,
                "numerator": 0,
                "denominator": 72
            }
        ],
        "share_shelf_by_macrocategories": [
            {
                "facing": "72.0",
                "product_macro_category_id": "5e5236ee77ac4-7319",
                "product_macro_category_name": "OTHER_H",
                "value": 72,
                "percent": 0,
                "matched": 0
            }
        ],
        "share_shelf_by_categories": [
            {
                "facing": "72.0",
                "macro_category_id": "5e5236ee77ac4-7319",
                "product_category_id": "5e5236ee77ac4-7319",
                "product_category_name": "OTHER_H",
                "value": 72,
                "percent": 0,
                "matched": 0
            }
        ],
        "share_shelf_by_brands": [
            {
                "facing": "0.0",
                "product_category_id": "5e5236ee77ac4-7319",
                "product_category_name": "OTHER_H",
                "brand_id": "ed9c3c78-2ecb-4373-935a-21c3548bb1f5",
                "brand_name": "Ermigurt",
                "is_own": 0,
                "value": 0,
                "percent": 0
            },
            {
                "facing": "0.0",
                "product_category_id": "5e5236ee77ac4-7319",
                "product_category_name": "OTHER_H",
                "brand_id": "1f7e652d-a65e-4297-a50e-008387b592e0",
                "brand_name": "Fruttis",
                "is_own": 0,
                "value": 0,
                "percent": 0
            }
        ],
        "share_shelf_type": "facing_cm",
        "share_shelf_name": "sos_2"
    },
    "share_shelf_by_metrics": [
        {
            "share_shelf_by_visit": [
                {
                    "plan": 0,
                    "percent": 0,
                    "value": 0,
                    "value_previous": 0,
                    "numerator": 0,
                    "denominator": 72
                }
            ],
            "share_shelf_by_macrocategories": [
                {
                    "facing": "72.0",
                    "product_macro_category_id": "5e5236ee77ac4-7319",
                    "product_macro_category_name": "OTHER_H",
                    "value": 72,
                    "percent": 0,
                    "matched": 0
                }
            ],
            "share_shelf_by_categories": [
                {
                    "facing": "72.0",
                    "macro_category_id": "5e5236ee77ac4-7319",
                    "product_category_id": "5e5236ee77ac4-7319",
                    "product_category_name": "OTHER_H",
                    "value": 72,
                    "percent": 0,
                    "matched": 0
                }
            ],
            "share_shelf_by_brands": [
                {
                    "facing": "0.0",
                    "product_category_id": "5e5236ee77ac4-7319",
                    "product_category_name": "OTHER_H",
                    "brand_id": "ed9c3c78-2ecb-4373-935a-21c3548bb1f5",
                    "brand_name": "Ermigurt",
                    "is_own": 0,
                    "value": 0,
                    "percent": 0
                },
                {
                    "facing": "0.0",
                    "product_category_id": "5e5236ee77ac4-7319",
                    "product_category_name": "OTHER_H",
                    "brand_id": "1f7e652d-a65e-4297-a50e-008387b592e0",
                    "brand_name": "Fruttis",
                    "is_own": 0,
                    "value": 0,
                    "percent": 0
                }
            ],
            "share_shelf_type": "facing_cm",
            "share_shelf_name": "sos_2"
        }
    ],
    "perfectstore": {
        "tasks": [
            {
                "kpis": [
                    {
                        "name": "OSA SKU",
                        "metric_type": "osa_sku",
                        "matrix_type": "general",
                        "plan_value": 2,
                        "fact_value": 1,
                        "percentage": 0.5,
                        "score_value": 1
                    },
                    {
                        "name": "OSA Facing",
                        "metric_type": "osa_facing",
                        "matrix_type": "general",
                        "plan_value": 3,
                        "fact_value": 8,
                        "percentage": 1,
                        "score_value": 8
                    }
                ],
                "questions": [
                    {
                        "index": 2,
                        "type": "multiselect",
                        "name": "Has the store a CSR zone?",
                        "answers": [
                            {
                                "index": 1,
                                "name": "yes",
                                "point": 0
                            }
                        ]
                    },
                    {
                        "index": 4,
                        "type": "select",
                        "name": "Has the store a CSR zone?",
                        "answers": [
                            {
                                "index": 2,
                                "name": "no",
                                "point": 0
                            }
                        ]
                    },
                    {
                        "index": 3,
                        "type": "text",
                        "name": "asdasd",
                        "answers": [
                            {
                                "index": 1,
                                "name": "123",
                                "point": 0
                            }
                        ]
                    }
                ],
                "id": "e4ef7a488012c1-98ec7589",
                "name": "for testing",
                "percentage": 10.9,
                "total_score": 9
            }
        ],
        "total_visit_score": 9
    },
    "visit_stats": {
        "photo": {
            "badQuality": 0,
            "completed": 4,
            "created": 0,
            "deleted": 1,
            "goodQuality": 4,
            "retake": 0,
            "sent": 0,
            "sentWithError": 0,
            "status": "RESULT_OK",
            "uncompressed": 4,
            "wait": 0
        }
    }
}
```