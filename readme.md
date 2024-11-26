![Latest Release](https://img.shields.io/badge/latest%20release-4.5.4-brightgreen)

# 1. Integrate Ailet library into project

- [1. Integrate Ailet library into project](#1-integrate-ailet-library-into-project)
  - [1.1. Подключение используя Maven (GitHub)](#11-подключение-используя-maven-github)
    - [1.1.1. Создайте GitHub personal access token](#111-создайте-github-personal-access-token)
    - [1.1.2. Добавьте в проект репозиторий Ailet](#112-добавьте-в-проект-репозиторий-ailet)
    - [1.1.3. Добавьте в build.gradle модуля две зависимости:](#113-добавьте-в-buildgradle-модуля-две-зависимости)
  - [1.2. Использование](#12-использование)
    - [1.2.1. Инициализация](#121-инициализация)
    - [1.2.2. Использование](#122-использование)
  - [1.3 Методы](#13-методы)
    - [1.3.1 Список доступных серверов. Метод getServers()](#131-список-доступных-серверов-метод-getservers)
    - [1.3.2 Инициализация библиотеки. Метод init()](#132-инициализация-библиотеки-метод-init)
    - [1.3.3 Начало визита. Метод start()](#133-начало-визита-метод-start)
    - [1.3.4 Получение отчета по визиту. Метод getReports()](#134-получение-отчета-по-визиту-метод-getreports)
    - [1.3.5 Отображение сводного отчета по визиту. Метод showSummaryReport()](#135-отображение-сводного-отчета-по-визиту-метод-showsummaryreport)
    - [1.3.6 Выбор активного портала. Метод setPortal()](#136-выбор-активного-портала-метод-setportal)
    - [1.3.7 Загрузка справочников. Метод requestSyncCatalogs()](#137-загрузка-справочников-метод-requestsynccatalogs)
      - [Пример загрузки справочников в мультипортальном режиме](#пример-загрузки-справочников-в-мультипортальном-режиме)
    - [1.3.8 Отображение визита. Метод showVisit()](#138-отображение-визита-метод-showvisit)
    - [1.3.9 Завершение визита. Метод finishVisit()](#139-завершение-визита-метод-finishvisit)
  - [1.4 Широковещательное (broadcast) сообщение](#14-широковещательное-broadcast-сообщение)
  - [1.7 Известные проблемы и их устранение](#17-известные-проблемы-и-их-устранение)
    - [1.7.1 Gradle 8.x и обсфукация](#171-gradle-8x-и-обсфукация)
  - [1.6 Пример отчета](#16-пример-отчета)

## 1.1. Подключение используя Maven (GitHub)

### 1.1.1. Создайте GitHub personal access token

- В правом верхнем углу любой страницы щелкните фотографию своего профиля и нажмите «Settings» .
- В левой боковой панели нажмите «Developer settings»
- В левой боковой панели нажмите «Personal access tokens» и затем чтобы создать новый токен нажмите «Generate new token»
- Задайте scope ``read:packages``

### 1.1.2. Добавьте в проект репозиторий Ailet

Вариант 1 (классический). Добавьте репозиторий в корневой ``build.gradle``:

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

Вариант 2 (используя ``settings.gradle`` и ``DependencyResolutionManagement``). Добавьте репозиторий
в ``settings.gradle``:

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

### 1.1.3. Добавьте в build.gradle модуля две зависимости:

[Список версий](https://github.com/intrtl/IRLib/packages/1361609/versions)

```groovy
// библиотека Ailet
implementation "com.ailet.android:lib:1.0.0"// Указать конкретную версию (список версий по ссылке https://github.com/intrtl/IRLib/packages/1361609/versions)
implementation "com.ailet.android:lib:+"// Последняя доступная версия
// необязательно: модуль техподдержки
implementation "com.ailet.android:lib-feature-techsupport-intercom:1.0.0"// Конкретная версия, совпадает с версией библиотеки
implementation "com.ailet.android:lib-feature-techsupport-intercom:+"// Последняя доступная версия
```

## 1.2. Использование

### 1.2.1. Инициализация

Перед началом работы необходимо инициализировать объект ``Ailet`` в вашем наследнике ``Application``:

```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // модули опционального функционала библиотеки
        val features = setOf<AiletFeature>(
                DefaultStockCameraFeature(), // модуль стоковой камеры
                IntercomTechSupportManager(this), // модуль техподдержки
                HostAppInstallInfoProviderFeature(
                        this,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                        AiletLibInstallInfo
                ) // модуль идентификации (поможет при диагностике проблем)
        )

        // токен начальной авторизации, предоставленный командой Ailet
        val accessToken = "..."
        
        // инициализация библиотеки с вашим токеном и выбранными модулями
        Ailet.initialize(this, accessToken, features)
    }
}
```

Если в приложении пользователем дано разрешение на использование камеры и при вызове экрана камеры Ailet он сразу же закрывается без ошибок (exceptions), то добавьте в поле ``features`` дополнительный модуль

```kotlin
DefaultAiletPermissionsFeature(
    excludedPermissions = setOf(AiletPermissionsFeature.Exclude.CAMERA)
)
```

### 1.2.2. Использование
После инициализации вам становится доступен единый клиент библиотеки ``AiletClient``, который вы можете использовать для вызова ее методов:
```kotlin
Ailet.getClient()
```

## 1.3 Методы
> **ВНИМАНИЕ!** В Java для методов не применяются значения по умолчанию, по этому необходимо заполнять все поля.

Начиная с версии 3.0 класс-клиент библиотеки ``IntRtl`` отмечен как устаревший. 
Вместо него необходимо использовать экземпляр ``AiletClient``.
Методы нового клиента концептуально соответствуют
[методам устаревшего клиента](https://github.com/intrtl/AiletLibraryExamples/blob/master/Android/IrLibExample/readme.md#методы):

Метод  | Описание
--- | ---
[init](#132-инициализация-библиотеки-метод-init) | Инициализация библиотеки, авторизация пользователя и загрузка справочников.
[getServers](#131-список-доступных-серверов-метод-getservers) | Получение списка доступных порталов.
[start](#133-начало-визита-метод-start) | Старт визита.
[getReports](#134-получение-отчета-по-визиту-метод-getreports) | Возвращает отчет по указанному визиту.
[showSummaryReport](#135-отображение-сводного-отчета-по-визиту-метод-showsummaryreport) | Сводный отчет по указанному визиту.
[setPortal](#136-выбор-активного-портала-метод-setportal) | Установка активного портала.
[requestSyncCatalogs](#137-загрузка-справочников-метод-requestsynccatalogs) | Загрузка справочников.

Для удобства перехода на новый клиент, в аннотацию Deprecated каждого метода ``IntRtl`` добавлены блоки ``ReplaceWith``, позволяющие автоматически заменить старый метод на новый с помощью подсказок Android Studio.

Тем не менее, между старым и новым клиентами есть несколько существенных отличий:

1. Методы клиента теперь не являются блокирующими. Вызов каждого из них возвращает объект ``AiletCall``, который, в свою очередь, можно выполнить либо синхронно с помощью метода ``executeBlocking()``, либо асинхронно с помощью метода ``execute()``.

    До версии 3.0.0:

    ```kotlin
    client.setPortal(portalName)
    ```

    Начиная с версии 3.0.0:

    ```kotlin
    Ailet.getClient()
            .setPortal(portalName)
            .execute({ result -> 
                when(result) {
                    // обработка результата
                }
            }, { throwable -> 
                // обработка ошибки
            })
    ```
2. Блокирующее выполнение методов также возможно, но в этом случае ответственность за выбор правильного потока исполнения ложится на пользователя библиотеки.

    ```kotlin
    val result = Ailet.getClient()
            .setPortal(portalName)
            .executeBlocking()
    ```

3. Пример вызова на Java
   
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
            return null;
        },
        throwable -> {
            return null;
        },
        () -> {
            return null;
        }
    );
   ```
### 1.3.1 Список доступных серверов. Метод getServers()

Метод отдаем список серверов (AiletServer), которые можно использовать в методы init. Метод необзательный и необходим только для мультипортального режима.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:---------:|:-----------------:
login           |String      | Логин пользователя в системе Ailet.      | + | 
password        |String      | Пароль пользователя в системе Ailet.     | + | 
externalUserId  |String      | Внешний идентификатор пользователя (ID пользователя из внешней системы). | | null 

**Ошибки**
Ошибка  | Описание 
---------|----------
BackendApiException | Ошибка сервера с [HTTP кодом](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) 

### 1.3.2 Инициализация библиотеки. Метод init()

Данный метод отвечает за авторизацию пользователя в библиотеке, а также за инициализацию самой библиотеки Ailet Lib и загрузку справочников, необходимых для работы модуля.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:---------:|:-----------------:
login           |String      | Логин пользователя в системе Ailet.      | + | 
password        |String      | Пароль пользователя в системе Ailet.     | + | 
externalUserId  |String      | Внешний идентификатор пользователя (ID пользователя из внешней системы). | | null 
multiPortalMode |Boolean     | Поддержка мультипортальности.            | | true 
server          |AiletServer | Сервер, на который выполняется вход.     | | null 
isNeedSyncCatalogs|Boolean | Необходимость синхронизации каталогов.     | | true

**Ошибки**
Ошибка  | Текст ошибки | Описание
---------|----------|----------
DataInconsistencyException | Current auth state data is null | Данные аутенитификации не корректны
IllegalStateException | Inconsistency! server is null | Сервер пустой
IllegalStateException | No portals available | Нет доступных порталов
BackendApiException | | Ошибка сервера с [HTTP кодом](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) 
Другие типы исключений | | Внутренняя ошибка библиотеки, необходимо обращение в поддержку 


### 1.3.3 Начало визита. Метод start()

Метод запускает съемку в рамках визита.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:-:|---
storeId         |AiletMethodStart.StoreId      | Внешний идентификатор торговой точки.        | + | 
externalVisitId |String      | Внешний идентификатор визита.     |  | null
sceneGroupId    |Int         | Идентификатор группы сцен.            | | null 
taskId       |String      | Внешний идентификатор задачи.         | | null 
visitType       |String      | Тип визита (before, after).         | | null 
visitUuid       |String      | Внутренний ИД визита.         | | null 
retailTaskIterationUuid       |String      | ИД итерации (ритейл).         | | null 
retailTaskId       |String      | ИД задачи (ритейл).         | | null 
retailTaskActionId       |String      | ИД экшена (ритейл).         | | null 
retailTaskActionId       |String      | ИД экшена (ритейл).         | | null 
sceneTypes | List | Список типов сцен | | listOf()
launchConfig | LaunchConfig | Конфигурация запуска | | AiletMethodStart.LaunchConfig()

**Ошибки**
Ошибка  | Текст ошибки | Описание
---------|----------
Throwable | Uneditable(historical) visit | Завершенный визит (не редактируемый)
IllegalStateException | Inconsistent AiletClient state: (Unknown, Warning, Error) | Ошибка несогласования (с кодом)
Throwable | Unauthorized | Не авторизован

### 1.3.4 Получение отчета по визиту. Метод getReports()

Метод возвращает отчет по визиту в формате `json` ([Пример](#15-пример-отчета))

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:-:|:-:
externalVisitId | String | Внешний идентификатор визита. | +  
taskId       |String      | Внешний идентификатор задачи.         | | null 
visitType       |String      | Тип визита (before, after).         | | null 

**Ошибки**
Ошибка  | Текст ошибки | Описание
---------|----------|----------
AiletException | Visit with externalId [externalId] is not found | Визит в идентификатором не найден

### 1.3.5 Отображение сводного отчета по визиту. Метод showSummaryReport()

Метод открывает экран просмотра сводного отчета по визиту.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:-:|:-:
externalVisitId | String | Внешний идентификатор визита. | +  
taskId       |String      | Внешний идентификатор задачи.         | | null 
visitType       |String      | Тип визита (before, after).         | | null

**Ошибки**
Ошибка  | Описание 
---------|----------|----------
Throwable | Unauthorized4
IllegalArgumentException | No store for externalId [externalId]
IllegalArgumentException | No store for storeId [storeId]
IllegalArgumentException | No visit for summary report request $param found
IllegalArgumentException | Incorrect historical visit params
BackendApiException | | Ошибка сервера с [HTTP кодом](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) 
RuntimeException | No visit/Offline

### 1.3.6 Выбор активного портала. Метод setPortal()

Метод используется для установки текущего портала в мультипортальном режиме.

Параметр | Тип | Описание | Обязательный 
---------|-----|----------|:-:
portalName | String | Идентификатор портала | + 

**Ошибки**
Ошибка  | Текст ошибки | Описание
---------|----------|----------
Throwable | Unauthorized | Не авторизован
IllegalArgumentException | No [server] found in local portals list | Не найден сервер в списке 
AiletException | no [server] in servers list | Нет сервера в списке 

### 1.3.7 Загрузка справочников. Метод requestSyncCatalogs()

Метод нужен для загрузки справочника выбранного портала в мультипортальном режиме.

Параметр | Тип | Описание | По умолчанию
---------|-----|----------|:-:
syncMode | AiletMethodSyncCatalogs.SyncMode | AiletMethodSyncCatalogs.SyncMode.EAGER - загрузка все справочников</br>AiletMethodSyncCatalogs.SyncMode.SOFT - загрузка только обязательных справочников | AiletMethodSyncCatalogs.SyncMode.EAGER
strategy | AiletMethodSyncCatalogs.Strategy | AiletMethodSyncCatalogs.Strategy.Schedule - поставить загрузку справочников в очередь </br>AiletMethodSyncCatalogs.Strategy.SyncRightNow - синхронизировать немедленно | AiletMethodSyncCatalogs.Strategy.Schedule

**Ошибки**
Ошибка  | Текст ошибки | Описание
---------|----------|----------
Throwable | Unauthorized | Не авторизован

#### Пример загрузки справочников в мультипортальном режиме

```kotlin
Ailet.getClient()
    .getServers(
        "login",
        "password",
        "external_user_id"
    )
    .execute({ result ->
        runBlocking {
            async(Dispatchers.Default) {
                result.servers.forEach { server ->
                    Ailet.getClient().setPortal(
                        portalName = server.name
                    ).executeBlocking()
                    
                    Ailet.getClient().requestSyncCatalogs(
                        syncMode = AiletMethodSyncCatalogs.SyncMode.EAGER,
                        strategy = AiletMethodSyncCatalogs.Strategy.SyncRightNow
                    ).executeBlocking()
                }
            }.await()
            
            //Действия выполняемые после загрузки всех справочников
    }, { })
```

### 1.3.8 Отображение визита. Метод showVisit()

Метод открывает экран просмотра фотографий визита. Открывается первая фотография в визите.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:-:|:-:
externalVisitId | String | Внешний идентификатор визита. | +  
taskId       |String      | Внешний идентификатор задачи.         | | null 
visitType       |String      | Тип визита (before, after).         | | null 

Ошибки
Ошибка  | Текст ошибки | Описание
---------|----------|----------
Throwable | Unauthorized | Не авторизован
IllegalArgumentException | No visit with id: [visitId] | Нет визита
IndexOutOfBoundsException | No photos in visit with id: [$visitId] | Визит пустой
RuntimeException | No visit/Offline | Нет локальноного визита и устройство оффлайн (невозможно проверить есть ли визит на сервере)
BackendApiException | | Ошибка сервера с [HTTP кодом](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) 

### 1.3.9 Завершение визита. Метод finishVisit()

Метод закрывает визит, после этого он доступен только для просмотра.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:-:|:-:
externalVisitId | String | Внешний идентификатор визита. | +  

Ошибки
Ошибка  | Текст ошибки | Описание
---------|----------|----------
Throwable | Unauthorized | Не авторизован
AiletException | Visit with externalId [externalVisitId] is already finished | Визит завершен
AiletException | Visit with externalId [externalVisitId] is not found | Визит не найден


## 1.4 Широковещательное (broadcast) сообщение 

При получении всех данных по визту библиотека Ailet генерирует широковещательное сообщение с ```intent.action = com.ailet.app.BROADCAST_WIDGETS_RECEIVED``` (либо ```com.ailet.russia.BROADCAST_WIDGETS_RECEIVED```).

**Пример обработки сообщения**

```kotlin
broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        parseBroadcaseMesasge(intent)
    }
}

registerReceiver(
    broadcastReceiver,
    IntentFilter(IR_BROADCAST_V3)
)
...

private const val NOT_SET = "not set"
private const val VISIT_ID = "visit_id"
private const val INTERNAL_VISIT_ID = "internal_visit_id"
private const val STORE_ID = "store_id"
private const val TASK_ID = "task_id"
private const val TOTAL_PHOTOS = "total_photos"
private const val COMPLETED_PHOTOS = "completed_photos"
private const val RESULT = "result"

private fun parseBroadcaseMesasge(intent: Intent) {
    val extras = intent.extras
    val visitId = extras?.getString(VISIT_ID, NOT_SET)    
    val internalVisitId = extras?.getString(INTERNAL_VISIT_ID, NOT_SET)    
    val storeId = extras?.getString(STORE_ID, NOT_SET)
    val taskId = extras?.getString(TASK_ID, NOT_SET)
    val totalPhotos = extras?.getString(TOTAL_PHOTOS, NOT_SET)
    val completedPhotos = extras?.getString(COMPLETED_PHOTOS, NOT_SET)
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

**Intent extras**

Параметр | Тип | Описание 
---------|-----|----------
internal_visit_id           |String      | Внутренний (Ailet) ИД визита
visit_id           |String      | ИД визита
store_id           |String      | ИД торговой точки
user_id           |String      | ИД пользователя (Ailet)
total_photos           |Int      | Количество фото в визите
completed_photos           |Int      | Количество обработанных фото
result           | String | Uri файла отчета 

## 1.7 Известные проблемы и их устранение

### 1.7.1 Gradle 8.x и обсфукация

Необходимо в файл `proguard-rules.pro` добавить:

```gradle
-keep class com.ailet.** { *; }
-keep class com.ailet.lib3.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.ailet.lib3.**
```

## 1.6 Пример отчета

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
        "message": "Успешно обработан"
    },
    "photos": {
        "e4ef7672014924-def3dccc-PHOTO-000001": {
            "error": {
                "code": "RESULT_OK",
                "codeInt": 1,
                "message": "Успешно обработан"
            },
            "products": [
                {
                    "product_id": "00fc4c31-a332-4a6b-b219-6dceb80e245d",
                    "facing": 1,
                    "facing_group": 1,
                    "price": 0,
                    "price_type": 0,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Домик в деревне Сливки пит.стер.20%, Тетра, .480"
                },
                {
                    "product_id": "147f7d0e-35c3-4edb-aba1-e31084406494",
                    "facing": 4,
                    "facing_group": 0,
                    "price": 14.99,
                    "price_type": 1,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Ermigurt Пудинг ШОКОЛАДНЫЙ 3,2%, Стакан, .100"
                }
            ],
            "scene_type": "Теплая полка",
            "scene_id": "e4ef7672014924-def3dccc-SCENE-000001",
            "image_path": "/data/user/0/com.intrtl.lib2test/files/files/1/19/19f/19f2/19f2e/19f2e39d0991e36585bad2ea58ee0e0f.jpg",
            "image_url": "https://dairy-demo.intrtl.com/api/photo_raw/2023/01/17/e4ef7672014924-def3dccc/2023-01-17-15-44-30-2885-o.jpg",
            "task_id": "67"
        },
        "e4ef7672014924-def3dccc-PHOTO-000002": {
            "error": {
                "code": "RESULT_OK",
                "codeInt": 1,
                "message": "Успешно обработан"
            },
            "products": [
                {
                    "product_id": "00fc4c31-a332-4a6b-b219-6dceb80e245d",
                    "facing": 2,
                    "facing_group": 2,
                    "price": 0,
                    "price_type": 0,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Домик в деревне Сливки пит.стер.20%, Тетра, .480"
                },
                {
                    "product_id": "147f7d0e-35c3-4edb-aba1-e31084406494",
                    "facing": 6,
                    "facing_group": 2,
                    "price": 14.99,
                    "price_type": 1,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Ermigurt Пудинг ШОКОЛАДНЫЙ 3,2%, Стакан, .100"
                }
            ],
            "scene_type": "Холодная полка ",
            "scene_id": "e4ef7672014924-def3dccc-SCENE-000002",
            "image_path": "/data/user/0/com.intrtl.lib2test/files/files/4/4c/4c9/4c9d/4c9d1/4c9d1afd71269a5e08791f963938e5f3.jpg",
            "image_url": "https://dairy-demo.intrtl.com/api/photo_raw/2023/01/17/e4ef7672014924-def3dccc/2023-01-17-15-44-47-4064-o.jpg",
            "task_id": "67"
        },
        "e4ef7672014924-def3dccc-PHOTO-000004": {
            "error": {
                "code": "RESULT_OK",
                "codeInt": 1,
                "message": "Успешно обработан"
            },
            "products": [
                {
                    "product_id": "6156f4da52105-4578",
                    "facing": 1,
                    "facing_group": 0,
                    "price": 59.9,
                    "price_type": 1,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Fruttis Продукт йогуртный 8% Вишн. плом/Груша-Ваниль, формованный стакан, .115"
                },
                {
                    "product_id": "6156f545c20df-5885",
                    "facing": 1,
                    "facing_group": 1,
                    "price": 28.99,
                    "price_type": 0,
                    "category_id": "5e5236ee77ac4-7319",
                    "name": "Fruttis Продукт йогуртный 8% Абрикос-Манго/Лес. ягоды, формованный стакан, .115"
                }
            ],
            "scene_type": "Холодная полка ",
            "scene_id": "e4ef7672014924-def3dccc-SCENE-000002",
            "image_path": "/data/user/0/com.intrtl.lib2test/files/files/7/76/767/767c/767c3/767c32d09dc3617bb1c049378ccdb7b7.jpg",
            "image_url": "https://dairy-demo.intrtl.com/api/photo_raw/2023/01/17/e4ef7672014924-def3dccc/2023-01-17-16-37-46-9543-o.jpg",
            "task_id": "67"
        },
        "e4ef7672014924-def3dccc-PHOTO-000005": {
            "error": {
                "code": "RESULT_OK",
                "codeInt": 1,
                "message": "Успешно обработан"
            },
            "products": [],
            "scene_type": "Холодная полка ",
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
            "name": "Actimel Вишня-черешня-имбирь упак, картонная коробка (десерты), .600",
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
            "name": "Actimel Вишня-черешня-имбирь, Бутылка, .100",
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
                    "name": "Actimel Вишня-черешня-имбирь упак, картонная коробка (десерты), .600",
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
                    "name": "Actimel Вишня-черешня-имбирь, Бутылка, .100",
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
                        "name": "В магазине есть зона КСО?",
                        "answers": [
                            {
                                "index": 1,
                                "name": "Да",
                                "point": 0
                            }
                        ]
                    },
                    {
                        "index": 4,
                        "type": "select",
                        "name": "В магазине есть зона КСО?",
                        "answers": [
                            {
                                "index": 2,
                                "name": "Нет",
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
                "name": "Для регресса",
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
