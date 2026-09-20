package net.microfalx.lang.service;

import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.Initializable;
import net.microfalx.lang.Releasable;
import net.microfalx.lang.annotation.DependsOn;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ClassUtils.isSubClassOf;

/**
 * A factory which provides implementations of services.
 * <p>
 * The factory uses the JDK {@link ServiceLoader} and {@link ClassUtils#resolveProviderInstances(Class)}
 * to discover both the implementations of services and the implementations of {@link Service.Listener}.
 */
public class ServiceLocator {

    private static final Logger LOGGER = Logger.get(ServiceLocator.class);

    private static final Map<Class<?>, Service> services = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Service> serviceImplementations = new ConcurrentHashMap<>();
    private static final Map<Class<?>, WeakReference<Service>> serviceProxies = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ServiceStatistics<?>> serviceStatistics = new ConcurrentHashMap<>();
    private static final List<Service.Listener> listeners = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicBoolean listenersLoaded = new AtomicBoolean(false);

    private static final AtomicBoolean quiet = new AtomicBoolean(false);
    static final net.microfalx.lang.Logger quietLogger = net.microfalx.lang.Logger.create();

    /**
     * Shuts down all services. This method should be called when the application is shutting down to ensure
     * that all services are properly stopped.
     */
    public static void shutdown() {
        synchronized (ServiceLocator.class) {
            LOGGER.debug("Shutting down services");
            Collection<Service> loadedServices = getServices();
            loadedServices.forEach(service -> {
                stopService(service);
                notifyStopped(service);
            });
            loadedServices.forEach(ServiceLocator::destroyService);
            services.clear();
            serviceImplementations.clear();
            serviceStatistics.clear();
        }
    }

    /**
     * Shuts down a service.
     *
     * @param serviceClass the class of the service to shut down.
     * @param <S>          the type of the service
     */
    @SuppressWarnings("unchecked")
    public static <S extends Service> void shutdown(Class<S> serviceClass) {
        requireNonNull(serviceClass);
        synchronized (ServiceLocator.class) {
            LOGGER.debug("Shutting down service {}", ClassUtils.getName(serviceClass));
            S service = (S) services.get(serviceClass);
            if (service != null) {
                Class<S> implementationClass = (Class<S>) service.getClass();
                getServiceInterfaces(service).forEach(services::remove);
                serviceImplementations.remove(implementationClass);
                try {
                    if (service instanceof Releasable) {
                        try {
                            ((Releasable) service).release();
                        } catch (Exception e) {
                            LOGGER.atWarn().setCause(e).log("Error while releasing service {}", ClassUtils.getName(serviceClass));
                        }
                    }
                    stopService(service);
                    notifyStopped(service);
                } catch (Exception e) {
                    LOGGER.atWarn().setCause(e).log("Error while shutting down service {}", ClassUtils.getName(serviceClass));
                }
                serviceStatistics.values().removeIf(statistics -> isSubClassOf(statistics.getService(), implementationClass));
            }
        }
    }

    /**
     * Returns a collection of all loaded services.
     *
     * @return a non-null instance
     */
    public static Collection<Service> getServices() {
        return serviceImplementations.values().stream()
                .filter(Objects::nonNull).toList();
    }

    /**
     * Returns a collection of external services.
     *
     * @return a non-null instance
     */
    public static Collection<Service> getServiceProxies() {
        return serviceProxies.values().stream().map(Reference::get)
                .filter(Objects::nonNull).toList();
    }

    /**
     * Checks if a service is loaded.
     *
     * @param serviceClass the class of the service to check
     * @param <S>          the type of the service
     * @return true if the service is loaded, false otherwise
     */
    public static <S extends Service> boolean isLoaded(Class<S> serviceClass) {
        requireNonNull(serviceClass);
        return services.containsKey(serviceClass);
    }

    /**
     * Registers a new service.
     *
     * @param service the service instance
     * @param <S>     the service type
     */
    @SuppressWarnings("unchecked")
    public static <S extends Service> void register(S service) {
        requireNonNull(service);
        loadDependencies(service.getClass());
        synchronized (ServiceLocator.class) {
            loadListeners();
            Collection<Class<?>> serviceInterfaces = getServiceInterfaces(service);
            if (!serviceInterfaces.isEmpty()) {
                serviceInterfaces.forEach(sc -> services.put(sc, service));
            } else {
                services.put(service.getClass(), service);
            }
            initialize(service, (Class<S>) service.getClass());
            if (service instanceof ServiceProxy) {
                Class<?> serviceClass = getRealServiceClass(service);
                serviceProxies.put(serviceClass, new WeakReference<>(service));
            } else {
                serviceImplementations.put(service.getClass(), service);
            }
            serviceStatistics.computeIfPresent(service.getClass(),
                    (cls, statistics) -> statistics.getService() == service ? statistics : null);
        }
    }

