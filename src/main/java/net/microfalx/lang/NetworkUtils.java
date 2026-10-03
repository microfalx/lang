package net.microfalx.lang;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Various utilities for network.
 */
public class NetworkUtils {

    private static InetAddress anyAddress;
    private static final Pattern IP_PATTERN = Pattern.compile("((^\\s*((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))\\s*$)|(^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$))", Pattern.CASE_INSENSITIVE);

    /**
     * Returns an network address which means "any" (interface) - basically "0.0.0.0".
     *
     * @return a non-null instance
     */
    public static InetAddress getAnyAddress() {
        return anyAddress;
    }

    /**
     * Returns whether the value matches an IP and not a hostname.
     *
     * @param value the value
     * @return {@code true} if IP, {@code false} otherwise
     */
    public static boolean isIP(String value) {
        if (StringUtils.isEmpty(value)) return false;
        return IP_PATTERN.matcher(value).matches();
    }

    /**
     * Returns whether the host/IP is actually "local"
     *
     * @param hostOrIp host or IP
     * @return {@code true} if local, {@code false} otherwise
     */
    public static boolean isLocalHost(String hostOrIp) {
        return "localhost".equalsIgnoreCase(hostOrIp) || "127.0.0.1".equals(hostOrIp) || "::1".equals(hostOrIp) || "0:0:0:0:0:0:0:1".equals(hostOrIp);
    }

    /**
     * Returns whether the host/IP belongs to a local network.
     *
     * @param hostOrIp the host or IP
     * @return {@code true} if local network, {@code false} otherwise
     */
    public static boolean isLocalNetwork(String hostOrIp) {
        if (StringUtils.isEmpty(hostOrIp)) return true;
        try {
            InetAddress address = InetAddress.getByName(hostOrIp);
            return address.isLoopbackAddress() || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress();

        } catch (UnknownHostException e) {
            // Invalid IP format
            return false;
        }
    }


    /**
     * Returns the domain from the hostname.
     * <p>
     * If the host name is an IP, it returns the IP.
     *
     * @param hostOrIp the hostname or IP
     * @return the domain name
     */
    public static String getDomainName(String hostOrIp) {
        if (isIP(hostOrIp)) {
            return hostOrIp;
        } else {
            int position = hostOrIp.indexOf('.');
            if (position == -1) {
                return hostOrIp;
            } else {
                return hostOrIp.substring(position + 1);
            }
        }
    }
}
