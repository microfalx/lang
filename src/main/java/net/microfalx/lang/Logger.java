package net.microfalx.lang;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireBounded;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.TextUtils.insertSpaces;
import static net.microfalx.lang.TimeUtils.toLocalDateTime;

/**
 * An in-memory logger used by services to create a report of an activity.
 * <p>
 * The logger can also redirect all entries to an application logger.
 */
public final class Logger implements Identifiable<String>, Nameable, Descriptable, Serializable {

    private static final long serialVersionUID = 3775117519553206152L;

    public static final int SMALL_INDENT = 2;
    public static final int MEDIUM_INDENT = 5;
    public static final int LARGE_INDENT = 10;
    public static final int MEMORY_MAX_SIZE = 5_000_000;
    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss ");

    private static final int INDENT_STEPS = 3;

    private final StringBuilder buffer = new StringBuilder();
    private transient volatile byte[] bufferCompressed;
    private int position;
    private final Logger parent;
    private transient org.slf4j.Logger logger;
    private boolean severity;
    private int indent;
    private boolean includeTimestamp = false;
    private boolean includeBullet;
    private boolean includeDebug;
    private final String id = IdGenerator.get().nextAsString();
    private String name;
    private String description;
    private int maximumSize = MEMORY_MAX_SIZE;
    private int indentStep = INDENT_STEPS;

    private long createdAt;
    private long firstAccess;
    private long lastAccess;

    private int debugCount;
    private int infoCount;
    private int warningCount;
    private int errorCount;
    private int clearCount;

    private final AtomicInteger eventCount = new AtomicInteger();

    private static final ThreadLocal<Stack<Logger>> LOGGER = ThreadLocal.withInitial(Stack::new);
    private static final ThreadLocal<Logger> LAST = new ThreadLocal<>();

    /**
     * Returns the logger attached to the current thread.
     * <p>
     * If there is no logger attached, a new logger is created.
     *
     * @return a non-null instance
     */
    public static Logger current() {
        Stack<Logger> stack = LOGGER.get();
        if (stack.isEmpty()) stack.add(Logger.create());
        return stack.peek();
    }

    /**
     * Returns the last logger detached from the current thread.
     * <p>
     * If there is no logger attached, a new logger is created.
     *
     * @return a non-null instance
     */
    public static Logger last() {
        Logger last = LAST.get();
        return last != null ? last : Logger.create();
    }

    /**
     * Returns whether a logger is attached to the current thread.
     *
     * @return <code>true</code> if it has a logger,
     */
    public static boolean hasCurrent() {
        return LOGGER.get() != null;
    }

    /**
     * Resets logger information for the current thread.
     */
    public static void remove() {
        LOGGER.remove();
    }

    /**
     * Creates a report from a logger output, with a given initial message.
     *
     * @param logger    the logger
     * @param pattern   the pattern of the message
     * @param arguments the arguments for the pattern
     * @return the report
     */
    public static String formatReport(Logger logger, String pattern, Object... arguments) {
        String report = formatMessage(pattern, arguments);
        String log = EMPTY_STRING;
        if (!logger.isEmpty()) log = ", log:\n" + insertSpaces(logger.getOutput(), 2);
        report += log;
        return report;
    }

    /**
     * Creates a logger with default settings.
     *
     * @return a non-null instance
     */
    public static Logger create() {
        return new Logger(null);
    }

    /**
     * Creates a simple logger, to be used to log a list of items
     * <p>
     * The method enables the bullets and adds a small indent
     *
     * @param heading the heading of the list
     * @return a non-null instance
     * @see #create()
     */
    public static Logger createList(String heading) {
        Logger logger = create();
        logger.info(heading);
        logger.increaseIndent(SMALL_INDENT);
        logger.withBullets(true);
        return logger;
    }

    /**
     * Create a logger with default values and a parent logger (all log entries are also routed to the parent)
     *
     * @param parent the parent
     * @return a non-null instance
     */
    public static Logger create(Logger parent) {
        return new Logger(parent);
    }

