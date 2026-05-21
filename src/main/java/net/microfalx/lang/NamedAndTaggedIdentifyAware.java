package net.microfalx.lang;

import com.google.common.base.MoreObjects;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all objects which can be identified, named, tagged and described.
 *
 * @param <T> the type of the identity
 */
public class NamedAndTaggedIdentifyAware<T> extends NamedIdentityAware<T> {

    public static String SELF_TAG = "self";
    public static String AUTO_TAG = "auto";
    public static String LOCAL_TAG = "local";

    private Set<String> tags;

    /**
     * Returns the tags associated with this instance.
     *
     * @return a non-null instance
     */
    public Set<String> getTags() {
        return tags != null ? unmodifiableSet(tags) : Collections.emptySet();
    }

    /**
     * Updates the tags associated with this instance.
     *
     * @param tags the tags to add, must not be null
     */
    protected void updateTags(Set<String> tags) {
        requireNonNull(tags);
        if (this.tags == null) this.tags = new HashSet<>();
        this.tags.addAll(tags);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NamedAndTaggedIdentifyAware.class)
                .add("super", super.toString())
                .add("tags", tags)
                .toString();
    }

    /**
     * A builder class.
     *
     * @param <T> the type of the identity
     */
    public static abstract class Builder<T> extends NamedIdentityAware.Builder<T> {

        private Set<String> tags;

        public Builder(T id) {
            super(id);
        }

        public Builder() {
        }

        public Builder<T> tag(String tag) {
            requireNonNull(tag);
            if (tags == null) tags = new HashSet<>();
            tags.add(tag);
            return this;
        }

        public Builder<T> tags(Collection<String> tags) {
            requireNonNull(tags);
            if (this.tags == null) this.tags = new HashSet<>();
            this.tags.addAll(tags);
            return this;
        }

        @Override
        public NamedAndTaggedIdentifyAware<T> build() {
            NamedAndTaggedIdentifyAware<T> item = (NamedAndTaggedIdentifyAware<T>) super.build();
            item.tags = tags;
            return item;
        }
    }
}
