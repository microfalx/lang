package net.microfalx.lang.convert;

/**
 * Object converter interface.
 */
public interface Converter<T> {

    /**
     * Converts object received as parameter into object of another class.
     *
     * @param value object to convert from
     * @return resulting object converted to target type
     * @throws ConversionException if conversion fails
     */
    T convert(Object value);
}
