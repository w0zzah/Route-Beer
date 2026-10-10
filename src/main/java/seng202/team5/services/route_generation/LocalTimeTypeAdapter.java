package seng202.team5.services.route_generation;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Helper class for formatting days correctly in GsonBuilder
 */
public class LocalTimeTypeAdapter extends TypeAdapter<LocalTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Writes a LocalTime object to JSON using the HH:mm format
     * @param out the JSON writer used to write the value
     * @param value the local time value to write
     * @throws IOException if an error occurs while writing the JSON
     */
    @Override
    public void write(JsonWriter out, LocalTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(FORMATTER));
        }
    }

    /**
     * Reads a JSON value and converts it to a LocalTime object
     * using the specified formatter
     * @param in the JSON reader containing the time value
     * @return the parsed LocalTime, or null if the JSON value is null
     * @throws IOException if an error occurs while reading the JSON
     */
    @Override
    public LocalTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return LocalTime.parse(in.nextString(), FORMATTER);
    }

}
