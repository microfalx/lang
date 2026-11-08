package net.microfalx.lang;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

class SerdeTest {

    @Test
    void serializeAndDeserializeObject() {
        TestItem item = new TestItem("1", "John", "Doe", 30);

        byte[] bytes = Serde.serialize(item);
        TestItem deserialized = (TestItem) Serde.deserialize(bytes);

        Assertions.assertNotNull(bytes);
        Assertions.assertNotSame(item, deserialized);
        Assertions.assertEquals(item, deserialized);
    }

    @Test
    void serializeAndDeserializeNull() {
        byte[] bytes = Serde.serialize(null);

        Assertions.assertNull(bytes);
        Assertions.assertNull(Serde.deserialize(bytes));
    }

    @Test
    void serializeAndDeserializeBuiltInTypes() {
        Assertions.assertEquals("text", Serde.deserialize(Serde.serialize("text")));
        Assertions.assertEquals("", Serde.deserialize(Serde.serialize("")));
        Assertions.assertEquals(42L, (long) Serde.deserialize(Serde.serialize(42L)));
        Assertions.assertEquals(Long.MAX_VALUE, (long) Serde.deserialize(Serde.serialize(Long.MAX_VALUE)));
        Assertions.assertEquals(3.14d, Serde.deserialize(Serde.serialize(3.14d)));
        Assertions.assertEquals(Boolean.TRUE, Serde.deserialize(Serde.serialize(true)));
        Assertions.assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) Serde.deserialize(Serde.serialize(new byte[]{1, 2, 3})));
    }

    @Test
    void serializeAndDeserializeCollections() {
        List<TestItem> list = new ArrayList<>();
        list.add(new TestItem("1", "John", "Doe", 30));
        list.add(new TestItem("2", "Jane", "Doe", 25));
        Map<String, Object> map = new HashMap<>();
        map.put("items", list);
        map.put("count", 2);
        map.put("empty", null);

        Object deserialized = Serde.deserialize(Serde.serialize(map));

        Assertions.assertInstanceOf(HashMap.class, deserialized);
        Assertions.assertEquals(map, deserialized);
    }

    @Test
    void serializeDoesNotTrackReferences() {
        TestItem item = new TestItem("1", "John", "Doe", 30);
        List<TestItem> list = new ArrayList<>(List.of(item, item));

        @SuppressWarnings("unchecked")
        List<TestItem> deserialized = (List<TestItem>) Serde.deserialize(Serde.serialize(list));

        Assertions.assertEquals(2, deserialized.size());
        Assertions.assertEquals(deserialized.get(0), deserialized.get(1));
        Assertions.assertNotSame(deserialized.get(0), deserialized.get(1));
    }

    @Test
    void unregisteredClassIsWrittenByName() {
        byte[] bytes = Serde.serialize(new UnregisteredItem("a"));

        Assertions.assertTrue(containsClassName(bytes, UnregisteredItem.class));
        Assertions.assertEquals(new UnregisteredItem("a"), Serde.deserialize(bytes));
    }

    @Test
    void registerWithIdWritesIdInsteadOfName() {
        IdItem item = new IdItem("a");
        byte[] before = Serde.serialize(item);

        Serde.registerSerializer(IdItem.class, 1001);
        byte[] after = Serde.serialize(item);

        Assertions.assertTrue(containsClassName(before, IdItem.class));
        Assertions.assertFalse(containsClassName(after, IdItem.class));
        Assertions.assertTrue(after.length < before.length);
        Assertions.assertEquals(item, Serde.deserialize(after));
    }

    @Test
    void registerCustomSerializer() {
        PointSerializer serializer = new PointSerializer();
        Serde.registerSerializer(Point.class, serializer);

        Point deserialized = (Point) Serde.deserialize(Serde.serialize(new Point(3, 4)));

        Assertions.assertEquals(new Point(3, 4), deserialized);
        Assertions.assertEquals(1, serializer.writes.get());
        Assertions.assertEquals(1, serializer.reads.get());
    }

    @Test
    void registerCustomSerializerWithId() {
        Point3DSerializer serializer = new Point3DSerializer();
        Serde.registerSerializer(Point3D.class, serializer, 1002);

        byte[] bytes = Serde.serialize(new Point3D(1, 2, 3));

        Assertions.assertFalse(containsClassName(bytes, Point3D.class));
        Assertions.assertEquals(new Point3D(1, 2, 3), Serde.deserialize(bytes));
        Assertions.assertEquals(1, serializer.writes.get());
    }

    @Test
    void registerRejectsNullArguments() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Serde.registerSerializer(null, 1003));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Serde.registerSerializer(null, new PointSerializer()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Serde.registerSerializer(Point.class, null, 1004));
    }

    @Test
    void deserializeRejectsNull() {
        Assertions.assertNull(Serde.deserialize((byte[])null));
    }

    @Test
    void deserializeInvalidBytes() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Serde.deserialize(new byte[0]));
        Assertions.assertThrows(KryoException.class, () -> Serde.deserialize(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
    }

    @Test
    void serializeFromMultipleThreads() throws Exception {
        int threads = 8;
        int iterations = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                int thread = t;
                futures.add(executor.submit(() -> {
                    for (int i = 0; i < iterations; i++) {
                        TestItem item = new TestItem(thread + ":" + i, "John", "Doe", i);
                        Assertions.assertEquals(item, Serde.deserialize(Serde.serialize(item)));
                    }
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static boolean containsClassName(byte[] bytes, Class<?> clazz) {
        // Kryo flags the last character of an ASCII string with the high bit, so match all but the last one
        String name = clazz.getName();
        return new String(bytes, StandardCharsets.ISO_8859_1).contains(name.substring(0, name.length() - 1));
    }

    record UnregisteredItem(String value) {
    }

    record IdItem(String value) {
    }

    record Point(int x, int y) {
    }

    record Point3D(int x, int y, int z) {
    }

    static class PointSerializer extends Serializer<Point> {

        private final AtomicInteger writes = new AtomicInteger();
        private final AtomicInteger reads = new AtomicInteger();

        @Override
        public void write(Kryo kryo, Output output, Point point) {
            writes.incrementAndGet();
            output.writeInt(point.x());
            output.writeInt(point.y());
        }

        @Override
        public Point read(Kryo kryo, Input input, Class<? extends Point> type) {
            reads.incrementAndGet();
            return new Point(input.readInt(), input.readInt());
        }
    }

    static class Point3DSerializer extends Serializer<Point3D> {

        private final AtomicInteger writes = new AtomicInteger();

        @Override
        public void write(Kryo kryo, Output output, Point3D point) {
            writes.incrementAndGet();
            output.writeInt(point.x());
            output.writeInt(point.y());
            output.writeInt(point.z());
        }

        @Override
        public Point3D read(Kryo kryo, Input input, Class<? extends Point3D> type) {
            return new Point3D(input.readInt(), input.readInt(), input.readInt());
        }
    }
}
