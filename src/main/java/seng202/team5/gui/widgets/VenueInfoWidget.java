package seng202.team5.gui.widgets;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.Venue;

import java.io.IOException;

/**
 * Controller for the bar detail widget: a drawer that slides in from the right
 * edge of the map when a marker is clicked, and can be toggled by the arrow tab
 * on its left edge.
 */
public class VenueInfoWidget {

    private static final Duration SLIDE_DURATION = Duration.millis(260);

    @FXML private Label nameLabel, descriptionLabel, themeLabel;
    @FXML private Label addressLabel, featureLabel, openingHours;
    @FXML private Label featureParent, openingHoursParent;

    private Node root;
    private Pane content;
    private Region slider;
    private SVGPath arrow;

    private TranslateTransition slide;
    private RotateTransition spin;
    private ParallelTransition animation;
    private boolean open;

    /**
     * Loads the widget into the given content pane and wires up the slide animation.
     * Call once during MainWindowController.initialise.
     *
     * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/animation/Animation.html">Animation Documentation</a>
     * @param content the pane the card is loaded into (the right half of the slider)
     * @param slider  the node that is translated - contains both toggle and content
     * @param arrow   the arrow graphic inside the toggle button, rotated on open/close
     */
    public static VenueInfoWidget attachTo(Pane content, Region slider, SVGPath arrow) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    VenueInfoWidget.class.getResource("/fxml/venue_info.fxml"));
            Node node = loader.load();
            VenueInfoWidget controller = loader.getController();
            controller.root = node;
            controller.content = content;
            controller.slider = slider;
            controller.arrow = arrow;
            content.getChildren().add(node);
            controller.initialiseSlide();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException("Could not load /fxml/venue_info.fxml", e);
        }
    }

    /**
     * Initialises the sliding bar info panel
     */
    private void initialiseSlide() {
        slide = new TranslateTransition(SLIDE_DURATION, slider);
        slide.setInterpolator(Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0));

        spin = new RotateTransition(SLIDE_DURATION, arrow);
        spin.setInterpolator(Interpolator.EASE_BOTH);

        animation = new ParallelTransition(slide, spin);

        showPlaceholder();

        open = false;
        slider.setTranslateX(hiddenOffset());
        arrow.setRotate(0);
        content.setMouseTransparent(true);

        content.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (!open) {
                animation.stop();
                slider.setTranslateX(newWidth.doubleValue());
            }
        });

    }

    /**
     * Distance the slider must travel to park the card off-screen.
     */
    private double hiddenOffset() {
        double width = content.getWidth();
        return width > 0 ? width : content.getPrefWidth();
    }

    /**
     * Populates the widget for the given bar and slides it into view.
     */
    public void show(Venue venue) {
        nameLabel.setText(venue.getName());
        openingHoursParent.setVisible(true);
        featureParent.setVisible(true);
        setParameter(addressLabel, venue.getStrippedAddress());
        setTitleAndParameter(openingHoursParent, openingHours, venue.printHours());
        // Clear theme and features
        setParameter(themeLabel, "");
        setTitleAndParameter(featureParent, featureLabel, "");
        setParameter(descriptionLabel, "");

        if (venue instanceof Bar bar) {
            setParameter(themeLabel, bar.getTheme() != null ? bar.getTheme().toString() : null);
            setParameter(descriptionLabel, bar.getDescription());
            setTitleAndParameter(featureParent, featureLabel, bar.printFeatures());
        }
        setOpen(true);
    }

    /**
     * Slides the panel out of view. Content is left intact.
     */
    public void hide() {
        setOpen(false);
    }

    /**
     * Slides the panel in or out depending on its current state.
     */
    public void toggle() {
        setOpen(!open);
    }

    /**
     * Controls the state of the panel
     * @param shouldOpen Whether the panel should open or not
     */
    private void setOpen(boolean shouldOpen) {
        if (open == shouldOpen) {
            return;
        }
        open = shouldOpen;

        animation.stop();
        slide.setFromX(slider.getTranslateX());
        slide.setToX(open ? 0 : hiddenOffset());
        spin.setFromAngle(arrow.getRotate());
        spin.setToAngle(open ? 180 : 0);

        // While parked, the card must not intercept clicks meant for the map.
        content.setMouseTransparent(!open);

        animation.play();
    }

    @FXML
    private void close() {
        hide();
    }

    /** Shown before any marker has been clicked. */
    private void showPlaceholder() {
        nameLabel.setText("No bar selected");
        themeLabel.setVisible(false);
        setParameter(addressLabel, null);
        setParameter(descriptionLabel, "Click a marker on the map to see its details.");
        openingHoursParent.setVisible(false);
        featureParent.setVisible(false);
    }

    /**
     * Sets an optional label field to given string
     * @param label The label to be updated
     * @param text The string for the label to display
     */
    private void setParameter(Label label, String text) {
        boolean present = text != null && !text.isBlank();
        label.setText(present ? text : "");
        label.setVisible(present);
        label.setManaged(present);
    }

    /**
     * Sets the title of the panel
     * @param parent The title label
     * @param label The label to be updated
     * @param text The string for the label to display
     */
    private void setTitleAndParameter(Label parent, Label label, String text) {
        boolean present = text != null && !text.isBlank();
        label.setText(present ? text : "");
        label.setVisible(present);
        label.setManaged(present);
        parent.setVisible(present);
        parent.setManaged(present);
    }
}