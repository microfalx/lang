package net.microfalx.lang.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used to control how the deep size of an object is calculated.
 */
@Target({FIELD, TYPE})
@Retention(RUNTIME)
@Inherited
@Documented
public @interface SizeOf {

    /**
     * Returns whether the annotated object should be calculated shallowly or deeply.
     * <p>
     * If shallow, the size of the object is calculated without traversing its fields.
     * If deep, the size of the object is calculated by traversing its fields and calculating
     * their sizes as well.
     *
     * @return {@code true} if the size should be calculated shallowly, {@code false} if it should be calculated deeply.
     */
    boolean shallow() default true;

    /**
     * Returns the worst case scenario size of the annotated object.
     *
     * @return positive integer if it can be approximated, -1 if it cannot be approximated
     */
    int deepSize() default -1;

    /**
     * A round number which indicates that the size of the object cannot be estimated and should be
     * treated as a large object.
     */
    int UNESTIMATED_SIZE = 10_000;
}
