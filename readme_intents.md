# 1. Интеграция через интенты

- [1. Интеграция через интенты](#1-интеграция-через-интенты)
  - [1.1. Вызов метода (на примере метода visit)](#11-вызов-метода-на-примере-метода-visit)
  - [1.2 Методы](#12-методы)
    - [1.2.1 Метод Visit](#121-метод-visit)
    - [1.2.2 Метод Report](#122-метод-report)
      - [Для версии 4.19 Offline и выше](#для-версии-419-offline-и-выше)
        - [Ошибки](#ошибки)
    - [1.2.3 Метод Summary Report](#123-метод-summary-report)
    - [1.2.4 Метод Sync](#124-метод-sync)
      - [Для версии 4.19 Offline и выше](#для-версии-419-offline-и-выше-1)
        - [Параметры](#параметры)
        - [Ошибки](#ошибки-1)
  - [1.3 Широковещательное (broadcast) сообщение](#13-широковещательное-broadcast-сообщение)
  - [1.4 Примеры отчетов](#14-примеры-отчетов)
    - [Для версии 4.19 Offline и выше](#для-версии-419-offline-и-выше-2)

## 1.1. Вызов метода (на примере метода visit)

Action задается в формате ```com.ailet.[метод]```

```kotlin
private fun visit() {
    Intent().apply {
        action = "com.ailet.ACTION_VISIT"
        flags = 0
        putExtra("login", "логин")
        putExtra("password", "пароль")
        putExtra("id", "ИД пользователя")
        putExtra("visit_id", "ИД визита")
        putExtra("store_id", "ИД ТТ")
        
        startActivityForResult(this, VISIT_RESULT)
    }
}

override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    super.onActivityResult(requestCode, resultCode, intent)
        
    when (resultCode) {
        RESULT_OK -> {
            intent?.data?.let { uri ->
                val result = readFromUri(uri)
                try {
                    val json = JSONObject(result)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
         }

        else -> {
            intent?.extras?.get("error")?.let { log("error: $it") }
            intent?.extras?.get("message")?.let { log("error message: $it") }
        }
    }
}

```
```Intent.extras``` содержит следующие параметры:

Параметр | Тип | Описание 
---------|-----|----------
action | String | Метод
error | String | Тип ошибки (если была ошибка)
message | String | Тест ошибки (если была ошибка)

```Intent.data``` содержит ```uri``` файла отчета ([пример отчета](method_result.json)) для методов ```visit, report, summaryReport```.


**Типы ошибок**

Тип | Описание 
---------|-----
ERROR | Ошибка при выполнении метода
AUTH | Ошибка авторизации
INCORRECT_INPUT | Некорректные входные данные
CATALOGS_SYNCHRONIZATION | Выполняется синхронизация справочников (добавлено в 4.11)
FAILED_SYNC | Ошибка при синхронизации справочников (добавлено в 4.11)

## 1.2 Методы

Метод  | Описание
--- | ---
[ACTION_VISIT](#121-метод-visit) | Старт/редактирование визита 
[ACTION_REPORT](#122-метод-report) | Получение отчета по визиту
[ACTION_SUMMARY_REPORT ](#123-метод-summary-report) | Сводный отчет по указанному визиту
[ACTION_SYNC](#124-метод-sync) | Запуск синхронизации

### 1.2.1 Метод Visit

Метод запускает съемку в рамках визита.

Параметр | Тип | Описание | Обязательный 
---------|-----|----------|:-:
login           |String      | Логин пользователя в системе Ailet      | + | 
password        |String      | Пароль пользователя в системе Ailet     | + | 
id  |String      | Идентификатор пользователя 
visit_id         |String      | Идентификатор Визита        | + | 
store_id         |String      | Идентификатор торговой точки        | + | 
task_id       |String      | Внешний идентификатор задачи         | |  

### 1.2.2 Метод Report

Метод возвращает отчет по визиту ([пример отчета](method_result.json)).

Параметр | Тип | Описание | Обязательный 
---------|-----|----------|:-:
login           |String      | Логин пользователя в системе Ailet      | + | 
password        |String      | Пароль пользователя в системе Ailet     | + | 
id  |String      | Идентификатор пользователя 
visit_id         |String      | Идентификатор Визита        | + | 
store_id         |String      | Идентификатор торговой точки        | + | 
task_id       |String      | Внешний идентификатор задачи         | |  

#### Для версии 4.19 Offline и выше
Для версии с поддержкой Palomna в метод `ACTION_REPORT` добавлен новый тип ошибоки.

##### Ошибки
Тип | Описание 
---------|-----
ONDEVICE_DATA_NOT_LOADED | Модели или классы не загружены

### 1.2.3 Метод Summary Report

Открывает окно сводного отчета по визиту.

Параметр | Тип | Описание | Обязательный 
---------|-----|----------|:-:
login           |String      | Логин пользователя в системе Ailet      | + | 
password        |String      | Пароль пользователя в системе Ailet     | + | 
id  |String      | Идентификатор пользователя 
visit_id         |String      | Идентификатор Визита        | + | 
store_id         |String      | Идентификатор торговой точки        | + | 
task_id       |String      | Внешний идентификатор задачи         | |  

### 1.2.4 Метод Sync

Метод запускает синхронизацию.

Параметр | Тип | Описание | Обязательный 
---------|-----|----------|:-:
login           |String      | Логин пользователя в системе Ailet     | + | 
password        |String      | Пароль пользователя в системе Ailet     | + | 
id  |String      | Идентификатор пользователя 

Метод возвращает только resultCode, если RESULT_OK - то есть данные для синхронизации и сервис синхронизации запустился, если иной, например RESULT_CANCELED - то нет данных для синхронизации.

#### Для версии 4.19 Offline и выше
Для версии с поддержкой Palomna в метод `ACTION_SYNC` добавлены новые параметры и типы ошибок.

##### Параметры 
Параметр | Тип | Описание | Обязательный 
---------|-----|----------|:-: 
store_ids  |[String]      | Идентификаторы торговых точек |
download_ondevice |Boolean      | Активация режима Palomna |

При вызове `ACTION_SYNC` с параметром `download_ondevice = true` и настройки со стороны портала будет активирована Palomna (распознование и создание отчетов на устройстве), при этом будет загружены модели и справочники, необходимые для работы. Если модели и справочники загружены, то будет проверено наличие обновлений. Если параметр `false`, то загрузки/обновления моделей и справочников производится не будет.

Параметр `store_ids` отвечает за загрузку матриц по торговым точкам, необходимых для работы Palomna.

Значение store_ids| Пример | Описание 
---|---|---
отсуствует | | будет загружен справочник торговых точек и матрице по первой 1000 точек из справочника
один идентификатор| ['store1']| будет загружен справочник торговых точек и матрицы по ближайшей 1000 точек к указанной 
несколько идентификаторов|['store1','store5','store999']| будут загруженый матрицы только по указанным точкам

##### Ошибки
Тип | Описание 
---------|-----
ONDEVICE_USER_CANCELED_WITH_MOBILE | Пользователь отменил загрузку на мобильной сети
ONDEVICE_DOWNLOAD_FAILED|Неуспешная загрузка (ошибки, потеря соединения)
ONDEVICE_USER_CANCELED_UPDATE|пользователь отменил обновление
ONDEVICE_NOT_AVAILABLE|Приложение (без Palomna), отсутствуют модели и классы в портальных настройках

## 1.3 Широковещательное (broadcast) сообщение 

При получении всех данных по визту приложение Ailet генерирует широковещательное сообщение с ```intent.action = com.ailet.app.BROADCAST_WIDGETS_RECEIVED``` (либо ```com.ailet.russia.BROADCAST_WIDGETS_RECEIVED```).

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
result           | String | Uri файла отчета ([пример отчета](broadcast_result.json))

## 1.4 Примеры отчетов

[Пример отчета, возвращаемого методами (кроме метода ACTION_SYNC)](method_result.json)

[Пример отчета, возврашаемого в широковещательном сообщении](broadcast_result.json)

### Для версии 4.19 Offline и выше

В отчеты (`result`, `report`.`result`) добавлены новые поля:

Поле | Описание
 ---|---
source | Источник данных:<br>`online` - если все фото в визите распознаны онлайн<br>`on-device` - если хотя бы одно фото из визита распознано on-device и не было обработано онлайн
completed_on_device | Количество, распознанных на устройстве, фотографий

*Пример части отчета с новыми полями*
 ```json
 {
    "result": {
        "source": "ondevice",
        "visit_id": "1",
        "total_photos": 1,
        "sended_photos": 1,
        "code": "RESULT_OK",
        "codeInt": 1,
        "message": "Успешно обработан"
    },
    "visit_stats": {
        "photo": {
            "badQuality": 0,
            "completed": 1,
            "completed_on_device": 1,
            "created": 0,
            "deleted": 0,
            "goodQuality": 1,
            "retake": 0,
            "sent": 0,
            "sentWithError": 0,
            "status": "RESULT_OK",
            "uncompressed": 1,
            "wait": 0
        }
    }
}
 ```