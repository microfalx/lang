package net.microfalx.lang.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceLocatorTest {

    @BeforeEach
    void setup() {
        ServiceLocator.shutdown();
    }

    @Test
    void service1() {
        Test1Service instance = Test1Service.getInstance();
        assertNotNull(instance);
        assertTrue(ServiceLocator.isLoaded(Test1Service.class));
    }

    @Test
    void service2() {
        Test2Service instance = Test2Service.getInstance();
        assertNotNull(instance);
        assertTrue(ServiceLocator.isLoaded(Test2Service.class));
    }

    @Test
    void register() {
        ServiceLocator.register(new Test1ServiceImpl());
        ServiceLocator.register(new Test2ServiceImpl());
        assertTrue(ServiceLocator.isLoaded(Test1Service.class));
        assertTrue(ServiceLocator.isLoaded(Test2Service.class));
    }

}