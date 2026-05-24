package net.microfalx.lang;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.StringUtils.*;

/**
 * Various utilities around URIs
 */
public class UriUtils {

    public static final String SLASH = "/";
    public static final String JAR_SCHEME = "jar";

    /**
     * Returns whether the URI is valid and can be parsed based on {@link URI} rules.
     *
     * @param uri the URI
     * @return {@code true} if valid, {@code false} otherwise
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isValidUri(String uri) {
        if (StringUtils.isEmpty(uri)) return false;
        try {
            return URI.create(uri) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parses a URI from a string.
     * <p>
     * The method detects invalid characters and replace them.
     *
     * @param uri the URI
     * @return the URI, NULL if the original string was NULL or EMPTY
     */
    public static URI parseUri(String uri) {
        if (StringUtils.isEmpty(uri)) {
            return null;
        } else {
            if (!isValidUri(uri)) uri = escapeUnsafe(uri);
            return URI.create(uri);
        }
    }

    /**
     * Parses a URL from a string.
     * <p>
     * The method detects invalid characters and replace them.
     *
     * @param url the URL
     * @return the URI, NULL if the original string was NULL or EMPTY
     */
    public static URL parseUrl(String url) {
        URI uri = parseUri(url);
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Returns whether the URI has the authority component.
     *
     * @param uri the URI
     * @return {@code true} if the authority is present, {@code false} otherwise
     */
    public static boolean hasAuthority(String uri) {
        requireNonNull(uri);
        return uri.contains("://");
    }

    /**
     * Appends an URI fragment to the existing URI.
     * <p>
     * If the URI already has a fragment, the fragment is replaced.
     *
     * @param uri      the URI
     * @param fragment the fragment
     * @return the URI with the fragment
     */
    public static URI appendFragment(URI uri, String fragment) {
        requireNonNull(uri);
        return appendFragment(uri.toASCIIString(), fragment);
    }

    /**
     * Appends a URI fragment to the existing URI.
     * <p>
     * If the URI already has a fragment, the fragment is replaced.
     *
     * @param uri      the URI
     * @param fragment the fragment
     * @return the URI with the fragment
     */
    public static URI appendFragment(String uri, String fragment) {
        requireNonNull(uri);
        int lastHash = uri.lastIndexOf('#');
        if (lastHash != -1) uri = uri.substring(0, lastHash);
        if (isNotEmpty(fragment)) uri += "#" + fragment;
        return parseUri(uri);
    }

    /**
     * Removes the fragment from a URI.
     *
     * @param uri the URI
     * @return a new URi without a fragment
     */
    public static URI removeFragment(URI uri) {
        if (uri == null) return null;
        try {
            if (isMultiScheme(uri)) {
                URI schemePartUri = removeFragment(URI.create(uri.getSchemeSpecificPart()));
                return new URI(uri.getScheme(), schemePartUri.toASCIIString(), null);
            } else {
                return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), null);
            }
        } catch (URISyntaxException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Returns whether the path points to the root of the domain (null, empty or "/").
     *
     * @param path the path
     * @return {@code true} if root, {@code false} otherwise
     */
    public static boolean isRoot(String path) {
        return StringUtils.isEmpty(path) || SLASH.equals(path);
    }

    /**
     * Returns whether the URI's path points to the root of the domain (null, empty or "/").
     *
     * @param uri the path
     * @return {@code true} if root, {@code false} otherwise
     */
    public static boolean isRoot(URI uri) {
        requireNonNull(uri);
        return isRoot(uri.getPath());
    }

    /**
     * Returns whether the URI identifies a JAR file.
     *
     * @param uri the URI
     * @return {@code true} if identifies a JAR file, {@code false} otherwise
     */
    public static boolean isJar(URI uri) {
        requireNonNull(uri);
        return JAR_SCHEME.equalsIgnoreCase(uri.getScheme());
    }

    /**
     * Replaces the host in the URI.
     *
     * @param uri      the URI
     * @param ipOrHost the new hostname
     * @return a non-null instance
     */
    public static URI replaceHost(URI uri, String ipOrHost) {
        requireNonNull(uri);
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), ipOrHost, uri.getPort(), uri.getPath(),
                    uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Returns the file from the URL.
     *
     * @param url the url
     * @return the file, null if no reference can be extracted
     */
    public static File getFile(URL url) {
        requireNonNull(url);
        String protocol = url.getProtocol();
        String path = url.getPath();
        if (protocol.equals("jar")) {
            try {
                url = new URL(path);
                path = url.getPath();
                int index = path.indexOf('!');
                if (index != -1) {
                    return new File(path.substring(0, index));
                } else {
                    return new File(path);
                }
            } catch (MalformedURLException e) {
                ExceptionUtils.rethrowException(e);
            }
        } else if (protocol.equals("file")) {
            return new File(path);
        }
        return null;
    }

    /**
     * Returns whether the URI contains a multi-scheme URI.
     *
     * @param uri the URI
     * @return {@code true} if multi-scheme URI, {@code false} otherwise
     */
    public static boolean isMultiScheme(URI uri) {
        String scheme = uri.getScheme();
        if (scheme != null) {
            try {
                URI schemePartUri = URI.create(uri.getSchemeSpecificPart());
                return isNotEmpty(scheme) && isNotEmpty(schemePartUri.getScheme());
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Joins the paths, creating ab absolute path out of all components.
     *
     * @param paths the paths
     * @return the absolute path
     */
    public static String joinPaths(String... paths) {
        if (paths == null) return SLASH;
        StringBuilder builder = new StringBuilder();
        builder.append('/');
        for (String path : paths) {
            builder.append(removeEndSlash(removeStartSlash(path))).append('/');
        }
        return removeEndSlash(builder.toString());
    }

    /**
     * Tries to parse the URI and in case of fixable issues it will correct the URI to be accepted by {@link URI}.
     * <p>
     * These correction are based on RFC2396 recommendations, and it will not fix {@code unfixable} problems.
     *
     * @param uri the URI
     * @return a fixed URI, null if the original URI was null
     */
    public static String escapeUnsafe(String uri) {
        if (uri == null) return null;
        for (; ; ) {
            try {
                return URI.create(uri).toASCIIString();
            } catch (Exception e) {
                URISyntaxException se = null;
                if (e instanceof IllegalArgumentException && e.getCause() instanceof URISyntaxException) {
                    se = (URISyntaxException) e.getCause();
                }
                if (se != null) {
                    int index = se.getIndex();
                    String escapedUri = escapeCharacterAtPosition(uri, index);
                    if (escapedUri == null) break;
                    uri = escapedUri;
                } else {
                    return rethrowExceptionAndReturn(e);
                }
            }
        }
        return uri;
    }


    /**
     * Returns the TLD (top level domain) from a hostname
     *
     * @param hostname the hostname
     * @return a non-null instance
     */
    public static String getTld(String hostname) {
        if (StringUtils.isEmpty(hostname)) return NA_ID_STRING;
        String[] parts = split(hostname, ".");
        if (parts.length > 2) parts = Arrays.copyOfRange(parts, 0, 2);
        return String.join(".", parts);
    }

    /**
     * Appends a URI path to the existing URI.
     * <p>
     *
     * @param uri  the URI
     * @param path the fragment
     * @return the URI with the fragment
     */
    public static URI appendPath(URI uri, String path) {
        requireNonNull(uri);
        requireNonNull(path);
        String uriPath = addEndSlash(uri.getPath()) + removeStartSlash(path);
        try {
            return parseUri(new URI(uri.getScheme(), uri.getAuthority(), uriPath, uri.getQuery(),
                    uri.getFragment()).toASCIIString());
        } catch (URISyntaxException e) {
            return rethrowExceptionAndReturn(e);
        }
    }


    /**
     * Appends a URI path to the existing URI.
     * <p>
     * If the URI already has a path, the path is replaced.
     *
     * @param uri  the URI
     * @param path the path
     * @return the URI with the fragment
     */
    public static URI appendPath(String uri, String path) {
        requireNonNull(uri);
        requireNonNull(path);
        return appendPath(URI.create(uri), path);
    }

    /**
     * Returns the string representation of the URI.
     *
     * @param uri the URI
     * @return the string representation, null if URI is null
     */
    public static String toString(URI uri) {
        return uri != null ? uri.toASCIIString() : null;
    }

    /**
     * Returns the URL representation of the URI.
     * <p>
     * Any exceptions thrown during the conversion will be wrapped in a runtime exception.
     *
     * @param uri the URI
     * @return the URL representation, null if URI is null
     */
    public static URL toUrl(URI uri) {
        if (uri == null) return null;
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            return rethrowExceptionAndReturn(e);
        }
    }


    private static String escapeCharacterAtPosition(String value, int index) {
        String result = value.substring(0, index);
        char c = value.charAt(index);
        if (!isUnsafeAndEscapable(c)) return null;
        result += "%" + Integer.toHexString(c);
        result += value.substring(index + 1);
        return result;
    }

    private static boolean isUnsafeAndEscapable(char c) {
        for (char unsafeUriChar : UNSAFE_URI_CHARS) {
            if (c == unsafeUriChar) return true;
        }
        return false;
    }

    public static final char[] UNSAFE_URI_CHARS = new char[]{' ', '<', '>', '{', '}', '[', ']', '|', '\\', '^'};
}
