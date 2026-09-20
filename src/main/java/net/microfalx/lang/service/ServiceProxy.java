package net.microfalx.lang.service;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A proxy for a service.
 */
class ServiceProxy implements Service, Service.Lifecycle {

    private final Object service;

    ServiceProxy(Object service) {
        requireNonNull(service);
        this.service = service;
    }

    public Object getService() {
        return service;
    }

    @Override
    public String getName() {
        return ServiceUtils.getName(service);
    }

    @Override
    public String getDescription() {
        return ServiceUtils.getDescription(service);
    }

    @Override
    public void start() {
        ServiceLocator.startService(service);
    }

    @Override
    public void stop() {
        ServiceLocator.stopService(service);
    }
}
