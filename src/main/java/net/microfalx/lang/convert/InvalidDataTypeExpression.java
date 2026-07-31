package net.microfalx.lang.convert;

/**
 * An exception for data type conversion.
 */
public class InvalidDataTypeExpression extends ConversionException {

    public InvalidDataTypeExpression(String message) {
        super(message);
    }

    public InvalidDataTypeExpression(String message, Throwable cause) {
        super(message, cause);
    }
}
