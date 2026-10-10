package net.microfalx.lang;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.microfalx.lang.RichText.Color;
import static net.microfalx.lang.RichText.Style;
import static net.microfalx.lang.ArgumentUtils.requireBounded;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * The HTML counterpart of {@link Ansi}: colors and formats text using {@code <span>} elements with inline styles.
 * <p>
 * It uses the same {@link Color colors} and {@link Style styles} as {@link Ansi}; use {@link RichText} to create text
 * which can be rendered as plain text, ANSI or HTML. The class can be used in two ways:
 * <ul>
 *     <li>static helpers which wrap a piece of text, e.g. {@code Html.color("error", Color.RED)}</li>
 *     <li>a fluent builder, e.g. {@code Html.html().fg(Color.RED).bold().a("error").reset().toString()}</li>
 * </ul>
 * <p>
 * Text is always HTML escaped, which means the output of a helper cannot be passed to another helper (it would be
 * escaped again). Use {@link #format(Object, Color, Color, Style...)} or the builder to combine colors and styles,
 * and {@link #raw(Object)} to append HTML produced elsewhere to a builder.
 * <p>
 * The helpers and the builder honor the {@link #isEnabled() enabled} flag: when disabled they return the
 * (escaped) text without any formatting.
 * <p>
 * The standard colors are rendered with a fixed palette (close to the VS Code terminal theme), since unlike a
 * terminal, a browser has no theme to resolve them. {@link Color#DEFAULT} renders as {@code inherit}.
 */
public final class Html {

    private static final String[] PALETTE = {
            "#000000", "#cd3131", "#0dbc79", "#e5e510", "#2472c8", "#bc3fbc", "#11a8cd", "#e5e5e5",
            "#666666", "#f14c4c", "#23d18b", "#f5f543", "#3b8eea", "#d670d6", "#29b8db", "#ffffff"
    };

    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern ENTITY_PATTERN = Pattern.compile("&(#[0-9]+|#[xX][0-9a-fA-F]+|[a-zA-Z]+);");

    private static volatile boolean enabled = true;

    private final StringBuilder builder = new StringBuilder();
    private String foreground;
    private String background;
    private final Set<Style> styles = EnumSet.noneOf(Style.class);
    private String openStyle;

    private Html() {
    }

    /**
     * Creates a fluent builder for HTML formatted text.
     *
     * @return a non-null instance
     */
    public static Html html() {
        return new Html();
    }

    /**
     * Returns whether helpers and the builder emit formatting.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Changes whether helpers and the builder emit formatting.
     *
     * @param enabled {@code true} to emit formatting, {@code false} to emit plain (escaped) text
     */
    public static void setEnabled(boolean enabled) {
        Html.enabled = enabled;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // CSS colors

    /**
     * Returns the CSS color for a standard color.
     *
     * @param color the color
     * @return a non-null instance
     */
    public static String toCss(Color color) {
        requireNonNull(color);
        return color.getIndex() < 0 ? "inherit" : PALETTE[color.getIndex()];
    }

    /**
     * Returns the CSS color for a color from the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return a non-null instance
     */
    public static String toCss(int index) {
        requireBounded(index, 0, 255);
        if (index < 16) {
            return PALETTE[index];
        } else if (index < 232) {
            index -= 16;
            return toCss(cubeLevel(index / 36), cubeLevel((index / 6) % 6), cubeLevel(index % 6));
        } else {
            int gray = 8 + (index - 232) * 10;
            return toCss(gray, gray, gray);
        }
    }

    /**
     * Returns the CSS color for a true color (24-bit) color.
     *
     * @param red   the red component, between 0 and 255
     * @param green the green component, between 0 and 255
     * @param blue  the blue component, between 0 and 255
     * @return a non-null instance
     */
    public static String toCss(int red, int green, int blue) {
        requireBounded(red, 0, 255);
        requireBounded(green, 0, 255);
        requireBounded(blue, 0, 255);
        return String.format("#%02x%02x%02x", red, green, blue);
    }

    /**
     * Returns the CSS color for a hex color.
     *
     * @param hex the color as hex, with or without "#" (e.g. "#ff8800" or "f80")
     * @return a non-null instance
     */
    public static String toCss(String hex) {
        requireNonNull(hex);
        String value = hex.startsWith("#") ? hex.substring(1) : hex;
        if ((value.length() != 3 && value.length() != 6) || !value.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
            throw new IllegalArgumentException("Invalid hex color: " + hex);
        }
        return "#" + value.toLowerCase();
    }

    /**
     * Returns the CSS declarations for a style.
     *
     * @param style the style
     * @return a non-null instance
     */
    public static String toCss(Style style) {
        requireNonNull(style);
        return toCss(null, null, EnumSet.of(style));
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
        return format(text, foreground, null);
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
        return format(text, foreground, background);
    }

    /**
     * Colors the text with a foreground color from the 256 colors palette.
     *
     * @param text  the text
     * @param index the color index, between 0 and 255
     * @return the formatted text
     */
    public static String color(Object text, int index) {
        return wrap(text, "color:" + toCss(index));
    }

    /**
     * Colors the text with a true color (24-bit) foreground color.
     *
     * @param text the text
     * @param hex  the color as hex, with or without "#"
     * @return the formatted text
     */
    public static String color(Object text, String hex) {
        return wrap(text, "color:" + toCss(hex));
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
        return format(text, null, background);
    }

    /**
     * Applies one or more styles to the text.
     *
     * @param text   the text
     * @param styles the styles
     * @return the formatted text
     */
    public static String style(Object text, Style... styles) {
        return format(text, null, null, styles);
    }

    /**
     * Applies colors and styles to the text.
     *
     * @param text       the text
     * @param foreground the foreground color, can be null
     * @param background the background color, can be null
     * @param styles     the styles
     * @return the formatted text
     */
    public static String format(Object text, Color foreground, Color background, Style... styles) {
        requireNonNull(styles);
        Set<Style> set = EnumSet.noneOf(Style.class);
        for (Style style : styles) {
            set.add(requireNonNull(style));
        }
        return wrap(text, toCss(foreground != null ? toCss(foreground) : null,
                background != null ? toCss(background) : null, set));
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
     * Swaps the foreground and background colors of the text (rendered by inverting the colors).
     *
     * @param text the text
     * @return the formatted text
     */
    public static String reverse(Object text) {
        return style(text, Style.REVERSE);
    }

    /**
     * Creates a hyperlink.
     *
     * @param text the text displayed
     * @param url  the URL
     * @return the formatted text
     */
    public static String link(Object text, String url) {
        requireNonNull(url);
        String value = escape(text);
        if (!enabled || value.isEmpty()) return value;
        return "<a href=\"" + escape(url) + "\">" + value + "</a>";
    }

    /**
     * Escapes the characters with a special meaning in HTML (both in text and in attribute values).
     *
     * @param text the text
     * @return the escaped text, empty if the text is null
     */
    public static String escape(Object text) {
        if (text == null) return StringUtils.EMPTY_STRING;
        String value = text.toString();
        StringBuilder buffer = null;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            String replacement = switch (c) {
                case '<' -> "&lt;";
                case '>' -> "&gt;";
                case '&' -> "&amp;";
                case '"' -> "&quot;";
                case '\'' -> "&#39;";
                default -> null;
            };
            if (replacement != null && buffer == null) {
                buffer = new StringBuilder(value.length() + 16);
                buffer.append(value, 0, i);
            }
            if (buffer != null) {
                if (replacement != null) {
                    buffer.append(replacement);
                } else {
                    buffer.append(c);
                }
            }
        }
        return buffer != null ? buffer.toString() : value;
    }

    /**
     * Removes all HTML tags from the text and decodes the HTML entities.
     *
     * @param text the text
     * @return the plain text, null if the text is null
     */
    public static String strip(String text) {
        if (StringUtils.isEmpty(text)) return text;
        String value = TAG_PATTERN.matcher(text).replaceAll(StringUtils.EMPTY_STRING);
        if (value.indexOf('&') == -1) return value;
        Matcher matcher = ENTITY_PATTERN.matcher(value);
        StringBuilder buffer = new StringBuilder(value.length());
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(decodeEntity(matcher.group(1), matcher.group())));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Returns the number of visible characters, ignoring HTML tags and counting an entity as one character.
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
    public Html fg(Color color) {
        foreground = toCss(color);
        return this;
    }

    /**
     * Changes the foreground color using the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return self
     */
    public Html fg(int index) {
        foreground = toCss(index);
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
    public Html fg(int red, int green, int blue) {
        foreground = toCss(red, green, blue);
        return this;
    }

    /**
     * Changes the foreground color using a true color (24-bit).
     *
     * @param hex the color as hex, with or without "#"
     * @return self
     */
    public Html fg(String hex) {
        foreground = toCss(hex);
        return this;
    }

    /**
     * Changes the background color.
     *
     * @param color the color
     * @return self
     */
    public Html bg(Color color) {
        background = toCss(color);
        return this;
    }

    /**
     * Changes the background color using the 256 colors palette.
     *
     * @param index the color index, between 0 and 255
     * @return self
     */
    public Html bg(int index) {
        background = toCss(index);
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
    public Html bg(int red, int green, int blue) {
        background = toCss(red, green, blue);
        return this;
    }

    /**
     * Changes the background color using a true color (24-bit).
     *
     * @param hex the color as hex, with or without "#"
     * @return self
     */
    public Html bg(String hex) {
        background = toCss(hex);
        return this;
    }

    /**
     * Turns on a style.
     *
     * @param style the style
     * @return self
     */
    public Html style(Style style) {
        styles.add(requireNonNull(style));
        return this;
    }

    /**
     * Turns off a style.
     *
     * @param style the style
     * @return self
     */
    public Html styleOff(Style style) {
        styles.remove(requireNonNull(style));
        return this;
    }

    /**
     * Turns on bold.
     *
     * @return self
     */
    public Html bold() {
        return style(Style.BOLD);
    }

    /**
     * Turns on dim (faint).
     *
     * @return self
     */
    public Html dim() {
        return style(Style.DIM);
    }

    /**
     * Turns on italic.
     *
     * @return self
     */
    public Html italic() {
        return style(Style.ITALIC);
    }

    /**
     * Turns on underline.
     *
     * @return self
     */
    public Html underline() {
        return style(Style.UNDERLINE);
    }

    /**
     * Turns on strikethrough.
     *
     * @return self
     */
    public Html strikethrough() {
        return style(Style.STRIKETHROUGH);
    }

    /**
     * Turns on reverse (rendered by inverting the colors).
     *
     * @return self
     */
    public Html reverse() {
        return style(Style.REVERSE);
    }

    /**
     * Appends text, which will be escaped.
     *
     * @param text the text
     * @return self
     */
    public Html a(Object text) {
        String value = escape(text);
        if (!value.isEmpty()) {
            updateSpan();
            builder.append(value);
        }
        return this;
    }

    /**
     * Appends HTML as is, without escaping it (for example, the output of a helper).
     *
     * @param html the HTML
     * @return self
     */
    public Html raw(Object html) {
        updateSpan();
        builder.append(html);
        return this;
    }

    /**
     * Appends a line break.
     *
     * @return self
     */
    public Html newline() {
        builder.append("<br>");
        return this;
    }

    /**
     * Resets all colors and styles.
     *
     * @return self
     */
    public Html reset() {
        foreground = null;
        background = null;
        styles.clear();
        return this;
    }

    @Override
    public String toString() {
        return openStyle != null ? builder + "</span>" : builder.toString();
    }

    private void updateSpan() {
        String style = enabled ? toCss(foreground, background, styles) : StringUtils.EMPTY_STRING;
        if (style.isEmpty()) style = null;
        if (Objects.equals(style, openStyle)) return;
        if (openStyle != null) builder.append("</span>");
        if (style != null) builder.append("<span style=\"").append(style).append("\">");
        openStyle = style;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Internals

    private static String wrap(Object text, String style) {
        String value = escape(text);
        if (!enabled || value.isEmpty() || style.isEmpty()) return value;
        return "<span style=\"" + style + "\">" + value + "</span>";
    }

    private static String toCss(String foreground, String background, Set<Style> styles) {
        StringBuilder css = new StringBuilder();
        if (foreground != null) css.append("color:").append(foreground).append(';');
        if (background != null) css.append("background-color:").append(background).append(';');
        StringBuilder decoration = new StringBuilder();
        for (Style style : styles) {
            switch (style) {
                case BOLD -> css.append("font-weight:bold;");
                case DIM -> css.append("opacity:0.6;");
                case ITALIC -> css.append("font-style:italic;");
                case UNDERLINE, DOUBLE_UNDERLINE -> appendDecoration(decoration, "underline");
                case BLINK -> appendDecoration(decoration, "blink");
                case REVERSE -> css.append("filter:invert(100%);");
                case HIDDEN -> css.append("visibility:hidden;");
                case STRIKETHROUGH -> appendDecoration(decoration, "line-through");
                case OVERLINE -> appendDecoration(decoration, "overline");
            }
        }
        if (!decoration.isEmpty()) css.append("text-decoration:").append(decoration).append(';');
        if (styles.contains(Style.DOUBLE_UNDERLINE)) css.append("text-decoration-style:double;");
        if (!css.isEmpty()) css.setLength(css.length() - 1);
        return css.toString();
    }

    private static void appendDecoration(StringBuilder decoration, String value) {
        if (decoration.indexOf(value) != -1) return;
        if (!decoration.isEmpty()) decoration.append(' ');
        decoration.append(value);
    }

    private static int cubeLevel(int value) {
        return value == 0 ? 0 : 55 + value * 40;
    }

    private static String decodeEntity(String entity, String original) {
        try {
            if (entity.startsWith("#x") || entity.startsWith("#X")) {
                return new String(Character.toChars(Integer.parseInt(entity.substring(2), 16)));
            } else if (entity.startsWith("#")) {
                return new String(Character.toChars(Integer.parseInt(entity.substring(1))));
            }
        } catch (IllegalArgumentException e) {
            return original;
        }
        return switch (entity) {
            case "lt" -> "<";
            case "gt" -> ">";
            case "amp" -> "&";
            case "quot" -> "\"";
            case "apos" -> "'";
            case "nbsp" -> " ";
            default -> original;
        };
    }
}
