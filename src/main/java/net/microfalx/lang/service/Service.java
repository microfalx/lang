package net.microfalx.lang.service;

import net.microfalx.lang.*;

/**
 * Base class for simple application services.
 * <p>
 * If a service implements {@link Initializable} it will be automatically initialized when the service is looked
 * up for the first.
 * Similar, if the service implemented {@link Releasable} the service (resources) is destroyed when the JVM is shutdown.
 * <p>
 * If a service implement {@link Lifecycle} it will be automatically started when the service is looked up for the
 * first time and stopped when the JVM is shutdown (before it is destroyed).
 * <p>
 * A service reports what happens to it with {@link #report(Metric)}; the {@link ServiceLocator} translates these
 * events into the {@link Statistics} of the service, which are later used to produce reports, and also forwards
 * them (along with the service start/stop lifecycle) to any registered {@link Listener}.
 */
public interface Service extends Identifiable<String>, Nameable, Descriptable {

    @Override
    default String getId() {
        return Hashing.hash(getClass().getName());
    }

    @Override
    default String getName() {
        return ServiceUtils.getName(this);
    }

    @Override
    default String getDescription() {
        return ServiceUtils.getDescription(this);
    }

    /**
     * Reports a metric about this service.
     * <p>
     * The metric is applied to the statistics of the service with a value of one, which increments the
     * counters changed by the metric.
     *
     * @param metric the metric
     * @see ServiceLocator#report(Service, Metric)
     */
    default void report(Metric metric) {
        ServiceLocator.report(this, metric);
    }

    /**
     * Reports a metric about this service.
     * <p>
     * The value carries how much the metric changes the statistics: the amount added to the counters changed
     * by the metric or the new value of a gauge (see {@link Metric#isGauge()}).
     *
     * @param metric the metric
     * @param value  the value carried by the metric
     * @see ServiceLocator#report(Service, Metric, long)
     */
    default void report(Metric metric, long value) {
        ServiceLocator.report(this, metric, value);
    }

