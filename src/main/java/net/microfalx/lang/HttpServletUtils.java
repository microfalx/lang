package net.microfalx.lang;

import jakarta.servlet.http.HttpServletRequest;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;

public class HttpServletUtils {

    private static final String HTTP_HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HTTP_HEADER_X_HEALTH_CHECK = "X-Health-Check";
    private static final int DEFAULT_ERROR_THRESHOLD = 3;

    /**
     * Returns the client IP address from the request, taking into account the
     * "X-Forwarded-For" header if present.
     *
     * @param request the HTTP servlet request
     * @return a non-null string containing the client IP address
     */
    public static String getClientIp(HttpServletRequest request) {
        requireNonNull(request);
        String forwardedHost = request.getHeader(HTTP_HEADER_X_FORWARDED_FOR);
        if (isEmpty(forwardedHost)) {
            forwardedHost = request.getRemoteAddr();
        } else {
            forwardedHost = StringUtils.split(forwardedHost, ",")[0];
        }
        return forwardedHost;
    }

    /**
     * Returns whether the client is local (i.e., from a local network).
     *
     * @param request the HTTP servlet request
     * @return {@code true} if the client is local, {@code false} otherwise
     */
    public static boolean isClientLocal(HttpServletRequest request) {
        String clientIp = HttpServletUtils.getClientIp(request);
        return NetworkUtils.isLocalNetwork(clientIp);
    }
}
