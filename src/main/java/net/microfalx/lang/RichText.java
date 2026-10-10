package net.microfalx.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static net.microfalx.lang.ArgumentUtils.requireBounded;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Text with colors and styles, which can be rendered as plain text, ANSI (terminal) or HTML.
 * <p>
 * The text is created once, the same way regardless of where it ends up, and the {@link Format} is selected only
 * when it is rendered:
 * <pre>{@code
 * RichText text = RichText.create().a("Status: ").fg(Color.GREEN).bold().a("OK").reset().a(" (3 tests)");
 * text.render(Format.ANSI); // terminal
 * text.render(Format.HTML); // web page
 * text.render(Format.TEXT); // logs, files
 * }</pre>
 * <p>
 * Rich texts can be nested: a rich text appended with {@link #a(Object)} keeps its own colors and styles and
 * inherits the ones it does not define (like nested elements in HTML).
 * <p>
 * Rendering delegates to {@link Ansi} and {@link Html}, so their {@code enabled} flags still apply: for example,
 * when {@link Ansi#isEnabled()} is {@code false} (NO_COLOR), {@link Format#ANSI} renders plain text.
 * <p>
 * Instances are mutable (like a {@link StringBuilder}) and not thread-safe.
 */
public final class RichText {

    private static volatile Format defaultFormat = Format.TEXT;

    private final List<Segment> segments = new ArrayList<>();

    private Object foreground;
    private Object background;
    private final Set<Style> styles = EnumSet.noneOf(Style.class);

    private RichText() {
    }

    /**
     * Creates an empty rich text.
     *
     * @return a non-null instance
     */
    public static RichText create() {
        return new RichText();
    }

    /**
     * Creates a rich text with unformatted text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static RichText of(Object text) {
        return create().a(text);
    }

    /**
     * Returns the format used by {@link #toString()}.
     *
     * @return a non-null instance
     */
    public static Format getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * Changes the format used by {@link #toString()}.
     *
     * @param format the format
     */
    public static void setDefaultFormat(Format format) {
        RichText.defaultFormat = requireNonNull(format);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Text helpers

    /**
     * Creates a rich text colored with a foreground color.
     *
     * @param text       the text
     * @param foreground the foreground color
     * @return a non-null instance
     */
    public static RichText color(Object text, Color foreground) {
        return create().fg(foreground).a(text);
    }

    /**
     * Creates a rich text colored with a foreground and a background color.
     *
     * @param text       the text
     * @param foreground the foreground color
     * @param background the background color
     * @return a non-null instance
     */
    public static RichText color(Object text, Color foreground, Color background) {
        return create().fg(foreground).bg(background).a(text);
    }

    /**
     * Creates a highlighted rich text (black text on a yellow background).
     *
     * @param text the text
     * @return a non-null instance
     */
    public static RichText highlight(Object text) {
        return color(text, Color.BLACK, Color.YELLOW);
    }

    /**
     * Creates a rich text highlighted with a background color.
     *
     * @param text       the text
     * @param background the background color
     * @return a non-null instance
     */
    public static RichText highlight(Object text, Color background) {
        return create().bg(background).a(text);
    }

    /**
     * Creates a rich text with one or more styles.
     *
     * @param text   the text
     * @param styles the styles
     * @return a non-null instance
     */
    public static RichText style(Object text, Style... styles) {
        requireNonNull(styles);
        RichText richText = create();
        for (Style style : styles) {
            richText.style(style);
        }
        return richText.a(text);
    }

    /**
     * Creates a bold rich text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static RichText bold(Object text) {
        return style(text, Style.BOLD);
    }

    /**
     * Creates a dim (faint) rich text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static RichText dim(Object text) {
        return style(text, Style.DIM);
    }

    /**
     * Creates an italic rich text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static RichText italic(Object text) {
        return style(text, Style.ITALIC);
    }

    /**
     * Creates an underlined rich text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static RichText underline(Object text) {
        return style(text, Style.UNDERLINE);
    }

    /**
     * Creates a struck through rich text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static RichText strikethrough(Object text) {
        return style(text, Style.STRIKETHROUGH);
    }

    /**
     * Creates a rich text with the foreground and background colors swapped.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static RichText reverse(Object text) {
        return style(text, Style.REVERSE);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Builder

    /**
     * Changes the foreground color.
     *
     * @param color the color
     * @return self
     */
    public RichText fg(Color color) {
        foreground = requireNonNull(color);
        return this;
    }

    /**
     * Changes the foreground color using the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return self
     */
    public RichText fg(int index) {
        foreground = requireBounded(index, 0, 255);
        return this;
    }

    /**
     * Changes the foreground color using a true color (24-bit).
     *
     * @param red   the red component, between 0 and 255
     * @param green the green component, between 0 and 255
     * @param blue  the blue component, between 0 and 255
     * @return self
     */
    public RichText fg(int red, int green, int blue) {
        foreground = Html.toCss(red, green, blue);
        return this;
    }

    /**
     * Changes the foreground color using a true color (24-bit).
     *
     * @param hex the color as hex, with or without "#"
     * @return self
     */
    public RichText fg(String hex) {
        foreground = Html.toCss(hex);
        return this;
    }

    /**
     * Changes the background color.
     *
     * @param color the color
     * @return self
     */
    public RichText bg(Color color) {
        background = requireNonNull(color);
        return this;
    }

    /**
     * Changes the background color using the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return self
     */
    public RichText bg(int index) {
        background = requireBounded(index, 0, 255);
        return this;
    }

    /**
     * Changes the background color using a true color (24-bit).
     *
     * @param red   the red component, between 0 and 255
     * @param green the green component, between 0 and 255
     * @param blue  the blue component, between 0 and 255
     * @return self
     */
    public RichText bg(int red, int green, int blue) {
        background = Html.toCss(red, green, blue);
        return this;
    }

    /**
     * Changes the background color using a true color (24-bit).
     *
     * @param hex the color as hex, with or without "#"
     * @return self
     */
    public RichText bg(String hex) {
        background = Html.toCss(hex);
        return this;
    }

    /**
     * Turns on a style.
     *
     * @param style the style
     * @return self
     */
    public RichText style(Style style) {
        styles.add(requireNonNull(style));
        return this;
    }

    /**
     * Turns off a style.
     *
     * @param style the style
     * @return self
     */
    public RichText styleOff(Style style) {
        styles.remove(requireNonNull(style));
        return this;
    }

    /**
     * Turns on bold.
     *
     * @return self
     */
    public RichText bold() {
        return style(Style.BOLD);
    }

    /**
     * Turns on dim (faint).
     *
     * @return self
     */
    public RichText dim() {
        return style(Style.DIM);
    }

    /**
     * Turns on italic.
     *
     * @return self
     */
    public RichText italic() {
        return style(Style.ITALIC);
    }

    /**
     * Turns on underline.
     *
     * @return self
     */
    public RichText underline() {
        return style(Style.UNDERLINE);
    }

    /**
     * Turns on strikethrough.
     *
     * @return self
     */
    public RichText strikethrough() {
        return style(Style.STRIKETHROUGH);
    }

    /**
     * Turns on reverse (swaps foreground and background colors).
     *
     * @return self
     */
    public RichText reverse() {
        return style(Style.REVERSE);
    }

    /**
     * Resets all colors and styles.
     *
     * @return self
     */
    public RichText reset() {
        foreground = null;
        background = null;
        styles.clear();
        return this;
    }

    /**
     * Appends text with the current colors and styles.
     * <p>
     * If the text is a {@link RichText}, its segments are appended with their own colors and styles, inheriting
     * the current ones they do not define.
     *
     * @param text the text
     * @return self
     */
    public RichText a(Object text) {
        if (text instanceof RichText richText) {
            Attributes current = attributes();
            for (Segment segment : List.copyOf(richText.segments)) {
                Attributes attributes = segment.kind == Kind.NEWLINE ? Attributes.NONE : current.merge(segment.attributes);
                segments.add(new Segment(segment.kind, segment.text, segment.url, attributes));
            }
        } else {
            String value = String.valueOf(text);
            if (!value.isEmpty()) segments.add(new Segment(Kind.TEXT, value, null, attributes()));
        }
        return this;
    }

    /**
     * Appends a hyperlink with the current colors and styles.
     *
     * @param text the text displayed
     * @param url  the URL
     * @return self
     */
    public RichText link(Object text, String url) {
        requireNonNull(url);
        segments.add(new Segment(Kind.LINK, String.valueOf(text), url, attributes()));
        return this;
    }

    /**
     * Appends a new line.
     *
     * @return self
     */
    public RichText newline() {
        segments.add(new Segment(Kind.NEWLINE, StringUtils.EMPTY_STRING, null, Attributes.NONE));
        return this;
    }

    /**
     * Returns whether there is no text.
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /**
     * Returns the number of visible characters (a new line counts as one character, a link counts as its text).
     *
     * @return a positive integer
     */
    public int length() {
        int length = 0;
        for (Segment segment : segments) {
            length += segment.kind == Kind.NEWLINE ? 1 : segment.text.length();
        }
        return length;
    }

    /**
     * Renders the text in a given format.
     *
     * @param format the format
     * @return a non-null instance
     */
    public String render(Format format) {
        requireNonNull(format);
        return switch (format) {
            case TEXT -> renderText();
            case ANSI -> renderAnsi();
            case HTML -> renderHtml();
        };
    }

    /**
     * Renders the text in the {@link #getDefaultFormat() default format}.
     *
     * @return a non-null instance
     */
    @Override
    public String toString() {
        return render(defaultFormat);
    }

    private Attributes attributes() {
        return new Attributes(foreground, background, styles);
    }

    private String renderText() {
        StringBuilder builder = new StringBuilder();
        for (Segment segment : segments) {
            switch (segment.kind) {
                case TEXT -> builder.append(segment.text);
                case LINK -> {
                    builder.append(segment.text);
                    if (!segment.text.equals(segment.url)) builder.append(" (").append(segment.url).append(')');
                }
                case NEWLINE -> builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }

    private String renderAnsi() {
        Ansi ansi = Ansi.ansi();
        Attributes previous = Attributes.NONE;
        for (Segment segment : segments) {
            if (!segment.attributes.equals(previous)) {
                // reset also before a new line, so a background color does not bleed to the end of the line
                if (previous != Attributes.NONE) ansi.reset();
                Attributes attributes = segment.attributes;
                if (attributes.foreground instanceof Color color) ansi.fg(color);
                else if (attributes.foreground instanceof Integer index) ansi.fg(index);
                else if (attributes.foreground instanceof String hex) ansi.fg(hex);
                if (attributes.background instanceof Color color) ansi.bg(color);
                else if (attributes.background instanceof Integer index) ansi.bg(index);
                else if (attributes.background instanceof String hex) ansi.bg(hex);
                attributes.styles.forEach(ansi::style);
                previous = attributes.isEmpty() ? Attributes.NONE : attributes;
            }
            switch (segment.kind) {
                case TEXT -> ansi.a(segment.text);
                case LINK -> ansi.a(Ansi.link(segment.text, segment.url));
                case NEWLINE -> ansi.newline();
            }
        }
        if (previous != Attributes.NONE) ansi.reset();
        return ansi.toString();
    }

    private String renderHtml() {
        Html html = Html.html();
        for (Segment segment : segments) {
            Attributes attributes = segment.attributes;
            html.reset();
            if (attributes.foreground instanceof Color color) html.fg(color);
            else if (attributes.foreground instanceof Integer index) html.fg(index);
            else if (attributes.foreground instanceof String hex) html.fg(hex);
            if (attributes.background instanceof Color color) html.bg(color);
            else if (attributes.background instanceof Integer index) html.bg(index);
            else if (attributes.background instanceof String hex) html.bg(hex);
            attributes.styles.forEach(html::style);
            switch (segment.kind) {
                case TEXT -> html.a(segment.text);
                case LINK -> html.raw(Html.link(segment.text, segment.url));
                case NEWLINE -> html.newline();
            }
        }
        return html.toString();
    }

    /**
     * The format used to render a rich text.
     */
    public enum Format {

        /**
         * Plain text, without any formatting.
         */
        TEXT,

        /**
         * Text with ANSI escape sequences, for terminals.
         */
        ANSI,

        /**
         * HTML, using {@code <span>} elements with inline styles.
         */
        HTML
    }

    /**
     * The 16 standard colors, plus the default color.
     * <p>
     * In a terminal, the actual color depends on the terminal theme; in HTML, a fixed palette is used.
     */
    public enum Color {

        BLACK(0),
        RED(1),
        GREEN(2),
        YELLOW(3),
        BLUE(4),
        MAGENTA(5),
        CYAN(6),
        WHITE(7),
        DEFAULT(-1),
        BRIGHT_BLACK(8),
        BRIGHT_RED(9),
        BRIGHT_GREEN(10),
        BRIGHT_YELLOW(11),
        BRIGHT_BLUE(12),
        BRIGHT_MAGENTA(13),
        BRIGHT_CYAN(14),
        BRIGHT_WHITE(15);

        /**
         * Alias for {@link #BRIGHT_BLACK}.
         */
        public static final Color GRAY = BRIGHT_BLACK;

        private final int index;

        Color(int index) {
            this.index = index;
        }

        /**
         * Returns the index of the color in the standard 16 colors palette (which is also the index in the 256
         * colors palette).
         *
         * @return the index, between 0 and 15, or -1 for {@link #DEFAULT}
         */
        public int getIndex() {
            return index;
        }

        /**
         * Returns whether this is one of the bright colors.
         *
         * @return {@code true} if bright, {@code false} otherwise
         */
        public boolean isBright() {
            return index >= 8;
        }
    }

    /**
     * Text styles.
     * <p>
     * Support varies between terminals; {@link #BOLD}, {@link #UNDERLINE} and {@link #REVERSE} are almost
     * universally supported.
     */
    public enum Style {
        BOLD,
        DIM,
        ITALIC,
        UNDERLINE,
        BLINK,
        REVERSE,
        HIDDEN,
        STRIKETHROUGH,
        DOUBLE_UNDERLINE,
        OVERLINE
    }

    private enum Kind {
        TEXT,
        LINK,
        NEWLINE
    }

    private record Segment(Kind kind, String text, String url, Attributes attributes) {
    }

    /**
     * Colors are stored as {@link Color}, {@link Integer} (256 colors palette) or {@link String} (hex).
     */
    private record Attributes(Object foreground, Object background, Set<Style> styles) {

        private static final Attributes NONE = new Attributes(null, null, Collections.emptySet());

        private Attributes {
            Set<Style> copy = EnumSet.noneOf(Style.class);
            copy.addAll(styles);
            styles = Collections.unmodifiableSet(copy);
        }

        private boolean isEmpty() {
            return foreground == null && background == null && styles.isEmpty();
        }

        private Attributes merge(Attributes attributes) {
            Set<Style> merged = EnumSet.noneOf(Style.class);
            merged.addAll(styles);
            merged.addAll(attributes.styles);
            return new Attributes(attributes.foreground != null ? attributes.foreground : foreground,
                    attributes.background != null ? attributes.background : background, merged);
        }
    }
}
