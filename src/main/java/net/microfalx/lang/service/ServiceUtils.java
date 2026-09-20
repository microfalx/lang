package net.microfalx.lang.service;

import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;

import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.replaceFirst;

/**
 * Variable utilities for services.
 */
public class ServiceUtils {

    /**
     * Returns the name of the service implementation. If the service is annotated with {@link Name}, the value of
     * the annotation is returned. Otherwise, the name is derived from the class name.
     *
     * @param service the service instance
     * @return a non-null instance
     */
    public static String getName(Object service) {
        Name nameAnnot = AnnotationUtils.getAnnotation(service, Name.class);
        if (nameAnnot != null) {
            return nameAnnot.value();
        } else {
            String name = StringUtils.beautifyCamelCase(service.getClass().getSimpleName());
            return replaceFirst(name, "Impl", EMPTY_STRING);
        }
    }

    /**
     * Returns the description of the service implementation. If the service is annotated with {@link Description},
     * the value of the annotation is returned. Otherwise, an empty string is returned.
     *
     * @param service the service instance
     * @return a non-null instance
     */
    public static String getDescription(Object service) {
        Description descriptionAnnot = AnnotationUtils.getAnnotation(service, Description.class);
        if (descriptionAnnot != null) {
            return descriptionAnnot.value();
        } else {
            return EMPTY_STRING;
        }
    }
}