    private Logger() {
        this(null);
    }

    private Logger(Logger parent) {
        this.parent = parent;
    }

    /**
     * Returns the number of entries logged, all levels.
     *
     * @return a positive integer
     */
    public int getCount() {
        return getDebugCount() + getInfoCount() + getWarningCount() + getErrorCount();
    }

    /**
     * Returns whether the logger has no entries.
     *
     * @return <code>true</code> if empty, <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return getCount() == 0;
    }

    /**
     * Returns the number of entries logged with DEBUG level.
     *
     * @return a positive integer
     */
    public int getDebugCount() {
        return debugCount;
    }

    /**
     * Returns the number of entries logged with INFO level.
     *
     * @return a positive integer
     */
    public int getInfoCount() {
        return infoCount;
    }

    /**
     * Returns the number of entries logged with WARN level.
     *
     * @return a positive integer
     */
    public int getWarningCount() {
        return warningCount;
    }

    /**
     * Returns the number of entries logged with ERROR level.
     *
     * @return a positive integer
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Returns the number of times the logger was reset (start from scatch).
     *
     * @return a positive integer
     */
    public int getClearCount() {
        return clearCount;
    }

    /**
     * Returns the number of  processed events.
     * <p>
     * An event can be any individual unit of work done and logged with this class.
     *
     * @return a non-null instance
     */
    public int getEventCount() {
        return eventCount.get();
    }

    /**
     * Returns the time when the logger was created
     *
     * @return a non-null instance
     */
    public LocalDateTime getCreatedAt() {
        return toLocalDateTime(createdAt);
    }

    /**
     * Returns the time when the logger was used first time.
     *
     * @return a non-null instance
     */
    public LocalDateTime getFirstAccess() {
        return firstAccess > 0 ? toLocalDateTime(firstAccess) : null;
    }

    /**
     * Returns the time when the logger was used last time.
     *
     * @return a non-null instance
     */
    public LocalDateTime getLastAccess() {
        return lastAccess > 0 ? toLocalDateTime(lastAccess) : null;
    }