    /**
     * Returns the statistics collected out of the events reported by this service.
     *
     * @return a non-null instance
     * @see ServiceLocator#getStatistics(Service)
     */
    default Statistics<? extends Service> getStatistics() {
        return ServiceLocator.getStatistics(this);
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

    /**
     * Creates a proxy for a service not managed by the service locator.
     *
     * @param reference the original service instance
     * @return a proxy for the service
     */
    static Service proxy(Object reference) {
        return new ServiceProxy(reference);
    }

    /**
     * Provides lifecycle management for a service.
     */
    interface Lifecycle {

        /**
         * Starts the service.
         */
        void start();

        /**
         * Stops the service.
         */
        default void stop() {

        }
    }

    /**
     * An enum describing the events reported by a service.
     * <p>
     * Most events are counters and increment the statistics with the value carried by the event (one by default);
     * a few of them are gauges and change the statistics to the value carried by the event. An event can change
     * more than one metric: a task which was started, for example, is not pending anymore.
     */
    enum Metric {

        /**
         * The service generated a warning.
         */
        WARNING,

        /**
         * The service generated an error.
         */
        ERROR,

        /**
         * The service completed an operation successfully.
         */
        SUCCESS,

        /**
         * The service failed to complete an operation.
         */
        FAILURE,

        /**
         * A task owned by the service was scheduled and waits to be executed.
         */
        TASK_SCHEDULED,

        /**
         * A task owned by the service started to execute (and it is not pending anymore).
         */
        TASK_STARTED,

        /**
         * A task owned by the service completed successfully (and it is not running anymore).
         */
        TASK_SUCCEEDED,

        /**
         * A task owned by the service failed to complete (and it is not running anymore).
         */
        TASK_FAILED,

        /**
         * The service consumed (received) an event.
         */
        EVENT_IN,

        /**
         * The service produced (sent) an event.
         */
        EVENT_OUT,

        /**
         * A thread used by the service was started.
         */
        THREAD_STARTED,

        /**
         * A thread used by the service was stopped.
         */
        THREAD_STOPPED,

        /**
         * The memory used by the service was measured, the value of the metric is the memory usage, in bytes.
         */
        MEMORY_USAGE(true),

        /**
         * The memory used by the service was measured, the value of the metric is number of referenced objects.
         */
        MEMORY_OBJECTS(true),

        /**
         * The memory used by the service was measured, the value of the metric is the memory usage, in bytes.
         */
        MEMORY_ARRAY_USAGE(true),

        /**
         * The memory used by the service was measured, the value of the metric is number of referenced objects.
         */
        MEMORY_ARRAY_OBJECTS(true),

        /**
         * The threads used by the service were counted, the value of the metric is the number of threads.
         */
        THREAD_COUNT(true);

        private final boolean gauge;

        Metric() {
            this(false);
        }

        Metric(boolean gauge) {
            this.gauge = gauge;
        }

        /**
         * Returns whether the event carries the new value of a metric instead of an amount added to a metric.
         *
         * @return {@code true} if the event is a gauge, {@code false} if the event is a counter
         */
        public boolean isGauge() {
            return gauge;
        }
    }

    /**
     * A listener notified about the lifecycle and the events of a service.
     * <p>
     * Listeners are discovered with the JDK {@link java.util.ServiceLoader} and with the
     * {@link net.microfalx.lang.annotation.Provider} pattern (see
     * {@link net.microfalx.lang.ClassUtils#resolveProviderInstances(Class)}), and can also be registered directly
     * with {@link ServiceLocator#addListener(Listener)}. All methods are called with the service instance they
     * refer to and have a default (empty) implementation, so a listener only needs to override what it cares about.
     * <p>
     * Listeners are called synchronously, from the thread which changed the service (started it, stopped it or
     * reported the event); an implementation should not block.
     */
    interface Listener {

        /**
         * Notifies the listener that a service was started (looked up or registered for the first time).
         *
         * @param service the service
         */
        default void onServiceStarted(Service service) {
        }

        /**
         * Notifies the listener that a service was stopped (shut down).
         *
         * @param service the service
         */
        default void onServiceStopped(Service service) {
        }

        /**
         * Notifies the listener that a service reported an event.
         *
         * @param service the service which reported the event
         * @param metric  the event
         * @param value   the value carried by the event
         * @see Service#report(Metric, long)
         */
        default void onServiceEvent(Service service, Metric metric, long value) {
        }
    }

    /**
     * An interface describing the statistics of a service.
     * <p>
     * The statistics are a read-only view over the metrics collected out of the events reported with
     * {@link Service#report(Metric)}, and they are owned by the {@link ServiceLocator}.
     */
    interface Statistics<S extends Service> extends Identifiable<String>, Nameable, Descriptable {

        /**
         * Returns the service for which the statistics are collected.
         *
         * @return a non-null instance
         */
        S getService();

        /**
         * Returns the class name of the service for which the statistics are collected.
         *
         * @return a non-null instance
         */
        String getClassName();

        /**
         * Returns the memory usage of the service for which the statistics are collected.
         *
         * @return a positive number or -1 if the memory usage cannot be determined
         */
        long getMemoryUsage();

        /**
         * Returns the number of objects referenced by the service for which the statistics are collected.
         *
         * @return a positive number or -1 if the number of objects cannot be determined
         */
        int getMemoryObjects();

        /**
         * Returns the memory usage of arrays of the service for which the statistics are collected.
         *
         * @return a positive number or -1 if the memory usage cannot be determined
         */
        long getMemoryArrayUsage();

        /**
         * Returns the number of objects for arrays referenced by the service for which the statistics are collected.
         *
         * @return a positive number or -1 if the number of objects cannot be determined
         */
        int getMemoryArrayObjects();

        /**
         * Returns the number of warnings generated by this service.
         *
         * @return a positive number or 0 if no warnings were generated
         */
        int getWarningCount();

        /**
         * Returns the number of errors generated by this service.
         *
         * @return a positive number or 0 if no errors were generated
         */
        int getErrorCount();

        /**
         * Returns the number of successful operations performed by this service.
         *
         * @return a positive number or 0 if no successful operations were performed
         */
        int getSuccessCount();

        /**
         * Returns the number of failed operations performed by this service.
         *
         * @return a positive number or 0 if no successful operations were performed
         */
        int getFailedCount();

        /**
         * Returns the number of events consumed (received) by this service.
         *
         * @return a positive number or 0 if no events were consumed
         */
        int getEventInCount();

        /**
         * Returns the number of events produced (sent) by this service.
         *
         * @return a positive number or 0 if no events were produced
         */
        int getEventOutCount();

        /**
         * Returns the number of running of tasks owned by this service.
         *
         * @return a positive number or 0 if no tasks are running
         */
        int getTaskRunningCount();

        /**
         * Returns the number of pending tasks owned by this service.
         *
         * @return a positive number or 0 if no tasks are pending
         */
        int getTaskPendingCount();

        /**
         * Returns the number of threads used by this service.
         *
         * @return a positive number or 0 if no threads are used
         */
        int getThreadCount();
    }
}
