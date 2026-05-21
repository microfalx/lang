package net.microfalx.lang;

import com.google.common.base.MoreObjects;
import net.microfalx.lang.annotation.Name;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;

/**
 * Base class for all objects which can be identified, named and described.
 *
 * @param <T> the type of the identity
 */
public abstract class NamedIdentityAware<T> extends IdentityAware<T> implements Nameable, Descriptable {

    @Name
    private String name;
    private String description;

    @Override
    public final String getName() {
        return defaultIfNull(name, dynamicName());
    }

    protected final NamedIdentityAware<T> setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    protected NamedIdentityAware<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns a new named instance with a different name.
     *
     * @param name the new name
     * @return a new instance
     */
    public NamedIdentityAware<T> withName(String name) {
        requireNotEmpty(name);
        NamedIdentityAware<T> copy = (NamedIdentityAware<T>) copy();
        copy.name = name;
        return copy;
    }

    /**
     * Subclasses can provide a different name for this object.
     *
     * @return a new name.
     */
    protected String dynamicName() {
        return name;
    }

    /**
     * Returns a string representation of this object made out of the name and identifier.
     *
     * @return a non-null instance
     */

    protected final String getNameAndId() {
        return getName() + " (" + getId() + ")";
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NamedIdentityAware.class)
                .add("super", super.toString())
                .add("name", name)
                .add("description", description)
                .toString();
    }

    /**
     * A builder class.
     *
     * @param <T> the type of the identity
     */
    public static abstract class Builder<T> extends IdentityAware.Builder<T> {

        private String name;
        private String description;

        public Builder(T id) {
            super(id);
            this.name = capitalizeWords(ObjectUtils.toString(id));
        }

        public Builder() {
        }

        protected final String name() {
            return name;
        }

        public final Builder<T> name(String name) {
            requireNotEmpty(name);
            this.name = name;
            return this;
        }

        public final Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        protected final boolean emptyName() {
            return isEmpty(name);
        }

        @Override
        public NamedIdentityAware<T> build() {
            NamedIdentityAware<T> instance = (NamedIdentityAware<T>) super.build();
            instance.name = defaultIfEmpty(name, capitalizeWords(ObjectUtils.toString(instance.getId())));
            instance.description = description;
            return instance;
        }
    }
}
