package net.microfalx.lang;

import java.time.ZoneId;

/**
 * A collection of utilities around zones.
 */
public class ZoneContext {

    public static final String UTC_ZONE = "UTC_ZONE";
    public static final String SERVER_ZONE = "SERVER_ZONE";
    public static final String BROWSER_ZONE = "BROWSER_ZONE";

    public static final ThreadLocal<ZoneId> BROWSER_ZONE_THREAD = ThreadLocal.withInitial(() -> ZoneId.systemDefault());

    /**
     * Resolves a zone by its identifier.
     * <p>
     * The method also handles special cases.
     *
     * @param zoneId the zone identifier
     * @return a non-null instance
     */
    public static ZoneId resolve(String zoneId) {
        switch (zoneId) {
            case UTC_ZONE:
                return ZoneId.of("UTC");
            case SERVER_ZONE:
                return ZoneId.systemDefault();
            case BROWSER_ZONE:
                return BROWSER_ZONE_THREAD.get();
            default:
                return ZoneId.of(zoneId);
        }
    }

}
