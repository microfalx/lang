package net.microfalx.lang.convert;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A facade for data type conversions.
 * <p>
 * The string representation of a collection or map or a complex object is expected to be in JSON format. JSON content
 * can be provided as a String, Reader, File, InputStream or Resource.
 */
public class Types {

    private Types() {
    }

    /**
     * Converts an object to a target type.
     *
     * @param value  the value to convert
     * @param target the target class
     * @param <T>    the type of the target value
     * @return the converted value
     */
    public static <T> T from(Object value, Class<T> target) {
        return DefaultConverters.from(value, target);
    }

    /**
     * Converts the object to a String.
     * <p>
     * If complex object is provided, it will be converted to JSON string.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static String asString(Object value) {
        if (value == null) return null;
        return from(value, String.class);
    }

    /**
     * Converts a JSON object to a Collection.
     *
     * @param value the value
     * @return the converted collection
     */
    public static Collection<?> asCollection(Object value) {
        try {
            return getConverter().asCollection(value);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert value to collection", e);
        }
    }

    /**
     * Converts a JSON object to a Collection of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public static <T> Collection<T> asCollection(Object value, Class<T> elementType) {
        try {
            return getConverter().asCollection(value, elementType);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert value to collection of " + elementType.getName(), e);
        }
    }

    /**
     * Converts a JSON object to a Set.
     *
     * @param value the value
     * @return the converted collection
     */
    public static Set<?> asSet(Object value) {
        try {
            return getConverter().asSet(value);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert value to collection", e);
        }
    }

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public static <T> Set<T> asSet(Object value, Class<T> elementType) {
        try {
            return getConverter().asSet(value, elementType);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert value to collection of " + elementType.getName(), e);
        }
    }

    /**
     * Converts a JSON object to a Map.
     *
     * @param value the value
     * @return the converted collection
     */
    public static <T> Map<String, T> asMap(Object value) {
        try {
            return getConverter().asMap(value);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert value to collection", e);
        }
    }

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public static <T> T asObject(Object value, Class<T> elementType) {
        try {
            return getConverter().asObject(value, elementType);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert value to object of " + elementType.getName(), e);
        }
    }

    static JsonConverter getConverter() {
        if (converter == null) {
            converter = ConverterService.getInstance().getJsonConverter();
        }
        return converter;
    }

    static volatile JsonConverter converter;

}
