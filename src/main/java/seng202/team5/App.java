package seng202.team5;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import seng202.team5.gui.SetupWindow;

import java.io.IOException;

/**
 * Default entry point class
 */
public class App {
    public static final Logger log = LogManager.getLogger(App.class);

    /**
     * Entry point which runs the javaFX application
     * Also shows off some different logging levels
     * @param args program arguments from command line
     */
    static void main(String[] args) throws IOException {
        log.info("App is loading");

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        SetupWindow.main(args);
        }
}

