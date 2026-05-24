package net.microfalx.lang;

import org.apache.commons.lang3.math.Fraction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilsTest {

    @Test
    void isBaseClassWithObject() {
        assertTrue(ClassUtils.isBaseClass(Integer.valueOf("80")));
    }

    @Test
    void isBaseClassWithClass() {
        assertTrue(ClassUtils.isBaseClass(Boolean.class));
    }

    @Test
    void getNameWithClass() {
        assertEquals("java.lang.Number", ClassUtils.getName(Number.class));
        assertEquals("", ClassUtils.getName(null));
    }

    @Test
    void getNameWithClassAndDefaultValue() {
        assertEquals("java.lang.Number", ClassUtils.getName(Number.class, "The class is null"));
        assertEquals("The class is null", ClassUtils.getName(null, "The class is null"));
    }

    @Test
    void getNameWithObject() {
        assertEquals("java.lang.Integer", ClassUtils.getName(Integer.valueOf("80")));
        assertEquals("", ClassUtils.getName(null));
    }

    @Test
    void getNameWithObjectAndDefaultValue() {
        assertEquals("java.lang.Integer", ClassUtils.getName(Integer.valueOf("80"), "The class is null"));
        assertEquals("The class is null", ClassUtils.getName(null, "The class is null"));
    }


    @Test
    void getSimpleName() {
        assertEquals("java.lang.Integer", ClassUtils.getSimpleName(Integer.valueOf("80")));
        assertEquals("", ClassUtils.getSimpleName((Object) null));

    }

    @Test
    void getSimpleNameWithDefaultValue() {
        assertEquals("java.lang.Integer", ClassUtils.getSimpleName(Integer.valueOf("80"), "The class is null"));
        assertEquals("The class is null", ClassUtils.getSimpleName((Object) null, "The class is null"));
    }

    @Test
    void getSimpleNameWithString() {
        assertEquals("I am writing java code", ClassUtils.getSimpleName("I am writing java code"));
        assertEquals("", ClassUtils.getSimpleName((Object) null));
    }


    @Test
    void isSubClassOfWithObject() {
        assertFalse(ClassUtils.isSubClassOf((Object) null, Number.class));
        assertTrue(ClassUtils.isSubClassOf(Integer.valueOf("80"), Number.class));
    }

    @Test
    void isSubClassOfWithClass() {
        assertTrue(ClassUtils.isSubClassOf(Integer.class, Number.class));
    }

    @Test
    void isJdkClassWithObject() {
        assertTrue(ClassUtils.isJdkClass(Integer.valueOf("80")));
    }

    @Test
    void testIsJdkClassWithClass() {
        assertTrue(ClassUtils.isJdkClass(Integer.class));
    }

    @Test
    void canInstantiate() {
        assertFalse(ClassUtils.canInstantiate(Number.class));
        assertFalse(ClassUtils.canInstantiate(null));
    }

    @Test
    void resolveProviders() {
        assertIterableEquals(Collections.EMPTY_LIST, ClassUtils.resolveProviders(Number.class));
    }

    @Test
    void resolveProviderInstances() {
        assertIterableEquals(Collections.EMPTY_LIST, ClassUtils.resolveProviderInstances(Number.class));
    }

    @Test
    void clearCaches() {
    }

    @Test
    void getClassParametrizedTypes() {
        assertIterableEquals(Collections.EMPTY_LIST, ClassUtils.getClassParametrizedTypes(Fraction.class));
    }

    @Test
    void getClassParametrizedTypeWithIndex() {
        assertEquals(null, ClassUtils.getClassParametrizedType(Fraction.class, 0));

    }

    @Test
    void getClassParametrizedTypeWithClass() {
        assertEquals(null, ClassUtils.getClassParametrizedType(Fraction.class, Number.class));

    }

    @Test
    void create() {
        assertEquals("", ClassUtils.create(String.class));
        assertEquals(1, ClassUtils.create(PublicClass.class).getI());
        assertEquals(1, ClassUtils.create(ProtectedClass.class).getI());
        assertThrows(Exception.class, () -> ClassUtils.create(Number.class));
    }

    @Test
    void getCompactName() {
        assertEquals("j.l.Integer", ClassUtils.getCompactName(Integer.class));
    }

    @Test
    void getClassPath() {
        Assertions.assertThat(ClassUtils.getClassPath().size()).isGreaterThan(10);
    }

    public static class PublicClass {

        private int i = 1;

        PublicClass() {
        }

        public int getI() {
            return i;
        }

        public PublicClass setI(int i) {
            this.i = i;
            return this;
        }
    }

    static class ProtectedClass {

        private int i = 1;

        ProtectedClass() {
        }

        public int getI() {
            return i;
        }

        public ProtectedClass setI(int i) {
            this.i = i;
            return this;
        }
    }
}