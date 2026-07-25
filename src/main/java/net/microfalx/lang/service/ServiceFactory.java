package net.microfalx.lang.service;

import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ClassUtils.isSubClassOf;

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
            services.clear();
        }
    }

    /**
     * Shuts down a service.
     *
     * @param serviceClass the class of the service to shut down.
     * @param <S>          the type of the service
     */
    public static <S extends Service> void shutdown(Class<S> serviceClass) {
        requireNonNull(serviceClass);
        synchronized (ServiceFactory.class) {
            LOGGER.info("Shutting down service {}", ClassUtils.getName(serviceClass));
            Service service = services.remove(serviceClass);
            if (service != null) service.stop();
            services.remove(serviceClass);
        }
    }

    /**
     * Loads a service.
     * <p>
     * The factory uses the Java {@link ServiceLoader} mechanism to load implementations of services and
     * {@link ClassUtils#resolveProviderInstances(Class)}. If a service has already been loaded (and initialized),
     * it will be returned from the cache.
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

    @SuppressWarnings("unchecked")
    private static <S extends Service> S doLoad(Class<S> serviceClass) {
        LOGGER.info("Loading service {}", ClassUtils.getName(serviceClass));
        initShutdown();
        Collection<S> services = new ArrayList<>();
        ServiceLoader.load(serviceClass).stream().forEach(s -> services.add(s.get()));
        services.addAll(ClassUtils.resolveProviderInstances(serviceClass));
        if (services.size() > 1) {
            throw new ServiceException("Multiple service implementations located for type " + serviceClass.getName()
                    + ": " + services.stream().map(ClassUtils::getName).collect(Collectors.joining(",")));
        } else if (!services.isEmpty()) {
            S service = services.iterator().next();
            if (!isSubClassOf(service, serviceClass)) {
                throw new ServiceException("The service " + ClassUtils.getName(service) + " is not a subclass of "
                        + ClassUtils.getName(serviceClass));
            }
            if (service instanceof Initializable) ((Initializable) service).initialize();
            service.start();
            return service;
        } else {
            throw new ServiceException("A service of type " + serviceClass.getName() + " could not be found");
        }
    }

    private static void initShutdown() {
        if (initialized.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(ServiceFactory::shutdown));
        }
    }
}
