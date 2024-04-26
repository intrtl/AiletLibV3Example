# 1. Интеграция через интенты

- [1. Интеграция через интенты](#1-интеграция-через-интенты)
  - [1.1. Вызов метода (на примере метода visit)](#11-вызов-метода-на-примере-метода-visit)
  - [1.2 Методы](#12-методы)
    - [1.2.1 Метод Visit](#121-метод-visit)
    - [1.2.2 Метод Report](#122-метод-report)
    - [1.2.3 Метод Summary Report](#123-метод-summary-report)
    - [1.2.4 Метод Sync](#124-метод-sync)
  - [1.3 Широковещательное (broadcast) сообщение](#13-широковещательное-broadcast-сообщение)
  - [1.4 Примеры отчетов](#14-примеры-отчетов)

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