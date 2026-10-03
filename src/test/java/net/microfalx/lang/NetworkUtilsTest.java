package net.microfalx.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkUtilsTest {

    @Test
    void getAnyAddress() {
        assertEquals("0.0.0.0", NetworkUtils.getAnyAddress().getHostAddress());
        assertEquals("0.0.0.0", NetworkUtils.getAnyAddress().getHostName());
    }

    @Test
    void isIP() {
        assertTrue(NetworkUtils.isIP("127.0.0.1"));
        assertFalse(NetworkUtils.isIP("google.com"));
    }

    @Test
    void isLocalHost() {
        assertTrue(NetworkUtils.isLocalHost("127.0.0.1"));
        assertFalse(NetworkUtils.isLocalHost("google.com"));
    }

    @Test
    void getDomainName() {
        assertEquals("127.0.0.1", NetworkUtils.getDomainName("127.0.0.1"));
        assertEquals("10.0.0.1", NetworkUtils.getDomainName("10.0.0.1"));
        assertEquals("google.com", NetworkUtils.getDomainName("maps.google.com"));
    }

}