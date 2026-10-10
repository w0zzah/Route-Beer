package seng202.team5.models.locations;

import java.util.Map;

/**
 * Holds data from Christchurch City Council for later processing
 */
public class CCCLocation {
    public String type;
    public Map<String, Object> properties;
    public Map<String, Object> geometry;

    public String getType() {
        return type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Map<String, Object> getGeometry() {
        return geometry;
    }
}
