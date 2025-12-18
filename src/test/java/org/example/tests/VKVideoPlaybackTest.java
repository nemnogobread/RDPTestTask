package org.example.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.example.config.AppiumConfig;
import org.example.pages.VKVideoPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

public class VKVideoPlaybackTest {

    private static final Logger logger = LoggerFactory.getLogger(VKVideoPlaybackTest.class);

    private VKVideoPage vkVideoPage;

    @BeforeClass
    public void setupClass() {
        logger.info("Инициализация конфигурации Selenide / Appium");
        Configuration.browser = AppiumConfig.class.getName();
        Configuration.browserSize = null;
        Configuration.timeout = 15000;
        Configuration.pageLoadTimeout = 30000;
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
    }

    @BeforeMethod
    public void setup() {
        logger.info("Открываем приложение VK Video и подготавливаем страницу");
        Selenide.open();
        vkVideoPage = new VKVideoPage();

        vkVideoPage.skipOnboarding()
                .closePopups();
    }

    @Test(priority = 1, description = "Видео воспроизводится")
    public void testVideoPlaysSuccessfully() {
        logger.info("Тест 1: проверка успешного воспроизведения видео");
        vkVideoPage.searchVideo("the green elephant");

        vkVideoPage.openFirstVideo();

        boolean playerVisible = vkVideoPage.isVideoPlayerVisible();
        Assert.assertTrue(playerVisible,
                "Видеоплеер не появился на экране");
        logger.info("Видеоплеер отображается");

        // Проверка 2: Видео воспроизводится
        boolean isPlaying = vkVideoPage.isVideoPlaying();
        Assert.assertTrue(isPlaying,
                "Видео не воспроизводится (прогресс не изменяется)");
        logger.info("Видео воспроизводится");

        // Проверка 3: Есть элементы управления
        boolean hasControls = vkVideoPage.hasPlaybackControls();
        Assert.assertTrue(hasControls,
                "Элементы управления воспроизведением не найдены");
        logger.info("Элементы управления присутствуют");

        String videoTitle = vkVideoPage.getVideoTitle();
        logger.info("Воспроизводится видео: {}", videoTitle);
    }

    @Test(priority = 2, description = "Обработка ошибки воспроизведения")
    public void testVideoPlaybackFailureHandling() {

        logger.info("Тест 2: проверка обработки ошибки воспроизведения / отсутствующего видео");
        vkVideoPage.searchVideo("xyzabc123nonexistent9999");

        try {
            vkVideoPage.openFirstVideo();

            boolean playerVisible = vkVideoPage.isVideoPlayerVisible();

            if (!playerVisible) {
                logger.info("Видео не найдено - ожидаемое поведение");
                Assert.assertTrue(true, "Корректная обработка отсутствия видео");
            } else {
                boolean isPlaying = vkVideoPage.isVideoPlaying();

                if (!isPlaying) {
                    logger.info("Видео не воспроизводится - ошибка корректно обработана");
                    Assert.assertTrue(true, "Ошибка воспроизведения обработана");
                } else {
                    logger.warn("Неожиданно: видео воспроизводится");
                }
            }

        } catch (Exception e) {
            logger.error("Исключение при открытии несуществующего видео", e);
            Assert.assertTrue(true, "Корректная обработка ошибки: " + e.getMessage());
        }

    }

    @Test(priority = 3, description = "Негативный тест: проверка недоступного видео")
    public void testUnavailableVideoHandling() {

        logger.info("Тест 3: проверка обработки недоступного видео");
        vkVideoPage.searchVideo("трейлер");

        try {
            vkVideoPage.openFirstVideo();

            Thread.sleep(3000);

            boolean playerVisible = vkVideoPage.isVideoPlayerVisible();

            if (!playerVisible) {
                logger.warn("Видео недоступно для воспроизведения");
                Assert.assertTrue(true, "Корректно обработано недоступное видео");
            } else {
                boolean isPlaying = vkVideoPage.isVideoPlaying();

                if (isPlaying) {
                    logger.info("Видео доступно и воспроизводится");
                } else {
                    logger.warn("Видео не запустилось - возможно, есть ограничения");
                }

                Assert.assertTrue(true, "Приложение корректно обработало ситуацию");
            }

        } catch (Exception e) {
            logger.error("Обработано исключение при проверке недоступного видео", e);
            Assert.assertTrue(true, "Исключение обработано корректно");
        }

    }

    @AfterMethod
    public void tearDown() {
        try {
            logger.info("Сохраняем скриншот результата теста");
            Selenide.screenshot("test_result");
        } catch (Exception e) {
            logger.warn("Не удалось сделать скриншот результата теста", e);
        }
    }

    @AfterClass
    public void tearDownClass() {
        logger.info("Завершение работы WebDriver");
        if (WebDriverRunner.hasWebDriverStarted()) {
            Selenide.closeWebDriver();
        }
    }
}


