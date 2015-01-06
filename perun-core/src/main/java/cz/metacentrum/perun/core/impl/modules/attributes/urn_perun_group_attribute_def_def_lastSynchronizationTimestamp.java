package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Last synchronization timestamp
 *
 * If group is synchronized, there will be the last timestamp of group synchronization.
 * Timestamp will be saved even if synchronization failed.
 * Timestamp will be empty only if group has never been synchronized.
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_group_attribute_def_def_lastSynchronizationTimestamp extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		//test of timestamp format
		String attrValue = (String) attribute.getValue();
		try {
			Date date = BeansUtils.getDateFormatter().parse(attrValue);
		} catch (ParseException ex) {
			throw new WrongAttributeValueException(attribute, group, "Format of timestamp is not correct and can't be parsed correctly. Ex. 'dd-MM-yyyy HH:mm:ss'", ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("lastSynchronizationTimestamp");
		attr.setDisplayName("Last synchronization timestamp");
		attr.setType(String.class.getName());
		attr.setDescription("If group is synchronized, there will be the last timestamp of group synchronization.");
		return attr;
	}
}
