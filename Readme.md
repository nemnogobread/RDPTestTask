## Инструкция по запуску автотестов

### Предварительные требования
- **Установлен Java JDK**: не ниже версии, чем 22.
- **Установлен Maven**: добавлен в `PATH` (`mvn -v` должен отрабатывать из консоли).
- **Запущен Appium-сервер**: по адресу `http://127.0.0.1:4723` (как указано в `AppiumConfig`), по типу:
```bash
PS C:\Users\Глеб> appium -p 4723
[Appium] Welcome to Appium v3.1.2
[Appium] The autodetected Appium home path: C:\Users\Глеб\.appium
[Appium] Attempting to load driver uiautomator2...
[Appium] Requiring driver at C:\Users\Глеб\.appium\node_modules\appium-uiautomator2-driver\build\index.js
[Appium] AndroidUiautomator2Driver has been successfully loaded in 8.420s
[Appium] Appium REST http interface listener started on http://0.0.0.0:4723
[Appium] You can provide the following URLs in your client code to connect to this server:
        http://192.168.1.108:4723/
        http://127.0.0.1:4723/ (only accessible from the same host)
[Appium] Available drivers:
[Appium]   - uiautomator2@6.7.3 (automationName 'UiAutomator2')
[Appium] No plugins have been installed. Use the "appium plugin" command to install the one(s) you want to use.
```
- **Доступен Android-эмулятор/устройство**: с параметрами, прописанными в `AppiumConfig` (`PLATFORM_NAME`, `PLATFORM_VERSION`, `DEVICE_NAME`). 

### Как запустить тесты
- **Из корня проекта через Maven (все тесты TestNG):**

```bash
mvn clean test
```

- **Запуск конкретного TestNG suite (`testng.xml` в корне проекта):**

```bash
mvn clean test -Dsurefire.suiteXmlFiles=testng.xml
```

### Отчёты о запуске
- **HTML- и XML-отчёты TestNG** сохраняются в папке `target/surefire-reports/`.
- Для удобного просмотра можно открыть файл:
  - `target/surefire-reports/index.html` или
  - HTML-отчёты внутри папки `target/surefire-reports/VK Video Test Suite/`.


## С чем я столкнулся

### Образы эмулятора
Есть вероятность что не все образы в android studio могут корректно подняться, по крайней мере у меня долгое время была ошибка.
```bash
org.openqa.selenium.SessionNotCreatedException:
Could not start a new session. Response code 500. Message: Error getting device platform version. Original error: Error executing adbExec. Original error: 'Command ''L:\AndroidSDK\platform-tools\adb.exe' -P 5037 -s emulator-5554 shell getprop ro.build.version.release' exited with code 1'; Command output: adb.exe: device unauthorized.
This adb server's $ADB_VENDOR_KEYS is not set
Try 'adb kill-server' if that seems wrong.
Otherwise check for a confirmation dialog on your device.
```
Её удалось исправть на `API 30 "R"; Android 11` установив именно `Google APIs` образ. Правда подгружать vk.video пришлось прямым монтированием apk

### Кнопка `search` в vk.video

Предпологалось, что её имя `com.vk.vkvideo:id/search`, однако тесты падали с указанием не то, что такого элемента не находили:
```bash
Caused by: org.openqa.selenium.NoSuchElementException: An element could not be located on the page using the given search parameters.
For documentation on this error, please visit: https://www.selenium.dev/documentation/webdriver/troubleshooting/errors#nosuchelementexception
Build info: version: '4.39.0', revision: '126f156aee'
System info: os.name: 'Windows 10', os.arch: 'amd64', os.version: '10.0', java.version: '22.0.2'
Driver info: io.appium.java_client.android.AndroidDriver
Command: [3a622ad5-0070-4c1d-b327-836dfcc2c5f6, findElement {using=id, value=com.vk.vkvideo:id/search}]
```

По итогу пришлось устанавливать ещё Appium Inspector, подключаться через него к эмулятору и смотреть какой resource-id у кнопки поиска. Оказалось, что `com.vk.vkvideo:id/search_button`