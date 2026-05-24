package net.microfalx.lang;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class UriUtilsTest {

    @Test
    void isValidUri() {
        assertFalse(UriUtils.isValidUri(""));
        assertTrue(UriUtils.isValidUri("https://www.google.com/"));
        assertFalse(UriUtils.isValidUri("https://www.google.com/>test"));
    }

    @Test
    void parseUri() {
        assertEquals("https", UriUtils.parseUri("https://www.google.com/").getScheme());
        assertNull(UriUtils.parseUri(""));
        assertNull(UriUtils.parseUri("https://www.google.com/").getQuery());
        assertEquals("www.google.com", UriUtils.parseUri("https://www.google.com/").getHost());
        assertEquals("www.google.com", UriUtils.parseUri("https://www.google.com/").getAuthority());
        assertEquals(-1, UriUtils.parseUri("https://www.google.com/").getPort());
        assertNull(UriUtils.parseUri("https://www.google.com/").getRawUserInfo());
        assertEquals("https://www.google.com/", UriUtils.parseUri("https://www.google.com/").toASCIIString());
    }

    @Test
    void parseUrl() {
        assertEquals(443, UriUtils.parseUrl("https://www.google.com/").getDefaultPort());
        assertNull(UriUtils.parseUrl("https://www.google.com/").getQuery());
        assertEquals("www.google.com", UriUtils.parseUrl("https://www.google.com/").getHost());
        assertEquals("www.google.com", UriUtils.parseUrl("https://www.google.com/").getAuthority());
        assertEquals(-1, UriUtils.parseUrl("https://www.google.com/").getPort());
        assertNull(UriUtils.parseUrl("https://www.google.com/").getUserInfo());
        assertEquals("https://www.google.com/", UriUtils.parseUrl("https://www.google.com/").toExternalForm());
        assertThrows(MalformedURLException.class, () -> UriUtils.parseUrl("htp:/invalid-url"));
    }

    @Test
    void appendFragmentWithUri() {
        assertEquals(URI.create("https://www.example.com/page#section2"),
                UriUtils.appendFragment(URI.create("https://www.example.com/page"), "section2"));
    }

    @Test
    void appendFragmentWithString() {
        assertEquals(URI.create("https://www.example.com/page#section2"),
                UriUtils.appendFragment("https://www.example.com/page", "section2"));
    }

    @Test
    void removeFragment() {
        URI uriWithFragment = UriUtils.appendFragment("https://www.example.com/page", "section2");
        assertEquals(URI.create("https://www.example.com/page"), UriUtils.removeFragment(uriWithFragment));
        assertNull(UriUtils.removeFragment(null));
        assertEquals("jar:file:/tmp/test.jar!/bin/bash", UriUtils.removeFragment(URI.create("jar:file:/tmp/test.jar!/bin/bash")).toASCIIString());
        assertEquals("jar:file:/tmp/test.jar!/bin/bash", UriUtils.removeFragment(URI.create("jar:file:/tmp/test.jar!/bin/bash#hash")).toASCIIString());
    }

    @Test
    void isRootWithString() {
        assertTrue(UriUtils.isRoot(""));
        assertFalse(UriUtils.isRoot(URI.create("https://www.example.com/page").getPath()));
        assertTrue(UriUtils.isRoot(URI.create("https://www.example.com/").getPath()));
    }

    @Test
    void isRootWithUri() {
        assertFalse(UriUtils.isRoot(URI.create("https://www.example.com/page")));
        assertTrue(UriUtils.isRoot(URI.create("https://www.example.com/")));
    }

    @Test
    void joinPaths() {
        assertEquals("/", UriUtils.joinPaths(null));
        assertEquals("/page/data/id", UriUtils.joinPaths("/page", "/data", "/id"));
    }

    @Test
    void escapeUnsafe() {
        assertNull(UriUtils.escapeUnsafe(null));
        assertEquals("http://example.com/%20test", UriUtils.escapeUnsafe("http://example.com/ test"));
        assertEquals("http://example.com/%3etest", UriUtils.escapeUnsafe("http://example.com/>test"));
    }

    @Test
    void appendPath() throws URISyntaxException {
        URI uri = URI.create("https://www.example.com/page");
        assertEquals("https://www.example.com/page/test.txt",
                UriUtils.appendPath(uri, "/test.txt").toASCIIString());
        assertEquals("https://www.example.com/page/test.txt",
                UriUtils.appendPath(uri.toASCIIString(), "/test.txt").toASCIIString());
    }

    @Test
    void isJar() {
        // jar:file:/home/ady/.m2/repository/net/microfalx/maven/maven-boot/1.0.0-SNAPSHOT/maven-boot-1.0.0-SNAPSHOT.jar!/boot/bin
        assertFalse(UriUtils.isJar(URI.create("file:/tmp")));
        assertFalse(UriUtils.isJar(URI.create("http://localhost")));
        assertTrue(UriUtils.isJar(URI.create("jar:file:/tmp/test.jar!/bin/bash")));
    }

    @Test
    void getTld() {
        assertEquals("na", UriUtils.getTld(null));
        assertEquals("na", UriUtils.getTld(""));
        assertEquals("net", UriUtils.getTld("net"));
        assertEquals("net.microfalx", UriUtils.getTld("net.microfalx"));
        assertEquals("net.microfalx", UriUtils.getTld("net.microfalx.lang"));
    }

    @Test
    void getFileWithJarProtocol() throws MalformedURLException {
        java.net.URL jarUrl = new java.net.URL("jar:file:/home/user/.m2/repository/test.jar!/bin/bash");
        java.io.File result = UriUtils.getFile(jarUrl);
        assertNotNull(result);
        assertEquals("/home/user/.m2/repository/test.jar", result.getPath().replace('\\', '/'));
    }

    @Test
    void getFileWithJarProtocolAndMultipleSeparators() throws MalformedURLException {
        java.net.URL jarUrl = new java.net.URL("jar:file:/path/to/archive.jar!/dir/file.class");
        java.io.File result = UriUtils.getFile(jarUrl);
        assertNotNull(result);
        assertEquals("/path/to/archive.jar", result.getPath().replace('\\', '/'));
    }

    @Test
    void getFileWithFileProtocol() throws MalformedURLException {
        java.net.URL fileUrl = new java.net.URL("file:/home/user/document.txt");
        java.io.File result = UriUtils.getFile(fileUrl);
        assertNotNull(result);
        assertEquals("/home/user/document.txt", result.getPath().replace('\\', '/'));
        fileUrl = new java.net.URL("file:///home/user/document.txt");
        result = UriUtils.getFile(fileUrl);
        assertNotNull(result);
        assertEquals("/home/user/document.txt", result.getPath().replace('\\', '/'));
    }

    @Test
    void getFileWithHttpProtocol() throws MalformedURLException {
        java.net.URL httpUrl = new java.net.URL("http://example.com/path/to/resource");
        java.io.File result = UriUtils.getFile(httpUrl);
        assertNull(result);
        java.net.URL httpsUrl = new java.net.URL("https://example.com/path/to/resource");
        result = UriUtils.getFile(httpsUrl);
        assertNull(result);
    }

}