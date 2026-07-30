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
     * Looks up a service.
     *
     * @param serviceClass the service class
     * @param <T>          the service type
     * @return the service implementation
     * @see ServiceLocator#lookup(Class)
     */
    static <T extends Service> T lookup(Class<T> serviceClass) {
        return ServiceLocator.lookup(serviceClass);
    }
}
