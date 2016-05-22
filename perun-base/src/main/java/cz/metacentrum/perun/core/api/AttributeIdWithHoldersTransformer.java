package cz.metacentrum.perun.core.api;

import org.infinispan.query.Transformer;

/**
 * Transformer for AtributeIdWithHolders class.
 * AttributeIdWithHolders is used as a key for entries in Infinispan database and as such needs transformer from and to String.
 *
 * @author Simona Kruppova
 */
public class AttributeIdWithHoldersTransformer implements Transformer {

	public Object fromString(String s) {
		String[] ids = s.split("/", -1);

		Integer id = null;
		String attributeName = null;
		Holder primaryHolder = null;
		Holder secondaryHolder = null;
		String subject = null;

		if(ids[0].length() > 0) {
			id = Integer.parseInt(ids[0]);
		}
		if(ids[1].length() > 0) {
			attributeName = ids[1];
		}
		if(ids[2].length() > 0) {
			String[] primHolder = ids[2].split(",");
			primaryHolder = new Holder(Integer.parseInt(primHolder[0]), Holder.HolderType.valueOf(primHolder[1]));
		}
		if(ids[3].length() > 0) {
			String[] secHolder = ids[3].split(",");
			secondaryHolder = new Holder(Integer.parseInt(secHolder[0]), Holder.HolderType.valueOf(secHolder[1]));
		}
		if(ids[4].length() > 0) {
			subject = ids[4];
		}

		return new AttributeIdWithHolders(id, attributeName, primaryHolder, secondaryHolder, subject);
	}

	public String toString(Object attributeType) {
		AttributeIdWithHolders attr = (AttributeIdWithHolders) attributeType;
		StringBuilder str = new StringBuilder();

		if(attr.getAttributeId() != null) {
			str.append(attr.getAttributeId());
		}
		str.append("/");

		if(attr.getAttributeName() != null) {
			str.append(attr.getAttributeName());
		}
		str.append("/");

		if(attr.getPrimaryHolder() != null) {
			str.append(attr.getPrimaryHolder().getId()).append(",").append(attr.getPrimaryHolder().getType());
		}
		str.append("/");

		if(attr.getSecondaryHolder() != null) {
			str.append(attr.getSecondaryHolder().getId()).append(",").append(attr.getSecondaryHolder().getType());
		}
		str.append("/");

		if(attr.getSubject() != null) {
			str.append(attr.getSubject());
		}

		return str.toString();
	}
}

