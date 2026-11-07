package net.microfalx.lang;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A utility class which serializes and deserializes objects with <a href="https://github.com/EsotericSoftware/kryo">Kryo</a>.
 * <p>
 * Each payload holds the class of the value and its state, written with
 * {@link Kryo#writeClassAndObject(Output, Object)}. A payload can therefore be deserialized without knowing
 * its type in advance, and {@code null} values are supported.
 * <p>
 * Notes:
 * <ul>
 *     <li>Class registration is optional. An unregistered class is written by its fully qualified name.
 *     A registered class is written by its numeric ID, which gives smaller payloads.</li>
 *     <li>Classes without a custom serializer use {@link CompatibleFieldSerializer}. It tolerates fields
 *     being added or removed between versions of a class, so stored data stays readable after such changes.</li>
 *     <li>Registrations (classes, IDs and serializers) are global. The same registrations must be in place
 *     when data is read as when it was written. This matters most for explicit IDs: changing an ID makes
 *     existing data unreadable.</li>
 *     <li>Registrations should happen at application startup. A registration made later applies to Kryo
 *     instances the next time they are taken from the pool.</li>
 *     <li>References are not tracked. An object reachable more than once from the value is written each
 *     time and deserialized as separate copies, and an object graph with cycles cannot be serialized.</li>
 *     <li>All methods are thread-safe. Kryo instances are not thread-safe, so they are pooled and each
 *     operation borrows its own instance.</li>
 *     <li>Deserialization creates instances of any class named in the payload. Only deserialize data from
 *     a trusted source, such as a local store.</li>
 * </ul>
 */
public class Serde {

    private static final Logger LOGGER = LoggerFactory.getLogger(Serde.class);

    private static final Map<Class<?>, Serializer<?>> SERIALIZERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Integer> SERIALIZER_IDS = new ConcurrentHashMap<>();
    private static final AtomicInteger VERSION = new AtomicInteger();
    private static final String VERSION_KEY = Serde.class.getName() + ".version";

    private static final int DEFAULT_BUFFER_SIZE = 16 * 1024;

    /**
     * Registers a class with an explicit ID. Kryo's default serializer handles the class.
     * <p>
     * A registered class is written by its ID instead of its name. The ID must be unique, must not collide
     * with the IDs Kryo reserves for built-in types (0 to 9 in Kryo 5), and must stay the same for the
     * lifetime of the stored data.
     *
     * @param clazz the class to register
     * @param id    the ID that identifies the class in serialized data
     */
    public static void registerSerializer(Class<?> clazz, int id) {
        requireNonNull(clazz);
        SERIALIZER_IDS.put(clazz, id);
        VERSION.incrementAndGet();
        LOGGER.info("Registered serializer for class {} with id {}", clazz.getName(), id);
    }

    /**
     * Registers a custom serializer for a class. Kryo assigns the class ID.
     * <p>
     * Kryo assigns IDs in registration order. Use {@link #registerSerializer(Class, Serializer, Integer)}
     * with an explicit ID when serialized data must stay readable across application versions.
     *
     * @param clazz      the class to register
     * @param serializer the serializer used for instances of the class
     */
    public static void registerSerializer(Class<?> clazz, Serializer<?> serializer) {
        registerSerializer(clazz, serializer, null);
    }

    /**
     * Registers a custom serializer for a class, with an optional explicit ID.
     *
     * @param clazz      the class to register
     * @param serializer the serializer used for instances of the class
     * @param id         the ID that identifies the class in serialized data; {@code null} lets Kryo assign one
     * @see #registerSerializer(Class, int)
     */
    public static void registerSerializer(Class<?> clazz, Serializer<?> serializer, Integer id) {
        requireNonNull(clazz);
        requireNonNull(serializer);
        SERIALIZERS.put(clazz, serializer);
        if (id != null) {
            LOGGER.info("Registered serializer {} for class {} with id {}", ClassUtils.getName(serializer), clazz.getName(), id);
            SERIALIZER_IDS.put(clazz, id);
        } else {
            LOGGER.info("Registered serializer {} for class {}", ClassUtils.getName(serializer), clazz.getName());
        }
        VERSION.incrementAndGet();
    }

    /**
     * Obtains a Kryo instance from the pool. The instance is configured with the current registrations.
     * <p>
     * The instance needs to be returned to the pool with {@link #free(Kryo)} when it is no longer needed.
     *
     * @return a Kryo instance; never {@code null}
     */
    @SuppressWarnings("unchecked")
    public static Kryo obtain() {
        Kryo kryo = POOL.obtain();
        Object version = kryo.getContext().get(VERSION_KEY);
        if (version == null || (Integer) version != VERSION.get()) updateKryo(kryo);
        return kryo;
    }

    /**
     * Frees a Kryo instance back to the pool.
     *
     * @param kryo the Kryo instance to free; must not be {@code null}
     */
    public static void free(Kryo kryo) {
        requireNonNull(kryo);
        POOL.free(kryo);
    }

    /**
     * Serializes a value, including its class, into a byte array.
     *
     * @param value the value to serialize; can be {@code null}
     * @return the serialized bytes; never {@code null}, even for a {@code null} value
     * @throws com.esotericsoftware.kryo.KryoException if the value cannot be serialized
     */
    public static byte[] serialize(Object value) {
        if (value == null) return null;
        Kryo kryo = obtain();
        try (Output output = new Output(DEFAULT_BUFFER_SIZE, -1)) {
            kryo.writeClassAndObject(output, value);
            return output.toBytes();
        } finally {
            POOL.free(kryo);
        }
    }

    /**
     * Deserializes a value written by {@link #serialize(Object)}.
     *
     * @param bytes the serialized bytes
     * @return the deserialized value; {@code null} if a {@code null} value was serialized
     * @throws com.esotericsoftware.kryo.KryoException if the bytes are not a valid payload or reference
     *                                                 a class that cannot be found
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes) {
        if (bytes == null) return null;
        if (bytes.length == 0) throw new IllegalArgumentException("Cannot deserialize empty byte array");
        Kryo kryo = obtain();
        try (Input input = new Input(bytes)) {
            return (T) kryo.readClassAndObject(input);
        } finally {
            POOL.free(kryo);
        }
    }

    private static void updateKryo(Kryo kryo) {
        int version = VERSION.get();
        kryo.setRegistrationRequired(false);
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        for (Map.Entry<Class<?>, Integer> entry : SERIALIZER_IDS.entrySet()) {
            Class<?> clazz = entry.getKey();
            if (!SERIALIZERS.containsKey(clazz)) {
                // register(Class, int) keeps an existing (implicit) registration, so pass the serializer to replace it
                kryo.register(clazz, kryo.getDefaultSerializer(clazz), entry.getValue());
            }
        }
        for (Map.Entry<Class<?>, Serializer<?>> entry : SERIALIZERS.entrySet()) {
            Class<?> clazz = entry.getKey();
            Serializer<?> serializer = entry.getValue();
            Integer id = SERIALIZER_IDS.get(clazz);
            if (id != null) {
                kryo.register(clazz, serializer, id);
            } else {
                kryo.register(clazz, serializer);
            }
        }
        kryo.getContext().put(VERSION_KEY, version);
    }

    private static final Pool<Kryo> POOL = new Pool<>(true, false, 4) {

        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            updateKryo(kryo);
            return kryo;
        }

        @Override
        protected void reset(Kryo kryo) {
            kryo.reset();
        }
    };
}
