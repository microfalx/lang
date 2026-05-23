package net.microfalx.lang;

import java.util.StringTokenizer;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A class which provides support for <a href="https://semver.org/">SemVer2</a>.
 */
public class Version extends IdentityAware<String> implements Comparable<Version> {

    public static final int NO_VALUE = -1;
    public static final Version NO_VERSION = new Version("0.0.0");
    private static final char SEPARATOR = '.';
    private static final char PRE_RELEASE_SEPARATOR = '-';
    private static final char BUILD_NO_SEPARATOR = '+';
    private static final String SNAPSHOT = "-SNAPSHOT";

    private final String value;

    private int major = NO_VALUE;
    private int minor = NO_VALUE;
    private int patch = NO_VALUE;
    private int build = NO_VALUE;
    private int preRelease = NO_VALUE;
    private boolean snapshot;

    public static Version parse(String version) {
        return new Version(version);
    }

    Version(String value) {
        requireNotEmpty(value);
        this.value = value;
        setId(toIdentifier(value));
        parse();
    }

    /**
     * Returns the original version used to create this version.
     *
     * @return a non-null instance
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns whether the version indicates an unreleased product.
     *
     * @return {@code true} if snapshot, {@code false} otherwise
     */
    public boolean isSnapshot() {
        return snapshot;
    }

    /**
     * Returns the MAJOR component of the version
     *
     * @return a positive integer
     */
    public int getMajor() {
        return major;
    }

    /**
     * Returns the MINOR component of the version
     *
     * @return a positive integer
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Returns the PATCH component of the version
     *
     * @return a positive integer, {@link  #NO_VALUE} if not provided
     */
    public int getPatch() {
        return patch;
    }

    /**
     * Returns the BUILD component of the version
     *
     * @return a positive integer, {@link  #NO_VALUE} if not provided
     */
    public int getBuild() {
        return build;
    }

    /**
     * Returns a copy of this version with a different build number.
     *
     * @param build the new build number
     * @return a new instance
     */
    public Version withBuild(int build) {
        Version copy = (Version) copy();
        copy.build = build;
        return copy;
    }

    /**
     * Returns the PRE-RELEASE component of the version
     *
     * @return a positive integer, {@link  #NO_VALUE} if not provided
     */
    public int getPreRelease() {
        return preRelease;
    }

    @Override
    public int compareTo(Version o) {
        if (major != o.major) return Integer.compare(major, o.major);
        if (minor != o.minor) return Integer.compare(minor, o.minor);
        if (patch != o.patch) return Integer.compare(patch, o.patch);
        if (build != o.build) return Integer.compare(build, o.build);
        return 0;
    }

    private void parse() {
        snapshot = value.toUpperCase().contains(SNAPSHOT);
        String normalizedValue = StringUtils.replaceAll(value, SNAPSHOT, EMPTY_STRING);
        String[] parts = StringUtils.split(normalizedValue, ".");
        major = parseNumber(parts[0]);
        if (parts.length > 1) minor = parseNumber(parts[1]);
        if (parts.length > 2) {
            String patchString = parts[2];
            if (patchString.contains("-") || patchString.contains("+")) {
                StringTokenizer tokenizer = new StringTokenizer(patchString, "+-", true);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if (token.startsWith("-") && tokenizer.hasMoreTokens()) {
                        preRelease = parseNumber(tokenizer.nextToken());
                    } else if (token.startsWith("+") && tokenizer.hasMoreTokens()) {
                        build = parseNumber(tokenizer.nextToken());
                    } else {
                        patchString = token;
                    }
                }
            }
            patch = parseNumber(patchString);
        }
    }

    private int parseNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return NO_VALUE;
        }
    }

    public String toTag() {
        if (isSnapshot()) {
            return major + "." + minor + ".latest";
        } else {
            return toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(major).append(SEPARATOR).append(minor);
        if (patch != NO_VALUE) builder.append(SEPARATOR).append(patch);
        if (preRelease != NO_VALUE) builder.append(PRE_RELEASE_SEPARATOR).append(preRelease);
        if (build != NO_VALUE) builder.append(BUILD_NO_SEPARATOR).append(build);
        return builder.toString();
    }
}
