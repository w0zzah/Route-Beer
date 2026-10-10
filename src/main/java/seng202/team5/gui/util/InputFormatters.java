package seng202.team5.gui.util;

import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * Reusable input restrictions for text fields and spinners.
 */
public final class InputFormatters {

    private InputFormatters() {
    }

    /**
     * Allows digits with at most one decimal place, e.g. "12" or "3.5".
     * @return a new formatter
     */
    public static TextFormatter<Double> decimalOneDp() {
        return new TextFormatter<>(
                new DoubleStringConverter(),
                null,
                c -> c.getControlNewText().matches("\\d*(\\.\\d?)?") ? c : null);
    }

    /**
     * Commits whatever is typed in an editable spinner when it loses focus,
     * so the value factory can normalise it.
     * @param spinner an editable integer spinner with a value factory set
     */
    public static void commitOnFocusLost(Spinner<Integer> spinner) {
        spinner.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (Boolean.FALSE.equals(isFocused)) {
                StringConverter<Integer> converter = spinner.getValueFactory().getConverter();
                Integer converted = converter.fromString(spinner.getEditor().getText());
                spinner.getValueFactory().setValue(converted);
                spinner.getEditor().setText(converter.toString(converted));
            }
        });
    }

}