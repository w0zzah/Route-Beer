package seng202.team5.gui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Objects;
import java.util.Optional;

/**
 * Helpers for the standard alerts used across the GUI.
 */
public final class DialogUtil {

    /** Style class that scopes the dialog styling in the main stylesheet. */
    private static final String DIALOG_CSS_CLASS = "app-dialog";

    /** Path to the main stylesheet. Change this to match your project. */
    private static final String STYLESHEET = "/css/style.css";

    private DialogUtil() {
        /* This utility class should not be instantiated */
    }


    /**
     * Shows a confirmation dialog and waits.
     * @param title window title
     * @param header header text
     * @param content body text
     * @return true if the user pressed OK
     */
    public static boolean confirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        applyStyle(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows an information dialog and waits.
     * @param title window title
     * @param content body text
     */
    public static void info(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        applyStyle(alert);
        alert.showAndWait();
    }

    /**
     * Shows an error dialog and waits.
     *
     * @param owner owning window (may be null)
     * @param title window title
     * @param header header text
     * @param content body text
     */
    public static void error(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        applyStyle(alert);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    /**
     * Applies the app stylesheet and the scoping style class to an alert.
     * @param alert alert to style
     */
    private static void applyStyle(Alert alert) {
        alert.getDialogPane().getStylesheets()
                .add(Objects.requireNonNull(DialogUtil.class.getResource(STYLESHEET)).toExternalForm());
        alert.getDialogPane().getStyleClass().add(DIALOG_CSS_CLASS);
        alert.setGraphic(null);
    }
}