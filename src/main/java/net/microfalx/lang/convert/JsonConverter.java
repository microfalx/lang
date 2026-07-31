package net.microfalx.lang.convert;

import com.google.common.reflect.TypeToken;
import net.microfalx.lang.ObjectUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A bridge to the JSON reader/writer.
 */
public interface JsonConverter {

    /**
     * Converts the object to a String.
     *
     * @param value the value to convert
     * @return the converted value
     */
    String asString(Object value);

    /**
     * Converts the object to an array of bytes.
     *
     * @param value the value to convert
     * @return the converted value
     */
    byte[] asBytes(Object value);

    /**
     * Converts a JSON object to a Collection.
     *
     * @param value the value
     * @return the converted collection
     */
    Collection<?> asCollection(Object value) throws IOException;

    /**
     * Converts a JSON object to a Collection of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    <T> Collection<T> asCollection(Object value, Class<T> elementType) throws IOException;

    /**
     * Converts a JSON object to a Set.
     *
     * @param value the value
     * @return the converted collection
     */
    Set<?> asSet(Object value) throws IOException;

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    <T> Set<T> asSet(Object value, Class<T> elementType) throws IOException;

    /**
     * Converts a JSON object to a Map.
     *
     * @param value the value
     * @return the converted collection
     */
    <T> Map<String, T> asMap(Object value) throws IOException;

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    <T> T asObject(Object value, Class<T> elementType) throws IOException;
}
