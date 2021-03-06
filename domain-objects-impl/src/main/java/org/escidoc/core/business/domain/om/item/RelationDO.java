package org.escidoc.core.business.domain.om.item;

import java.net.URI;

import net.sf.oval.constraint.AssertFieldConstraints;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import org.escidoc.core.business.domain.base.AbstractBuilder;
import org.escidoc.core.business.domain.base.DomainObject;
import org.escidoc.core.business.util.annotation.Validate;

/**
 * @author Michael Hoppe (michael.hoppe@fiz-karlsruhe.de)
 */
@Validate
public class RelationDO extends DomainObject {

    @NotNull
    private URI predicate;

    @NotNull
	private URI resource;

    private RelationDO(Builder builder) {
        super(builder.validationProfile);
        this.predicate = builder.predicate;
        this.resource = builder.resource;
    }

    /**
	 * @return the predicate
	 */
    @AssertFieldConstraints
	public URI getPredicate() {
		return predicate;
	}

	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(@AssertFieldConstraints URI predicate) {
		this.predicate = predicate;
	}

	/**
	 * @return the resource
	 */
    @AssertFieldConstraints
	public URI getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(@AssertFieldConstraints URI resource) {
		this.resource = resource;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RelationDO relationDO = (RelationDO) o;

        if (!predicate.equals(relationDO.predicate)) {
            return false;
        }
        if (!resource.equals(relationDO.resource)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = predicate.hashCode();
        result = 31 * result + resource.hashCode();
        return result;
    }

    @Override
    @NotNull
    @NotBlank
    public String toString() {
        return toStringBuilder().toString();
    }

    @NotNull
    public StringBuilder toStringBuilder() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RelationDO");
        sb.append("{predicate=").append(predicate);
        sb.append(", resource=").append(resource);
        sb.append('}');
        return sb;
    }
    
    public static class Builder extends AbstractBuilder {
        private URI predicate = null;

        private URI resource = null;

        public Builder(String validationProfile) {
            super(validationProfile);
        }
        
        public Builder predicate(URI predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder resource(URI resource) {
            this.resource = resource;
            return this;
        }

        public RelationDO build() {
            return new RelationDO(this);
        }
        
    }
}
