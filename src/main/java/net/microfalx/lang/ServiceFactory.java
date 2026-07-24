package net.microfalx.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A factory which provides implementations of services. The factory uses the Java ServiceLoader mechanism to
 * load implementations of services.
 */
class ServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFactory.class);

    private static final Map<Class<?>, Service> services = new ConcurrentHashMap<>();
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Shuts down all services. This method should be called when the application is shutting down to ensure
     * that all services are properly stopped.
     */
    public static void shutdown() {
        synchronized (ServiceFactory.class) {
            LOGGER.info("Shutting down services");
            services.values().forEach(Service::stop);
        }
    }

    /**
     * Loads a service.
     * <p>
     * The factory uses the Java ServiceLoader mechanism to load implementations of services. If a service has already
     * been loaded, it will be returned from the cache.
     *
     * @param serviceClass the class of the service to load
     * @param <S>          the type of the service
     * @return an instance of the requested service
     */
    @SuppressWarnings("unchecked")
    public static <S extends Service> S load(Class<S> serviceClass) {
        requireNonNull(serviceClass);
        return (S) services.computeIfAbsent(serviceClass, c -> doLoad((Class<S>) c));
    }

    private static <S extends Service> S doLoad(Class<S> serviceClass) {
        LOGGER.info("Loading service {}", ClassUtils.getName(serviceClass));
        initShutdown();
        ServiceLoader<S> services = ServiceLoader.load(serviceClass);
        Optional<S> firstService = services.findFirst();
        if (firstService.isPresent()) {
            S service = firstService.get();
            service.start();
            return service;
        }
        throw new ServiceException("A service of type " + serviceClass.getName() + " could not be found");
    }

    private static void initShutdown() {
        if (initialized.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(ServiceFactory::shutdown));
        }
    }
}
