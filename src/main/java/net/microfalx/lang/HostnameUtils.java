package net.microfalx.lang;

import inet.ipaddr.IPAddressString;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Various utilities around hostnames and IPs.
 */
public class HostnameUtils {

    /**
     * Returns whether the value represents an IP and not a hostname
     *
     * @param value the IP or hostname
     * @return {@code true} if IP, {@code false} otherwise
     */
    public static boolean isIp(String value) {
        requireNonNull(value);
        return new IPAddressString(value).isIPAddress();
    }

    /**
     * Returns whether the value represents a valid hostname (not an IP, not a loopback).
     *
     * @param value the IP or hostname
     * @return {@code true} if hostname, {@code false} otherwise
     */
    public static boolean isHostname(String value) {
        requireNonNull(value);
        IPAddressString ipAddressString = new IPAddressString(value);
        return !(ipAddressString.isIPAddress() || ipAddressString.isLoopback());
    }

    /**
     * Returns a friendly server name from a host name,
     * <p>
     * Usually a server has a server id + a number + domain. The local server id (server id + number)
     * can be used to generate a friendly name for a server.
     *
     * @param hostName the host name.
     * @return a non-null instance
     */
    public static String getServerNameFromHost(String hostName) {
        requireNonNull(hostName);
        // cannot translate an IP to a friendly name
        if (isIp(hostName)) return hostName;
        if (hostName.contains(".")) {
            hostName = hostName.substring(0, hostName.indexOf('.'));
        }
        try {
            int numberIndex = -1;
            for (int index = 0; index < hostName.length(); index++) {
                if (Character.isDigit(hostName.charAt(index))) {
                    numberIndex = index;
                    break;
                }
            }
            String name = hostName;
            String id = null;
            if (numberIndex > 0) {
                name = hostName.substring(0, numberIndex);
                id = hostName.substring(numberIndex);
            }
            name = name.replace('_', ' ').replace('-', ' ');
            name = StringUtils.capitalizeWords(name);
            return id == null ? name : name + " " + id;
        } catch (Exception e) {
            return hostName;
        }
    }
}
