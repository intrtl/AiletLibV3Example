# 1. Integrate Ailet library into project

- [1. Integrate Ailet library into project](#1-integrate-ailet-library-into-project)
  - [1.1. Подключение используя Maven (GitHub)](#11-подключение-используя-maven-github)
    - [1.1.1. Создайте GitHub personal access token](#111-создайте-github-personal-access-token)
    - [1.1.2. Добавьте в проект репозиторий Ailet](#112-добавьте-в-проект-репозиторий-ailet)
    - [1.1.3. Добавьте в build.gradle модуля две зависимости:](#113-добавьте-в-buildgradle-модуля-две-зависимости)
  - [1.2. Использование](#12-использование)
    - [1.2.1. Инициализация](#121-инициализация)
    - [1.2.2. Использование](#122-использование)
  - [1.4 Методы](#14-методы)
    - [1.4.1 Список доступных серверов. Метод getServers()](#141-список-доступных-серверов-метод-getservers)
    - [1.4.2 Инициализация библиотеки. Метод init()](#142-инициализация-библиотеки-метод-init)
    - [1.4.3 Начало визита. Метод start()](#143-начало-визита-метод-start)
    - [1.4.4 Получение отчета по визиту. Метод getReports()](#144-получение-отчета-по-визиту-метод-getreports)
    - [1.4.5 Отображение сводного отчета по визиту. Метод showSummaryReport()](#145-отображение-сводного-отчета-по-визиту-метод-showsummaryreport)
    - [1.4.6 Выбор активного портала. Метод setPortal()](#146-выбор-активного-портала-метод-setportal)
  - [1.5 Примеры отчетов](#15-примеры-отчетов)

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

```groovy
def ailetLibVersion = '3.7.1'
// библиотека Ailet
implementation "com.ailet.android:lib:$ailetLibVersion"
// необязательно: модуль техподдержки
implementation "com.ailet.android:lib-feature-techsupport-intercom:$ailetLibVersion"
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

### 1.2.2. Использование
После инициализации вам становится доступен единый клиент библиотеки ``AiletClient``, который вы можете использовать для вызова ее методов:
```kotlin
Ailet.getClient()
```

## 1.4 Методы

Начиная с версии 3.0 класс-клиент библиотеки ``IntRtl`` отмечен как устаревший. 
Вместо него необходимо использовать экземпляр ``AiletClient``.
Методы нового клиента концептуально соответствуют
[методам устаревшего клиента](https://github.com/intrtl/AiletLibraryExamples/blob/master/Android/IrLibExample/readme.md#методы):

Метод  | Описание
--- | ---
[init](#метод-init) | Инициализация библиотеки, авторизация пользователя и загрузка справочников.
[start](#метод-start) | Старт визита.
[getReports](#метод-reports) | Возвращает отчет по указанному визиту.
[showSummaryReport](#метод-showsummaryreport) | Сводный отчет по указанному визиту.
[setPortal](#метод-setportal) | Установка активного портала.

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

### 1.4.1 Список доступных серверов. Метод getServers()

Метод отдаем список серверов (AiletServer), которые можно использовать в методы init. Метод необзательный и необходим только для мультипортального режима.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:---------:|:-----------------:
login           |String      | Логин пользователя в системе Ailet.      | + | 
password        |String      | Пароль пользователя в системе Ailet.     | + | 
externalUserId  |String      | Внешний идентификатор пользователя (ID пользователя из внешней системы). | | null 

### 1.4.2 Инициализация библиотеки. Метод init()

Данный метод отвечает за авторизацию пользователя в библиотеке, а также за инициализацию самой библиотеки Ailet Lib и загрузку справочников, необходимых для работы модуля.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:---------:|:-----------------:
login           |String      | Логин пользователя в системе Ailet.      | + | 
password        |String      | Пароль пользователя в системе Ailet.     | + | 
externalUserId  |String      | Внешний идентификатор пользователя (ID пользователя из внешней системы). | | null 
multiPortalMode |Boolean     | Поддержка мультипортальности.            | | true 
server          |AiletServer | Сервер, на который выполняется вход.     | | null 
isNeedSyncCatalogs|Boolean | Необходимость синхронизации каталогов.     | | true


### 1.4.3 Начало визита. Метод start()

Метод запускает съемку в рамках визита.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:-:|:-:
storeId         |AiletMethodStart.StoreId      | Внешний идентификатор торговой точки.        | + | 
externalVisitId |String      | Внешний идентификатор визита.     |  | null
sceneGroupId    |Int         | Идентификатор группы сцен.            | | null 
taskId       |String      | Внешний идентификатор задачи.         | | null 
visitType       |String      | Тип визита (before, after).         | | null 

### 1.4.4 Получение отчета по визиту. Метод getReports()

Метод возвращает отчет по визиту в формате `json` ([Пример](#15-пример-отчета))

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:-:|:-:
externalVisitId | String | Внешний идентификатор визита. | +  
taskId       |String      | Внешний идентификатор задачи.         | | null 
visitType       |String      | Тип визита (before, after).         | | null 

### 1.4.5 Отображение сводного отчета по визиту. Метод showSummaryReport()

Метод открывает экран просмотра сводного отчета по визиту.

Параметр | Тип | Описание | Обязательный | По умолчанию
---------|-----|----------|:-:|:-:
externalVisitId | String | Внешний идентификатор визита. | +  
taskId       |String      | Внешний идентификатор задачи.         | | null 
visitType       |String      | Тип визита (before, after).         | | null 

### 1.4.6 Выбор активного портала. Метод setPortal()

Метод используется для установки текущего портала в мультипортальном режиме.

Параметр | Тип | Описание | Обязательный 
---------|-----|----------|:-:
portalName | String | Идентификатор портала | + 


## 1.5 Примеры отчетов

[Пример отчета, возвращаемого методами](method_result.json)

[Пример бродкаст отчета](broadcast_result.json)