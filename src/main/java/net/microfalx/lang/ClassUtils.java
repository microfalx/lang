package net.microfalx.lang;

import net.microfalx.lang.annotation.Provider;
import org.atteo.classindex.ClassIndex;

import java.io.File;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;

/**
 * Various utilities around classes and class loaders.
 */
public class ClassUtils {

    private static final Map<Class<?>, Collection<?>> PROVIDERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_OBJECT_CLASSES = new HashMap<>();
    private static final Map<Class<?>, Class<?>> OBJECT_TO_PRIMITIVE_CLASSES = new HashMap<>();
    private static final Set<Class<?>> BASE_CLASSES = new HashSet<>();
    private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * Returns whether the object represents a basic Java object (numbers, string, boolean, dates, etc).
     *
     * @param value the object to test
     * @return {@code true} if a base class, {@code false} otherwise
     */
    public static boolean isBaseClass(Object value) {
        return value != null && isJdkClass(value.getClass());
    }

    /**
     * Returns whether the class represents a basic Java class (numbers, string, boolean, dates, etc).
     *
     * @param clazz the class to test
     * @return {@code true} if a base class, {@code false} otherwise
     */
    public static boolean isBaseClass(Class<?> clazz) {
        requireNotEmpty(clazz);
        return BASE_CLASSES.contains(clazz);
    }

    /**
     * Returns the class name.
     *
     * @param cls the class for which to get the class name; may be null
     * @return the class name or the empty string in case the argument is {@code null}
     */
    public static String getName(Class<?> cls) {
        return getName(cls, EMPTY_STRING);
    }

    /**
     * Returns the class name of the objectby reducing each package component to the first letter.
     *
     * @param object the class
     * @return the name, empty string if class was {@code null}
     */
    public static String getCompactName(Object object) {
        return object != null ? getCompactName(object.getClass()) : EMPTY_STRING;
    }

    /**
     * Returns the class name by reducing each package component to the first letter.
     *
     * @param cls the class
     * @return the name, empty string if class was {@code null}
     */
    public static String getCompactName(Class<?> cls) {
        if (cls == null) return EMPTY_STRING;
        return getCompactName(cls.getName());
    }

