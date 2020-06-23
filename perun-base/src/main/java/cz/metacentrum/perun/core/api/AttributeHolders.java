package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.hibernate.search.annotations.*;

import java.io.Serializable;

/**
 * Class used as a value for Infinispan database entries. It contains Attribute and its Holders (member, group..).
 *
 * @author Simona Kruppova
 */
@Indexed
public class AttributeHolders extends Attribute implements Serializable {

	@IndexedEmbedded(indexNullAs = Field.DEFAULT_NULL_TOKEN)
	private Holder primaryHolder;

	@IndexedEmbedded(indexNullAs = Field.DEFAULT_NULL_TOKEN)
	private Holder secondaryHolder;

	/**
	 * Subject of the attribute, used only with entityless attributes
	 */
	@Field(analyze = Analyze.NO, indexNullAs = Field.DEFAULT_NULL_TOKEN)
	private String subject;

	/**
	 * Attribute id.
	 * Needed for cache search.
	 */
	@Field
	@NumericField
	private int idForSearch;

	/**
	 * Whole attribute name including namespace (attribute namespace + friendly name)
	 * Needed for cache search.
	 */
	@Field(analyze = Analyze.NO)
	private String nameForSearch;

	/**
	 * Attribute namespace, including the whole namespace.
	 * Needed for cache search.
	 */
	@Field(analyze = Analyze.NO)
	private String namespaceForSearch;

	/**
	 * Attribute name, <strong>excluding</strong> the whole namespace.
	 * Needed for cache search.
	 */
	@Field(analyze = Analyze.NO)
	private String friendlyNameForSearch;

	/**
	 * Type of attribute's value. It's a name of java class. "Java.lang.String" for expample. (To get this use something like <em>String.class.getName()</em>)
	 * Needed for cache search.
	 */
	@Field(analyze = Analyze.NO)
	private String typeForSearch;

	/**
	 * Value of the attribute, can be Map, List, String, Integer, Boolean...
	 * Needed for cache search. It is used only for checking whether it is null or not. Converted to string in constructor.
	 */
	@Field(analyze = Analyze.NO, indexNullAs = Field.DEFAULT_NULL_TOKEN)
	private String valueForSearch;

	/**
	 * Specifies if this AttributeHolders entity was saved by id or by name in cache.
	 * Needed for cache search.
	 */
	@Field(analyze = Analyze.NO)
	private SavedBy savedBy;

	public enum SavedBy {
		ID, NAME
	}

	public AttributeHolders(Attribute attribute, Holder primaryHolder, Holder secondaryHolder, SavedBy savedBy) {
		super(attribute, true);

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

		this.nameForSearch = attribute.getNamespace() + ":" + attribute.getFriendlyName();
		this.namespaceForSearch = attribute.getNamespace();
		this.friendlyNameForSearch = attribute.getFriendlyName();
		this.typeForSearch = attribute.getType();
		this.idForSearch = attribute.getId();
		this.valueForSearch = BeansUtils.attributeValueToString(attribute);
		this.savedBy = savedBy;
	}

	public AttributeHolders(AttributeDefinition attribute, SavedBy savedBy) {
		super(attribute);
		this.nameForSearch = attribute.getNamespace() + ":" + attribute.getFriendlyName();
		this.namespaceForSearch = attribute.getNamespace();
		this.friendlyNameForSearch = attribute.getFriendlyName();
		this.typeForSearch = attribute.getType();
		this.idForSearch = attribute.getId();
		this.valueForSearch = null;
		this.savedBy = savedBy;
	}

	public AttributeHolders(Attribute attribute, String subject, SavedBy savedBy) {
		this(attribute, null, null, savedBy);
		this.subject = subject;

	}

	public Holder getPrimaryHolder() {
		return primaryHolder;
	}

	public void setPrimaryHolder(Holder primaryHolder) {
		this.primaryHolder = primaryHolder;
	}

	public Holder getSecondaryHolder() {
		return secondaryHolder;
	}

	public void setSecondaryHolder(Holder secondaryHolder) {
		this.secondaryHolder = secondaryHolder;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String toString() {
		return "AttributeHolders{" +
				"attribute=" + super.toString() +
				", primaryHolder=" + primaryHolder +
				", secondaryHolder=" + secondaryHolder +
				'}';
	}
}
