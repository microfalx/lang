package net.microfalx.lang;

/**
 * An interface implemented by objects which can provide their deep size and estimated number
 * of references to other objects without introspection.
 */
public interface Sizeable {

    /**
     * Returns the deep size of the object in bytes.
     *
     * @return a positive number or -1 to auto-calculate the deep size once and cache it for future calls
     */
    long getSizeOf();

    /**
     * Returns the estimated number of references (elements) to other objects.
     * <p>
     * This includes direct references to other objects, as well as references to objects in arrays.
     *
     * @return a positive number or -1 if not applicable
     */
    default int getCountOf() {
        return -1;
    }

    /**
     * Returns the deep size of the arrays allocated by the object in bytes.
     *
     * @return a positive number or -1 if not applicable
     */
    default long getArraySizeOf() {
        return -1;
    }

    /**
     * Returns the estimated number of primitives (elements) allocated in arrays objects.
     *
     * @return a positive number or -1 if not applicable
     */
    default int getArrayCountOf() {
        return -1;
    }
}
