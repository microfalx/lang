package net.microfalx.lang.convert;

import net.microfalx.lang.service.ServiceException;

/**
 * An exception for type conversion exception.
 */
public class ConversionException extends ServiceException {

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
