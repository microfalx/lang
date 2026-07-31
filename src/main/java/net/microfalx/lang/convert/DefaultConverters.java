package net.microfalx.lang.convert;

import com.google.gson.Gson;
import jodd.time.TimeUtil;
import jodd.typeconverter.TypeConversionException;
import jodd.typeconverter.TypeConverter;
import jodd.typeconverter.TypeConverterManager;
import jodd.typeconverter.impl.LocalDateTimeConverter;
import jodd.util.StringUtil;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.TimeUtils;

import java.io.IOException;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Hosts a collection of converters.
 */
class DefaultConverters {

    static final TypeConverterManager TYPE_CONVERTER_MANAGER = TypeConverterManager.get();

    private static final Set<Class<?>> SIMPLE_TYPES = new CopyOnWriteArraySet<>();
    private static Gson gson;

    /**
     * Converts an object to a target type.
     *
     * @param value  the value to convert
     * @param target the target class
     * @param <T>    the type of the target value
     * @return the converted value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> T from(Object value, Class<T> target) {
        if (target == null) return null;
        try {
            if (target.isEnum()) {
                return (T) toEnum((Class<Enum>) target, value);
            } else if (ClassUtils.isSubClassOf(target, Temporal.class)) {
                return (T) toTemporal(value, (Class<Temporal>) target);
            } else if (ClassUtils.isSubClassOf(target, Number.class) && ObjectUtils.isEmpty(value)) {
                return target.isPrimitive() ? from(0, target) : null;
            } else {
                return convert(value, target);
            }
        } catch (InvalidDataTypeExpression e) {
            throw e;
        } catch (Exception e) {
            return throwConversionException(value, target, e);
        }
    }

    /**
     * Converts special string to a temporal.
     *
     * @param value  the value
     * @param target the target
     * @param <T>    the subset of temporal
     * @return the temporal
     */
    private static <T extends Temporal> Temporal toTemporal(Object value, Class<T> target) {
        if (value instanceof Temporal) return (Temporal) value;
        if (ObjectUtils.isEmpty(value)) {
            return null;
        } else if ("now".equals(value)) {
            return convert(LocalDateTime.now(), target);
        } else if ("today".equals(value)) {
            return convert(LocalDate.now().atStartOfDay(), target);
        } else if ("yesterday".equals(value)) {
            return convert(LocalDate.now().minusDays(1).atStartOfDay(), target);
        } else if ("last week".equals(value)) {
            return convert(LocalDate.now().minusDays(7).atStartOfDay(), target);
        } else if ("next week".equals(value)) {
            return convert(LocalDate.now().plusDays(7).atStartOfDay(), target);
        } else {
            return convert(value, target);
        }
    }

    /**
     * Converts to an enum.
     *
     * @param type  the enum class
     * @param value the value
     * @param <E>   the enum type
     * @return the enum
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E toEnum(Class<E> type, Object value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        } else if (value instanceof Enum) {
            return (E) value;
        } else if (value instanceof Number) {
            return EnumUtils.fromOrdinal(type, ((Number) value).intValue());
        } else {
            return EnumUtils.fromName(type, ((String) value));
        }

    }

    /**
     * Returns whether the type is a simple type (primitive, wrapper, String, Temporal).
     *
     * @param type the type
     * @return {@code }
     */
    public static boolean isBaseClass(Class<?> type) {
        return ClassUtils.isBaseClass(type) || SIMPLE_TYPES.contains(type);
    }

    public static <T> void register(Class<T> type, Converter<T> converter) {
        requireNonNull(type);
        requireNonNull(converter);
        TYPE_CONVERTER_MANAGER.register(String.class, new StringConverter());
    }

    private static <T> T convert(Object value, Class<T> target) {
        try {
            return TYPE_CONVERTER_MANAGER.convertType(value, target);
        } catch (Exception e) {
            return throwConversionException(value, target, e);
        }
    }

    private static <T> T throwConversionException(Object value, Class<T> target, Throwable throwable) {
        throw new InvalidDataTypeExpression("Data conversion failure for '" + value + "' to type '" + ClassUtils.getName(target) + "'", throwable);
    }

    private static final int DATE_TIME_MIN_LENGTH = 12;

    static class TagsConverter implements Converter<String> {

        @Override
        public String convert(Object value) {
            return "";
        }
    }

    static class JoddConverter<T> implements TypeConverter<T> {

        private final Converter<T> delegate;

        public JoddConverter(Converter<T> delegate) {
            requireNonNull(delegate);
            this.delegate = delegate;
        }

        @Override
        public T convert(Object value) {
            return delegate.convert(value);
        }
    }

    static class DurationConverter implements TypeConverter<Duration> {

        @Override
        public Duration convert(Object value) {
            if (value == null) return null;
            if (value instanceof Duration) {
                return (Duration) value;
            } else if (value instanceof Number) {
                return Duration.ofMillis(((Number) value).longValue());
            } else {
                return TimeUtils.parseDuration(value.toString());
            }
        }
    }

    static class StringConverter implements TypeConverter<String> {

        private final TypeConverter<String> original = new jodd.typeconverter.impl.StringConverter();

