package net.microfalx.lang;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoggerTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    void testIndent() {
        Logger logger = Logger.create().withTimestamp(false);
        logger.info("test");
        logger.increaseIndent().info("test indented");
        logger.decreaseIndent().info("test");
        assertEquals("test\n" +
                "  test indented\n" +
                "test", logger.getOutput());
    }

    @Test
    void threadLocal() {
        Logger logger = Logger.createList("Test").attach();
        Logger.current().info("1");
        assertEquals("Test\n" +
                "  • 1", logger.getOutput());
    }


    @Test
    void withLogger() {
        Logger logger = Logger.create().withLogger(LOGGER);
        logger.info("info");
        assertEquals("info", logger.getOutput());
    }

    @Test
    void withIndents() {
        Logger logger = Logger.create();
        logger.info("info");
        logger.increaseIndent();
        logger.info("info 2");
        logger.decreaseIndent();
        logger.info("info 3");
        assertEquals("info\n" +
                "  info 2\n" +
                "info 3", logger.getOutput());
    }

    @Test
    void logWithDebugEnabled() {
        Logger logger = Logger.create().withDebug(true);
        logger.debug("debug1");
        logger.debug("debug1 {0}", 1);
        logger.info("info1");
        logger.info("info1 {0}", 1);
        assertEquals("debug1\n" +
                "debug1 1\n" +
                "info1\n" +
                "info1 1", logger.getOutput());
    }

    @Test
    void logWithLevels() {
        Logger logger = Logger.create();
        logger.debug("debug1");
        logger.debug("debug1 {0}", 1);
        logger.info("info1");
        logger.info("info1 {0}", 1);
        logger.warn("warn1");
        logger.warn("warn1 {0}", 1);
        logger.error("error1");
        logger.error("error1 {0}", 1);
        assertEquals("info1\n" +
                "info1 1\n" +
                "warn1\n" +
                "warn1 1\n" +
                "error1\n" +
                "error1 1", logger.getOutput());
    }

    @Test
    void logWithAppend() {
        Logger mainLogger = Logger.create();
        mainLogger.append("text1\ntext2");
        Logger logger = Logger.create();
        logger.info("logger");
        mainLogger.append(logger);
    }

    @Test
    void withReader() throws IOException {
        Logger logger = Logger.create();
        logger.append(new StringReader("Test1\nTest2"));
        assertEquals("Test1\n" +
                "Test2", logger.getOutput());
        logger.append(new StringReader("Test1\nTest2"), true);
        assertEquals("Test1\n" +
                "Test2\n" +
                "|  Test1\n" +
                "\\  Test2", logger.getOutput());
        logger.append(new StringReader("Test1\nTest2"), true, true);
        assertEquals("Test1\n" +
                "Test2\n" +
                "|  Test1\n" +
                "\\  Test2\n" +
                "|  Test1\n" +
                "\\  Test2", logger.getOutput());
    }

    @Test
    void compress() {
        Logger logger = Logger.create();
        logger.info("text1");
        assertEquals("text1", logger.getOutput());
        logger.compress();
        assertEquals("text1", logger.getOutput());
        logger.info("text2");
        assertEquals("text1\ntext2", logger.getOutput());
        logger.compress();
        assertEquals("text1\ntext2", logger.getOutput());
    }


    @Test
    void withAttach() {
        assertNotNull(Logger.current());
        Logger.remove();

        Logger logger1 = Logger.create().attach();
        Logger.current().info("logger1");
        Logger logger2 = Logger.create().attach();
        Logger.current().info("logger2");
        logger2.detach();
        logger1.detach();
        Logger.current().info("logger3");
        assertEquals("logger1", logger1.getOutput());
        assertEquals("logger2", logger2.getOutput());
    }

    @Test
    void withEntry() {
        Logger logger = Logger.create();
        logger.atDebug().check().append("Fourth").log();
        logger.atInfo().bullet().append("First").log();
        logger.atWarn().rightArrow().append("Second").log();
        logger.atError().triangle().append("Third").log();
        assertEquals("• First\n" +
                "→ Second\n" +
                "‣ Third", logger.getOutput());
    }


}