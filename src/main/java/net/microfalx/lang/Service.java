package net.microfalx.lang;

/**
 * Base class for simple application services.
 */
public interface Service {

    /**
     * Starts the service.
     */
    void start();

    /**
     * Stops the service.
     */
    void stop();

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
