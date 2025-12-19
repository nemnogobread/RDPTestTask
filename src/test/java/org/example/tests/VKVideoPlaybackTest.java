package org.example.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.appium.java_client.android.AndroidDriver;
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
        
        try {
            if (WebDriverRunner.hasWebDriverStarted()) {
                AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();
                driver.activateApp("com.vk.vkvideo");
                logger.info("Приложение VK Video активировано");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.warn("Не удалось активировать приложение VK Video, продолжаем: {}", e.getMessage());
        }
        
        vkVideoPage = new VKVideoPage();

        vkVideoPage.skipOnboarding()
                .closePopups();
    }

    @Test(priority = 1, description = "Видео воспроизводится")
    public void testVideoPlaysSuccessfully() {
        logger.info("Тест 1: проверка успешного воспроизведения видео");
        vkVideoPage.searchVideo("the green elephant");

        try {
            vkVideoPage.openFirstVideo();

            Thread.sleep(3000);

            boolean playerVisible = vkVideoPage.isVideoPlayerVisible();
            
            if (!playerVisible) {
                logger.warn("Видеоплеер не появился на экране");
                Assert.fail("Видеоплеер не появился на экране");
            } else {
                logger.info("Видеоплеер отображается");
                
                boolean isPlaying = vkVideoPage.isVideoPlaying();
                
                if (!isPlaying) {
                    logger.warn("Видео не воспроизводится");
                    Assert.fail("Видео не воспроизводится (прогресс не изменяется)");
                } else {
                    logger.info("Видео воспроизводится");
                }
                
                boolean hasControls = vkVideoPage.hasPlaybackControls();
                if (!hasControls) {
                    logger.warn("Элементы управления воспроизведением не найдены");
                    Assert.fail("Элементы управления воспроизведением не найдены");
                } else {
                    logger.info("Элементы управления присутствуют");
                }
                
                String videoTitle = vkVideoPage.getVideoTitle();
                logger.info("Воспроизводится видео: {}", videoTitle);
            }

        } catch (Exception e) {
            logger.error("Исключение при проверке воспроизведения видео", e);
            Assert.fail("Ошибка при проверке воспроизведения видео: " + e.getMessage());
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
        
        try {
            logger.info("Закрываем приложение VK Video");
            if (WebDriverRunner.hasWebDriverStarted()) {
                AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();
                driver.terminateApp("com.vk.vkvideo");
                logger.info("Приложение VK Video успешно закрыто");
                
                Thread.sleep(3000);
                logger.debug("Пауза после закрытия приложения завершена");
            }
        } catch (Exception e) {
            logger.warn("Не удалось закрыть приложение VK Video", e);
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


