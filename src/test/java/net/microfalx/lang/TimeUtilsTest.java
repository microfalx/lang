package net.microfalx.lang;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {

    private final LocalTime LOCAL_TIME = LocalTime.of(17, 12, 30);
    private final OffsetTime OFFSET_TIME = LocalTime.of(17, 12, 30).atOffset(ZoneOffset.ofHours(-2));
    private final LocalDate LOCAL_DATE = LocalDate.of(2023, 9, 27);
    private final Instant INSTANT = Instant.ofEpochMilli(1695849150000L);
    private final Date DATE = Date.from(INSTANT);
    private final LocalDateTime LOCAL_DATETIME = LocalDateTime.of(LOCAL_DATE, LOCAL_TIME);
    private final OffsetDateTime OFFSET_DATETIME = OffsetDateTime.of(LOCAL_DATETIME, ZoneOffset.ofHours(-2));
    private final ZonedDateTime ZONED_DATETIME = ZonedDateTime.of(LOCAL_DATE, LOCAL_TIME, ZoneId.systemDefault());

    @Test
    void fromMillis() {
        assertNull(TimeUtils.fromMillis(-1000));
        assertEquals(ZONED_DATETIME, TimeUtils.fromMillis(ZONED_DATETIME.toInstant().toEpochMilli()));
    }


    @Test
    void toLocalDateTime() {
        assertEquals(LOCAL_DATE.atTime(0, 0),
                TimeUtils.toLocalDateTime(LOCAL_DATE));
        assertNull(TimeUtils.toLocalDateTime(null));
    }


    @Test
    void toZonedDateTimeSameInstant() {
        assertNull(TimeUtils.toZonedDateTimeSameInstant(null, ZoneId.of(ZoneId.SHORT_IDS.values().iterator()
                .next())));
        assertEquals(LOCAL_DATE.atTime(0, 0).atZone(ZoneId.systemDefault()),
                TimeUtils.toZonedDateTimeSameInstant(LOCAL_DATE, ZoneId.systemDefault()));
    }

    @Test
    void toZonedDateTime() {
        assertNull(TimeUtils.toZonedDateTime(null));
        assertEquals(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()), TimeUtils.toZonedDateTime(LOCAL_DATE));
        assertEquals(LOCAL_DATETIME.atZone(ZoneId.systemDefault()), TimeUtils.toZonedDateTime(LOCAL_DATETIME));
        assertEquals(ZONED_DATETIME, TimeUtils.toZonedDateTime(ZONED_DATETIME));
        assertEquals(OFFSET_DATETIME.toZonedDateTime(), TimeUtils.toZonedDateTime(OFFSET_DATETIME));
        assertEquals(DATE.toInstant().atZone(ZoneId.systemDefault()), TimeUtils.toZonedDateTime(DATE));
        assertEquals(Instant.ofEpochMilli(TimeUtils.MILLISECONDS_IN_HOUR).atZone(ZoneId.systemDefault()),
                TimeUtils.toZonedDateTime(TimeUtils.MILLISECONDS_IN_HOUR));
    }

    @Test
    void toMillis() {
        assertEquals(0, TimeUtils.toMillis(null));
        assertEquals(LOCAL_DATETIME.atZone(TimeUtils.UTC_ZONE).toInstant().toEpochMilli(),
                TimeUtils.toMillis(LOCAL_DATETIME));
        assertEquals(LOCAL_DATE.atStartOfDay().atZone(TimeUtils.UTC_ZONE).toInstant().toEpochMilli(),
                TimeUtils.toMillis(LOCAL_DATE));
        assertEquals(ZONED_DATETIME.withZoneSameInstant(TimeUtils.UTC_ZONE).toInstant().toEpochMilli(),
                TimeUtils.toMillis(ZONED_DATETIME));
        assertEquals(OFFSET_DATETIME.toInstant().toEpochMilli(), TimeUtils.toMillis(OFFSET_DATETIME));
    }

    @Test
    void toTimestamp() {
        assertNull(TimeUtils.toTimestamp(null));
        assertEquals(new Timestamp(LOCAL_DATETIME.toInstant(TimeUtils.UTC_OFFSET).toEpochMilli()),
                TimeUtils.toTimestamp(LOCAL_DATETIME));
        assertEquals(new Timestamp(LOCAL_DATE.atStartOfDay().toInstant(TimeUtils.UTC_OFFSET).toEpochMilli()),
                TimeUtils.toTimestamp(LOCAL_DATE));
        assertEquals(new Timestamp(ZONED_DATETIME.toInstant().toEpochMilli()),
                TimeUtils.toTimestamp(ZONED_DATETIME));
        assertEquals(new Timestamp(OFFSET_DATETIME.toInstant().toEpochMilli()), TimeUtils.toTimestamp(OFFSET_DATETIME));
    }

    @Test
    void toDuration() {
        assertEquals(Duration.ofNanos(50), TimeUtils.toDuration(50, TimeUnit.NANOSECONDS));
        assertEquals(Duration.ofNanos(50_000), TimeUtils.toDuration(50, TimeUnit.MICROSECONDS));
        assertEquals(Duration.ofMillis(50), TimeUtils.toDuration(50, TimeUnit.MILLISECONDS));
        assertEquals(Duration.ofSeconds(50), TimeUtils.toDuration(50, TimeUnit.SECONDS));
    }


    @Test
    void withSystemZone() {
        assertNull(TimeUtils.withSystemZone(null));
        assertEquals(OFFSET_TIME, TimeUtils.withSystemZone(OFFSET_TIME));
        assertEquals(LOCAL_TIME.atOffset(ZoneId.systemDefault().getRules().getOffset(LOCAL_DATETIME)),
                TimeUtils.withSystemZone(LOCAL_TIME));
        assertEquals(LOCAL_DATETIME.atZone(ZoneId.systemDefault()), TimeUtils.withSystemZone(LOCAL_DATETIME));
        assertEquals(LOCAL_DATE.atTime(java.time.LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()),
                TimeUtils.withSystemZone(LOCAL_DATE));

    }


    @Test
    void toOffset() {
        assertEquals(ZONED_DATETIME.getOffset(), TimeUtils.toOffset(ZONED_DATETIME.getZone()));
    }

    @Test
    void durationSince() {
        assertEquals(Duration.ZERO, TimeUtils.durationSince(DATE));
        assertEquals(Duration.between(LOCAL_DATETIME, LocalDateTime.now()), TimeUtils.durationSince(LOCAL_DATETIME));
        assertEquals(Duration.between(ZONED_DATETIME, ZonedDateTime.now()), TimeUtils.durationSince(ZONED_DATETIME));
        assertEquals(Duration.between(OFFSET_DATETIME, OffsetDateTime.now()), TimeUtils.durationSince(OFFSET_DATETIME));
    }

    @Test
    void isBetween() {
        assertTrue(TimeUtils.isBetween(LOCAL_DATE, LocalDate.of(2023, 7, 4),
                LocalDate.of(2025, 8, 17)));
        assertFalse(TimeUtils.isBetween(LOCAL_DATE, LocalDate.of(2025, 7, 4),
                LocalDate.of(2025, 8, 17)));
        assertFalse(TimeUtils.isBetween(LOCAL_DATE, LocalDate.of(2023, 7, 4),
                LocalDate.of(2021, 8, 17)));
    }

    @Test
    void millisSince() {
        assertEquals(Long.MIN_VALUE, TimeUtils.millisSince(null));
        assertEquals(System.currentTimeMillis() - DATE.getTime(), TimeUtils.millisSince(DATE));
        assertEquals(System.currentTimeMillis() - TimeUtils.FIVE_MINUTE,
                TimeUtils.millisSince(TimeUtils.FIVE_MINUTE));
        assertEquals(System.currentTimeMillis() - INSTANT.toEpochMilli(),
                TimeUtils.millisSince(INSTANT));
        assertThrows(IllegalArgumentException.class,
                () -> TimeUtils.millisSince(OFFSET_TIME));
        assertEquals(System.currentTimeMillis() - Long.parseLong("1000"),
                TimeUtils.millisSince("1000"));
    }

    @Test
    void oneHourAgo() {
        assertEquals(currentTimeMillis() - TimeUtils.MILLISECONDS_IN_HOUR, TimeUtils.oneHourAgo());
    }

    @Test
    void oneDayAgo() {
        assertEquals(currentTimeMillis() - TimeUtils.MILLISECONDS_IN_DAY, TimeUtils.oneDayAgo());
    }


    @Test
    void format() {
        assertNull(TimeUtils.format(null));
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.format(Year.now()));
        assertEquals("2023-09-27T17:12:30-04:00", TimeUtils.format(ZONED_DATETIME));
        assertEquals("2023-09-27T21:12:30Z", TimeUtils.format(INSTANT));
        assertEquals("2023-09-27", TimeUtils.format(LOCAL_DATE));
        assertEquals("17:12:30Z", TimeUtils.format(LOCAL_TIME));
        assertEquals("17:12:30-02:00", TimeUtils.format(OFFSET_TIME));
    }


    @Test
    void parseTemporal() {
        assertNull(TimeUtils.parseTemporal(null));
        assertEquals(LOCAL_DATE, TimeUtils.parseTemporal(LOCAL_DATE.toString()));
        assertThrows(DateTimeException.class, () -> TimeUtils.parseTemporal(LOCAL_DATETIME.toString()));
    }

    @Test
    void parseDateTime() {
        assertNull(TimeUtils.parseDateTime(null));
        assertEquals(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()), TimeUtils.parseDateTime(LOCAL_DATE.toString()));
        assertThrows(DateTimeException.class, () -> TimeUtils.parseTemporal(LOCAL_DATETIME.toString()));
    }

    @Test
    void parseDate() {
        assertNull(TimeUtils.parseDate(null));
        assertEquals(LOCAL_DATE, TimeUtils.parseDate(LOCAL_DATE.toString()));
        assertThrows(DateTimeException.class, () -> TimeUtils.parseDate(LOCAL_DATETIME.toString()));
    }

    @Test
    void parseTime() {
        assertNull(TimeUtils.parseTime(null));
        assertThrows(DateTimeException.class, () -> TimeUtils.parseTime(LOCAL_DATETIME.toString()));
    }

    @Test
    void parseDuration() {
        assertNull(TimeUtils.parseDuration(null));
        assertNull(TimeUtils.parseDuration(""));
        assertEquals(Duration.ofMillis(10), TimeUtils.parseDuration("10"));
        assertEquals(Duration.ofSeconds(10), TimeUtils.parseDuration("10s"));
        assertEquals(Duration.ofMinutes(11), TimeUtils.parseDuration("11m"));
        assertEquals(Duration.ofHours(12), TimeUtils.parseDuration("12h"));
        assertEquals(Duration.ofDays(13), TimeUtils.parseDuration("13d"));
    }

    @Test
    void isTemporal() {
        assertTrue(TimeUtils.isTemporal(ZONED_DATETIME.toString()));
    }

    @Test
    void isDateTime() {
        assertTrue(TimeUtils.isDateTime(LOCAL_DATETIME.toString()));
        assertFalse(TimeUtils.isDateTime("2023-12-07T12:30:00.123+0100"));
        assertFalse(TimeUtils.isDateTime("2023-12-07T12:30:00.123-0100"));
        assertFalse(TimeUtils.isDateTime("2023-12-07T12:30:00+0100"));
        assertFalse(TimeUtils.isDateTime("2023-12-07T12:30:00-0100"));

        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00Z"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00.123Z"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00.123"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00.123+01:00"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00.123-01:00"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00.123+01:00"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00.123-01:00"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00+01:00"));
        assertTrue(TimeUtils.isDateTime("2023-12-07T12:30:00-01:00"));
    }

    @Test
    void isDate() {
        assertFalse(TimeUtils.isDate(DATE.toString()));
    }

    @Test
    void isTime() {
        assertFalse(TimeUtils.isTime(LOCAL_TIME.toString()));
    }

    @Test
    void seemsTemporal() {
        assertTrue(TimeUtils.seemsTemporal(ZONED_DATETIME.toString()));
    }

    @Test
    void seemsDateTime() {
        assertFalse(TimeUtils.seemsDateTime(null));
        assertFalse(TimeUtils.seemsDateTime(""));
        assertFalse(TimeUtils.seemsDateTime("a"));
        assertFalse(TimeUtils.seemsDateTime("20"));
        assertFalse(TimeUtils.seemsDateTime("2023"));
        assertFalse(TimeUtils.seemsDateTime("20231207"));
        assertFalse(TimeUtils.seemsDateTime("2024/03/10"));
        assertFalse(TimeUtils.seemsDateTime("2024-03-10"));
        assertFalse(TimeUtils.seemsDateTime("03/12/2024"));
        assertFalse(TimeUtils.seemsDateTime("12.03.2024"));
        assertFalse(TimeUtils.seemsDateTime("03/12/2024 123"));
        assertFalse(TimeUtils.seemsDateTime("12.03.2024T12:23"));

        assertTrue(TimeUtils.seemsDateTime("2024/03/10 12:20:45"));
        assertTrue(TimeUtils.seemsDateTime("2024/03/10 12:20:45Z"));
        assertTrue(TimeUtils.seemsDateTime("2024-03-10T12:20:45"));
        assertTrue(TimeUtils.seemsDateTime("2024-03-10T12:20:45Z"));
        assertTrue(TimeUtils.seemsDateTime("2024-03-10T12:20:45+02:00"));
        assertTrue(TimeUtils.seemsDateTime("03/12/2024 12:20:45"));
        assertTrue(TimeUtils.seemsDateTime("03/12/2024 12:20:45Z"));
        assertTrue(TimeUtils.seemsDateTime("03/12/2024 12:20:45-02:00"));
        assertTrue(TimeUtils.seemsDateTime("12.03.2024 12:20:45"));
        assertTrue(TimeUtils.seemsDateTime("12.03.2024 12:20:45Z"));
        assertTrue(TimeUtils.seemsDateTime("12.03.2024 12:20:45-02:00"));
    }


    @Test
    void seemsDate() {
        assertFalse(TimeUtils.seemsDate(null));
        assertFalse(TimeUtils.seemsDate(""));
        assertFalse(TimeUtils.seemsDate("a"));
        assertFalse(TimeUtils.seemsDate("12"));
        assertFalse(TimeUtils.seemsDate("2024"));
        assertFalse(TimeUtils.seemsDate("202403"));
        assertFalse(TimeUtils.seemsDate("20240310"));
        assertFalse(TimeUtils.seemsDate("2024-0310"));
        assertFalse(TimeUtils.seemsDate("2024\\03\\10"));

        assertTrue(TimeUtils.seemsDate("2024/03/10"));
        assertTrue(TimeUtils.seemsDate("2024-03-10"));
        assertTrue(TimeUtils.seemsDate("03/12/2024"));
        assertTrue(TimeUtils.seemsDate("12.03.2024"));
    }

    @Test
    void seemsTime() {
        assertFalse(TimeUtils.seemsTime(null));
        assertFalse(TimeUtils.seemsTime(""));
        assertFalse(TimeUtils.seemsTime("a"));
        assertFalse(TimeUtils.seemsTime("12"));
        assertFalse(TimeUtils.seemsTime("1234"));
        assertFalse(TimeUtils.seemsTime("12341234"));
        assertFalse(TimeUtils.seemsTime("12:20"));

        assertTrue(TimeUtils.seemsTime("12:20:45"));
        assertTrue(TimeUtils.seemsTime("12:20:45Z"));
        assertTrue(TimeUtils.seemsTime("12:20:45+02:00"));
        assertTrue(TimeUtils.seemsTime("12:20:45-02:00"));
    }

    @Test
    void durationAsString(){
        assertEquals("5s",TimeUtils.toString(Duration.ofSeconds(5)));
        assertEquals("60s",TimeUtils.toString(Duration.ofSeconds(60)));
        assertEquals("90s",TimeUtils.toString(Duration.ofSeconds(90)));
        assertEquals("120s",TimeUtils.toString(Duration.ofSeconds(120)));
        assertEquals("400s",TimeUtils.toString(Duration.ofSeconds(400)));
        assertEquals("60s",TimeUtils.toString(Duration.ofMinutes(1)));
        assertEquals("60m",TimeUtils.toString(Duration.ofMinutes(60)));
        assertEquals("234m",TimeUtils.toString(Duration.ofMinutes(234)));
        assertEquals("567m",TimeUtils.toString(Duration.ofMinutes(567)));
        assertEquals("60m",TimeUtils.toString(Duration.ofHours(1)));
        assertEquals("24h",TimeUtils.toString(Duration.ofHours(24)));
        assertEquals("123h",TimeUtils.toString(Duration.ofHours(123)));
        assertEquals("24h",TimeUtils.toString(Duration.ofDays(1)));
        assertEquals("360h",TimeUtils.toString(Duration.ofDays(15)));
        assertEquals("40d",TimeUtils.toString(Duration.ofDays(40)));
    }
}