package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Attribute;


/**
 *
 *
 *
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class RichAttribute<P,S>  {

	private P primaryHolder;
	private S secondaryHolder;
	private Attribute attribute;


	public RichAttribute() {
	}

	public RichAttribute(P primaryHolder, S secondaryHolder, Attribute attribute) {
		this.primaryHolder = primaryHolder;
		this.secondaryHolder = secondaryHolder;
		this.attribute = attribute;
	}


	public P getPrimaryHolder() {
		return this.primaryHolder;
	}

	public void setPrimaryHolder(P primaryHolder) {
		this.primaryHolder = primaryHolder;
	}

	public S getSecondaryHolder() {
		return this.secondaryHolder;
	}

	public void setSecondaryHolder(S secondaryHolder) {
		this.secondaryHolder = secondaryHolder;
	}

	public Attribute getAttribute() {
		return this.attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + (primaryHolder == null ? 0 : primaryHolder.hashCode());
		hash = 53 * hash + (secondaryHolder == null ? 0 : secondaryHolder.hashCode());
		hash = 53 * hash + (attribute == null ? 0 : attribute.hashCode());
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;

		if (getClass() != obj.getClass()) return false;

		RichAttribute other = (RichAttribute) obj;

		if(this.primaryHolder == null ? other.primaryHolder != null : !this.primaryHolder.equals(other.primaryHolder)) return false;
		if(this.secondaryHolder == null ? other.secondaryHolder != null : !this.secondaryHolder.equals(other.secondaryHolder)) return false;
		if(this.attribute == null ? other.attribute != null : !this.attribute.equals(other.attribute)) return false;

		return true;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			(attribute == null ? "Attribute: NULL." : attribute.toString())).append(
			" Primary holder: ").append(primaryHolder == null ? "NULL. " : primaryHolder.toString()).append(
			" Secondary holder: ").append(secondaryHolder == null ? "NULL. " : secondaryHolder.toString()).append(
			']').toString();
	}
}
