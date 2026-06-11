package net.microfalx.lang;

import java.sql.Timestamp;
import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;

public class TimeUtils {

    /**
     * How many microseconds are in a millisecond.
     */
    public static final long MICROSECONDS_IN_MILLISECONDS = 1000;

    /**
     * How many microseconds are in a millisecond.
     */
    public static final long NANOSECONDS_IN_MILLISECONDS = 1000 * MICROSECONDS_IN_MILLISECONDS;

    /**
     * How many milliseconds are in a second.
     */
    public static final long MILLISECONDS_IN_SECOND = 1000;

    /**
     * How many milliseconds are in a minute.
     */
    public static final long MILLISECONDS_IN_MINUTE = 60 * MILLISECONDS_IN_SECOND;

    /**
     * How many milliseconds are in an hour.
     */
    public static final long MILLISECONDS_IN_HOUR = 60 * MILLISECONDS_IN_MINUTE;

    /**
     * How many milliseconds are in an day.
     */
    public static final long MILLISECONDS_IN_DAY = 24 * MILLISECONDS_IN_HOUR;

    /**
     * A constant for 30 seconds.
     */
    public static final long THIRTY_SECONDS = 30 * MILLISECONDS_IN_SECOND;

    /**
     * A constant for 20 seconds.
     */
    public static final long TWENTY_SECONDS = 20 * MILLISECONDS_IN_SECOND;

    /**
     * A constant for 10 seconds.
     */
    public static final long TEN_SECONDS = 10 * MILLISECONDS_IN_SECOND;

    /**
     * A constant for 5 seconds.
     */
    public static final long FIVE_SECONDS = 5 * MILLISECONDS_IN_SECOND;

    /**
     * A constant for 2 seconds.
     */
    public static final long TWO_SECONDS = 2 * MILLISECONDS_IN_SECOND;

    /**
     * A constant for 1 second.
     */
    public static final long ONE_SECONDS = MILLISECONDS_IN_SECOND;

    /**
     * A constant for one minute.
     */
    public static final long ONE_MINUTE = MILLISECONDS_IN_MINUTE;

    /**
     * A constant for 5 minutes.
     */
    public static final long FIVE_MINUTE = 5 * MILLISECONDS_IN_MINUTE;

    /**
     * A constant for 15 minutes.
     */
    public static final long FIFTEEN_MINUTE = 15 * MILLISECONDS_IN_MINUTE;

    /**
     * A constant for one hour.
     */
    public static final long ONE_HOUR = MILLISECONDS_IN_HOUR;

    /**
     * A constant for one day.
     */
    public static final long ONE_DAY = MILLISECONDS_IN_DAY;

    /**
     * A constant for one month.
     */
    public static final long ONE_MONTH = 30 * MILLISECONDS_IN_DAY;