    /**
     * Returns the statistics collected for a service.
     * <p>
     * The statistics are created on demand, they are updated out of the events reported with
     * {@link #report(Service, Service.Metric)} and they live for as long as the service is registered.
     *
     * @param service the service
     * @param <S>     the service type
     * @return a non-null instance
     */
    public static <S extends Service> Service.Statistics<S> getStatistics(S service) {
        return doGetStatistics(service);
    }

    /**
     * Returns the statistics collected for all registered services, usually used to produce a report.
     *
     * @return a non-null instance
     */
    public static Collection<Service.Statistics<?>> getStatistics() {
        return List.copyOf(serviceStatistics.values());
    }

    /**
     * Reports an event about a service.
     * <p>
     * The event is applied to the statistics of the service with a value of one, which increments the counters
     * changed by the event:
     * <pre>
     *     ServiceLocator.report(service, Service.Event.SUCCESS);
     * </pre>
     *
     * @param service the service which reports the event
     * @param metric  the event
     * @param <S>     the service type
     * @see Service#report(Service.Metric)
     */
    public static <S extends Service> void report(S service, Service.Metric metric) {
        report(service, metric, 1);
    }

    /**
     * Reports an event about a service.
     * <p>
     * The value carries how much the event changes the statistics: the amount added to the counters changed by
     * the event or the new value of a gauge (see {@link Service.Metric#isGauge()}):
     * <pre>
     *     ServiceLocator.report(service, Service.Event.MEMORY_USAGE, 1024);
     * </pre>
     * <p>
     * Events can be reported from any thread.
     *
     * @param service the service which reports the event
     * @param metric  the event
     * @param value   the value carried by the event
     * @param <S>     the service type
     * @see Service#report(Service.Metric, long)
     */
    public static <S extends Service> void report(S service, Service.Metric metric, long value) {
        requireNonNull(service);
        requireNonNull(metric);
        doGetStatistics(service).apply(metric, value);
        notifyEvent(service, metric, value);
    }

