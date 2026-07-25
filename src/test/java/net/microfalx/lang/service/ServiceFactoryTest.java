package net.microfalx.lang.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceFactoryTest {

    @Test
    public void service1() {
        Test1Service instance = Test1Service.getInstance();
        assertNotNull(instance);
    }

    @Test
    public void service2() {
        Test2Service instance = Test2Service.getInstance();
        assertNotNull(instance);
    }

}