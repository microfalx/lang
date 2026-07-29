package net.microfalx.lang.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceFactoryTest {

    @BeforeEach
    void setup() {
        ServiceFactory.shutdown();
    }

    @Test
    void service1() {
        Test1Service instance = Test1Service.getInstance();
        assertNotNull(instance);
        assertTrue(ServiceFactory.isLoaded(Test1Service.class));
    }

    @Test
    void service2() {
        Test2Service instance = Test2Service.getInstance();
        assertNotNull(instance);
        assertTrue(ServiceFactory.isLoaded(Test2Service.class));
    }

    @Test
    void register() {
        ServiceFactory.register(new Test1ServiceImpl());
        ServiceFactory.register(new Test2ServiceImpl());
        assertTrue(ServiceFactory.isLoaded(Test1Service.class));
        assertTrue(ServiceFactory.isLoaded(Test2Service.class));
    }

}