    /**
     * A constant for one year.
     */
    public static final long ONE_YEAR = 365 * MILLISECONDS_IN_DAY;

    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    public static final ZoneOffset UTC_OFFSET = ZoneOffset.UTC;
    public static final ZoneOffset SYSTEM_OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.now());

    private static final DateTimeFormatter DATE_TINE_FORMATTER = createDateTimeFormatter();
    private static final DateTimeFormatter DATE_FORMATTER = createDateFormatter();
    private static final DateTimeFormatter TIME_FORMATTER = createTimeFormatter();

    /**
     * Converts millis since epoch to a {@link ZonedDateTime}.
     *
     * @param value the time in millis
     * @return the date/time, null if the value represents a null
     */
    public static ZonedDateTime fromMillis(long value) {
        if (value <= 0) return null;
        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault());
    }

    /**
     * Returns a local date/time from a temporal
     *
     * @param temporal the temporal
     * @return the local date/time, null if was null
     */
    public static LocalDate toLocalDate(Object temporal) {
        LocalDateTime localDateTime = toLocalDateTime(temporal);
        return localDateTime != null ? localDateTime.toLocalDate() : null;
    }

    /**
     * Returns a local date/time from a temporal
     *
     * @param temporal the temporal
     * @return the local date/time, null if was null
     */
    public static LocalDateTime toLocalDateTime(Object temporal) {
        ZonedDateTime zonedDateTime = toZonedDateTime(temporal);
        return zonedDateTime != null ? zonedDateTime.toLocalDateTime() : null;
    }

    /**
     * Converts a temporal (old and new API) to a {@link ZonedDateTime} and changes the zone.
     *
     * @param temporal any of the Java Time API temporal objects
     * @return a zoned date time
     * @see ZonedDateTime#withZoneSameInstant(ZoneId)
     */
    public static ZonedDateTime toZonedDateTimeSameInstant(Object temporal, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = toZonedDateTime(temporal);
        return zonedDateTime != null ? zonedDateTime.withZoneSameInstant(zoneId) : null;
    }

    /**
     * Converts a temporal (old and new API) to a {@link ZonedDateTime}.
     *
     * @param temporal any of the Java Time API temporal objects
     * @return a zoned date time
     */
    public static ZonedDateTime toZonedDateTime(Object temporal) {
        if (temporal == null) return null;
        if (temporal instanceof LocalDate) {
            return ((LocalDate) temporal).atStartOfDay(ZoneId.systemDefault());
        } else if (temporal instanceof LocalDateTime) {
            return ((LocalDateTime) temporal).atZone(ZoneId.systemDefault());
        } else if (temporal instanceof ZonedDateTime) {
            return (ZonedDateTime) temporal;
        } else if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime) temporal).toZonedDateTime();
        } else if (temporal instanceof Date) {
            return ((Date) temporal).toInstant().atZone(ZoneId.systemDefault());
        } else if (temporal instanceof Number) {
            long instant = ((Number) temporal).longValue();
            return instant <= 0 ? null : Instant.ofEpochMilli(instant).atZone(ZoneId.systemDefault());
        } else {
            throw new IllegalArgumentException("Cannot convert temporal '" + temporal + " to a zoned date/time");
        }
    }

    /**
     * Converts a {@link java.time.temporal.Temporal} to millis.
     *
     * @param temporal any of the Java Time API temporal objects
     * @return millis since epoch.
     */
    public static long toMillis(Temporal temporal) {
        if (temporal == null) return 0;
        if (temporal instanceof LocalDateTime) {
            return ((LocalDateTime) temporal).atZone(SYSTEM_OFFSET).toInstant().toEpochMilli();
        } else if (temporal instanceof LocalDate) {
            return ((LocalDate) temporal).atStartOfDay().atZone(SYSTEM_OFFSET).toInstant().toEpochMilli();
        } else if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporal).withZoneSameInstant(SYSTEM_OFFSET).toInstant().toEpochMilli();
        } else if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime) temporal).toInstant().toEpochMilli();
        } else if (temporal instanceof Instant) {
            return ((Instant) temporal).toEpochMilli();
        } else {
            throw new IllegalArgumentException("Value not a temporal: " + temporal);
        }
    }

    /**
     * Converts a temporal to a SQL timestamp.
     *
     * @param temporal the temporal
     * @return the timestamp, null if temporal was null
     */
    public static Timestamp toTimestamp(Temporal temporal) {
        if (temporal == null) return null;
        Instant instant;
        if (temporal instanceof LocalDateTime) {
            instant = ((LocalDateTime) temporal).toInstant(UTC_OFFSET);
        } else if (temporal instanceof LocalDate) {
            instant = ((LocalDate) temporal).atStartOfDay().toInstant(UTC_OFFSET);
        } else if (temporal instanceof ZonedDateTime) {
            instant = ((ZonedDateTime) temporal).toInstant();
        } else if (temporal instanceof OffsetDateTime) {
            instant = ((OffsetDateTime) temporal).toInstant();
        } else {
            throw new IllegalArgumentException("Value not a temporal: " + temporal);
        }
        return new Timestamp(instant.toEpochMilli());
    }

    /**
     * Converts a duration as value &amp; unit to {@link Duration}.
     *
     * @param duration the duration
     * @param unit     the unit of measure
     * @return a non-null instance
     */
    public static Duration toDuration(long duration, TimeUnit unit) {
        requireNonNull(unit);
        return Duration.ofNanos(TimeUnit.NANOSECONDS.convert(duration, unit));
    }

    /**
     * Returns a temporal which contains JVM zone (or offset) if the type is a local one (so no zone, which means JVM zone).
     * <p>
     * Some types (like date) will have the midnight attached in order to be able to get a zone
     *
     * @param value the temporal to convert
     * @return a new temporal with zone
     */
    public static Temporal withSystemZone(Temporal value) {
        if (value == null) return null;
        if (value instanceof ChronoLocalDateTime) {
            return ((ChronoLocalDateTime<?>) value).atZone(ZoneId.systemDefault());
        } else if (value instanceof ChronoLocalDate) {
            return ((ChronoLocalDate) value).atTime(java.time.LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault());
        } else if (value instanceof java.time.LocalTime) {
            return ((java.time.LocalTime) value).atOffset(toOffset(ZoneId.systemDefault()));
        } else {
            return value;
        }
    }

    /**
     * Converts a zone to a zone offset.
     *
     * @param zoneId the zone
     * @return a non-null instance
     */
    public static ZoneOffset toOffset(ZoneId zoneId) {
        java.time.Instant instant = java.time.Instant.now();
        return zoneId.getRules().getOffset(instant);
    }

    /**
     * Returns if all durations are zero
     *
     * @param durations a list of durations
     * @return {@code true} if all zero, {@code false} otherwise
     */
    public static boolean isZero(Duration... durations) {
        for (Duration duration : durations) {
            if (!duration.isZero()) return false;
        }
        return true;
    }

    /**
     * Returns the sum of all durations.
     *
     * @param durations a list of durations
     * @return {@code true} if all zero, {@code false} otherwise
     */
    public static Duration sum(Stream<Duration> durations) {
        return Duration.ofNanos(durations.mapToLong(Duration::toNanos).sum());
    }

    /**
     * Returns the sum of all durations.
     *
     * @param durations a list of durations
     * @return {@code true} if all zero, {@code false} otherwise
     */
    public static Duration sum(Duration... durations) {
        Duration total = Duration.ZERO;
        for (Duration duration : durations) {
            total = total.plus(duration);
        }
        return total;
    }

    /**
     * Returns the duration passed between a time reference in the past and now.
     *
     * @param value the time reference
     * @return duration
     */
    public static Duration durationSince(Object value) {
        Duration duration = Duration.ZERO;
        if (value instanceof LocalDateTime) {
            duration = Duration.between((LocalDateTime) value, LocalDateTime.now());
        } else if (value instanceof ZonedDateTime) {
            duration = Duration.between((ZonedDateTime) value, ZonedDateTime.now());
        } else if (value instanceof OffsetDateTime) {
            duration = Duration.between((OffsetDateTime) value, OffsetDateTime.now());
        }
        return duration;
    }

    /**
     * Returns whether the temporal is between two temporals.
     *
     * @param temporal the temporal to check
     * @param start    the start of the interval, can be NULL
     * @param end      the end of the interval, can be NULL
     * @return {@code true} if inside the interval, {@code false} otherwise
     */
    public static boolean isBetween(Temporal temporal, Temporal start, Temporal end) {
        requireNonNull(temporal);
        LocalDateTime localDateTime = toLocalDateTime(temporal);
        if (start != null && toLocalDateTime(start).isAfter(localDateTime)) return false;
        if (end != null && toLocalDateTime(end).isBefore(localDateTime)) return false;
        return true;
    }

    /**
     * Returns how must time passed compared with <code>now</code>, basically System.currentTimeMillis() - time.
     *
     * @param time an arbitrary time; accepted types: Date, DateTime, Number/String (instant)
     * @return time duration between now and time, <code>Long.MIN_VALUE</code> if time is null, 0 or less
     */
    public static long millisSince(Object time) {
        if (time == null) return Long.MIN_VALUE;
        long millis;
        if (time instanceof Date) {
            millis = ((Date) time).getTime();
        } else if (time instanceof Number) {
            millis = ((Number) time).longValue();
        } else if (time instanceof Temporal) {
            millis = toMillis((Temporal) time);
        } else if (time instanceof String) {
            millis = Long.parseLong((String) time);
        } else {
            throw new IllegalArgumentException("Invalid time object: " + time);
        }
        if (millis <= 0) return Long.MIN_VALUE;
        long duration = currentTimeMillis() - millis;
        return duration <= 0 ? 0 : duration;
    }

    /**
     * Returns a timestamp of 1 hour ago.
     *
     * @return millis since epoch
     */
    public static long oneHourAgo() {
        return currentTimeMillis() - MILLISECONDS_IN_HOUR;
    }

    /**
     * Returns a timestamp of 1 hour ago.
     *
     * @return millis since epoch
     */
    public static long oneDayAgo() {
        return currentTimeMillis() - MILLISECONDS_IN_DAY;
    }

    /**
     * Formats a temporal using ISO format.
     *
     * @param temporal the temporal
     * @return the temporal as String
     */
    public static String format(Temporal temporal) {
        if (temporal == null) return null;
        if (temporal instanceof LocalDateTime || temporal instanceof ZonedDateTime
            || temporal instanceof OffsetDateTime) {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(temporal);
        } else if (temporal instanceof LocalDate) {
            return DateTimeFormatter.ISO_DATE.format(temporal);
        } else if (temporal instanceof LocalTime) {
            return DateTimeFormatter.ISO_OFFSET_TIME.format(((LocalTime) temporal).atOffset(ZoneOffset.UTC));
        } else if (temporal instanceof OffsetTime) {
            return DateTimeFormatter.ISO_OFFSET_TIME.format(temporal);
        } else if (temporal instanceof Instant) {
            return DateTimeFormatter.ISO_INSTANT.format(temporal);
        } else {
            throw new IllegalArgumentException("Unsupported temporal type '" + ClassUtils.getName(temporal) + "', value " + temporal);
        }
    }

    /**
     * Parses a temporal.
     * <p>
     * The method accepts a range of date/time, date and time formats (including ISO).
     *
     * @param temporal the temporal as String
     * @return the temporal, null if input is null or empty
     */
    public static Temporal parseTemporal(String temporal) {
        if (isEmpty(temporal)) return null;
        DateTimeMetadata dateTimeMetadata = extractTemporalMetadataAndFail(temporal);
        if (dateTimeMetadata.isDateTime()) {
            return ZonedDateTime.parse(temporal, DATE_TINE_FORMATTER);
        } else if (dateTimeMetadata.isDate()) {
            return LocalDate.parse(temporal, DATE_FORMATTER);
        } else {
            return OffsetTime.parse(temporal, TIME_FORMATTER);
        }
    }

    /**
     * Parses a date/time.
     * <p>
     * The method accepts a range of date/time &amp; date formats (including ISO).
     *
     * @param temporal the date/time as String
     * @return the date/time, null if input is null or empty
     */
    public static ZonedDateTime parseDateTime(String temporal) {
        if (isEmpty(temporal)) return null;
        DateTimeMetadata dateTimeMetadata = extractTemporalMetadataAndFail(temporal);
        if (dateTimeMetadata.isDateTime()) {
            if (dateTimeMetadata.hasZone) {
                return ZonedDateTime.parse(temporal, DATE_TINE_FORMATTER);
            } else {
                return LocalDateTime.parse(temporal, DATE_TINE_FORMATTER).atZone(ZoneId.systemDefault());
            }
        } else if (dateTimeMetadata.isDate()) {
            return LocalDate.parse(temporal, DATE_FORMATTER).atStartOfDay(ZoneId.systemDefault());
        } else {
            return throwInvalidTemporal(temporal);
        }
    }

    /**
     * Parses a date/time.
     * <p>
     * The method accepts a range of date/time &amp; date formats (including ISO).
     *
     * @param temporal the date/time as String
     * @return the date/time, null if input is null or empty
     */
    public static LocalDate parseDate(String temporal) {
        if (isEmpty(temporal)) return null;
        DateTimeMetadata dateTimeMetadata = extractTemporalMetadataAndFail(temporal);
        if (dateTimeMetadata.isDateTime()) {
            return ZonedDateTime.parse(temporal, DATE_TINE_FORMATTER).toLocalDate();
        } else if (dateTimeMetadata.isDate()) {
            return LocalDate.parse(temporal, DATE_FORMATTER);
        } else {
            return throwInvalidTemporal(temporal);
        }
    }

    /**
     * Parses a date/time.
     * <p>
     * The method accepts a range of date/time &amp; date formats (including ISO).
     *
     * @param temporal the date/time as String
     * @return the date/time, null if input is null or empty
     */
    public static OffsetTime parseTime(String temporal) {
        if (isEmpty(temporal)) return null;
        DateTimeMetadata dateTimeMetadata = extractTemporalMetadataAndFail(temporal);
        if (dateTimeMetadata.isTime()) {
            return OffsetTime.parse(temporal, TIME_FORMATTER);
        } else {
            return throwInvalidTemporal(temporal);
        }
    }

    /**
     * Parses a duration.
     * <p>
     * The duration is in the following format: value [m,s,h,d]. The space between number and unit is optional.
     *
     * @param duration the duration as a string
     * @return the duration, null if the initial value is null or empty
     */
    public static Duration parseDuration(String duration) {
        if (StringUtils.isEmpty(duration)) return null;
        duration = duration.trim();
        char unit = duration.charAt(duration.length() - 1);
        if (Character.isLetter(unit)) {
            duration = duration.substring(0, duration.length() - 1).trim();
        }
        int value = Integer.parseInt(duration);
        if ('s' == unit) {
            return Duration.ofSeconds(value);
        } else if ('m' == unit) {
            return Duration.ofMinutes(value);
        } else if ('h' == unit) {
            return Duration.ofHours(value);
        } else if ('d' == unit) {
            return Duration.ofDays(value);
        } else {
            return Duration.ofMillis(value);
        }
    }

    /**
     * Converts the duration to its string representation
     *
     * @param duration the duration
     * @return a non-null instance
     */
    public static String toString(Duration duration) {
        if (duration == null) return "";
        if (duration.toSeconds() < 60 * 60) {
            return duration.toSeconds() + "s";
        } else if (duration.toMinutes() < 24 * 60) {
            return duration.toMinutes() + "m";
        } else if (duration.toHours() <= 24 * 30) {
            return duration.toHours() + "h";
        } else {
            return duration.toDays() + "d";
        }
    }

    /**
     * Returns whether the String contains a temporal.
     *
     * @param temporal the value to test
     * @return {@code true} if temporal, {@code false} otherwise
     */
    public static boolean isTemporal(String temporal) {
        try {
            parseTemporal(temporal);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns whether the String contains a date/time.
     *
     * @param dateTime the value to test
     * @return {@code true} if a date/time, {@code false} otherwise
     */
    public static boolean isDateTime(String dateTime) {
        try {
            parseDateTime(dateTime);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns whether the String contains a date.
     *
     * @param date the value to test
     * @return {@code true} if a date, {@code false} otherwise
     */
    public static boolean isDate(String date) {
        try {
            parseDate(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns whether the String contains a time.
     *
     * @param time the value to test
     * @return {@code true} if a time, {@code false} otherwise
     */
    public static boolean isTime(String time) {
        try {
            parseTime(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns whether the String looks like it contains a temporal.
     *
     * @param temporal the value to test
     * @return {@code true} if temporal, {@code false} otherwise
     */
    public static boolean seemsTemporal(String temporal) {
        return seemsDateTime(temporal) || seemsDate(temporal) || seemsTime(temporal);
    }

    /**
     * Returns whether the String looks like it contains a date/time.
     * <p>
     * The method does not parse the value, it looks at the characters and decide if it meets the criteria to be a
     * date/time.
     *
     * @param dateTime the value to test
     * @return {@code true} if date/time, {@code false} otherwise
     */
    public static boolean seemsDateTime(String dateTime) {
        if (dateTime == null) return false;
        return extractTemporalMetadata(dateTime).isDateTime();
    }

    /**
     * Returns whether the String looks like it contains a date.
     * <p>
     * The method does not parse the value, it looks at the characters and decide if it meets the minimum criteria
     * to be a date.
     *
     * @param date the value to test
     * @return {@code true} if date, {@code false} otherwise
     */
    public static boolean seemsDate(String date) {
        if (date == null) return false;
        return extractTemporalMetadata(date).isDate();
    }

    /**
     * Returns whether the String looks like it contains a time.
     * <p>
     * The method does not parse the value, it looks at the characters and decide if it meets the minimum criteria
     * to be a time.
     *
     * @param time the value to test
     * @return {@code true} if time, {@code false} otherwise
     */
    public static boolean seemsTime(String time) {
        if (time == null) return false;
        return extractTemporalMetadata(time).isTime();
    }

    /**
     * Parses a date/time string and returns metadata (statistics).
     *
     * @param temporal the temporal value as a string
     * @return the metadata
     */
    private static DateTimeMetadata extractTemporalMetadataAndFail(String temporal) {
        DateTimeMetadata dateTimeMetadata = extractTemporalMetadata(temporal);
        if (!dateTimeMetadata.valid) throwInvalidTemporal(temporal);
        return dateTimeMetadata;
    }

    /**
     * Throws an exception complaining about the String not having a temporal
     *
     * @param temporal the temporal value as a string
     */
    private static <T> T throwInvalidTemporal(String temporal) {
        throw new DateTimeParseException("Unable to parse '" + temporal + "' into a temporal", temporal, 0);
    }

    /**
     * Parses a temporal string and returns metadata (statistics).
     *
     * @param temporal the temporal value as a string
     * @return the metadata
     */
    @SuppressWarnings({"Duplicates", "common-java:DuplicatedBlocks"})
    private static DateTimeMetadata extractTemporalMetadata(String temporal) {
        if (temporal == null || temporal.length() < 4 || !Character.isDigit(temporal.charAt(0))) {
            return new DateTimeMetadata(false);
        }
        DateTimeMetadata dateTimeMetadata = new DateTimeMetadata(true);
        int length = temporal.length();
        for (int index = 0; index < length; index++) {
            char c = temporal.charAt(index);
            if (Character.isDigit(c)) {
                dateTimeMetadata.digitCount++;
            } else if (c == '.') {
                dateTimeMetadata.dotCount++;
            } else if (c == '-' && dateTimeMetadata.colonCount < 2) {
                dateTimeMetadata.dashCount++;
            } else if (c == '/') {
                dateTimeMetadata.backSlashCount++;
            } else if (c == ':') {
                dateTimeMetadata.colonCount++;
            } else if (c == 'T') {
                dateTimeMetadata.hasTimeSeparator = true;
            } else if (c == 'Z') {
                dateTimeMetadata.hasZone = true;
            } else if (c == '+' || c == '-') {
                dateTimeMetadata.hasZone = true;
            } else if (c == ' ') {
                dateTimeMetadata.spaceCount++;
            } else {
                dateTimeMetadata.otherCount++;
            }
        }
        return dateTimeMetadata;
    }

    /**
     * Creates a date/time formatter which recognizes multiple formats, starting with the ISO format.
     *
     * @return a non-null instance
     */
    private static DateTimeFormatter createDateTimeFormatter() {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendOptional(DateTimeFormatter.ISO_DATE_TIME)
                .appendOptional(DateTimeFormatter.ISO_INSTANT)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);
        return builder.toFormatter();
    }

    /**
     * Creates a date/time formatter which recognizes multiple formats, starting with the ISO format.
     *
     * @return a non-null instance
     */
    private static DateTimeFormatter createDateFormatter() {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendOptional(DateTimeFormatter.ISO_DATE)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0);
        return builder.toFormatter();
    }

    /**
     * Creates a date/time formatter which recognizes multiple formats, starting with the ISO format.
     *
     * @return a non-null instance
     */
    private static DateTimeFormatter createTimeFormatter() {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendOptional(DateTimeFormatter.ISO_TIME);
        return builder.toFormatter();
    }

    private static final int DATE_DIGIT_COUNT = 8;
    private static final int TIME_DIGIT_COUNT = 4;
    private static final int DATE_TIME_DIGIT_COUNT = DATE_DIGIT_COUNT + TIME_DIGIT_COUNT;

    static class DateTimeMetadata {

        boolean valid;
        int digitCount;
        int dashCount;
        int colonCount;
        int backSlashCount;
        int spaceCount;
        int otherCount;
        int dotCount;
        boolean hasZone;
        boolean hasTimeSeparator;

        DateTimeMetadata(boolean valid) {
            this.valid = valid;
        }

        boolean isDateTime() {
            return valid && digitCount >= DATE_TIME_DIGIT_COUNT && hasValidDateSeparator() && hasValidTimeSeparator();
        }

        boolean isDate() {
            return valid && digitCount >= DATE_DIGIT_COUNT && otherCount == 0 && hasValidDateSeparator();
        }

        boolean isTime() {
            return valid && digitCount >= TIME_DIGIT_COUNT && hasValidTimeSeparator();
        }

        boolean hasValidDateSeparator() {
            return backSlashCount == 2 || dashCount == 2 || dotCount == 2;
        }

        boolean hasValidTimeSeparator() {
            return colonCount >= 2;
        }
    }
}