        private String convertJdk(Object value) {
            if (value instanceof URI) {
                return ((URI) value).toASCIIString();
            } else {
                return original.convert(value);
            }
        }

        @Override
        public String convert(Object value) {
            if (value == null) return null;
            if (isBaseClass(value.getClass()) && !value.getClass().isArray()) {
                return convertJdk(value);
            } else {
                try {
                    return Types.getConverter().asString(value);
                } catch (Exception e) {
                    return throwConversionException(value, String.class, e);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    static class MapConverter implements TypeConverter<Map> {

        @Override
        public Map<?, ?> convert(Object value) {
            if (value == null) return Collections.emptyMap();
            if (value instanceof Map) {
                return (Map<?, ?>) value;
            } else if (value instanceof String valueAsString) {
                if (valueAsString.isEmpty()) return Collections.emptyMap();
                try {
                    return Types.getConverter().asMap(valueAsString);
                } catch (IOException e) {
                    throw new TypeConversionException("Failed to decode JSON", e);
                }
            } else {
                return throwConversionException(value, Map.class, null);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    static class CollectionConverter implements TypeConverter<Collection> {

        @Override
        public Collection<?> convert(Object value) {
            if (value == null) return Collections.emptyList();
            if (value instanceof Collection) {
                return (Collection<?>) value;
            } else if (value instanceof String valueAsString) {
                if (valueAsString.isEmpty()) return Collections.emptyList();
                try {
                    return Types.getConverter().asCollection(valueAsString);
                } catch (IOException e) {
                    throw new TypeConversionException("Failed to decode JSON", e);
                }
            } else {
                return throwConversionException(value, Collection.class, null);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    static class SetConverter implements TypeConverter<Set> {

        @Override
        public Set<?> convert(Object value) {
            if (value == null) return Collections.emptySet();
            if (value instanceof Set) {
                return (Set<?>) value;
            } else if (value instanceof String valueAsString) {
                if (valueAsString.isEmpty()) return Collections.emptySet();
                try {
                    return Types.getConverter().asSet(valueAsString);
                } catch (IOException e) {
                    throw new TypeConversionException("Failed to decode JSON", e);
                }
            } else {
                return throwConversionException(value, Set.class, null);
            }
        }
    }

    static class ZonedDateTimeConverter implements TypeConverter<ZonedDateTime> {

        private final LocalDateTimeConverter converter = new LocalDateTimeConverter();

        @Override
        public ZonedDateTime convert(Object value) {
            if (value == null) return null;
            if (value instanceof ZonedDateTime) {
                return (ZonedDateTime) value;
            } else if (value instanceof OffsetDateTime) {
                return ((OffsetDateTime) value).toZonedDateTime();
            } else if (value instanceof String) {
                String stringValue = ((String) value).trim();
                if (!StringUtil.containsOnlyDigits(stringValue)) {
                    return stringValue.length() >= DATE_TIME_MIN_LENGTH ? ZonedDateTime.parse(stringValue)
                            : LocalDate.parse(stringValue).atStartOfDay(ZoneId.systemDefault());
                } else {
                    return TimeUtil.fromMilliseconds(Long.parseLong(stringValue)).atZone(ZoneId.systemDefault());
                }
            } else {
                LocalDateTime localDateTime = converter.convert(value);
                return localDateTime.atZone(ZoneId.systemDefault());
            }
        }
    }

    static class OffsetDateTimeConverter implements TypeConverter<OffsetDateTime> {

        private final LocalDateTimeConverter converter = new LocalDateTimeConverter();

        @Override
        public OffsetDateTime convert(Object value) {
            if (value == null) return null;
            if (value instanceof OffsetDateTime) {
                return (OffsetDateTime) value;
            } else if (value instanceof ZonedDateTime) {
                return ((ZonedDateTime) value).toOffsetDateTime();
            } else if (value instanceof String) {
                String stringValue = ((String) value).trim();
                if (!StringUtil.containsOnlyDigits(stringValue)) {
                    return stringValue.length() >= DATE_TIME_MIN_LENGTH ? OffsetDateTime.parse(stringValue)
                            : LocalDate.parse(stringValue).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
                } else {
                    return TimeUtil.fromMilliseconds(Long.parseLong(stringValue)).atZone(ZoneId.systemDefault()).toOffsetDateTime();
                }
            } else {
                LocalDateTime localDateTime = converter.convert(value);
                return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
            }
        }
    }

    static JsonConverter getConverter() {
        return Types.getConverter();
    }

    static {
        TYPE_CONVERTER_MANAGER.register(String.class, new StringConverter());
        TYPE_CONVERTER_MANAGER.register(ZonedDateTime.class, new ZonedDateTimeConverter());
        TYPE_CONVERTER_MANAGER.register(OffsetDateTime.class, new OffsetDateTimeConverter());
        TYPE_CONVERTER_MANAGER.register(Duration.class, new DurationConverter());
        TYPE_CONVERTER_MANAGER.register(Map.class, new MapConverter());
        TYPE_CONVERTER_MANAGER.register(Collection.class, new CollectionConverter());
        TYPE_CONVERTER_MANAGER.register(Set.class, new SetConverter());
    }
}
