package net.microfalx.lang;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IdGeneratorTest {

    private final Set<Long> ids = new ConcurrentSkipListSet<>();

    @Test
    void next() {
        for (int i = 0; i < 1000000; i++) {
            long next = IdGenerator.get().next();
            assertTrue(ids.add(next), "Failed after " + i + " iterations, duplicate id: " + next);
        }
    }

    @Test
    void rangeStart() {
        long rangeStart = IdGenerator.rangeStart(Instant.now());
        for (int i = 0; i < 1000000; i++) {
            long next = IdGenerator.get().next();
            assertTrue(next >= rangeStart);
        }
    }

}