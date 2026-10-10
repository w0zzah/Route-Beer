package seng202.team5.e2e;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import seng202.team5.gui.tab_pane_controllers.ExportWindowController;


import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExportRouteTest extends FXTestBase {

    @TempDir
    Path tempDirectory;

    @Start
    public void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    void setup(FxRobot robot) {
        robot.interact(() -> screenNavigator.goToMain(testUser));
        robot.clickOn("#startBarField");
        robot.write("Fat");
        robot.sleep(200);
        robot.clickOn("Fat Eddie's");
        robot.clickOn("#generateRouteButton");
    }

    @Test
    void exportRouteCancel(FxRobot robot) {
        robot.clickOn("#exportRouteButton");
        robot.sleep(2000);
        Window exportWindow = robot.window("Export Route");
        robot.targetWindow(exportWindow);
        robot.clickOn("#cancelExportButton");
        robot.sleep(200);
        assertFalse(exportWindow.isShowing());
        assertEquals("viewTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
    }

    @Test
    void exportRouteConfirm(FxRobot robot) {
        robot.clickOn("#exportRouteButton");
        robot.sleep(2000);
        Window exportWindow = robot.window("Export Route");
        File targetFile = tempDirectory.resolve("testRoute.png").toFile();
        ((ExportWindowController) exportWindow.getScene().getRoot().getProperties().get("controller")).setFileSelector(() -> targetFile); //make the file selector return the target file
        robot.targetWindow(exportWindow);
        robot.clickOn("#confirmExportButton");
        robot.sleep(200);

        assertTrue(targetFile.exists());
        assertTrue(targetFile.length() > 0);
        try {
            assertNotNull(ImageIO.read(targetFile));
        } catch (IOException _) {
            // shouldn't throw this error as we asserted earlier the file exists
        }
        DialogPane dialogPane = (DialogPane) robot.window(w -> w.isShowing()
                && w.getScene() != null
                && w.getScene().getRoot() instanceof DialogPane).getScene().getRoot();
        assertNotNull(dialogPane);
        robot.interact(
                ((Button) dialogPane.lookupButton(ButtonType.OK))::fire
        );
        assertFalse(exportWindow.isShowing());
        assertEquals("viewTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
    }

}
