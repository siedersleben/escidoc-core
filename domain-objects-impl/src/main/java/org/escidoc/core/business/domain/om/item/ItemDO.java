package org.escidoc.core.business.domain.om.item;

import java.util.Set;

import net.sf.oval.constraint.AssertFieldConstraints;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;

import org.escidoc.core.business.domain.base.DomainObject;
import org.escidoc.core.business.domain.base.ID;
import org.escidoc.core.business.domain.common.MdRecordDO;
import org.escidoc.core.business.domain.om.component.ComponentsDO;

/**
 * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
 * @author Michael Hoppe (michael.hoppe@fiz-karlsruhe.de)
 */
@Guarded(checkInvariants = true)
public final class ItemDO extends DomainObject {

	private ID id;

	@NotNull
    private ItemPropertiesDO properties;

    @NotNull
    private ComponentsDO components;

    @NotNull
    private Set<MdRecordDO> mdRecords;

    @NotNull
    private Set<RelationDO> relations;

	public void setId(ID id) {
		this.id = id;
	}
	
	public ItemDO(Builder builder) {
	    this.id = builder.id;
	    this.properties = builder.properties;
	    this.components = builder.components;
	    this.mdRecords = builder.mdRecords;
	    this.relations = builder.relations;
	}

	public void setProperties(@AssertFieldConstraints ItemPropertiesDO properties) {
		this.properties = properties;
	}

    @AssertFieldConstraints
	public ID getId() {
		return id;
	}

    @AssertFieldConstraints
    public ItemPropertiesDO getProperties() {
        return properties;
    }

    @AssertFieldConstraints
    public ComponentsDO getComponents() {
        return components;
    }

    @AssertFieldConstraints
    public Set<MdRecordDO> getMdRecords() {
        return mdRecords;
    }

    @AssertFieldConstraints
	public Set<RelationDO> getRelations() {
		return relations;
	}

	public void setRelations(@AssertFieldConstraints Set<RelationDO> relations) {
		this.relations = relations;
	}

	public void setMdRecords(@AssertFieldConstraints Set<MdRecordDO> mdRecords) {
		this.mdRecords = mdRecords;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemDO itemDO = (ItemDO) o;

        if (!components.equals(itemDO.components)) {
            return false;
        }
        if (!mdRecords.equals(itemDO.mdRecords)) {
            return false;
        }
        if (!properties.equals(itemDO.properties)) {
            return false;
        }
        if (!relations.equals(itemDO.relations)) {
            return false;
        }
        if (!id.equals(itemDO.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = properties.hashCode();
        result = 31 * result + components.hashCode();
        result = 31 * result + mdRecords.hashCode();
        result = 31 * result + relations.hashCode();
        result = 31 * result + id.hashCode();
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
        sb.append("ItemDO");
        sb.append("{properties=").append(properties);
        sb.append(", components=").append(components);
        sb.append(", mdRecords=").append(mdRecords);
        sb.append(", relations=").append(relations);
        sb.append(", id=").append(id);
        sb.append('}');
        return sb;
    }
    
    public static class Builder {
        private ID id = null;

        private ItemPropertiesDO properties = null;

        private ComponentsDO components = null;

        private Set<MdRecordDO> mdRecords = null;

        private Set<RelationDO> relations = null;
        
        public Builder id(ID id) {
            this.id = id;
            return this;
        }

        public Builder properties(ItemPropertiesDO properties) {
            this.properties = properties;
            return this;
        }

        public Builder components(ComponentsDO components) {
            this.components = components;
            return this;
        }

        public Builder mdRecords(Set<MdRecordDO> mdRecords) {
            this.mdRecords = mdRecords;
            return this;
        }

        public Builder relations(Set<RelationDO> relations) {
            this.relations = relations;
            return this;
        }
        
        public ItemDO build() {
            return new ItemDO(this);
        }
        
    }
}