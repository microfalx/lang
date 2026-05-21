package net.microfalx.lang;

import com.google.common.base.MoreObjects;
import net.microfalx.lang.annotation.Id;

import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all objects which can be identified.
 *
 * @param <T> the type of the identity
 */
public abstract class IdentityAware<T> implements Identifiable<T>, Cloneable {

    @Id
    private T id;

    @Override
    public final T getId() {
        return id;
    }

    protected final void setId(T id) {
        requireNotEmpty(id);
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityAware<?> that = (IdentityAware<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Creates a shallow copy of this {@link Identifiable}.
     *
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    protected IdentityAware<T> copy() {
        try {
            IdentityAware<T> copy = (IdentityAware<T>) clone();
            copyProperties(this, copy);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Subclasses can deep-copy properties from old to new
     *
     * @param source the source instance
     * @param target the target instance
     */
    protected void copyProperties(IdentityAware<T> source, IdentityAware<T> target) {
        // empty by default
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(IdentityAware.class)
                .add("id", id)
                .toString();
    }

    /**
     * A builder class.
     *
     * @param <T> the type of the identity
     */
    public static abstract class Builder<T> {

        private T id;

        public Builder(T id) {
            id(id);
        }

        public Builder() {
        }

        /**
         * Returns the current identifier.
         *
         * @return the identifier
         */
        public T id() {
            return id;
        }

        /**
         * Changes the object (natural) identifier.
         *
         * @param id the identifier
         * @return self
         */
        @SuppressWarnings("unchecked")
        public Builder<T> id(T id) {
            requireNonNull(id);
            if (id instanceof String) id = (T) StringUtils.toIdentifier((String) id);
            this.id = id;
            return this;
        }

        /**
         * Creates a new instance of the object create by this builder.
         *
         * @return a non-null instance
         */
        protected abstract IdentityAware<T> create();

        /**
         * Invoked before the object is created to update the (natural) identifier of the object.
         */
        protected T updateId() {
            return null;
        }

        /**
         * Builds an instance of the object create by this builder.
         *
         * @return a non-null instance
         */
        public IdentityAware<T> build() {
            T newId = updateId();
            if (newId != null && this.id == null) id(newId);
            if (id == null) throw new IllegalArgumentException("Identifier is required");
            IdentityAware<T> identityAware = create();
            identityAware.id = id;
            return identityAware;
        }
    }

}
