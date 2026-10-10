package net.microfalx.lang;

import java.util.Locale;
import java.util.regex.Pattern;

import static net.microfalx.lang.ArgumentUtils.requireBounded;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.RichText.Color;
import static net.microfalx.lang.RichText.Style;

/**
 * ANSI escape sequences used to color and format text in a terminal.
 * <p>
 * Colors and styles are described by {@link Color} and {@link Style}; use {@link RichText} to create text which
 * can be rendered as plain text, ANSI or HTML.
 * <p>
 * The class can be used in three ways:
 * <ul>
 *     <li>static helpers which wrap a piece of text, e.g. {@code Ansi.color("error", Color.RED)}</li>
 *     <li>a fluent builder, e.g. {@code Ansi.ansi().fg(Color.RED).bold().a("error").reset().toString()}</li>
 *     <li>raw escape sequences, e.g. {@code Ansi.foreground(Color.RED)} or {@link #RESET}</li>
 * </ul>
 * <p>
 * The helpers which wrap text and the builder honor the {@link #isEnabled() enabled} flag: when ANSI is disabled
 * they return the text without any escape sequences. The flag is initialized with {@link #isSupported()} and
 * can be changed with {@link #setEnabled(boolean)}. Raw escape sequences (constants and methods returning
 * escape sequences) are always returned as is.
 * <p>
 * Wrapping helpers close only what they open (for example, a foreground color is closed with "default foreground"
 * and not with a full reset), so they can be nested.
 */
public final class Ansi {

    /**
     * The escape character.
     */
    public static final char ESC = '\u001B';

    /**
     * Control Sequence Introducer.
     */
    public static final String CSI = ESC + "[";

    /**
     * Resets all attributes (colors and styles).
     */
    public static final String RESET = CSI + "0m";

    /**
     * Clears the whole screen.
     */
    public static final String CLEAR_SCREEN = CSI + "2J";

    /**
     * Clears the screen from the cursor to the end of the screen.
     */
    public static final String CLEAR_SCREEN_TO_END = CSI + "0J";

    /**
     * Clears the whole line where the cursor is.
     */
    public static final String CLEAR_LINE = CSI + "2K";

    /**
     * Clears the line from the cursor to the end of the line.
     */
    public static final String CLEAR_LINE_TO_END = CSI + "0K";

    /**
     * Moves the cursor to the top-left corner.
     */
    public static final String CURSOR_HOME = CSI + "H";

    /**
     * Saves the cursor position.
     */
    public static final String CURSOR_SAVE = ESC + "7";

    /**
     * Restores the cursor position saved with {@link #CURSOR_SAVE}.
     */
    public static final String CURSOR_RESTORE = ESC + "8";

    /**
     * Hides the cursor.
     */
    public static final String CURSOR_HIDE = CSI + "?25l";

    /**
     * Shows the cursor.
     */
    public static final String CURSOR_SHOW = CSI + "?25h";

    private static final String DEFAULT_FOREGROUND = CSI + "39m";
    private static final String DEFAULT_BACKGROUND = CSI + "49m";

    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B(?:\\[[0-?]*[ -/]*[@-~]|][^\u0007\u001B]*(?:\u0007|\u001B\\\\)|[0-~])");

    private static volatile boolean enabled = detectSupport();

    private final StringBuilder builder = new StringBuilder();

    private Ansi() {
    }

    /**
     * Creates a fluent builder for ANSI formatted text.
     *
     * @return a non-null instance
     */
    public static Ansi ansi() {
        return new Ansi();
    }

    /**
     * Returns whether helpers and the builder emit escape sequences.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Changes whether helpers and the builder emit escape sequences.
     *
     * @param enabled {@code true} to emit escape sequences, {@code false} to emit plain text
     */
    public static void setEnabled(boolean enabled) {
        Ansi.enabled = enabled;
    }