    /**
     * Returns the duration between first and last access (more or less the time of the "activity").
     *
     * @return a non-null instance
     */
    public Duration getDuration() {
        LocalDateTime firstAccess = getFirstAccess();
        if (firstAccess != null) {
            return Duration.between(getFirstAccess(), getLastAccess());
        } else {
            return Duration.ZERO;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Changes the name of the logger.
     *
     * @param name the name
     * @return self
     */
    public Logger setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Changes the description of the logger
     *
     * @param description the new description
     * @return self
     */
    public Logger setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Attaches logger to the current thread.
     */
    public Logger attach() {
        Stack<Logger> stack = LOGGER.get();
        stack.push(this);
        return this;
    }

    /**
     * Detaches logger to the current thread.
     */
    public Logger detach() {
        Stack<Logger> stack = LOGGER.get();
        if (!stack.isEmpty()) LAST.set(stack.pop());
        return this;
    }

    /**
     * Indicates whether the logger should print the severity.
     *
     * @param severity the severity
     * @return self
     */
    public Logger withSeverity(boolean severity) {
        this.severity = severity;
        return this;
    }

    /**
     * Indicates whether the logger should print the timestamp.
     *
     * @param includeTimestamp <code>true</code> to include the timestamp, <code>false</code> otherwise
     * @return self
     */
    public Logger withTimestamp(boolean includeTimestamp) {
        this.includeTimestamp = includeTimestamp;
        return this;
    }

    /**
     * Indicates whether the logger should print a bullet (to create a list).
     *
     * @param includeBullet <code>true</code> to include the bullet, <code>false</code> otherwise
     * @return self
     */
    public Logger withBullets(boolean includeBullet) {
        this.includeBullet = includeBullet;
        return this;
    }

    /**
     * Indicates whether the logger should accumulate entries with DEBUG level (default false).
     * <p>
     * DEBUG statements are still passed to the {@link org.slf4j.Logger}.
     *
     * @param includeDebug <code>true</code> to include the bullet, <code>false</code> otherwise
     * @return self
     */
    public Logger withDebug(boolean includeDebug) {
        this.includeDebug = includeDebug;
        return this;
    }

    /**
     * Attaches an application logger, to log every entry in the process log.
     *
     * @param logger the application logger
     * @return self
     */
    public Logger withLogger(org.slf4j.Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Returns the logger output.
     * <p>
     * If a logger is attached to the thread, it is automatically unregistered.
     *
     * @return a non-null string
     */
    public String getOutput() {
        synchronized (buffer) {
            String text;
            if (bufferCompressed != null) {
                text = getDecompressedBuffer();
            } else {
                text = buffer.toString();
            }
            return StringUtils.trim(text);
        }
    }

    /**
     * Removes all entries.
     */
    public void clear() {
        synchronized (buffer) {
            bufferCompressed = null;
            buffer.setLength(0);
        }
        clearCount = 0;
        debugCount = 0;
        infoCount = 0;
        warningCount = 0;
        errorCount = 0;
        touch();
    }

    /**
     * Compresses the text created by the logger to reduce memory consumption (long storage).
     */
    public void compress() {
        synchronized (buffer) {
            if (bufferCompressed == null && buffer.length() > 0) {
                try {
                    bufferCompressed = IOUtils.getInputStreamAsBytes(TextUtils.compressText(buffer.toString()));
                    buffer.setLength(0);
                } catch (IOException e) {
                    ExceptionUtils.rethrowException(e);
                }
            }
        }
    }

    /**
     * Marks the current position in the log. A subsequent call to the <code>reset</code> method repositions
     * the log write position at the last marked position so that subsequent writes will override some parts of the log.
     */
    public void mark() {
        synchronized (buffer) {
            uncompress();
            position = buffer.length();
        }
    }

    /**
     * Repositions the log position at the time the <code>mark</code> method was last called.
     */
    public void unmark() {
        synchronized (buffer) {
            uncompress();
            if (position > 0) {
                buffer.setLength(position);
            }
        }
    }

    /**
     * Appends the output of one logger to the current logger.
     * <p>
     * The text is indented using the current indentation.
     *
     * @param logger the logger
     */
    public Logger append(Logger logger) {
        requireNonNull(logger);
        synchronized (buffer) {
            uncompress();
            String text = insertSpaces(logger.getOutput(), getIndentationSpaces());
            doAppend(text, true);
            errorCount += logger.getErrorCount();
            warningCount += logger.getWarningCount();
            infoCount += logger.getInfoCount();
        }
        return this;
    }

    /**
     * Appends a new line to the current logger.
     * <p>
     * The new line is not appended to the logger, if present.
     *
     * @return self
     */
    public Logger ln() {
        return append("\n");
    }

    public Entry atDebug() {
        return new Entry(this, Severity.DEBUG);
    }

    public Entry atInfo() {
        return new Entry(this, Severity.INFO);
    }

    public Entry atWarn() {
        return new Entry(this, Severity.WARN);
    }

    public Entry atError() {
        return new Entry(this, Severity.ERROR);
    }

    /**
     * Appends a block of text (multi-line) to the logger without forwarding the text to the application logger.
     * <p>
     * The text is indented using the current indentation.
     *
     * @param reader the text
     */
    public Logger append(Reader reader) throws IOException {
        return append(reader, false);
    }

    /**
     * Appends a block of text (multi-line) to the logger without forwarding the text to the application logger.
     * <p>
     * The text is indented using the current indentation.
     *
     * @param reader        the text
     * @param withSeparator <code>true</code> to add a separator in front of each line, <code>false</code> otherwise
     */
    public Logger append(Reader reader, boolean withSeparator) throws IOException {
        return append(reader, withSeparator, false);
    }

    /**
     * Appends a block of text (multi-line) to the logger without forwarding the text to the application logger.
     * <p>
     * The text is indented using the current indentation.
     *
     * @param reader         the text
     * @param withSeparator  <code>true</code> to add a separator in front of each line, <code>false</code> otherwise
     * @param spaceFirstLine <code>true</code> to space the first line too, <code>false</code> otherwise
     */
    public Logger append(Reader reader, boolean withSeparator, boolean spaceFirstLine) throws IOException {
        requireNonNull(reader);
        return append(IOUtils.getReaderAsString(reader), withSeparator, spaceFirstLine);
    }

    /**
     * Appends a block of text (multi-line) to the logger without forwarding the text to the application logger.
     * <p>
     * The text is indented using the current indentation.
     *
     * @param text the text
     */
    public Logger append(String text) {
        return append(text, false);
    }

    /**
     * Appends a block of text (multi-line) to the logger.
     * <p>
     * The text is indented using the current indentation.
     *
     * @param text          the text
     * @param withSeparator <code>true</code> to add a separator in front of each line, <code>false</code> otherwise
     */
    public Logger append(String text, boolean withSeparator) {
        return append(text, withSeparator, false);
    }

    /**
     * Appends a block of text (multi-line) to the logger without forwarding the text to the application logger.
     * <p>
     * The text is indented using the current indentation.
     *
     * @param text           the text
     * @param withSeparator  <code>true</code> to add a separator in front of each line, <code>false</code> otherwise
     * @param spaceFirstLine <code>true</code> to space the first line too, <code>false</code> otherwise
     */
    public Logger append(String text, boolean withSeparator, boolean spaceFirstLine) {
        requireNonNull(text);
        synchronized (buffer) {
            uncompress();
            if (withSeparator) {
                text = insertSpaces(text, 2, true, true, true);
                spaceFirstLine = true;
            }
            text = insertSpaces(text, getIndentationSpaces(), false, false, spaceFirstLine);
            doAppend(text, true);
            infoCount++;
        }

        return this;
    }

    /**
     * Changes the indentation step.
     *
     * @param steps the number of spaces
     * @return self
     */
    public Logger setIndentStep(int steps) {
        requireBounded(steps, 2, 10);
        this.indentStep = steps;
        return this;
    }

    /**
     * Returns the current level of indentation.
     *
     * @return a positive integer
     */
    public int getIndentLevel() {
        return indent / indentStep;
    }

    /**
     * Returns the current indentation value (spaces).
     *
     * @return a positive integer
     */
    public int getIndent() {
        return indent;
    }

    /**
     * Increases the event counter.
     *
     * @return self
     */
    public Logger increaseEventCount() {
        eventCount.incrementAndGet();
        return this;
    }

    /**
     * Increases the indent.
     */
    public Logger increaseIndent() {
        indent += indentStep;
        return this;
    }

    /**
     * Increases the indent by multiple units.
     */
    public Logger increaseIndent(int units) {
        requireBounded(units, 1, 100);
        indent += units * indentStep;
        return this;
    }

    /**
     * Decreases the indent.
     */
    public Logger decreaseIndent() {
        indent -= indentStep;
        if (indent < 0) indent = 0;
        return this;
    }

    /**
     * Decreases the indent by multiple units.
     */
    public Logger decreaseIndent(int units) {
        requireBounded(units, 1, 100);
        indent -= units * indentStep;
        if (indent < 0) indent = 0;
        return this;
    }

    /**
     * Adds a new entry in the log with DEBUG.
     *
     * @param message the message
     * @return self
     */
    public Logger debug(String message) {
        return log(Severity.DEBUG, message);
    }

    /**
     * Adds a new entry in the log with DEBUG.
     *
     * @param pattern   the message pattern
     * @param arguments the arguments
     * @return self
     * @see java.text.MessageFormat
     */
    public Logger debug(String pattern, Object... arguments) {
        return log(Severity.DEBUG, formatMessage(pattern, arguments));
    }

    /**
     * Adds a new entry in the log with INFO.
     *
     * @param message the message
     * @return self
     */
    public Logger info(String message) {
        return log(Severity.INFO, message);
    }

    /**
     * Adds a new entry in the log with INFO.
     *
     * @param pattern   the message attern
     * @param arguments the arguments
     * @return self
     * @see java.text.MessageFormat
     */
    public Logger info(String pattern, Object... arguments) {
        return log(Severity.INFO, formatMessage(pattern, arguments));
    }

    /**
     * Adds a new entry in the log with WARN level.
     *
     * @param message the message
     * @return self
     */
    public Logger warn(String message) {
        return log(Severity.WARN, message);
    }

    /**
     * Adds a new entry in the log with INFO.
     *
     * @param pattern   the message pattern
     * @param arguments the arguments
     * @return self
     * @see java.text.MessageFormat
     */
    public Logger warn(String pattern, Object... arguments) {
        return log(Severity.WARN, formatMessage(pattern, arguments));
    }


    /**
     * Adds a new entry in the log with ERROR level.
     *
     * @param message the message
     * @return self
     */
    public Logger error(String message) {
        return log(Severity.ERROR, message);
    }

    /**
     * Adds a new entry in the log with ERROR level.
     *
     * @param pattern   the message pattern
     * @param arguments the arguments
     * @return self
     * @see java.text.MessageFormat
     */
    public Logger error(String pattern, Object... arguments) {
        return log(Severity.ERROR, formatMessage(pattern, arguments));
    }

    /**
     * Adds a new entry in the log with ERROR level.
     *
     * @param message the message
     * @return self
     */
    public Logger error(String message, Throwable throwable) {
        if (throwable != null) message += ", root cause " + ExceptionUtils.getRootCauseMessage(throwable);
        return log(Severity.ERROR, message, throwable);
    }

    /**
     * Appends a message to the log, without any severity or new lines.
     *
     * @param message the message
     */
    public Logger log(String message) {
        if (message == null) message = EMPTY_STRING;
        synchronized (buffer) {
            uncompress();
            doAppend(message, false);
        }
        return this;
    }

    /**
     * Adds a new entry in the log.
     *
     * @param severity the severity
     * @param message  the message
     * @return self
     */
    public Logger log(Severity severity, String message) {
        return log(severity, message, null);
    }

    /**
     * Copy all the logger properties from another logger.
     *
     * @param logger another logger
     * @return self
     */
    public Logger copyFrom(Logger logger) {
        synchronized (buffer) {
            debugCount = logger.debugCount;
            infoCount = logger.infoCount;
            warningCount = logger.warningCount;
            errorCount = logger.errorCount;
            position = logger.position;
            severity = logger.severity;
            buffer.setLength(0);
            buffer.append(logger.getOutput());
            indent = logger.indent;
            includeTimestamp = logger.includeTimestamp;
            includeBullet = logger.includeBullet;
            touch();
        }
        return this;
    }

    /**
     * Uncompresses the text, if required.
     * <p>
     * It should be executed under a synchronized block.
     */
    private void uncompress() {
        if (bufferCompressed == null) return;
        buffer.setLength(0);
        buffer.append(getDecompressedBuffer());
        bufferCompressed = null;
    }

    /**
     * Returns the buffer content if it was compressed.
     *
     * @return the buffer decompressed, null if it was not compressed
     */
    private String getDecompressedBuffer() {
        if (bufferCompressed == null) return null;
        try {
            return TextUtils.decompressText(new ByteArrayInputStream(bufferCompressed));
        } catch (IOException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Invoked under a lock on buffer to write a test to internal buffer and the file (if configured)
     *
     * @param text the text
     */
    private Logger doAppend(String text) {
        return doAppend(text, false);
    }

    /**
     * Invoked under a lock on buffer to write a test to internal buffer and the file (if configured)
     *
     * @param text    the text
     * @param newLine <code>true</code> to append a new line at the end of the message, <code>false</code> otherwise
     */
    private Logger doAppend(String text, boolean newLine) {
        buffer.append(text);
        if (buffer.length() > maximumSize) buffer.setLength(0);
        if (newLine) buffer.append('\n');
        touch();
        return this;
    }

    /**
     * Adds a new entry in the log.
     *
     * @param message the message
     */
    private Logger log(Severity severity, String message, Throwable throwable) {
        requireNonNull(severity);
        if (message == null) message = EMPTY_STRING;
        trackCounts(severity);
        if (parent != null) parent.log(severity, message);
        boolean log = severity != Severity.DEBUG || includeDebug;
        if (log) doLog(severity, message, throwable);
        if (logger != null) logWithLogger(severity, message, throwable);
        return this;
    }

    private void doLog(Severity severity, String message, Throwable throwable) {
        synchronized (buffer) {
            uncompress();
            if (includeTimestamp) {
                final String time = LOG_TIME_FORMATTER.format(LocalDateTime.now());
                doAppend(time);
            }
            if (this.severity) doAppend(severity.name()).doAppend(" ");
            doAppend(getIndentation());
            if (includeBullet) doAppend(Glyph.BULLET);
            if (throwable != null) message += ", with failure: " + getRootCauseDescription(throwable);
            doAppend(message, true);
        }
    }

    private void logWithLogger(Severity severity, String message, Throwable throwable) {
        switch (severity) {
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                if (throwable != null) {
                    logger.error(message, throwable);
                } else {
                    logger.error(message);
                }
                break;
        }
    }

    private void trackCounts(Severity severity) {
        switch (severity) {
            case DEBUG:
                debugCount++;
                break;
            case INFO:
                infoCount++;
                break;
            case WARN:
                warningCount++;
                break;
            case ERROR:
                errorCount++;
                break;
        }
    }

    private String getIndentation() {
        return StringUtils.getStringOfChar(' ', getIndentationSpaces());
    }

    private int getIndentationSpaces() {
        return getDepth() * 3 + indent;
    }

    private void touch() {
        if (firstAccess == 0) firstAccess = System.currentTimeMillis();
        lastAccess = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return getOutput();
    }

    /**
     * An enum for logger severity.
     */
    public enum Severity {
        DEBUG, INFO, WARN, ERROR
    }

    private int getDepth() {
        int depth = 0;
        Logger parent = this.parent;
        while (parent != null) {
            depth++;
            parent = parent.parent;
        }
        return depth;
    }

    public static class Glyph {

        private Glyph() {
        }

        // ----------------------------------------------------
        // BULLETS / LIST MARKERS
        // ----------------------------------------------------

        public static final String BULLET = "•";
        public static final String TRIANGULAR_BULLET = "‣";
        public static final String WHITE_BULLET = "◦";
        public static final String LARGE_CIRCLE = "○";
        public static final String LARGE_BLACK_CIRCLE = "●";
        public static final String SMALL_SQUARE = "▪";
        public static final String WHITE_SMALL_SQUARE = "▫";
        public static final String TRIANGLE_RIGHT = "▶";

        // ----------------------------------------------------
        // SUCCESS / FAILURE
        // ----------------------------------------------------

        public static final String CHECK = "✓";
        public static final String CHECK_HEAVY = "✔";
        public static final String CROSS = "✗";
        public static final String CROSS_HEAVY = "✖";
        public static final String CROSS_MARK = "❌";
        public static final String BALLOT_CHECK = "☑";
        public static final String BALLOT_X = "☒";

        // ----------------------------------------------------
        // WARNING / INFO / ALERT
        // ----------------------------------------------------

        public static final String WARNING = "⚠";
        public static final String INFO = "ℹ";
        public static final String QUESTION = "❓";
        public static final String EXCLAMATION = "❗";
        public static final String ALARM = "🚨";

        // ----------------------------------------------------
        // ARROWS / FLOW
        // ----------------------------------------------------

        public static final String ARROW_RIGHT = "→";
        public static final String ARROW_LEFT = "←";
        public static final String ARROW_UP = "↑";
        public static final String ARROW_DOWN = "↓";
        public static final String DOUBLE_ARROW_RIGHT = "⇒";
        public static final String HEAVY_ARROW_RIGHT = "➜";
        public static final String LONG_ARROW_RIGHT = "⮕";

        // ----------------------------------------------------
        // FLAGS / MARKERS
        // ----------------------------------------------------

        public static final String FLAG = "⚑";
        public static final String TRIANGULAR_FLAG = "🚩";
        public static final String LOCATION = "📍";

        // ----------------------------------------------------
        // SYSTEM / DEVOPS
        // ----------------------------------------------------

        public static final String GEAR = "⚙";
        public static final String HAMMER = "🔨";
        public static final String TOOLBOX = "🧰";
        public static final String PACKAGE = "📦";
        public static final String LINK = "🔗";
        public static final String LOCK = "🔒";
        public static final String UNLOCK = "🔓";
        public static final String KEY = "🔑";

        // ----------------------------------------------------
        // FILES / STORAGE
        // ----------------------------------------------------

        public static final String FILE = "📄";
        public static final String FOLDER = "📁";
        public static final String OPEN_FOLDER = "📂";
        public static final String DATABASE = "🗄";
        public static final String FLOPPY = "💾";

        // ----------------------------------------------------
        // NETWORK / CLOUD
        // ----------------------------------------------------

        public static final String CLOUD = "☁";
        public static final String NETWORK = "🌐";
        public static final String SATELLITE = "📡";
        public static final String WIFI = "📶";

        // ----------------------------------------------------
        // TIME / PERFORMANCE
        // ----------------------------------------------------

        public static final String CLOCK = "⏱";
        public static final String STOPWATCH = "⏲";
        public static final String HOURGLASS = "⏳";
        public static final String FAST = "⚡";

        // ----------------------------------------------------
        // PROGRESS / STATUS
        // ----------------------------------------------------

        public static final String START = "▶";
        public static final String STOP = "⏹";
        public static final String PAUSE = "⏸";
        public static final String RECORD = "⏺";

        // ----------------------------------------------------
        // DEBUG / DIAGNOSTICS
        // ----------------------------------------------------

        public static final String BUG = "🐞";
        public static final String MICROSCOPE = "🔬";
        public static final String MAGNIFIER = "🔍";
        public static final String MAGNIFIER_RIGHT = "🔎";
    }

    /**
     * A class which holds an entry, with a given severity, to be logged to the
     */
    public static class Entry implements Appendable {

        private final Logger logger;
        private final Severity severity;
        private Throwable throwable;
        private final StringBuilder buffer = new StringBuilder();

        private Entry(Logger logger, Severity severity) {
            this.logger = logger;
            this.severity = severity;
        }

        @Override
        public Entry append(CharSequence csq) {
            buffer.append(csq);
            return this;
        }

        @Override
        public Entry append(CharSequence csq, int start, int end) {
            buffer.append(csq, start, end);
            return this;
        }

        @Override
        public Entry append(char c) {
            buffer.append(c);
            return this;
        }

        public Entry append(CharSequence csq, Object... args) {
            if (csq == null) return this;
            buffer.append(StringUtils.formatMessage(csq.toString(), args));
            return this;
        }

        public Entry check() {
            append(Glyph.CHECK_HEAVY).append(SPACE_CHAR);
            return this;
        }

        public Entry cross() {
            append(Glyph.CROSS_MARK).append(SPACE_CHAR);
            return this;
        }

        public Entry bullet() {
            append(Glyph.BULLET).append(SPACE_CHAR);
            return this;
        }

        public Entry square() {
            append(Glyph.SMALL_SQUARE).append(SPACE_CHAR);
            return this;
        }

        public Entry triangle() {
            append(Glyph.TRIANGULAR_BULLET).append(SPACE_CHAR);
            return this;
        }

        public Entry rightArrow() {
            append(Glyph.ARROW_RIGHT).append(SPACE_CHAR);
            return this;
        }

        public Entry leftArrow() {
            append(Glyph.ARROW_LEFT).append(SPACE_CHAR);
            return this;
        }

        @Override
        public String toString() {
            return buffer.toString();
        }

        public Entry throwable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public void log() {
            logger.log(severity, buffer.toString(), throwable);
        }
    }
}
