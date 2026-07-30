package net.microfalx.lang.service;

import org.atteo.classindex.IndexSubclasses;

@IndexSubclasses
public interface Test1Service extends Service {

    static Test1Service getInstance() {
        return Service.lookup(Test1Service.class);
    }
}
