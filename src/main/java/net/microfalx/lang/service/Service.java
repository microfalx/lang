package net.microfalx.lang.service;

/**
 * Base class for simple application services.
 */
public interface Service {

    /**
     * Starts the service.
     */
    default void start() {
        // most services would not need to start
    }

    /**
     * Stops the service.
     */
    default void stop() {
        // most services would not need to stop/cleanup
    }

    /**
     * Loads a service implementation.
     *
     * @param serviceClass the service class
     * @param <T>          the service type
     * @return the service implementation
     * @see ServiceFactory#load(Class)
     */
    static <T extends Service> T load(Class<T> serviceClass) {
        return ServiceFactory.load(serviceClass);
    }
}
