package seng202.team5.gui.util;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Shows and clears the red-border / red-text validation state on form fields.
 */
public final class FieldErrorUtil {

    public static final String ERROR_IN_FIELD_CSS_CLASS = "error-in-field";

    private FieldErrorUtil() {
    }

    /**
     * Puts a red border on the field and shows the message in the label.
     * @param field field to highlight
     * @param label label that displays the message
     * @param message message to show
     */
    public static void show(Node field, Label label, String message) {
        if (!field.getStyleClass().contains(ERROR_IN_FIELD_CSS_CLASS)) {
            field.getStyleClass().add(ERROR_IN_FIELD_CSS_CLASS);
        }
        label.setText(message);
    }

    /**
     * Clears the error state of a single field and its label.
     * @param field field to un-highlight
     * @param label label to empty
     */
    public static void clear(Node field, Label label) {
        field.getStyleClass().remove(ERROR_IN_FIELD_CSS_CLASS);
        label.setText("");
    }

    /**
     * Removes the red border from each field.
     * @param fields fields to un-highlight
     */
    public static void clearBorders(Node... fields) {
        for (Node field : fields) {
            field.getStyleClass().remove(ERROR_IN_FIELD_CSS_CLASS);
        }
    }

    /**
     * Empties each error label.
     * @param labels labels to empty
     */
    public static void clearTexts(Label... labels) {
        for (Label label : labels) {
            label.setText("");
        }
    }
}