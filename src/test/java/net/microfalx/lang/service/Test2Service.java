package net.microfalx.lang.service;

public interface Test2Service extends Service {

    static Test2Service getInstance() {
        return Service.load(Test2Service.class);
    }
}
