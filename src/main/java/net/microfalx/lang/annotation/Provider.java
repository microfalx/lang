package net.microfalx.lang.annotation;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.*;

/**
 * An annotation used to indicate an implementation of an extension interface that should be discoverable
 * by the runtime during a provider scanning phase.
 * <p/>
 * A provider can be a service, a listener, a factory
 * or a <code>service</code> provider which can be discovered and registered.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@IndexAnnotated
public @interface Provider {

    /**
     * A list of classes which must exist in class path for the annotated class to be used as a provider.
     *
     * @return a non-null array
     */
    String[] dependsOn() default {};
}