    /**
     * Returns the class name by reducing each package component to the first letter.
     *
     * @param name the class name
     * @return the name, empty string if class was {@code null}
     */
    public static String getCompactName(String name) {
        if (name == null) return EMPTY_STRING;
        String classPackage = null;
        int i = name.lastIndexOf('.');
        if (i != -1) {
            classPackage = name.substring(0, i);
            name = name.substring(i + 1);
        }
        if (classPackage != null) {
            String[] parts = StringUtils.split(classPackage, ".");
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                if (builder.length() > 0) builder.append(".");
                builder.append(part.charAt(0));
            }
            name = builder + "." + name;
        }
        return convertLambda(name);
    }

    /**
     * Returns the class name.
     *
     * @param cls          the class for which to get the class name; may be null
     * @param defaultValue the return value if the argument {@code cls} is {@code null}
     * @return the class name or {@code valueIfNull}
     */
    public static String getName(final Class<?> cls, final String defaultValue) {
        return cls == null ? defaultValue : convertLambda(cls.getName());
    }

    /**
     * Returns the class name of an object.
     *
     * @param object the object for which to get the class name; may be null
     * @return the class name or the empty String
     */
    public static String getName(Object object) {
        return getName(object, EMPTY_STRING);
    }

    /**
     * Returns the class name of an object.
     *
     * @param object       the object for which to get the class name; may be null
     * @param defaultValue the value to return if {@code object} is {@code null}
     * @return the class name or {@code valueIfNull}
     */
    public static String getName(Object object, final String defaultValue) {
        return object == null ? defaultValue : getName(object.getClass());
    }

    /**
     * Returns the class name of an object.
     *
     * @param object the object for which to get the class name; may be null
     * @return the class name or the empty String
     */
    public static String getSimpleName(Object object) {
        return getSimpleName(object, EMPTY_STRING);
    }

    /**
     * Returns the class name of an object.
     *
     * @param object       the object for which to get the class name; may be null
     * @param defaultValue the value to return if {@code object} is {@code null}
     * @return the class name or {@code valueIfNull}
     */
    public static String getSimpleName(Object object, final String defaultValue) {
        return object == null ? defaultValue : convertLambda(object.getClass().getSimpleName());
    }

    /**
     * Returns the class name of an object.
     *
     * @param className the class name as a string; may be null
     * @return the class name or the empty String
     */
    public static String getSimpleName(String className) {
        if (className == null) return null;
        int lastIndex = className.lastIndexOf('.');
        return lastIndex >= 0 ? className.substring(lastIndex + 1) : className;
    }

    /**
     * Returns whether the object is a subclass of a given superclass.
     *
     * @param object     an object
     * @param superClass a super class
     * @return {@code true} if a subclass, {@code false} otherwise
     */
    public static boolean isSubClassOf(Object object, Class<?> superClass) {
        return (object == null) ? false : isSubClassOf(object.getClass(), superClass);
    }

    /**
     * Returns if clazz is a subclass of superclass
     *
     * @param clazz      a clazz
     * @param superClazz a super class
     * @return true if subclass
     */
    public static boolean isSubClassOf(Class<?> clazz, Class<?> superClazz) {
        requireNonNull(clazz);
        requireNonNull(superClazz);
        return superClazz.isAssignableFrom(clazz);
    }

    /**
     * Returns whether the object represents a class is part of the JDK.
     *
     * @param value the object
     * @return {@code true} if a JDK class, {@code false} otherwise
     */
    public static boolean isJdkClass(Object value) {
        return value != null && isJdkClass(value.getClass());
    }

    /**
     * Returns whether the class is part of the JDK.
     *
     * @param clazz the class to validate
     * @return {@code true} if a JDK class, {@code false} otherwise
     */
    public static boolean isJdkClass(Class<?> clazz) {
        requireNonNull(clazz);
        return clazz.isPrimitive() || StringUtils.EMPTY_STRING.getClass().getClassLoader() == clazz.getClassLoader();
    }

    /**
     * Checks if a class exists in the class path.
     *
     * @param className the class name
     * @return {@code true} if exists, {@code false} otherwise
     */
    @SuppressWarnings("ConstantValue")
    public static boolean exists(String className) {
        try {
            return Class.forName(className) != null;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    /**
     * Returns whether the class can be instantiated.
     *
     * @param clazz the class to validate
     * @return {@code true} if an object can be created,  {@code false} otherwise
     */
    public static boolean canInstantiate(Class<?> clazz) {
        if (clazz == null) return false;
        return !(clazz.isInterface() || ((clazz.getModifiers() & Modifier.ABSTRACT) != 0));
    }

    /**
     * Returns a collection of provider classes for a given type.
     *
     * @param providerClass the provider class
     * @param <T>           the provider type
     * @return a collection of provider classes
     * @see Provider
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Collection<Class<T>> resolveProviders(Class<T> providerClass) {
        requireNotEmpty(providerClass);
        Collection cachedProviders = ClassUtils.PROVIDERS.get(providerClass);
        if (cachedProviders != null) return cachedProviders;
        Collection<Class<T>> providerClasses = new HashSet<>();
        Iterable<Class<?>> scannedProviderClasses = ClassIndex.getAnnotated(Provider.class);
        for (Class<?> scannedProviderClass : scannedProviderClasses) {
            if (ClassUtils.isSubClassOf(scannedProviderClass, providerClass)) {
                providerClasses.add((Class<T>) scannedProviderClass);
            }
        }
        List<Class<T>> orderedProviders = new ArrayList<>(providerClasses);
        AnnotationUtils.sort(orderedProviders);
        ClassUtils.PROVIDERS.put(providerClass, orderedProviders);
        return orderedProviders;
    }

    /**
     * Returns a collection of providers for a given type.
     *
     * @param providerClass the provider class
     * @param <T>           the provider type
     * @return a collection of provider classes
     * @see Provider
     */
    @SuppressWarnings({"unchecked", "CastCanBeRemovedNarrowingVariableType"})
    public static <T> Collection<T> resolveProviderInstances(Class<T> providerClass) {
        requireNotEmpty(providerClass);
        Collection<T> providerInstances = new ArrayList<>();
        Collection<Class<T>> providerClasses = resolveProviders(providerClass);
        for (Class<?> providerClassLocated : providerClasses) {
            if (!canInstantiate(providerClassLocated)) continue;
            try {
                providerInstances.add(create((Class<T>) providerClassLocated));
            } catch (Exception e) {
                // ignore this for now, maybe report somewhere
            }
        }
        return providerInstances;
    }

    /**
     * Clears all caches.
     */
    public void clearCaches() {
        PROVIDERS.clear();
    }

    /**
     * Returns the parametrized types used in class signature.
     *
     * @param clazz the class
     * @return a collection with classes
     */
    public static List<Class<?>> getClassParametrizedTypes(Class<?> clazz) {
        requireNonNull(clazz);
        Type[] genericTypes = getGenericTypes(clazz);
        Type type = genericTypes.length > 0 ? genericTypes[0] : null;
        if (!(type instanceof ParameterizedType)) {
            genericTypes = getGenericTypes(type);
            type = genericTypes.length > 0 ? genericTypes[0] : null;
        }
        if (!(type instanceof ParameterizedType)) return Collections.emptyList();
        List<Class<?>> classes = new ArrayList<>();
        for (Type typeArgument : ((ParameterizedType) type).getActualTypeArguments()) {
            if (typeArgument instanceof Class) {
                classes.add((Class<?>) typeArgument);
            } else if (typeArgument instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) typeArgument).getRawType();
                if (rawType instanceof Class) classes.add((Class<?>) rawType);
            }
        }
        return unmodifiableList(classes);
    }

    /**
     * Returns the parametrized type used in class signature.
     *
     * @param clazz the class
     * @param index the index of the parametrized type
     * @return the type, null if the class is not parametrized, or there is no parametrized type at the given index
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassParametrizedType(Class<?> clazz, int index) {
        List<Class<?>> parametrizedTypes = getClassParametrizedTypes(clazz);
        if (index < parametrizedTypes.size()) {
            return (Class<T>) parametrizedTypes.get(index);
        }
        return null;
    }

    /**
     * Returns the first parameterized type used in class signature.
     *
     * @param clazz the class
     * @param type  the expected super type
     * @return the type, null if none is found
     */
    public static Class<?> getClassParametrizedType(Class<?> clazz, Class<?> type) {
        requireNonNull(clazz);
        List<Class<?>> existingTypes = getClassParametrizedTypes(clazz);
        for (Class<?> existingType : existingTypes) {
            if (ClassUtils.isSubClassOf(existingType, type)) {
                return existingType;
            }
        }
        return null;
    }

    /**
     * Returns an array of types from the generic interfaces or super class.
     *
     * @param type the class
     * @return the types
     */
    public static Type[] getGenericTypes(Type type) {
        requireNonNull(type);
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            type = clazz.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                return new Type[]{type};
            } else {
                Type[] types = clazz.getGenericInterfaces();
                if (types.length > 0) {
                    return types;
                } else {
                    return EMPTY_TYPE_ARRAY;
                }
            }
        } else {
            return new Type[]{type};
        }
    }

    /**
     * Returns the underlying class for a type.
     *
     * @param type the type
     * @return the underlying class, null if the type is a variable.
     */
    @SuppressWarnings("rawtypes")
    public static Class<?> getClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return componentClass;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns an instance of an object with a given class.
     *
     * @param clazz the class
     * @param <T>   the object type
     * @return a non-null instance
     */
    public static <T> T create(Class<T> clazz) {
        Constructor<T> constructor;
        try {
            constructor = clazz.getConstructor(ClassUtils.EMPTY_CLASS_ARRAY);
        } catch (NoSuchMethodException e) {
            try {
                constructor = clazz.getDeclaredConstructor(ClassUtils.EMPTY_CLASS_ARRAY);
            } catch (NoSuchMethodException ex) {
                return rethrowExceptionAndReturn(e);
            }
        }
        if (!constructor.isAccessible()) constructor.setAccessible(true);
        try {
            return constructor.newInstance();
        } catch (Exception e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Returns the files (JARs) from the class path.
     *
     * @return a non-null instance
     */
    public static Collection<File> getClassPath() {
        Collection<File> classPath = new ArrayList<>();
        updateClasspath(ClassUtils.class.getClassLoader(), classPath);
        if (classPath.isEmpty()) updateClassPathFromSystemProperty(classPath);
        return classPath;
    }

    private static void updateClasspath(ClassLoader classLoader, Collection<File> classPath) {
        while (classLoader != null) {
            if (classLoader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) classLoader).getURLs();
                for (URL url : urls) {
                    File file = UriUtils.getFile(url);
                    if (file != null) classPath.add(file);
                }
            }
            classLoader = classLoader.getParent();
        }
    }

    private static void updateClassPathFromSystemProperty(Collection<File> classPath) {
        String classpath = System.getProperty("java.class.path");
        String[] entries = classpath.split(File.pathSeparator);
        for (String entry : entries) {
            if (entry.toLowerCase().endsWith(".jar")) {
                classPath.add(new File(entry));
            }
        }
    }

    private static String convertLambda(String className) {
        int lambdaIndex = className.indexOf("$$Lambda");
        return lambdaIndex != -1 ? className.substring(0, lambdaIndex) + "$Lambda" : className;
    }

    static {
        PRIMITIVE_TO_OBJECT_CLASSES.put(byte.class, Byte.class);
        PRIMITIVE_TO_OBJECT_CLASSES.put(char.class, Character.class);
        PRIMITIVE_TO_OBJECT_CLASSES.put(short.class, Short.class);
        PRIMITIVE_TO_OBJECT_CLASSES.put(int.class, Integer.class);
        PRIMITIVE_TO_OBJECT_CLASSES.put(long.class, Long.class);
        PRIMITIVE_TO_OBJECT_CLASSES.put(float.class, Float.class);
        PRIMITIVE_TO_OBJECT_CLASSES.put(double.class, Double.class);
        PRIMITIVE_TO_OBJECT_CLASSES.put(boolean.class, Boolean.class);

        OBJECT_TO_PRIMITIVE_CLASSES.put(Byte.class, byte.class);
        OBJECT_TO_PRIMITIVE_CLASSES.put(Character.class, char.class);
        OBJECT_TO_PRIMITIVE_CLASSES.put(Short.class, short.class);
        OBJECT_TO_PRIMITIVE_CLASSES.put(Integer.class, int.class);
        OBJECT_TO_PRIMITIVE_CLASSES.put(Long.class, long.class);
        OBJECT_TO_PRIMITIVE_CLASSES.put(Float.class, float.class);
        OBJECT_TO_PRIMITIVE_CLASSES.put(Double.class, double.class);
        OBJECT_TO_PRIMITIVE_CLASSES.put(Boolean.class, boolean.class);

        BASE_CLASSES.addAll(PRIMITIVE_TO_OBJECT_CLASSES.keySet());
        BASE_CLASSES.addAll(OBJECT_TO_PRIMITIVE_CLASSES.keySet());
        BASE_CLASSES.add(String.class);
        BASE_CLASSES.add(Date.class);
        BASE_CLASSES.add(java.sql.Date.class);
        BASE_CLASSES.add(java.sql.Time.class);
        BASE_CLASSES.add(java.sql.Timestamp.class);
        BASE_CLASSES.add(Instant.class);
        BASE_CLASSES.add(LocalDate.class);
        BASE_CLASSES.add(LocalTime.class);
        BASE_CLASSES.add(LocalDateTime.class);
        BASE_CLASSES.add(ZonedDateTime.class);
        BASE_CLASSES.add(OffsetDateTime.class);
        BASE_CLASSES.add(Duration.class);
        BASE_CLASSES.add(ZoneId.class);
        BASE_CLASSES.add(ZoneOffset.class);
        BASE_CLASSES.add(URL.class);
        BASE_CLASSES.add(URI.class);
    }
}
