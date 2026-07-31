package net.microfalx.lang.convert;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.microfalx.lang.IOUtils;
import net.microfalx.lang.Initializable;
import net.microfalx.lang.ObjectUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A facade for Jackson ObjectMapper and related classes.
 * <p>
 * When deserializing, the value can be a {@link Reader}, {@link File},
 * {@link InputStream} or a String or byte[].
 * <p>
 * Any null or empty string or empty bytes[] will be deserialized as a null value or empty collection.
 */
public class GsonConverter implements JsonConverter, Initializable {

    private Gson gson;

    public String asString(Object value) {
        if (value == null) return null;
        return getGson().toJson(value);
    }

    public byte[] asBytes(Object value) {
        if (value == null) return null;
        return asString(value).getBytes(StandardCharsets.UTF_8);

    }

    /**
     * Converts a JSON object to a Collection.
     *
     * @param value the value
     * @return the converted collection
     */
    public Collection<?> asCollection(Object value) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptyList();
        Type collectionType = new TypeToken<List<?>>() {
        }.getType();
        return gson.fromJson(getReader(value), collectionType);
    }

    /**
     * Converts a JSON object to a Collection of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public <T> Collection<T> asCollection(Object value, Class<T> elementType) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptyList();
        Class<T[]> arrayClass = (Class<T[]>) Array.newInstance(elementType, 0).getClass();
        T[] array = gson.fromJson(getReader(value), arrayClass);
        return Arrays.asList(array);
    }

    /**
     * Converts a JSON object to a Set.
     *
     * @param value the value
     * @return the converted collection
     */
    public Set<?> asSet(Object value) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptySet();
        Type collectionType = new TypeToken<Set<?>>() {
        }.getType();
        return gson.fromJson(getReader(value), collectionType);
    }

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public <T> Set<T> asSet(Object value, Class<T> elementType) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptySet();
        return new HashSet<>(asCollection(value, elementType));
    }

    /**
     * Converts a JSON object to a Map.
     *
     * @param value the value
     * @return the converted collection
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> asMap(Object value) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptyMap();
        // Define the Map type using TypeToken
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(getReader(value), mapType);

    }

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public <T> T asObject(Object value, Class<T> elementType) throws IOException {
        if (ObjectUtils.isEmpty(value)) return null;
        return gson.fromJson(getReader(value), elementType);

    }

    @Override
    public void initialize(Object... context) {
        GsonBuilder builder = new GsonBuilder();
        builder.setStrictness(Strictness.LENIENT).
                setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        gson = builder.create();
    }

    /**
     * Returns the object mapper.
     *
     * @return a non-null instance
     */
    public Gson getGson() {
        return gson;
    }

    private static Reader getReader(Object value) throws IOException {
        requireNonNull(value);
        if (value instanceof String) {
            return new StringReader((String) value);
        } else if (value instanceof InputStream) {
            return new InputStreamReader((InputStream) value);
        } else if (value instanceof Reader) {
            return (Reader) value;
        } else if (value instanceof File) {
            return IOUtils.getBufferedReader(new FileReader((File) value));
        } else if (value instanceof byte[]) {
            return new InputStreamReader(new ByteArrayInputStream((byte[]) value));
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
        }
    }

    private static class DurationTypeAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

        @Override
        public JsonElement serialize(Duration src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Duration deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context)
                throws JsonParseException {
            return Duration.parse(json.getAsString());
        }
    }
}