    /**
     * Registers a listener which will be notified about the lifecycle and the events of all services.
     * <p>
     * Most listeners should be discovered automatically, either through the JDK {@link ServiceLoader} or the
     * {@code @Provider} pattern (see {@link Service.Listener}); this method exists for listeners which cannot be
     * discovered this way (for example, listeners created dynamically).
     *
     * @param listener the listener
     * @see #removeListener(Service.Listener)
     */
    public static void addListener(Service.Listener listener) {
        requireNonNull(listener);
        listeners.add(listener);
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the listener
     * @see #addListener(Service.Listener)
     */
    public static void removeListener(Service.Listener listener) {
        requireNonNull(listener);
        listeners.remove(listener);
    }

    /**
     * Returns the listeners registered with this locator, discovering them (via the JDK {@link ServiceLoader} and
     * the {@code @Provider} pattern) on the first call.
     *
     * @return a non-null instance
     */
    public static Collection<Service.Listener> getListeners() {
        loadListeners();
        return List.copyOf(listeners);
    }

    /**
     * Returns whether the service locator (and services) is in quiet mode.
     *
     * @return {@code true} if quiet, {@code false} otherwise
     */
    public static boolean isQuiet() {
        return quiet.get();
    }

    /**
     * Changes whether the service locator (and services) is in quiet mode.
     *
     * @param value {@code true} if quiet, {@code false} otherwise
     */
    public static void setQuiet(boolean value) {
        quiet.set(value);
    }

    /**
     * Returns (and resets) the service log, which is a log of all messages logged while the service locator
     * (and services).
     *
     * @return a non-null instance
     */
    public static String getLog() {
        return quietLogger.getOutput();
    }

    private static void loadListeners() {
        if (listenersLoaded.compareAndSet(false, true)) {
            ServiceLoader.load(Service.Listener.class).forEach(listeners::add);
            listeners.addAll(ClassUtils.resolveProviderInstances(Service.Listener.class));
        }
    }

    private static void notifyStarted(Service service) {
        for (Service.Listener listener : listeners) {
            try {
                listener.onServiceStarted(service);
            } catch (Exception e) {
                LOGGER.atWarn().setCause(e).log("Failed to notify listener {} that service {} started",
                        ClassUtils.getName(listener), ClassUtils.getName(service));
            }
        }
    }

    private static void notifyStopped(Service service) {
        for (Service.Listener listener : listeners) {
            try {
                listener.onServiceStopped(service);
            } catch (Exception e) {
                LOGGER.atWarn().setCause(e).log("Failed to notify listener {} that service {} stopped",
                        ClassUtils.getName(listener), ClassUtils.getName(service));
            }
        }
    }

    private static void notifyEvent(Service service, Service.Metric metric, long value) {
        for (Service.Listener listener : listeners) {
            try {
                listener.onServiceEvent(service, metric, value);
            } catch (Exception e) {
                LOGGER.atWarn().setCause(e).log("Failed to notify listener {} about event {} for service {}",
                        ClassUtils.getName(listener), metric, ClassUtils.getName(service));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <S extends Service> ServiceStatistics<S> doGetStatistics(S service) {
        requireNonNull(service);
        Class<?> serviceClass = getRealServiceClass(service);
        return (ServiceStatistics<S>) serviceStatistics.computeIfAbsent(serviceClass,
                cls -> new ServiceStatistics<>(service));
    }

    /**
     * Looks up a service.
     * <p>
     * The locator uses the Java {@link ServiceLoader} mechanism to load implementations of services and
     * {@link ClassUtils#resolveProviderInstances(Class)}. If a service has already been loaded (and initialized),
     * it will be returned from the cache.
     *
     * @param serviceClass the class of the service to load
     * @param <S>          the type of the service
     * @return an instance of the requested service
     */
    @SuppressWarnings("unchecked")
    public static <S extends Service> S lookup(Class<S> serviceClass) {
        requireNonNull(serviceClass);
        synchronized (ServiceLocator.class) {
            S service = (S) services.get(serviceClass);
            if (service == null) {
                service = doLoad(serviceClass);
                register(service);
            }
            return service;
        }
    }

    /**
     * Returns the reference to the real service implementation class. If the service is a proxy,
     * it will return the underlying service class.
     *
     * @param service the service instance
     * @param <S>     the service type
     * @return the real service implementation class
     */
    public static <S extends Service> Class<?> getRealServiceClass(S service) {
        return getRealService(service).getClass();
    }

    /**
     * Returns the reference to the real service implementation. If the service is a proxy,
     * it will return the underlying service.
     *
     * @param service the service instance
     * @param <S>     the service type
     * @return the real service implementation
     */
    public static <S extends Service> Object getRealService(S service) {
        if (service instanceof ServiceProxy) {
            return ((ServiceProxy) service).getService();
        } else {
            return service;
        }
    }

    static void startService(Object service) {
        if (service instanceof Service.Lifecycle) {
            try {
                ((Service.Lifecycle) service).start();
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to stop service {}",
                        ClassUtils.getName(service));
            }
        }
    }

    static void stopService(Object service) {
        if (service instanceof Service.Lifecycle) {
            try {
                ((Service.Lifecycle) service).stop();
            } catch (Exception e) {
                LOGGER.atWarn().setCause(e).log("Failed to stop service {}",
                        ClassUtils.getName(service));
            }
        }
    }

    static void destroyService(Object service) {
        if (service instanceof Releasable) {
            try {
                ((Releasable) service).release();
            } catch (Exception e) {
                LOGGER.atWarn().setCause(e).log("Failed to release service {}",
                        ClassUtils.getName(service));
            }
        }
    }

    private static <S extends Service> S doLoad(Class<S> serviceClass) {
        LOGGER.debug("Loading service {}", ClassUtils.getName(serviceClass));
        initShutdown();
        Collection<S> services = new ArrayList<>();
        ServiceLoader.load(serviceClass).stream().forEach(s -> services.add(s.get()));
        services.addAll(ClassUtils.resolveProviderInstances(serviceClass));
        if (services.size() > 1) {
            throw new ServiceException("Multiple service implementations located for type " + serviceClass.getName()
                    + ": " + services.stream().map(ClassUtils::getName).collect(Collectors.joining(",")));
        } else if (!services.isEmpty()) {
            return services.iterator().next();
        } else {
            throw new ServiceException("A service of type " + serviceClass.getName() + " could not be found");
        }
    }

    private static <S extends Service> Collection<Class<?>> getServiceInterfaces(S service) {
        return ClassUtils.getInterfaces(service.getClass()).stream()
                .filter(Service.class::isAssignableFrom)
                .filter(sc -> sc != Service.class).toList();
    }

    private static void initShutdown() {
        if (initialized.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(ServiceLocator::shutdown));
        }
    }

    private static <S extends Service> void initialize(S service, Class<S> serviceClass) {
        if (!isSubClassOf(service, serviceClass)) {
            throw new ServiceException("The service " + ClassUtils.getName(service) + " is not a subclass of "
                    + ClassUtils.getName(serviceClass));
        }
        // we do not initialize external services (like Spring beans) as they are initialized by the framework
        if (service instanceof ServiceProxy) return;
        if (service instanceof Initializable) ((Initializable) service).initialize();
        startService(service);
        notifyStarted(service);
    }

    private static <S extends Service> void loadDependencies(Class<S> serviceClass) {
        DependsOn dependsOnAnnot = AnnotationUtils.getAnnotation(serviceClass, DependsOn.class);
        if (dependsOnAnnot == null) return;
        for (Class<?> clazz : dependsOnAnnot.classes()) {
            if (ClassUtils.isSubClassOf(clazz, Service.class)) {
                lookup((Class<? extends Service>) clazz);
            } else {
                throw new ServiceException("The class " + ClassUtils.getName(clazz) + " is not a subclass of "
                        + ClassUtils.getName(Service.class));
            }
        }
    }


}
