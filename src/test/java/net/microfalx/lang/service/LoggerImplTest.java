package net.microfalx.lang.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggerImplTest {

    private boolean quiet;

    @BeforeEach
    void setUp() {
        quiet = ServiceLocator.isQuiet();
        ServiceLocator.setQuiet(false);
        ServiceLocator.quietLogger.clear();
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.quietLogger.clear();
        ServiceLocator.setQuiet(quiet);
    }

    @Test
    void forwardsAndAppendsWhenNotQuiet() {
        org.slf4j.Logger delegate = mock(org.slf4j.Logger.class);
        Logger logger = new LoggerImpl(delegate);

        logger.info("message");

        verify(delegate).info("message");
        assertEquals("message", ServiceLocator.getLog());
    }

    @Test
    void appendsButDoesNotForwardWhenQuiet() {
        org.slf4j.Logger delegate = mock(org.slf4j.Logger.class);
        Logger logger = new LoggerImpl(delegate);
        ServiceLocator.setQuiet(true);

        logger.info("quiet-message");

        verify(delegate, never()).info("quiet-message");
        assertEquals("quiet-message", ServiceLocator.getLog());
    }

    @Test
    void formattedMessageIsAppendedAsRenderedText() {
        org.slf4j.Logger delegate = mock(org.slf4j.Logger.class);
        Logger logger = new LoggerImpl(delegate);

        logger.warn("value {} {}", 10, 20);

        verify(delegate).warn("value {} {}", 10, 20);
        assertEquals("value 10 20", ServiceLocator.getLog());
    }

    @Test
    void markerOverloadForwardsAndAppendsRenderedText() {
        org.slf4j.Logger delegate = mock(org.slf4j.Logger.class);
        Logger logger = new LoggerImpl(delegate);
        Marker marker = MarkerFactory.getMarker("test");

        logger.error(marker, "failed {}", "task");

        verify(delegate).error(marker, "failed {}", "task");
        assertEquals("failed task", ServiceLocator.getLog());
    }

    @Test
    void enabledChecksRespectQuietMode() {
        org.slf4j.Logger delegate = mock(org.slf4j.Logger.class);
        when(delegate.isDebugEnabled()).thenReturn(true);
        Logger logger = new LoggerImpl(delegate);

        ServiceLocator.setQuiet(true);
        assertFalse(logger.isDebugEnabled());

        ServiceLocator.setQuiet(false);
        assertTrue(logger.isDebugEnabled());
    }
}

