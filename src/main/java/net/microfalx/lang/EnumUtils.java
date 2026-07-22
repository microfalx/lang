package net.microfalx.lang;

import net.microfalx.lang.annotation.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A collection of utilities around enums.
 */
public class EnumUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumUtils.class);

    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends Enum>, Map<String, ? extends Enum>> enumFromName = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends Enum>, Map<? extends Enum, String>> enumToName = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends Enum>, Map<Integer, ? extends Enum>> enumFromOrdinal = new ConcurrentHashMap<>();

    /**
     * Returns the enum with a given name.
     *
     * @param enumClass the enum class
     * @param name      the enum name, case-insensitive
     * @param <E>       the enum type
     * @return the enum
     * @throws java.lang.IllegalArgumentException if an enum with the given name does not exist
     */
    public static <E extends Enum<E>> E fromName(Class<E> enumClass, String name) {
        return doFindEnum(enumClass, name, true);
    }

    /**
     * Returns the enum with a given name.
     * <p>
     *
     * @param enumClass    the enum class
     * @param value        the enum name
     * @param defaultValue the default value if the enum name is empty or the enum cannot be found; if the enum value is null, an exception will be raised
     * @param <E>          the enum type
     * @return the enum, default if such an enum cannot be located
     */
    public static <E extends Enum<E>> E fromName(Class<E> enumClass, String value, E defaultValue) {
        if (StringUtils.isEmpty(value)) return defaultValue;
        return ObjectUtils.defaultIfNull(doFindEnum(enumClass, value, false), defaultValue);
    }

    /**
     * Returns the enum with a given ordinal.
     *
     * @param enumClass the enum class
     * @param ordinal   the enum ordinal
     * @param <E>       the enum type
     * @return the enum
     * @throws java.lang.IllegalArgumentException if an enum with the given name does not exist
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E fromOrdinal(Class<E> enumClass, int ordinal) {
        requireNonNull(enumClass);

        Map<Integer, E> map = buildFromOrdinalCache(enumClass);
        Enum<?> _enum = map.get(ordinal);
        if (_enum != null) return (E) _enum;
        throw new IllegalArgumentException("Invalid enum ordinal '" + ordinal + "' for " + enumClass);
    }

    /**
     * Returns the enum with a given ordinal.
     *
     * @param enumClass    the enum class
     * @param ordinal      the enum ordinal
     * @param defaultValue the default value
     * @param <E>          the enum type
     * @return the enum
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E fromOrdinal(Class<E> enumClass, int ordinal, E defaultValue) {
        requireNonNull(enumClass);
        Map<Integer, E> map = buildFromOrdinalCache(enumClass);
        Enum<?> _enum = map.get(ordinal);
        return _enum != null ? (E) _enum : defaultValue;
    }

    /**
     * Returns the enum name.
     * <p>
     * If an alias ({@link Name} is used, the alias is returned.
     *
     * @param enumInstance the enum
     * @param <E>          the enum type
     * @return the enum name, NULL if the enum is null
     */
    public static <E extends Enum<E>> String toName(E enumInstance) {
        if (enumInstance == null) return null;
        Map<E, String> nameCache = buildToNameCache(enumInstance.getClass());
        String name = nameCache.get(enumInstance);
        if (name != null) return name;
        throw new IllegalArgumentException("Failed to extract enum name for '" + enumInstance + "', type " + enumInstance.getClass().getName());
    }

    /**
     * Returns the enum label.
     * <p>
     * If an alias ({@link Name} is used, the alias is returned.
     *
     * @param enumInstance the enum
     * @param <E>          the enum type
     * @return the enum name, NULL if the enum is null
     */
    public static <E extends Enum<E>> String toLabel(E enumInstance) {
        return toName(enumInstance);
    }

    /**
     * Returns the names for all enums for a given type.
     * <p>
     * If an alias ({@link Name} is used, the alias is returned.
     *
     * @param enumType the enum type
     * @param <E>      the enum type
     * @return the names of each enum
     * @throws java.lang.IllegalArgumentException if an enum with the given name does not exist
     */
    public static <E extends Enum<E>> Collection<String> toNames(Class<E> enumType) {
        if (enumType == null) return Collections.emptyList();
        Collection<String> names = new ArrayList<>();
        for (E _enum : enumType.getEnumConstants()) {
            names.add(toName(_enum));
        }
        return Collections.unmodifiableCollection(names);
    }

    /**
     * Returns whether the enum value is contained in the list of enum values.
     *
     * @param value  the enum to test
     * @param values the list of enums
     * @param <E>    the enum type
     * @return <code>true</code> if the enum is along the given enums, <code>false</code> otherwise
     */
    public static <E extends Enum<E>> boolean contains(E value, E... values) {
        if (value == null || values == null) return false;
        for (E _value : values) {
            if (_value == value) return true;
        }
        return false;
    }

    /**
     * Adds a new value to an existing set of enums.
     *
     * @param values the original set
     * @param value  the enum to add to the set
     * @param <E>    the enum type
     * @return a new (enum) set
     */
    public static <E extends Enum<E>> Set<E> add(Set<E> values, E value) {
        requireNonNull(values);
        requireNonNull(value);
        EnumSet<E> newSet = EnumSet.noneOf(value.getDeclaringClass());
        newSet.addAll(values);
        values.add(value);
        return newSet;
    }

    private static Collection<String> getAliases(Enum<?> enumInstance) {
        Set<String> aliases = new CopyOnWriteArraySet<>();
        try {
            Name named = enumInstance.getClass().getField(enumInstance.name()).getAnnotation(Name.class);
            if (named != null && isNotEmpty(named.value())) {
                aliases.add(named.value());
            } else {
                aliases.add(StringUtils.capitalizeWords(enumInstance.name()));
            }
        } catch (NoSuchFieldException e) {
            LOGGER.error("Enum " + enumInstance + " cannot be accessed with reflection, root cause: " + e.getMessage());
        }
        return aliases;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <E extends Enum<E>> Map<String, Enum> buildFromNameCache(Class<E> enumClass) {
        Map<String, Enum> map = (Map<String, Enum>) enumFromName.get(enumClass);
        if (map == null) {
            map = new HashMap<>();
            Enum[] enumConstants = enumClass.getEnumConstants();
            for (Enum enumConstant : enumConstants) {
                Collection<String> aliases = getAliases(enumConstant);
                for (String alias : aliases) {
                    map.put(alias.toUpperCase(), enumConstant);
                }
                map.put(enumConstant.name(), enumConstant);
            }
            enumFromName.put(enumClass, map);
        }

        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <E extends Enum<E>> Map<E, String> buildToNameCache(Class<E> enumClass) {
        Map<E, String> map = (Map<E, String>) enumToName.get(enumClass);
        if (map == null) {
            map = new HashMap<>();
            Enum[] enumConstants = enumClass.getEnumConstants();
            for (Enum enumConstant : enumConstants) {
                String enumName = enumConstant.name();
                Collection<String> aliases = getAliases(enumConstant);
                if (!aliases.isEmpty()) {
                    enumName = aliases.iterator().next();
                }
                map.put((E) enumConstant, enumName);
            }
            enumToName.put(enumClass, map);
        }
        return map;
    }

    @SuppressWarnings({"unchecked"})
    private static <E extends Enum<E>> Map<Integer, E> buildFromOrdinalCache(Class<E> enumClass) {
        Map<Integer, E> map = (Map<Integer, E>) enumFromOrdinal.get(enumClass);
        if (map == null) {
            map = new HashMap<>();
            Enum<?>[] enumConstants = enumClass.getEnumConstants();
            for (Enum<?> enumConstant : enumConstants) {
                map.put(enumConstant.ordinal(), (E) enumConstant);
            }
            enumFromOrdinal.put(enumClass, map);
        }
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <E extends Enum<E>> E doFindEnum(Class<E> enumClass, String name, boolean failOnInvalid) {
        requireNonNull(enumClass);
        requireNonNull(name);

        Map<String, Enum> nameCache = buildFromNameCache(enumClass);
        name = name.trim().toUpperCase();
        Enum<?> _enum = nameCache.get(name);
        if (_enum != null) return (E) _enum;
        name = name.toLowerCase();
        _enum = nameCache.get(name);
        if (_enum != null) return (E) _enum;
        name = name.replace('-', '_');
        _enum = nameCache.get(name);
        if (_enum != null) return (E) _enum;
        name = name.toUpperCase();
        _enum = nameCache.get(name);
        if (_enum != null) return (E) _enum;

        if (failOnInvalid) {
            throw new IllegalArgumentException("Invalid enum '" + name + "' for " + enumClass.getName());
        } else {
            return null;
        }
    }
}
