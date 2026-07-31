package net.microfalx.lang.convert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;

import static org.junit.jupiter.api.Assertions.*;

class ConverterServiceTest {

    private ConverterService converterService;

    @BeforeEach
    void setup() {
        converterService = new ConverterService();
        converterService.initialize();
    }

    @Test
    void getJsonConverter() {
        assertNotNull(converterService.getJsonConverter());

    }

    @Test
    void setJsonConverter() {
        converterService.setJsonConverter(Mockito.mock(JsonConverter.class));
        assertTrue(MockUtil.isMock(converterService.getJsonConverter()));

    }

}