package seng202.team5.models;

import java.util.Map;

/**
 * Holds data from OpenStreetMap API for later processing
 */
public class OpenStreetMapLocation {
    public String type;
    public Map<String, Object> properties;
    public Map<String, Object> geometry;
    public String id;
}
