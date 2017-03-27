package cz.metacentrum.perun.core.api;

import org.infinispan.query.Transformable;

import java.io.Serializable;

/**
 * Class used as a key for infinispan database entries.
 * Uniquely identifies Attribute and also who does it belong to (holders or subject).
 *
 * @author Simona Kruppova
 */
@Transformable(transformer = AttributeIdWithHoldersTransformer.class)
public class AttributeIdWithHolders implements Serializable {

	private Integer attributeId;
	private String attributeName;
	private Holder primaryHolder;
	private Holder secondaryHolder;
	private String subject;

	public AttributeIdWithHolders(Integer attributeId, String attributeName, Holder primaryHolder, Holder secondaryHolder, String subject) {
		if (primaryHolder != null && secondaryHolder != null) {
			if (secondaryHolder.getType().equals(Holder.HolderType.GROUP) && (!primaryHolder.getType().equals(Holder.HolderType.MEMBER))
					|| (secondaryHolder.getType().equals(Holder.HolderType.MEMBER))
					|| (secondaryHolder.getType().equals(Holder.HolderType.USER))) {
				this.primaryHolder = secondaryHolder;
				this.secondaryHolder = primaryHolder;
			}
			else {
				this.primaryHolder = primaryHolder;
				this.secondaryHolder = secondaryHolder;
			}
		}
		else {
			this.primaryHolder = primaryHolder;
			this.secondaryHolder = secondaryHolder;
		}

		this.attributeId = attributeId;
		this.attributeName = attributeName;
		this.subject = subject;
	}

	public AttributeIdWithHolders(Integer attributeId, Holder primaryHolder, Holder secondaryHolder) {
		this(attributeId, null, primaryHolder, secondaryHolder, null);
	}

	public AttributeIdWithHolders(String attributeName, Holder primaryHolder, Holder secondaryHolder) {
		this(null, attributeName, primaryHolder, secondaryHolder, null);
	}

	public AttributeIdWithHolders(Integer attributeId, String subject) {
		this(attributeId, null, null, null, subject);
	}

	public AttributeIdWithHolders(String attributeName, String subject) {
		this(null, attributeName, null, null, subject);
	}

	public AttributeIdWithHolders(Integer attributeId) {
		this(attributeId, null, null, null, null);
	}

	public AttributeIdWithHolders(String attributeName) {
		this(null, attributeName, null, null, null);
	}

	public Integer getAttributeId() {
		return attributeId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Holder getSecondaryHolder() {
		return secondaryHolder;
	}

	public Holder getPrimaryHolder() {
		return primaryHolder;
	}

	public String getSubject() {
		return subject;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AttributeIdWithHolders)) return false;

		AttributeIdWithHolders that = (AttributeIdWithHolders) o;

		if (attributeId != null ? !attributeId.equals(that.attributeId) : that.attributeId != null) return false;
		if (attributeName != null ? !attributeName.equals(that.attributeName) : that.attributeName != null)
			return false;
		if (primaryHolder != null ? !primaryHolder.equals(that.primaryHolder) : that.primaryHolder != null)
			return false;
		if (secondaryHolder != null ? !secondaryHolder.equals(that.secondaryHolder) : that.secondaryHolder != null)
			return false;
		return !(subject != null ? !subject.equals(that.subject) : that.subject != null);

	}

	@Override
	public int hashCode() {
		int result = attributeId != null ? attributeId.hashCode() : 0;
		result = 31 * result + (attributeName != null ? attributeName.hashCode() : 0);
		result = 31 * result + (primaryHolder != null ? primaryHolder.hashCode() : 0);
		result = 31 * result + (secondaryHolder != null ? secondaryHolder.hashCode() : 0);
		result = 31 * result + (subject != null ? subject.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append("AttributeIdWithHolders{attributeId=").append(attributeId)
				.append(", attributeName='").append(attributeName).append('\'')
				.append(", primaryHolder=").append(primaryHolder)
				.append(", secondaryHolder=").append(secondaryHolder)
				.append(", subject='").append(subject).append('\'').append('}').toString();
	}
}