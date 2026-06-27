package net.microfalx.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionTest {

    @Test
    void parseSimple() {
        assertEquals("1.0", Version.parse("1.0").toString());
        assertEquals("1.0", Version.parse("1.0").toTag());
        assertEquals("1.0-beta", Version.parse("1.0-SNAPSHOT").toString());
        assertEquals("1.0.latest", Version.parse("1.0-SNAPSHOT").toTag());
        assertEquals("1.0.0", Version.parse("1.0.0").toString());
        assertEquals("1.0.0-beta", Version.parse("1.0.0-SNAPSHOT").toString());
        assertEquals("1.0.0", Version.parse("1.0.0").toTag());
        assertEquals("1.0.latest", Version.parse("1.0.0-SNAPSHOT").toTag());
        assertEquals("1.0.1", Version.parse("1.0.1").toString());
        assertEquals("1.0.1", Version.parse("1.0.1").toTag());
        assertEquals("2.3.11", Version.parse("2.3.11").toString());
        assertEquals("2.3.11", Version.parse("2.3.11").toTag());
    }

    @Test
    void parseFileName() {
        assertEquals("1.0", Version.parseFileName("myapp-1.0.jar").toString());
        assertEquals("1.0-beta", Version.parseFileName("myapp-1.0-SNAPSHOT.jar").toString());
        assertEquals("1.0.0", Version.parseFileName("myapp-1.0.0.jar").toString());
        assertEquals("1.0.0-beta", Version.parseFileName("myapp-1.0.0-SNAPSHOT.jar").toString());
        assertEquals("3.5.3", Version.parseFileName("antlr@antlr-runtime-3.5.3.jar").toString());
        assertEquals("9.6", Version.parseFileName("ow2@asm-9.6.jar").toString());
    }

    @Test
    void parseBuild() {
        assertEquals("1.0.0+10", Version.parse("1.0.0+10").toString());
    }

    @Test
    void parsePreRelease() {
        assertEquals("1.0.0-2", Version.parse("1.0.0-2").toString());
    }

    @Test
    void withMajor() {
        assertEquals(2, Version.parse("1.0.0").withMajor(2).getMajor());
    }

    @Test
    void withMinor() {
        assertEquals(3, Version.parse("1.0.0").withMinor(3).getMinor());
    }

    @Test
    void withPatch() {
        assertEquals(7, Version.parse("1.0.0").withPatch(7).getPatch());
    }

    @Test
    void withBuild() {
        assertEquals("1.0.0+123", Version.parse("1.0.0").withBuild(123).toString());
        assertEquals(123, Version.parse("1.0.0").withBuild(123).getBuild());
    }

    @Test
    void compareTo() {
        assertEquals(0, Version.parse("1.0").compareTo(Version.parse("1.0")));
        assertEquals(-1, Version.parse("1.0").compareTo(Version.parse("1.1")));
        assertEquals(1, Version.parse("1.1").compareTo(Version.parse("1.0")));
        assertEquals(0, Version.parse("1.0.0").compareTo(Version.parse("1.0.0")));
        assertEquals(-1, Version.parse("1.0.0").compareTo(Version.parse("1.0.1")));
        assertEquals(1, Version.parse("1.0.1").compareTo(Version.parse("1.0.0")));
        assertEquals(-1, Version.parse("1.0.0+10").compareTo(Version.parse("1.0.0+20")));
        assertEquals(1, Version.parse("1.0.0+20").compareTo(Version.parse("1.0.0+10")));
    }

    @Test
    void generateToMaven() {
        assertEquals("1.0", Version.of(1, 0).toMaven());
        assertEquals("1.0.1", Version.of(1, 0, 1).toMaven());
        assertEquals("1.0.2-SNAPSHOT", Version.of(1, 0, 2).withSnapshot(true).toMaven());
        assertEquals("1.0.2-SNAPSHOT+17", Version.of(1, 0, 2).withBuild(17).withSnapshot(true).toMaven());
    }

    @Test
    void generateToString() {
        assertEquals("1.0", Version.of(1, 0).toString());
        assertEquals("1.0.1", Version.of(1, 0, 1).toString());
        assertEquals("1.0.2-beta", Version.of(1, 0, 2).withSnapshot(true).toString());
        assertEquals("1.0.2-beta+17", Version.of(1, 0, 2).withBuild(17).withSnapshot(true).toString());
    }

    @Test
    void generateToTag() {
        assertEquals("1.0", Version.of(1, 0).toTag());
        assertEquals("1.0.1", Version.of(1, 0, 1).toTag());
        assertEquals("1.0.latest", Version.of(1, 0, 2).withSnapshot(true).toTag());
        assertEquals("1.0.latest", Version.of(1, 0, 2).withBuild(17).withSnapshot(true).toTag());
    }

}