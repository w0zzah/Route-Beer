package seng202.team5.e2e;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeoutException;


import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapControllerTest extends FXTestBase {

    WebEngine webEngine;

    @Start
    void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    void setUp(FxRobot robot) throws Exception {
        robot.interact(() -> screenNavigator.goToMain(testUser));
        webEngine = robot.lookup("#mapView").queryAs(WebView.class).getEngine();

        WaitForAsyncUtils.waitFor(20, SECONDS, () ->
                Boolean.TRUE.equals(WaitForAsyncUtils.asyncFx(() ->
                        webEngine.executeScript("document.querySelectorAll('.leaflet-marker-icon').length > 0")
                ).get()));
    }

    @AfterEach
    void tearDown() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    void testClickOnBar(FxRobot robot) {

        robot.interact(() -> webEngine.executeScript("document.querySelector('img.leaflet-marker-icon[title=\"Finnegans Irish Bar & Cafe\"]').click();"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Finnegans Irish Bar & Cafe", robot.lookup("#nameLabel").queryLabeled().getText());
    }

}
