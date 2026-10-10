package net.microfalx.lang;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionUtilsTest {

    @Test
    void throwException() {
        assertThrows(Throwable.class, () -> ExceptionUtils.rethrowException(new Throwable()));
    }

    @Test
    void rethrowInterruptedException() {
        assertThrows(Exception.class, () -> ExceptionUtils.rethrowException(new InterruptedException()));
    }

    @Test
    void getStackTrace() {
        org.assertj.core.api.Assertions.assertThat(ExceptionUtils.getStackTrace(new Throwable()))
                .contains("ExceptionUtilsTest.getStackTrace")
                .contains("org.junit.platform.launcher.core.EngineExecutionOrchestrator");
        assertEquals("N/A", ExceptionUtils.getStackTrace(null));
    }

    @Test
    void getRootCauseMessage() {
        assertEquals(StringUtils.NA_STRING, ExceptionUtils.getRootCauseMessage(new Throwable()));
        assertEquals(StringUtils.NA_STRING, ExceptionUtils.getRootCauseMessage(null));
    }

    @Test
    void getRootCauseDescription() {
        assertEquals("N/A (Throwable)", ExceptionUtils.getRootCauseDescription(new Throwable()));

    }

    @Test
    void getRootCause() {
        assertEquals("java.lang.Throwable", ExceptionUtils.getRootCause(new Throwable()).toString());
    }

    @Test
    void getRootCauseClass() {
        assertEquals(Throwable.class, ExceptionUtils.getRootCauseClass(new Throwable()));
    }

    @Test
    void getRootCauseNameWithThrowable() {
        assertEquals(StringUtils.NA_STRING, ExceptionUtils.getRootCauseName((Throwable) null));
        assertEquals("Illegal State", ExceptionUtils.getRootCauseName(new IllegalStateException("Demo")));
        assertEquals("I/O", ExceptionUtils.getRootCauseName(new IOException("Demo")));
    }

    @Test
    void getRootCauseNameWithString() {
        assertEquals(StringUtils.NA_STRING, ExceptionUtils.getRootCauseName((String) null));
        assertEquals("Illegal State", ExceptionUtils.getRootCauseName(IllegalStateException.class.getSimpleName()));
        assertEquals("I/O", ExceptionUtils.getRootCauseName(IOException.class.getSimpleName()));
    }

    @Test
    void getSQLErrorCode() {
        assertEquals(-1, ExceptionUtils.getSQLErrorCode(new Throwable()));
        assertEquals(0, ExceptionUtils.getSQLErrorCode(new SQLException()));
    }
}