# 1. Интеграция через интенты

- [1. Интеграция через интенты](#1-интеграция-через-интенты)
  - [1.1. Вызов метода (на примере метода visit)](#11-вызов-метода-на-примере-метода-visit)
  - [1.2 Методы](#12-методы)
    - [1.2.1 Метод Visit](#121-метод-visit)
    - [1.2.2 Метод Report](#122-метод-report)
    - [1.2.3 Метод Summary Report](#123-метод-summary-report)
    - [1.2.4 Метод Sync](#124-метод-sync)
  - [1.3 Примеры отчетов](#13-примеры-отчетов)

## 1.1. Вызов метода (на примере метода visit)

Action задается в формате ```com.ailet.app.[метод]```

```kotlin
private fun visit() {
    Intent().apply {
        action = "com.ailet.app.ACTION_VISIT"
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

    if (resultCode == RESULT_OK) {
            
            intent?.data?.let { uri ->
                val result = readFromUri(uri)                
                try {
                    val json = JSONObject(result)
                    
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
    }

```

```Intent``` в ```onActivityResult``` содержит следующие ```extras```:


Параметр | Тип | Описание 
---------|-----|----------
action | String | Метод
error | String | Тип ошибки (если была ошибка)
message | String | Тест ошибки (если была ошибка)

А так же ```data``` содержит ```uri``` файла отчета ([пример отчета](method_result.json))


**Типы ошибок**

Тип | Описание 
---------|-----
ERROR | Метод
AUTH | Тип ошибки (если была ошибка)
INCORRECT_INPUT | Тест ошибки (если была ошибка)

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

## 1.3 Примеры отчетов

[Пример отчета, возвращаемого методами](method_result.json)

[Пример бродкаст отчета](broadcast_result.json)