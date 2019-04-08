package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.List;

/**
 * Module for user virtual attribute loa
 *
 * This module return the highest value from user's UserExtSources LoAs. If the attribute value is null throw WrongAttributeValueException.
 *
 * @author Pavel Vyskocil vyskocilpavel@muni.cz
 */
public class urn_perun_user_attribute_def_virt_loa extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "Attribute value is null.");
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {

		List<UserExtSource> extSources = sess.getPerunBl().getUsersManagerBl().getActiveUserExtSources(sess, user);
		Integer maxLoa = 0;
		for(UserExtSource e : extSources) {
			if(maxLoa < e.getLoa()) maxLoa = e.getLoa();
		}
		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(maxLoa.toString());
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("loa");
		attr.setDisplayName("Level of assurance");
		attr.setType(String.class.getName());
		attr.setDescription("The highest value of LoA from all user's userExtSources.");
		return attr;
	}

}
