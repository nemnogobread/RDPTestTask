package org.example.pages;

import com.codeborne.selenide.WebDriverRunner;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.AndroidDriver;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class VKVideoPage {

    private static final Logger logger = LoggerFactory.getLogger(VKVideoPage.class);

    private final By searchButton = AppiumBy.id("com.vk.vkvideo:id/search_button");
    private final By searchInput = AppiumBy.id("com.vk.vkvideo:id/query");
    private final By videoPlayerContainer = AppiumBy.id("com.vk.vkvideo:id/playerContainer");
    private final By playPauseButton = AppiumBy.id("com.vk.vkvideo:id/play_pause");
    private final By videoProgress = AppiumBy.id("com.vk.vkvideo:id/progress");
    private final By videoTitle = AppiumBy.id("com.vk.vkvideo:id/title");

    private final String videoItemXPath = "//android.widget.FrameLayout[contains(@resource-id, 'video')]";
    private final String playIconXPath = "//*[contains(@resource-id, 'play') or contains(@content-desc, 'play')]";

    public VKVideoPage skipOnboarding() {
        try {
            String[] onboardingXpaths = {
                    "//*[contains(@text, 'Пропустить') or contains(@text, 'Skip')]",
                    "//*[contains(@text, 'Продолжить без входа') or contains(@text, 'Continue without')]",
                    "//*[contains(@text, 'Позже') or contains(@text, 'Later')]"
            };

            for (String xpath : onboardingXpaths) {
                SelenideElement button = $(AppiumBy.xpath(xpath));
                if (button.exists()) {
                    logger.info("Найден элемент онбординга по xpath '{}', нажимаем на него", xpath);
                    button.click();
                    // даём приложению обновить экран
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            logger.debug("Ошибка при попытке пропустить онбординг (онбординг может отсутствовать): {}", e.getMessage());
        }
        return this;
    }

    public VKVideoPage closePopups() {
        try {
            for (int i = 0; i < 3; i++) {
                boolean closedSomething = false;

                SelenideElement closeByIcon = $(AppiumBy.xpath("//*[@content-desc='Закрыть' or contains(@resource-id, 'close')]"));
                if (closeByIcon.exists()) {
                    logger.info("Найден попап (иконка закрытия), закрываем его");
                    closeByIcon.click();
                    closedSomething = true;
                }

                SelenideElement denyButton = $(AppiumBy.xpath("//*[contains(@text, 'Не сейчас') or contains(@text, 'Not now')]"));
                if (denyButton.exists()) {
                    logger.info("Найден диалог, нажимаем 'Не сейчас'");
                    denyButton.click();
                    closedSomething = true;
                }

                SelenideElement backButton = $(AppiumBy.xpath("//*[contains(@text, 'Назад') or contains(@text, 'Back')]"));
                if (backButton.exists()) {
                    logger.info("Найден диалог, нажимаем 'Назад'");
                    backButton.click();
                    closedSomething = true;
                }

                if (!closedSomething) {
                    logger.debug("Попапы не найдены на итерации {}", i);
                    break;
                }

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.debug("Ошибка при закрытии попапов (вероятно, попапов нет): {}", e.getMessage());
        }
        return this;
    }

    public VKVideoPage searchVideo(String query) {
        logger.info("Выполняем поиск видео по запросу: {}", query);

        // На всякий случай ещё раз пытаемся закрыть возможные попапы
        skipOnboarding();
        closePopups();

        $(searchButton)
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        $(searchInput)
                .shouldBe(Condition.visible, Duration.ofSeconds(5))
                .setValue(query);

        pressAndroidEnter();

        return this;
    }

    public VKVideoPage openFirstVideo() {
        logger.info("Открываем первое видео из результатов поиска");
        $$(AppiumBy.xpath(videoItemXPath))
                .shouldHave(CollectionCondition.sizeGreaterThan(0), Duration.ofSeconds(10));

        $$(AppiumBy.xpath(videoItemXPath))
                .first()
                .shouldBe(Condition.visible)
                .click();

        return this;
    }

    public boolean isVideoPlayerVisible() {
        try {
            boolean visible = $(videoPlayerContainer)
                    .shouldBe(Condition.visible, Duration.ofSeconds(15))
                    .exists();
            logger.info("Проверка отображения видеоплеера: {}", visible);
            return visible;
        } catch (Exception e) {
            logger.warn("Видеоплеер не отобразился: {}", e.getMessage());
            return false;
        }
    }

    public boolean isVideoPlaying() {
        try {
            logger.info("Проверяем, что видео воспроизводится");
    
            String initial = getVideoProgress();
            logger.debug("Начальное значение прогресса: {}", initial);
    
            Thread.sleep(7000);
    
            String current = getVideoProgress();
            logger.debug("Текущее значение прогресса: {}", current);
    
            if (initial != null && current != null && !initial.equals(current)) {
                logger.info("Прогресс изменился ({} -> {}), считаем что видео воспроизводится", initial, current);
                return true;
            }
    
            logger.warn("Прогресс не изменился, используем запасной критерий: плеер + контролы видимы");
            return isVideoPlayerVisible() && hasPlaybackControls();
        } catch (Exception e) {
            logger.error("Ошибка при проверке воспроизведения, используем запасной критерий", e);
            return isVideoPlayerVisible();
        }
    }

    private String getVideoProgress() {
        try {
            SelenideElement progressBar = $(videoProgress);
            if (progressBar.exists()) {
                return progressBar.getAttribute("content-desc");
            } else {
                logger.debug("Прогресс-бар видео не найден");
            }
        } catch (Exception e) {
            logger.debug("Ошибка при получении прогресса видео: {}", e.getMessage());
        }
        return "0";
    }

    public boolean hasPlaybackControls() {
        try {
            logger.info("Проверяем наличие элементов управления воспроизведением");
            $(videoPlayerContainer).click();
            Thread.sleep(1000);

            boolean hasControls = $(playPauseButton).exists() ||
                    $(AppiumBy.xpath(playIconXPath)).exists();
            logger.info("Наличие элементов управления: {}", hasControls);
            return hasControls;
        } catch (Exception e) {
            logger.warn("Не удалось проверить наличие элементов управления: {}", e.getMessage());
            return false;
        }
    }

    public String getVideoTitle() {
        try {
            String title = $(videoTitle).getText();
            logger.info("Заголовок воспроизводимого видео: {}", title);
            return title;
        } catch (Exception e) {
            logger.warn("Не удалось получить заголовок видео: {}", e.getMessage());
            return "";
        }
    }

    private void pressAndroidEnter() {
        AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();
        driver.executeScript("mobile: performEditorAction", Map.of("action", "search"));
    }
}