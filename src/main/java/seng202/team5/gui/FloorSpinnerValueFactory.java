package seng202.team5.gui;

import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Custom SpinnerValueFactory class to allow better Spinner customisation
 * Shows prompt text when the input is less than MIN_DISPLAYED
 * Shows MAX when input is greater than MAX
 */
public class FloorSpinnerValueFactory extends SpinnerValueFactory<Integer> {
    private static final int MIN_DISPLAYED = 2;
    private static final int MAX = 9;

    public FloorSpinnerValueFactory() {
        setConverter(new StringConverter<Integer>() {

            /**
             * Override toString function to show intended values
             * @param value the value entered
             * @return the string to be shown in the spinner
             */
            @Override
            public String toString(Integer value) {
                if (value == null) {
                    return "";
                } else if (value < MIN_DISPLAYED) {
                    return String.valueOf(MIN_DISPLAYED);
                }
                return value.toString();
            }

            /**
             * Override fromString to return intended integers for
             * different spinner string values
             * @param text text currently in spinner
             * @return the integer that this value should be interpreted as
             */
            @Override
            public Integer fromString(String text) {
                text = text.strip();
                if (text.isEmpty()) {
                    return null;
                }
                try {
                    int parsed = Integer.parseInt(text.trim());
                    if (parsed > MAX) {
                        return 9;
                    } else if (parsed < MIN_DISPLAYED) {
                        return null;
                    } else {
                        return parsed;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
        setValue(null);
    }

    /**
     * Override decrement function
     * @param steps amount to decrement by
     */
    @Override
    public void decrement(int steps) {
        Integer current = getValue();

        if ((current == null) || ((current - steps) < MIN_DISPLAYED)) {
            setValue(null);
        } else {
            setValue(current-steps);
        }
    }

    /**
     * Override increment function
     * @param steps amount to increment by
     */
    @Override
    public void increment(int steps) {
        Integer current = getValue();

        if (current == null) {
            setValue((MIN_DISPLAYED - 1) + steps);
        } else {
            setValue(Math.min(current + steps, MAX));
        }
    }
}
