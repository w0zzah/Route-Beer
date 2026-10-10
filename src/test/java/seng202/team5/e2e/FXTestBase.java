package seng202.team5.e2e;

import javafx.stage.Stage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import seng202.team5.gui.ScreenNavigator;
import seng202.team5.models.User;
import seng202.team5.services.MainManager;

import static seng202.team5.models.types.UserType.MEMBER;

@ExtendWith(ApplicationExtension.class)
abstract class FXTestBase {

    protected ScreenNavigator screenNavigator;
    protected MainManager mainManager;
    protected Stage stage;

    protected User testUser;

    protected void startSetup(Stage stage) {
        mainManager = new MainManager();
        screenNavigator = new ScreenNavigator(stage, mainManager);
        testUser = new User(277, "TEST1", MEMBER);
        this.stage = stage;
        stage.setAlwaysOnTop(true);
        stage.show();
        stage.toFront();
        stage.requestFocus();
    }
}