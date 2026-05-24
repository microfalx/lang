package net.microfalx.lang;

import com.google.common.base.MoreObjects;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.FileUtils.validateFileExists;
import static net.microfalx.lang.StringUtils.*;

/**
 * Various utilities around JVM.
 */
public class JvmUtils {

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);
    public static final String OS_ARCH = System.getProperty("os.arch").toLowerCase(Locale.US);
    public static final String OS_VERSION = System.getProperty("os.version").toLowerCase(Locale.US);
    public static final String PATH_SEP = File.pathSeparator;

    public static final String STORE_NAME = "microfalx";
    public static final String CACHE_DIRECTORY_NAME = ".cache";
    private static volatile String PROJECT_NAME;

    public static final LocalDateTime STARTUP_TIME = LocalDateTime.now();

    public static final int UNAVAILABLE_PORT = -1;

    private static volatile File homeDirectory;
    private static volatile File varDirectory;
    private static volatile File tmpDirectory;
    private static volatile File logsDirectory;
    private static boolean logsDirectoryExist;
    private static volatile File cacheDirectory;
    private static volatile File workingDirectory;
    private static volatile File nativeDirectory;

    private static volatile Boolean homeWritable;

    private static volatile InetAddress localhost;
    private static volatile SoftReference<Collection<Jar>> CACHED_JARS = new SoftReference<>(null);

    /**
     * Returns the local of this JVM.
     *
     * @return a non-null instance
     */
    public static InetAddress getLocalHost() {
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            try {
                return InetAddress.getByName("localhost");
            } catch (UnknownHostException ex) {
                throw new IllegalStateException("Cannot extract the machine host", ex);
            }
        }
        return localhost;
    }


    /**
     * Returns whether the operating system is Microsoft Windows.
     *
     * @return {@code true} if Windows, {@code false} otherwise
     */
    public static boolean isWindows() {
        return OS_NAME.toLowerCase().contains("windows");
    }

    /**
     * Returns whether the operating system is Linux.
     *
     * @return {@code true} if Linux, {@code false} otherwise
     */
    public static boolean isLinux() {
        return !isWindows();
    }

    /**
     * Returns whether the C2 compiler is disabled (runs in client mode).
     *
     * @return {@code true} if the C2 compiler is disabled, {@code false} otherwise
     */
    public static boolean isClient() {
        String vmInfo = System.getProperty("java.vm.info", EMPTY_STRING);
        return vmInfo.contains("emulated-client");
    }

    /**
     * Returns the OS user's name which owns the JVM process.
     *
     * @return a non-null instance
     */
    public static String getUserName() {
        String userName = System.getProperty("user.name");
        return StringUtils.isNotEmpty(userName) ? userName : "na";
    }

    /**
     * Returns whether the user's home directory is writable.
     *
     * @return {@code true} if writable, {@code false} otherwise
     */
    public static boolean isHomeWritable() {
        if (homeWritable == null) homeWritable = FileUtils.isDirectoryWritable(getHomeDirectory());
        return homeWritable;
    }

    /**
     * Returns the user's home directory.
     *
     * @return a non-null instance
     */
    public static File getHomeDirectory() {
        if (homeDirectory != null) return homeDirectory;
        String homeDirectory = System.getProperty("user.home");
        if (homeDirectory == null) throw new IllegalStateException("JVM does not provide system property 'user.home'");
        JvmUtils.homeDirectory = new File(removeEndSlash(homeDirectory));
        return JvmUtils.homeDirectory;
    }

    /**
     * Returns the user's variable data directory.
     *
     * @return a non-null instance
     */
    public static File getVariableDirectory() {
        try {
            return doGetVariableDirectory();
        } catch (IllegalStateException e) {
            // is not there, fall back to something inside user home
        }
        JvmUtils.varDirectory = getCacheDirectory();
        return JvmUtils.varDirectory;
    }

    /**
     * Returns a directory inside the user's variable data directory.
     * <p>
     * If the subdirectory name is NULL/Empty, the parent directory is returned
     *
     * @param name the subdirectory name
     * @return a non-null instance
     */
    public static File getVariableDirectory(String name) {
        return validateDirectoryExists(getSubDirectory(getVariableDirectory(), name));
    }

    private static File doGetVariableDirectory() {
        if (varDirectory != null) return varDirectory;
        String varDirectory = System.getProperty("user.home.var");
        // when Linux, a common practice for apps stored in /opt is to have the variable area in /var/opt
        if (varDirectory == null && isLinux()) varDirectory = "/var" + getHomeDirectory();
        if (varDirectory != null) JvmUtils.varDirectory = new File(varDirectory);
        if (JvmUtils.varDirectory == null || !JvmUtils.varDirectory.exists()) {
            JvmUtils.varDirectory = getCacheDirectory();
        }
        return JvmUtils.varDirectory;
    }

    /**
     * Changes the directory used to store variable data directory.
     *
     * @param directory the new variable directory
     */
    public static void setVariableDirectory(File directory) {
        JvmUtils.varDirectory = requireNonNull(directory);
    }

    /**
     * Returns the working directory.
     *
     * @return a non-null instance
     */
    public static File getWorkingDirectory() {
        if (workingDirectory != null) return workingDirectory;
        String workingDirectory = System.getProperty("user.dir");
        if (workingDirectory == null) {
            throw new IllegalStateException("JVM does not provide system property 'user.dir'");
        }
        JvmUtils.workingDirectory = validateDirectoryExists(new File(removeEndSlash(workingDirectory)));
        return JvmUtils.workingDirectory;
    }

    /**
     * Returns whether the logs directory was available at runtime.
     *
     * @return {@code true} if the logs directory really exists, {@code false} otherwise
     */
    public static boolean hasLogsDirectory() {
        return logsDirectoryExist;
    }

    /**
     * Returns the logs directory at runtime.
     * <p>
     * The application expects a directory (symlink) in the hone directory named "logs". If the logs directory
     * is not available, the temporary directory will be used.
     *
     * @return a non-null instance
     */
    public static File getLogsDirectory() {
        if (logsDirectory != null) return logsDirectory;
        File directory = new File(getHomeDirectory(), "logs");
        logsDirectoryExist = directory.exists();
        if (!logsDirectoryExist) {
            directory = getTemporaryDirectory();
        }
        logsDirectory = directory;
        return directory;
    }

    /**
     * Returns the temporary directory.
     *
     * @return a non-null instance
     */
    public static File getTemporaryDirectory() {
        if (tmpDirectory != null) return tmpDirectory;
        File directory = null;
        if (isLinux()) directory = new File(getHomeDirectory(), "tmp");
        if (directory == null || !directory.exists() || !Files.isSymbolicLink(directory.toPath())) {
            String tmpDir = System.getProperty("java.io.tmpdir");
            if (tmpDir != null) directory = new File(tmpDir);
        }
        tmpDirectory = directory;
        System.getProperty("java.io.tmpdir", tmpDirectory.getAbsolutePath());
        return directory;
    }

    /**
     * Returns the temporary directory inside the temporary directory.
     *
     * @param prefix the prefix used to generate the directory name
     * @param suffix the optional suffix used to generate the directory name
     * @return a non-null instance
     */
    public static File getTemporaryDirectory(String prefix, String suffix) {
        if (StringUtils.isEmpty(prefix)) prefix = STORE_NAME;
        String name = prefix + Long.toString(currentTimeMillis(), Character.MAX_RADIX);
        if (StringUtils.isNotEmpty(suffix)) name += suffix;
        return validateDirectoryExists(new File(getTemporaryDirectory(), name));
    }

    /**
     * Returns the temporary file inside the temporary directory.
     *
     * @param prefix the prefix used to generate the file name
     * @param suffix the optional suffix used to generate the file name, usually a file extension
     * @return a non-null instance
     */
    public static File getTemporaryFile(String prefix, String suffix) {
        if (StringUtils.isEmpty(prefix)) prefix = STORE_NAME;
        String name = prefix + Long.toString(currentTimeMillis(), Character.MAX_RADIX);
        if (StringUtils.isNotEmpty(suffix)) name += suffix;
        return validateFileExists(new File(getTemporaryDirectory(), name));
    }

    /**
     * Changes the temporary directory for current JVM.
     *
     * @param directory the new temporary directory
     */
    public static void setTemporaryDirectory(File directory) {
        tmpDirectory = requireNonNull(directory);
        System.getProperty("java.io.tmpdir", directory.getAbsolutePath());
    }

    /**
     * Changes the directory used to store variable data directory.
     *
     * @param directory the new variable directory
     */
    public static void setCacheDirectory(File directory) {
        JvmUtils.cacheDirectory = requireNonNull(directory);
        System.setProperty("user.cache", JvmUtils.cacheDirectory.getAbsolutePath());
    }

    /**
     * Returns a directory used to store files used between process restarts (caches).
     *
     * @return a non-null instance
     * @see #getCacheDirectory(String)
     */
    public static File getCacheDirectory() {
        if (cacheDirectory != null) return cacheDirectory;
        File cacheDirectoryTmp = new File(new File(getHomeDirectory(), CACHE_DIRECTORY_NAME), STORE_NAME);
        if (StringUtils.isNotEmpty(PROJECT_NAME)) cacheDirectoryTmp = new File(cacheDirectoryTmp, PROJECT_NAME);
        setCacheDirectory(cacheDirectoryTmp);
        return cacheDirectory;
    }

    /**
     * Returns a subdirectory used to store files used between process restarts (caches).
     *
     * @return a non-null instance
     * @see #getCacheDirectory()
     */
    public static File getCacheDirectory(String name) {
        return validateDirectoryExists(getSubDirectory(getCacheDirectory(), name));
    }

    /**
     * Returns a sub-directory used to store native libraries.
     *
     * @return a non-null instance
     */
    public static File getNativeDirectory() {
        if (nativeDirectory != null) return nativeDirectory;
        JvmUtils.nativeDirectory = getCacheDirectory("native");
        return nativeDirectory;
    }

    /**
     * Returns the shared memory directory. If there is no shared memory, it will use the temporary directory
     * and create a subdirectory called "shm".
     *
     * @return a non-null file
     */
    public static File getSharedMemoryDirectory() {
        File directory = new File("/run/shm");
        if (!directory.exists()) directory = new File("/dev/shm");
        if (!directory.exists()) {
            directory = new File(getTemporaryDirectory(), "shm");
            return validateDirectoryExists(directory);
        } else {
            return validateDirectoryExists(new File(directory, STORE_NAME));
        }
    }

    /**
     * Returns the next available port starting with a given port.
     *
     * @return the available port, {@link #UNAVAILABLE_PORT} if no available port was found
     */
    public static int getNextAvailablePort(int startPort) {
        return getNextAvailablePort(startPort, 65000);
    }

    /**
     * Returns the next available port for a given range.
     *
     * @param maxPort maximum port number
     * @return the available port, {@link #UNAVAILABLE_PORT} if no available port was found
     */
    public static int getNextAvailablePort(int startPort, int maxPort) {
        while (startPort <= maxPort) {
            if (available(startPort)) return startPort;
            startPort++;
        }
        return UNAVAILABLE_PORT;
    }

    /**
     * Checks to see if a specific port is available.
     * <p/>
     * The method tries to bind on UDP and TCP for the given port and if successful, the port is considered free.
     *
     * @param port the port to check for availability
     */
    public static boolean available(int port) {
        InetSocketAddress address = new InetSocketAddress(port);
        return availableTcp(address) || availableUdp(address);
    }

    /**
     * Loads a JAR file and extracts the information from the manifest.
     *
     * @param file the file
     * @return a non-null instance
     */
    public static Jar getJar(File file) {
        return new Jar(file);
    }

    /**
     * Returns the JAR files from the class path.
     *
     * @return a non-null instance
     */
    public static Collection<Jar> getJars() {
        Collection<Jar> jars = CACHED_JARS.get();
        if (jars == null) {
            jars = ClassUtils.getClassPath().stream()
                    .map(JvmUtils::getJar).collect(Collectors.toList());
            CACHED_JARS = new SoftReference<>(jars);
        }
        return jars;
    }

    /**
     * Replaces standard System properties placeholders.
     *
     * @param value the value.
     * @return the value with variables replaces
     */
    public static String replacePlaceholders(String value) {
        if (StringUtils.isEmpty(value)) return value;
        value = org.apache.commons.lang3.StringUtils.replaceOnce(value, "${user.home}", getHomeDirectory().getAbsolutePath());
        value = org.apache.commons.lang3.StringUtils.replaceOnce(value, "${user.dir}", getHomeDirectory().getAbsolutePath());
        value = org.apache.commons.lang3.StringUtils.replaceOnce(value, "${user.cache}", getCacheDirectory().getAbsolutePath());
        value = org.apache.commons.lang3.StringUtils.replaceOnce(value, "${shm.home}", getSharedMemoryDirectory().getAbsolutePath());
        value = org.apache.commons.lang3.StringUtils.replaceOnce(value, "${tmp.home}", getTemporaryDirectory().getAbsolutePath());
        return value;
    }

    private static File getSubDirectory(File directory, String name) {
        return StringUtils.isEmpty(name) ? directory : new File(directory, name);
    }

    private static boolean availableTcp(InetSocketAddress address) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.setSoTimeout(1000);
            try {
                ss.bind(address);
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Exception e) {
            return ThreadUtils.throwException(e);
        } finally {
            IOUtils.closeQuietly(ss);
        }
    }

    private static boolean availableUdp(InetSocketAddress address) {
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            ds.setReuseAddress(true);
            ds.setSoTimeout(1000);
            try {
                ds.bind(address);
                return true;
            } catch (SocketException e) {
                return false;
            }
        } catch (Exception e) {
            return ThreadUtils.throwException(e);
        } finally {
            IOUtils.closeQuietly(ds);
        }
    }

    public static class Jar implements Identifiable<String>, Nameable, Descriptable {

        private final String id;
        private final File file;

        private String name = EMPTY_STRING;
        private String description = EMPTY_STRING;
        private String extensionName = EMPTY_STRING;
        private String specificationTitle = EMPTY_STRING;
        private String specificationVersion = EMPTY_STRING;
        private String specificationVendor = EMPTY_STRING;
        private String implementationTitle = EMPTY_STRING;
        private String implementationVendor = EMPTY_STRING;
        private Version implementationVersion;
        private String implementationBuild = EMPTY_STRING;
        private String buildId = EMPTY_STRING;
        private String buildTime = EMPTY_STRING;
        private int attributeCount;

        Jar(File file) {
            requireNonNull(file);
            this.id = Hashing.hash(file.getAbsolutePath());
            this.file = file;
            initialize();
        }

        @Override
        public String getId() {
            return id;
        }

        public File getFile() {
            return file;
        }

        public String getName() {
            if (implementationTitle != null) {
                return implementationTitle;
            } else if (extensionName != null) {
                return extensionName;
            } else if (name != null) {
                return name;
            } else {
                return file.getName();
            }
        }

        @Override
        public String getDescription() {
            return description;
        }

        public String getExtensionName() {
            return extensionName;
        }

        public String getSpecificationTitle() {
            return specificationTitle;
        }

        public String getSpecificationVersion() {
            return specificationVersion;
        }

        public String getSpecificationVendor() {
            return specificationVendor;
        }

        public String getImplementationTitle() {
            return implementationTitle;
        }

        public String getImplementationVendor() {
            return implementationVendor;
        }

        public Version getImplementationVersion() {
            return implementationVersion;
        }

        public String getImplementationBuild() {
            return implementationBuild;
        }

        public String getBuildId() {
            return buildId;
        }

        public String getBuildTime() {
            return buildTime;
        }

        public int getAttributeCount() {
            return attributeCount;
        }

        private void initialize() {
            Attributes mainAttributes;
            try (JarFile jar = new JarFile(file, false, JarFile.OPEN_READ)) {
                Manifest manifest = jar.getManifest();
                mainAttributes = manifest.getMainAttributes();
            } catch (Exception e) {
                // if we cannot load the manifest, we just ignore it and return with empty values
                return;
            }
            name = mainAttributes.getValue(NAME_ATTR);
            extensionName = mainAttributes.getValue(EXTENSION_NAME_ATTR);
            specificationTitle = mainAttributes.getValue(SPECIFICATION_TITLE_ATTR);
            specificationVendor = mainAttributes.getValue(SPECIFICATION_VENDOR_ATTR);
            specificationVersion = mainAttributes.getValue(SPECIFICATION_VERSION_ATTR);
            implementationTitle = mainAttributes.getValue(IMPLEMENTATION_TITLE_ATTR);
            implementationVendor = mainAttributes.getValue(IMPLEMENTATION_VENDOR_ATTR);
            String versionValue = mainAttributes.getValue(IMPLEMENTATION_VERSION_ATTR);
            implementationVersion = versionValue != null ? Version.parse(versionValue) : Version.NO_VERSION;
            implementationBuild = trim(defaultIfEmpty(mainAttributes.getValue(IMPLEMENTATION_BUILD_ATTR), mainAttributes.getValue(BUILD_REVISION_ATTR)), false);
            if (implementationBuild.startsWith(VARIABLE_PREFIX)) implementationBuild = EMPTY_STRING;
            buildTime = defaultIfEmpty(mainAttributes.getValue(BUILD_TIME_ATTR), mainAttributes.getValue(BUILD_DATE_ATTR));
            buildId = trim(mainAttributes.getValue(BUILD_ID_ATTR), false);
            if (buildId.startsWith(VARIABLE_PREFIX)) buildId = EMPTY_STRING;
            long buildIdNumber = NumberUtils.toNumber(buildId, -1L).longValue();
            if (isNotEmpty(buildId) && buildIdNumber != -1) {
                buildIdNumber = (int) (buildIdNumber & 0xffffff);
                implementationVersion = implementationVersion.withBuild((int) buildIdNumber);
            }
            StringBuilder descriptionBuilder = new StringBuilder();
            attributeCount = 0;
            mainAttributes.keySet().stream().map(Objects::toString)
                    .filter(s -> !s.startsWith("Bundle-"))
                    .filter(s -> !STANDARD_JAR_ATTRIBUTES.contains(s)).forEach(key -> {
                        String value = defaultIfEmpty(ObjectUtils.toString(mainAttributes.getValue(key)), NA_STRING);
                        StringUtils.append(descriptionBuilder, key + ": '" + value + "'", ", ");
                        attributeCount++;
                    });
            description = descriptionBuilder.toString();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("file", file)
                    .add("name", name)
                    .add("extensionName", extensionName)
                    .add("specificationTitle", specificationTitle)
                    .add("specificationVendor", specificationVendor)
                    .add("implementationTitle", implementationTitle)
                    .add("implementationVendor", implementationVendor)
                    .add("implementationVersion", implementationVersion)
                    .add("implementationBuild", implementationBuild)
                    .add("buildTime", buildTime)
                    .toString();
        }
    }

    private static final String NAME_ATTR = "Name";
    private static final String EXTENSION_NAME_ATTR = "Extension-Name";
    private static final String MANIFEST_VERSION_ATTR = "Manifest-Version";
    private static final String SPECIFICATION_TITLE_ATTR = "Specification-Title";
    private static final String SPECIFICATION_VENDOR_ATTR = "Specification-Vendor";
    private static final String SPECIFICATION_VERSION_ATTR = "Specification-Version";
    private static final String IMPLEMENTATION_TITLE_ATTR = "Implementation-Title";
    private static final String IMPLEMENTATION_VENDOR_ATTR = "Implementation-Vendor";
    private static final String IMPLEMENTATION_VERSION_ATTR = "Implementation-Version";
    private static final String IMPLEMENTATION_BUILD_ATTR = "Implementation-Build";
    private static final String BUILD_TIME_ATTR = "Build-Time";
    private static final String BUILD_DATE_ATTR = "Build-Date";
    private static final String BUILD_ID_ATTR = "Build-Id";
    private static final String BUILD_REVISION_ATTR = "Build-Revision";

    private static final String EXPORT_PACKAGE_ATTR = "Export-Package";
    private static final String IMPORT_PACKAGE_ATTR = "Import-Package";
    private static final String PRIVATE_PACKAGE_ATTR = "Private-Package";
    private static final String REQUIRE_CAPABILITY_ATTR = "Require-Capability";
    private static final String PROVIDE_CAPABILITY_ATTR = "Provide-Capability";

    private static final String VARIABLE_PREFIX = "${";

    private static final Set<String> STANDARD_JAR_ATTRIBUTES = Set.of(
            NAME_ATTR, EXTENSION_NAME_ATTR, MANIFEST_VERSION_ATTR, SPECIFICATION_TITLE_ATTR, SPECIFICATION_VENDOR_ATTR, SPECIFICATION_VERSION_ATTR,
            IMPLEMENTATION_TITLE_ATTR, IMPLEMENTATION_VENDOR_ATTR, IMPLEMENTATION_VERSION_ATTR, IMPLEMENTATION_BUILD_ATTR,
            BUILD_TIME_ATTR, BUILD_DATE_ATTR, BUILD_ID_ATTR, BUILD_REVISION_ATTR,
            EXPORT_PACKAGE_ATTR, IMPORT_PACKAGE_ATTR, REQUIRE_CAPABILITY_ATTR, PROVIDE_CAPABILITY_ATTR, PRIVATE_PACKAGE_ATTR
    );
}
