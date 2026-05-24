package net.microfalx.lang;

import java.text.MessageFormat;
import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Utilities around strings.
 */
public class StringUtils {

    public static final String EMPTY_STRING = "";
    public static final char SPACE_CHAR = ' ';
    public static final String SPACE = " ";
    public static final String NA_STRING = "N/A";
    public static final String NA_ID_STRING = "na";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final String ELLIPSES = "...";
    public static final String ELLIPSES_WITH_SPACES = " " + ELLIPSES + " ";
    public static final String COMMA = ",";
    public static final String COMMA_WITH_SPACE = ", ";

    /**
     * Returns whether the string is empty.
     *
     * @param value the string to validate
     * @return {@code true} if empty, @{code false} otherwise
     */
    public static boolean isEmpty(CharSequence value) {
        return value == null || value.length() == 0;
    }

    /**
     * Returns whether the string is not empty.
     *
     * @param value the string to validate
     * @return {@code true} if empty, @{code false} otherwise
     */
    public static boolean isNotEmpty(CharSequence value) {
        return !isEmpty(value);
    }

    /**
     * Returns a default value if the input is empty.
     *
     * @param value        the value
     * @param defaultValue the default value
     * @return the original value or a default
     */
    public static String defaultIfEmpty(String value, String defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }

    /**
     * Returns a default value if the input is null.
     *
     * @param value        the value
     * @param defaultValue the default value
     * @return the original value or a default
     */
    public static String defaultIfNull(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Returns an empty string if the input is null.
     *
     * @param value the value
     * @return the original value or a default
     */
    public static String emptyIfNull(String value) {
        return value == null ? EMPTY_STRING : value;
    }

    /**
     * Returns a null string if the input is empty.
     *
     * @param value the value
     * @return the original value or null
     */
    public static String nullIfEmpty(String value) {
        return isEmpty(value) ? null : value;
    }

    /**
     * Returns <code>N/A</code> value if the input is empty.
     *
     * @param value the value
     * @return the original value or a default
     */
    public static String naIfEmpty(String value) {
        return isEmpty(value) ? NA_STRING : value;
    }

    /**
     * Returns whether the fragment is contained in the text, case-insensitive
     *
     * @param text     the  String to test
     * @param fragment the fragment to be searched withing the text
     * @return {@code true} if contained, {@code false} otherwise
     */
    @SuppressWarnings("Duplicates")
    public static boolean contains(String text, String fragment) {
        if (text == null && fragment == null) return true;
        if (text == null) return false;
        return text.toLowerCase().contains(fragment.toLowerCase());
    }

    /**
     * Returns whether the string contains multiple lines of text.
     *
     * @param value the value to test
     * @return {@code true} if it contains more than one line, {@code false} otherwise
     */
    public static boolean containsNewLines(String value) {
        if (value == null) return false;
        char[] chars = value.toCharArray();
        for (char c : chars) {
            if (c == '\n' || c == '\r') return true;
        }
        return false;
    }

    /**
     * Returns whether the string contains at least one white spaces: spaces, tabs, new lines, etc.
     *
     * @param value the value to test
     * @return {@code true} if it contains only white spaces, {@code false} otherwise
     */
    public static boolean containsWhiteSpaces(String value) {
        if (value == null) return false;
        char[] chars = value.toCharArray();
        for (char c : chars) {
            if (Character.isWhitespace(c)) return true;
        }
        return false;
    }

    /**
     * Returns whether the string contains only white spaces: spaces, tabs, new lines, etc.
     *
     * @param value the value to test
     * @return {@code true} if it contains only white spaces, {@code false} otherwise
     */
    public static boolean containsWhiteSpacesOnly(String value) {
        if (value == null) return false;
        char[] chars = value.toCharArray();
        for (char c : chars) {
            if (!Character.isWhitespace(c)) return false;
        }
        return true;
    }

    /**
     * Converts a String to an identifier.
     * <p>
     * The identifier contains only chars, digits and "_".
     *
     * @param value the String value
     * @return the identifier
     */
    @SuppressWarnings("Duplicates")
    public static String toIdentifier(String value) {
        return toIdentifier(value, false);
    }

    /**
     * Converts a collection of string to an identifier.
     * <p>
     * Any NULL value is eliminated from the identifier. At least one identifier needs to be present.
     *
     * @param values the String values
     * @return the identifier
     */
    public static String toIdentifier(String... values) {
        if (ObjectUtils.isEmpty(values)) throw new IllegalArgumentException("At least one value needs to be provided");
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value != null) append(builder, value, "_");
        }
        return StringUtils.toIdentifier(builder.toString());
    }

    /**
     * Converts a string to lower case.
     *
     * @param value the value, can be null
     * @return the string lower case
     */
    public static String toLowerCase(String value) {
        return value != null ? value.toLowerCase() : null;
    }

    /**
     * Converts a string to upper case.
     *
     * @param value the value, can be null
     * @return the string upper case
     */
    public static String toUpperCase(String value) {
        return value != null ? value.toUpperCase() : null;
    }

    /**
     * Returns a string containing the same character Nth times.
     *
     * @param c      the character
     * @param length the number of characters
     * @return a non-null instance
     */
    public static String getStringOfChar(char c, int length) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buffer.append(c);
        }
        return buffer.toString();
    }

    /**
     * Returns whether these two strings are equal (case-sensitive).
     *
     * @param a the first string
     * @param b the seconds string
     * @return <code>true</code> if equal, <code>false</code> otherwise
     */
    public static boolean equals(String a, String b) {
        return Objects.equals(a, b);
    }

    /**
     * Returns whether these two strings are equal (case-insensitive).
     *
     * @param a the first string
     * @param b the second string
     * @return <code>true</code> if equal, <code>false</code> otherwise
     */
    public static boolean equalsIgnoreCase(String a, String b) {
        return a == b || (a != null && a.equalsIgnoreCase(b));
    }

    /**
     * Joins all the strings with a standard separator to separate the values
     *
     * @param names the names
     * @return a non-null string
     */
    public static String joinNames(String... names) {
        if (ObjectUtils.isEmpty(names)) return NA_STRING;
        return String.join(" :: ", names);
    }

    /**
     * Returns if the value exists in the array of values, case-insensitive.
     *
     * @param value  the value to check
     * @param values the values
     * @return <code>true</code> if the value exists in array, <code>false</code> otherwise
     */
    public static boolean containsInArray(char value, char[] values) {
        if (values == null || value == 0x00) return false;
        for (char _value : values) {
            if (Character.toLowerCase(value) == Character.toLowerCase(_value)) return true;
        }
        return false;
    }

    /**
     * Returns if the value exists in the array of values, case-insensitive.
     *
     * @param value  the value to check
     * @param values the values
     * @return <code>true</code> if the value exists in array, <code>false</code> otherwise
     */
    public static boolean containsInArray(String value, String[] values) {
        if (value == null || values == null) return false;
        for (String _value : values) {
            if (value.equalsIgnoreCase(_value)) return true;
        }
        return false;
    }

    /**
     * Returns if the value exists in the array of values, case-insensitive.
     *
     * @param value  the value to check
     * @param values the values
     * @return <code>true</code> if the value exists in array, <code>false</code> otherwise
     */
    public static boolean containsInCollection(String value, Collection<String> values) {
        if (value == null || values == null) return false;
        for (String _value : values) {
            if (value.equalsIgnoreCase(_value)) return true;
        }
        return false;
    }

    /**
     * Converts a value to a boolean.
     * <p>
     * A null value or a rule mismatch will result in using the default value
     * <p>
     * The following values are considered <code>true</code>
     * - any number that is not equal to 0
     * - a boolean object with value TRUE
     * - a string is "true","yes","y", "t","on", and "1"
     * The following values are considered <code>false</code>
     * - any number that is equal to 0
     * - a boolean object with value FALSE
     * - a string is "false","no","n","f","off", and "0"
     *
     * @param value        the value
     * @param defaultValue the default value when null or no rule is matched
     * @return the boolean value
     */
    public static boolean asBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        } else {
            String valueAsString = value.toString();
            if ("y".equalsIgnoreCase(valueAsString) || "yes".equalsIgnoreCase(valueAsString) || "on".equalsIgnoreCase(valueAsString)
                    || "true".equalsIgnoreCase(valueAsString) || "t".equalsIgnoreCase(valueAsString) || "1".equalsIgnoreCase(valueAsString)) {
                return true;
            } else if ("n".equalsIgnoreCase(valueAsString) || "no".equalsIgnoreCase(valueAsString) || "off".equalsIgnoreCase(valueAsString)
                    || "false".equalsIgnoreCase(valueAsString) || "f".equalsIgnoreCase(valueAsString) || "0".equalsIgnoreCase(valueAsString)) {
                return false;
            } else {
                return defaultValue;
            }
        }
    }

    /**
     * Converts a String to an identifier and replaces "_" with "-".
     * <p>
     * The identifier contains only chars, digits and "-".
     *
     * @param value the path
     * @return the id
     * @see #toIdentifier(String)
     */
    public static String toDashIdentifier(String value) {
        value = toIdentifier(value);
        return value.replace('_', '-');
    }

    /**
     * Converts a String to an identifier.
     * <p>
     * The return identifier contains only characters, digits and "_" (and "-" if allowed). If "-" is not allow and
     * is in the value, replace it with "_".
     *
     * @param value the path
     * @return the identifier
     */
    public static String toIdentifier(String value, boolean allowDash) {
        requireNonNull(value);
        StringBuilder builder = new StringBuilder(value.length());
        char prevChar = 0x00;
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (Character.isDigit(c) || Character.isAlphabetic(c) || (allowDash && c == '-')) {
                builder.append(c);
            } else {
                c = '_';
                if (prevChar != c) builder.append(c);
            }
            prevChar = c;
        }
        String identifier = builder.toString();
        if (identifier.startsWith("_")) identifier = identifier.substring(1);
        if (identifier.endsWith("_")) identifier = identifier.substring(0, identifier.length() - 1);
        return identifier.toLowerCase();
    }

    /**
     * Trims a value.
     *
     * @param value the value to be trimmed
     * @return the trimmed value
     */
    public static String trim(String value) {
        return trim(value, true);
    }

    /**
     * Trims a value.
     *
     * @param value the value to be trimmed
     * @return the trimmed value
     */
    public static String trim(String value, boolean allowNull) {
        return value != null ? value.trim() : allowNull ? null : EMPTY_STRING;
    }

    /**
     * Capitalizes the given string
     *
     * @param value the string value
     * @return capitalized string
     */
    public static String capitalize(String value) {
        if (isEmpty(value)) return value;
        value = value.toLowerCase().trim();
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * Capitalizes the given string by capitalizing first letter.
     *
     * @param value the string value
     * @return capitalized string
     */
    public static String capitalizeFirst(String value) {
        if (isEmpty(value)) return value;
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * Unvapitalizes the given string by not capitalizing first letter.
     *
     * @param value the string value
     * @return capitalized string
     */
    public static String uncapitalizeFirst(String value) {
        if (isEmpty(value)) return value;
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * Capitalizes each word in the given string.
     * <p>
     * The method also replaces delimiter characters with space to create multiple works out of delimiters.
     *
     * @param value the string value
     * @return capitalized string
     */
    public static String capitalizeWords(String value) {
        if (isEmpty(value)) return value;
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (containsInArray(c, CAPITALIZATION_SPECIAL_CHARS)) chars[i] = ' ';
        }
        value = String.valueOf(chars);
        StringBuilder rc = new StringBuilder();
        for (String word : split(value, " ")) {
            rc.append(capitalize(word)).append(" ");
        }
        return rc.toString().trim();
    }

    /**
     * Translates camel case into words separated by spaces.
     *
     * @param value the value to normalize
     * @return the beautified value
     */
    public static String beautifyCamelCase(String value) {
        if (isEmpty(value)) return value;
        StringBuilder builder = new StringBuilder(value.length() * 2);
        value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
        Case prevCase = Case.NONE;
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            Case currentCase = Character.isLowerCase(c) ? Case.LOWER_CASE : Case.UPPER_CASE;
            if (prevCase == Case.LOWER_CASE && currentCase == Case.UPPER_CASE) builder.append(" ");
            builder.append(c);
            prevCase = currentCase;
        }
        return builder.toString().trim();
    }

    /**
     * Removes new lines from a string.
     *
     * @param value the value
     * @return the value without new lines
     */
    public static String removeLineBreaks(String value) {
        return removeLineBreaks(value, ELLIPSES_WITH_SPACES);
    }

    /**
     * Removes new lines from a string.
     *
     * @param value     the value
     * @param separator the separator to use to separate the lines of the string
     * @return the value without new lines
     */
    public static String removeLineBreaks(String value, String separator) {
        if (isEmpty(value)) return value;
        StringBuilder builder = new StringBuilder();
        value.lines().forEach(s -> {
            if (builder.length() > 0) builder.append(separator);
            builder.append(s);
        });
        return builder.toString();
    }

    /**
     * Joins an array of strings.
     *
     * @param glue  the delimiter to join the strings together
     * @param parts the strings to concatenate
     * @return a non-null instance
     * @see #split(String, String, boolean)
     */
    public static String join(String glue, String... parts) {
        StringBuilder builder = new StringBuilder(100);
        for (String part : parts) {
            if (builder.length() > 0) builder.append(glue);
            builder.append(part);
        }
        return builder.toString();
    }

    /**
     * Splits a string using a list of delimiters.
     * <p>
     * The method automatically trims the tokens.
     *
     * @param string the string
     * @param delims the delimiters
     * @return a non-null instance
     * @see #split(String, String, boolean)
     */
    public static String[] split(String string, String delims) {
        return split(string, delims, true);
    }

    /**
     * Splits a string using a list of delimiters.
     *
     * @param string the string
     * @param delims the delimiters
     * @param trim   {@code true} to trim the tokens, {@code false} otherwise
     * @return a non-null instance
     */
    public static String[] split(String string, String delims, boolean trim) {
        if (string == null) return EMPTY_STRING_ARRAY;
        StringTokenizer st = new StringTokenizer(string, delims, false);
        String[] values = new String[st.countTokens()];
        int index = 0;
        while (st.hasMoreElements()) {
            String value = (String) st.nextElement();
            values[index++] = trim ? value.trim() : value;
        }

        return values;
    }

    /**
     * Replaces each substring of the text String that matches the given pattern
     * with the given replacement.
     *
     * @param text        text to search and replace in, may be null
     * @param pattern     the pattern to search for a replacement
     * @param replacement the string to be substituted for each match
     * @return the new text.
     */
    public static String replaceAll(String text, String pattern, String replacement) {
        return doReplace(text, pattern, replacement, false);
    }

    /**
     * Replaces first substring of the text String that matches the given pattern
     * with the given replacement.
     *
     * @param text        text to search and replace in, may be null
     * @param pattern     the pattern to search for a replacement
     * @param replacement the string to be substituted for each match
     * @return the new text.
     */
    public static String replaceFirst(String text, String pattern, String replacement) {
        return doReplace(text, pattern, replacement, true);
    }

    private static String doReplace(String text, String pattern, String replacement, boolean onlyFirst) {
        if (text == null || pattern == null) return null;
        int startIndex = 0;
        int patternLength = pattern.length();
        while ((startIndex = text.indexOf(pattern, startIndex)) != -1) {
            text = text.substring(0, startIndex) + replacement +
                    text.substring(startIndex + patternLength);
            startIndex += replacement.length();
            if (startIndex >= text.length()) break;
            if (onlyFirst) break;
        }
        return text;
    }

    /**
     * Appends a value to a builder, inserting a {@code ,} if the builder always contains content.
     * <p>
     * A null value is ignored by the method.
     *
     * @param builder the builder
     * @param value   the value, null is ignored
     * @return the builder
     */
    public static StringBuilder append(StringBuilder builder, Object value) {
        return append(builder, value, COMMA);
    }

    /**
     * Appends a value to a builder, inserting a separator character if the builder always contains content.
     * <p>
     * A null value is ignored by the method.
     *
     * @param builder   the builder
     * @param value     the value, null is ignored
     * @param separator the separator, ignored if not printable
     * @return the builder
     */
    public static StringBuilder append(StringBuilder builder, Object value, char separator) {
        ArgumentUtils.requireNonNull(builder);
        if (value == null) return builder;
        if (builder.length() > 0 && separator >= 0x20) builder.append(separator);
        builder.append(ObjectUtils.toString(value));
        return builder;
    }

    /**
     * Appends a value to a builder, inserting a separator character if the builder always contains content.
     * <p>
     * A null value is ignored by the method.
     *
     * @param builder   the builder
     * @param value     the value, null is ignored
     * @param separator the separator, ignored if not printable
     * @return the builder
     */
    public static StringBuilder append(StringBuilder builder, Object value, String separator) {
        ArgumentUtils.requireNonNull(builder);
        if (value == null) return builder;
        if (builder.length() > 0) builder.append(separator);
        builder.append(ObjectUtils.toString(value));
        return builder;
    }

    /**
     * Returns a maximum number of lines out of a text.
     *
     * @param text         the text
     * @param maximumLines the maximum number of lines, -1 for automatic
     * @return the lines
     */
    public static String getMaximumLines(String text, int maximumLines) {
        if (isEmpty(text)) return text;
        if (maximumLines <= 0) return text;
        StringBuilder builder = new StringBuilder();
        try (Scanner sc = new Scanner(text)) {
            while (sc.hasNextLine() && maximumLines-- > 0) {
                if (builder.length() > 0) builder.append("\n");
                builder.append(sc.nextLine());
            }
        }
        return builder.toString();
    }

    /**
     * Removes backslash or forward slash from the value if found at the beginning of the string.
     *
     * @param value the string value
     * @return the string value without backslash or forward slash
     */
    public static String removeStartSlash(String value) {
        if (isEmpty(value)) return value;
        char c = value.charAt(0);
        if (c == '/' || c == '\\') return value.substring(1);
        return value;
    }


    /**
     * Removes backslash or forward slash from the value if found at the end of the string.
     *
     * @param value the string value
     * @return the string value without backslash or forward slash
     */
    public static String removeEndSlash(String value) {
        if (isEmpty(value)) return value;
        char c = value.charAt(value.length() - 1);
        if (c == '/' || c == '\\') return value.substring(0, value.length() - 1);
        return value;
    }

    /**
     * Adds forward slash at the end of the value, if needed.
     *
     * @param value the string value
     * @return the string value without backslash or forward slash
     */
    public static String addEndSlash(String value) {
        if (isEmpty(value)) return "/";
        char c = value.charAt(value.length() - 1);
        if (c != '/') value += "/";
        return value;
    }

    /**
     * Adds forward slash at the beginning of the value, if needed.
     *
     * @param value the string value
     * @return the string value without backslash or forward slash
     */
    public static String addStartSlash(String value) {
        if (isEmpty(value)) return "/";
        char c = value.charAt(0);
        if (c != '/') value = "/" + value;
        return value;
    }

    /**
     * Formats a message.
     *
     * @param pattern   the pattern message
     * @param arguments the arguments passed for format the message
     * @return the formatted message
     * @see java.text.MessageFormat#format(String, Object...)
     */
    public static String formatMessage(String pattern, Object... arguments) {
        if (StringUtils.isEmpty(pattern)) {
            return "Unmapped message, null/empty pattern, arguments: " + Arrays.asList(arguments);
        }
        try {
            return MessageFormat.format(pattern, arguments);
        } catch (Exception e) {
            return pattern + "[" + Arrays.toString(arguments) + "]";
        }
    }

    public enum Case {
        NONE,
        LOWER_CASE,
        UPPER_CASE
    }

    private static final char[] CAPITALIZATION_SPECIAL_CHARS = new char[]{'.', '_', '-', ',', '$', '&'};
}
