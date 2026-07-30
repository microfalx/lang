package net.microfalx.lang.service;

public interface Test2Service extends Service {

    static Test2Service getInstance() {
        return Service.lookup(Test2Service.class);
    }
}