    /**
     * Returns whether the current process is (most likely) attached to a terminal which understands ANSI escape sequences.
     * <p>
     * The detection honors the <a href="https://no-color.org">NO_COLOR</a> and {@code FORCE_COLOR} environment variables.
     *
     * @return {@code true} if supported, {@code false} otherwise
     */
    public static boolean isSupported() {
        return detectSupport();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Escape sequences

    /**
     * Returns the escape sequence which changes the foreground (text) color.
     *
     * @param color the color
     * @return a non-null instance
     */
    public static String foreground(Color color) {
        requireNonNull(color);
        return CSI + (30 + colorOffset(color)) + "m";
    }

    /**
     * Returns the escape sequence which changes the background color.
     *
     * @param color the color
     * @return a non-null instance
     */
    public static String background(Color color) {
        requireNonNull(color);
        return CSI + (40 + colorOffset(color)) + "m";
    }

    /**
     * Returns the escape sequence which turns on a style.
     *
     * @param style the style
     * @return a non-null instance
     */
    public static String on(Style style) {
        requireNonNull(style);
        int code = switch (style) {
            case BOLD -> 1;
            case DIM -> 2;
            case ITALIC -> 3;
            case UNDERLINE -> 4;
            case BLINK -> 5;
            case REVERSE -> 7;
            case HIDDEN -> 8;
            case STRIKETHROUGH -> 9;
            case DOUBLE_UNDERLINE -> 21;
            case OVERLINE -> 53;
        };
        return CSI + code + "m";
    }

    /**
     * Returns the escape sequence which turns off a style.
     *
     * @param style the style
     * @return a non-null instance
     */
    public static String off(Style style) {
        requireNonNull(style);
        int code = switch (style) {
            case BOLD, DIM -> 22;
            case ITALIC -> 23;
            case UNDERLINE, DOUBLE_UNDERLINE -> 24;
            case BLINK -> 25;
            case REVERSE -> 27;
            case HIDDEN -> 28;
            case STRIKETHROUGH -> 29;
            case OVERLINE -> 55;
        };
        return CSI + code + "m";
    }

    /**
     * Returns the escape sequence for a foreground color from the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return a non-null instance
     */
    public static String foreground(int index) {
        requireBounded(index, 0, 255);
        return CSI + "38;5;" + index + "m";
    }

    /**
     * Returns the escape sequence for a background color from the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return a non-null instance
     */
    public static String background(int index) {
        requireBounded(index, 0, 255);
        return CSI + "48;5;" + index + "m";
    }

    /**
     * Returns the escape sequence for a true color (24-bit) foreground color.
     *
     * @param red   the red component, between 0 and 255
     * @param green the green component, between 0 and 255
     * @param blue  the blue component, between 0 and 255
     * @return a non-null instance
     */
    public static String foreground(int red, int green, int blue) {
        return CSI + "38;2;" + rgb(red, green, blue) + "m";
    }

    /**
     * Returns the escape sequence for a true color (24-bit) background color.
     *
     * @param red   the red component, between 0 and 255
     * @param green the green component, between 0 and 255
     * @param blue  the blue component, between 0 and 255
     * @return a non-null instance
     */
    public static String background(int red, int green, int blue) {
        return CSI + "48;2;" + rgb(red, green, blue) + "m";
    }

    /**
     * Returns the escape sequence for a true color (24-bit) foreground color.
     *
     * @param hex the color as hex, with or without "#" (e.g. "#ff8800" or "f80")
     * @return a non-null instance
     */
    public static String foreground(String hex) {
        int[] rgb = parseHex(hex);
        return foreground(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Returns the escape sequence for a true color (24-bit) background color.
     *
     * @param hex the color as hex, with or without "#" (e.g. "#ff8800" or "f80")
     * @return a non-null instance
     */
    public static String background(String hex) {
        int[] rgb = parseHex(hex);
        return background(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Returns the escape sequence which moves the cursor up.
     *
     * @param lines the number of lines
     * @return a non-null instance
     */
    public static String cursorUp(int lines) {
        return CSI + lines + "A";
    }

    /**
     * Returns the escape sequence which moves the cursor down.
     *
     * @param lines the number of lines
     * @return a non-null instance
     */
    public static String cursorDown(int lines) {
        return CSI + lines + "B";
    }

    /**
     * Returns the escape sequence which moves the cursor right.
     *
     * @param columns the number of columns
     * @return a non-null instance
     */
    public static String cursorRight(int columns) {
        return CSI + columns + "C";
    }

    /**
     * Returns the escape sequence which moves the cursor left.
     *
     * @param columns the number of columns
     * @return a non-null instance
     */
    public static String cursorLeft(int columns) {
        return CSI + columns + "D";
    }

    /**
     * Returns the escape sequence which moves the cursor to a given position.
     *
     * @param row    the row, 1-based
     * @param column the column, 1-based
     * @return a non-null instance
     */
    public static String cursorTo(int row, int column) {
        return CSI + row + ";" + column + "H";
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Text helpers

    /**
     * Colors the text with a foreground color.
     *
     * @param text       the text
     * @param foreground the foreground color
     * @return the formatted text
     */
    public static String color(Object text, Color foreground) {
        requireNonNull(foreground);
        return wrap(text, foreground(foreground), DEFAULT_FOREGROUND);
    }

    /**
     * Colors the text with a foreground and a background color.
     *
     * @param text       the text
     * @param foreground the foreground color
     * @param background the background color
     * @return the formatted text
     */
    public static String color(Object text, Color foreground, Color background) {
        requireNonNull(foreground);
        requireNonNull(background);
        return wrap(text, foreground(foreground) + background(background), DEFAULT_FOREGROUND + DEFAULT_BACKGROUND);
    }

    /**
     * Colors the text with a foreground color from the 256 colors palette.
     *
     * @param text  the text
     * @param index the color index, between 0 and 255
     * @return the formatted text
     */
    public static String color(Object text, int index) {
        return wrap(text, foreground(index), DEFAULT_FOREGROUND);
    }

    /**
     * Colors the text with a true color (24-bit) foreground color.
     *
     * @param text the text
     * @param hex  the color as hex, with or without "#"
     * @return the formatted text
     */
    public static String color(Object text, String hex) {
        return wrap(text, foreground(hex), DEFAULT_FOREGROUND);
    }

    /**
     * Highlights the text (black text on a yellow background).
     *
     * @param text the text
     * @return the formatted text
     */
    public static String highlight(Object text) {
        return color(text, Color.BLACK, Color.YELLOW);
    }

    /**
     * Highlights the text with a background color.
     *
     * @param text       the text
     * @param background the background color
     * @return the formatted text
     */
    public static String highlight(Object text, Color background) {
        requireNonNull(background);
        return wrap(text, background(background), DEFAULT_BACKGROUND);
    }

    /**
     * Applies one or more styles to the text.
     *
     * @param text   the text
     * @param styles the styles
     * @return the formatted text
     */
    public static String style(Object text, Style... styles) {
        requireNonNull(styles);
        StringBuilder on = new StringBuilder();
        StringBuilder off = new StringBuilder();
        for (Style style : styles) {
            on.append(on(style));
            off.insert(0, off(style));
        }
        return wrap(text, on.toString(), off.toString());
    }

    /**
     * Makes the text bold.
     *
     * @param text the text
     * @return the formatted text
     */
    public static String bold(Object text) {
        return style(text, Style.BOLD);
    }

    /**
     * Makes the text dim (faint).
     *
     * @param text the text
     * @return the formatted text
     */
    public static String dim(Object text) {
        return style(text, Style.DIM);
    }

    /**
     * Makes the text italic.
     *
     * @param text the text
     * @return the formatted text
     */
    public static String italic(Object text) {
        return style(text, Style.ITALIC);
    }

    /**
     * Underlines the text.
     *
     * @param text the text
     * @return the formatted text
     */
    public static String underline(Object text) {
        return style(text, Style.UNDERLINE);
    }

    /**
     * Strikes through the text.
     *
     * @param text the text
     * @return the formatted text
     */
    public static String strikethrough(Object text) {
        return style(text, Style.STRIKETHROUGH);
    }

    /**
     * Swaps the foreground and background colors of the text.
     *
     * @param text the text
     * @return the formatted text
     */
    public static String reverse(Object text) {
        return style(text, Style.REVERSE);
    }

    /**
     * Creates a clickable hyperlink (OSC 8), supported by most modern terminals.
     *
     * @param text the text displayed
     * @param url  the URL
     * @return the formatted text
     */
    public static String link(Object text, String url) {
        requireNonNull(url);
        return wrap(text, ESC + "]8;;" + url + ESC + "\\", ESC + "]8;;" + ESC + "\\");
    }

    /**
     * Removes all ANSI escape sequences from the text.
     *
     * @param text the text
     * @return the text without escape sequences, null if the text is null
     */
    public static String strip(String text) {
        if (StringUtils.isEmpty(text) || text.indexOf(ESC) == -1) return text;
        return ANSI_PATTERN.matcher(text).replaceAll(StringUtils.EMPTY_STRING);
    }

    /**
     * Returns the number of visible characters, ignoring ANSI escape sequences.
     *
     * @param text the text
     * @return a positive integer
     */
    public static int length(String text) {
        if (text == null) return 0;
        return strip(text).length();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Builder

    /**
     * Changes the foreground color.
     *
     * @param color the color
     * @return self
     */
    public Ansi fg(Color color) {
        requireNonNull(color);
        return code(foreground(color));
    }

    /**
     * Changes the foreground color using the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return self
     */
    public Ansi fg(int index) {
        return code(foreground(index));
    }

    /**
     * Changes the foreground color using a true color (24-bit).
     *
     * @param red   the red component, between 0 and 255
     * @param green the green component, between 0 and 255
     * @param blue  the blue component, between 0 and 255
     * @return self
     */
    public Ansi fg(int red, int green, int blue) {
        return code(foreground(red, green, blue));
    }

    /**
     * Changes the foreground color using a true color (24-bit).
     *
     * @param hex the color as hex, with or without "#"
     * @return self
     */
    public Ansi fg(String hex) {
        return code(foreground(hex));
    }

    /**
     * Changes the background color.
     *
     * @param color the color
     * @return self
     */
    public Ansi bg(Color color) {
        requireNonNull(color);
        return code(background(color));
    }

    /**
     * Changes the background color using the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return self
     */
    public Ansi bg(int index) {
        return code(background(index));
    }

    /**
     * Changes the background color using a true color (24-bit).
     *
     * @param red   the red component, between 0 and 255
     * @param green the green component, between 0 and 255
     * @param blue  the blue component, between 0 and 255
     * @return self
     */
    public Ansi bg(int red, int green, int blue) {
        return code(background(red, green, blue));
    }

    /**
     * Changes the background color using a true color (24-bit).
     *
     * @param hex the color as hex, with or without "#"
     * @return self
     */
    public Ansi bg(String hex) {
        return code(background(hex));
    }

    /**
     * Turns on a style.
     *
     * @param style the style
     * @return self
     */
    public Ansi style(Style style) {
        requireNonNull(style);
        return code(on(style));
    }

    /**
     * Turns off a style.
     *
     * @param style the style
     * @return self
     */
    public Ansi styleOff(Style style) {
        requireNonNull(style);
        return code(off(style));
    }

    /**
     * Turns on bold.
     *
     * @return self
     */
    public Ansi bold() {
        return style(Style.BOLD);
    }

    /**
     * Turns on dim (faint).
     *
     * @return self
     */
    public Ansi dim() {
        return style(Style.DIM);
    }

    /**
     * Turns on italic.
     *
     * @return self
     */
    public Ansi italic() {
        return style(Style.ITALIC);
    }

    /**
     * Turns on underline.
     *
     * @return self
     */
    public Ansi underline() {
        return style(Style.UNDERLINE);
    }

    /**
     * Turns on strikethrough.
     *
     * @return self
     */
    public Ansi strikethrough() {
        return style(Style.STRIKETHROUGH);
    }

    /**
     * Turns on reverse (swaps foreground and background colors).
     *
     * @return self
     */
    public Ansi reverse() {
        return style(Style.REVERSE);
    }

    /**
     * Appends text.
     *
     * @param text the text
     * @return self
     */
    public Ansi a(Object text) {
        builder.append(text);
        return this;
    }

    /**
     * Appends a new line.
     *
     * @return self
     */
    public Ansi newline() {
        builder.append(System.lineSeparator());
        return this;
    }

    /**
     * Resets all colors and styles.
     *
     * @return self
     */
    public Ansi reset() {
        return code(RESET);
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    private Ansi code(String code) {
        if (enabled) builder.append(code);
        return this;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Internals

    private static String wrap(Object text, String on, String off) {
        String value = String.valueOf(text);
        if (!enabled || value.isEmpty()) return value;
        return on + value + off;
    }

    private static int colorOffset(Color color) {
        int index = color.getIndex();
        if (index < 0) return 9;
        return index < 8 ? index : 60 + index - 8;
    }

    private static String rgb(int red, int green, int blue) {
        requireBounded(red, 0, 255);
        requireBounded(green, 0, 255);
        requireBounded(blue, 0, 255);
        return red + ";" + green + ";" + blue;
    }

    private static int[] parseHex(String hex) {
        requireNonNull(hex);
        String value = hex.startsWith("#") ? hex.substring(1) : hex;
        if (value.length() == 3) {
            value = "" + value.charAt(0) + value.charAt(0) + value.charAt(1) + value.charAt(1) + value.charAt(2) + value.charAt(2);
        }
        if (value.length() != 6) throw new IllegalArgumentException("Invalid hex color: " + hex);
        try {
            int rgb = Integer.parseInt(value, 16);
            return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color: " + hex, e);
        }
    }

    private static boolean detectSupport() {
        if (System.getenv("NO_COLOR") != null) return false;
        String forceColor = System.getenv("FORCE_COLOR");
        if (forceColor != null) return !"0".equals(forceColor) && !"false".equalsIgnoreCase(forceColor);
        if (System.console() == null) return false;
        String term = System.getenv("TERM");
        if ("dumb".equalsIgnoreCase(term)) return false;
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
        if (!windows) return true;
        // classic Windows console does not process escape sequences unless the application enables it
        return term != null || System.getenv("WT_SESSION") != null || System.getenv("TERM_PROGRAM") != null
                || System.getenv("ANSICON") != null || "ON".equalsIgnoreCase(System.getenv("ConEmuANSI"));
    }
}
