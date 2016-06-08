package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupVirtualAttributesModuleImplApi;

/**
 *
 * @author Michal Stava &lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_group_resource_attribute_def_virt_googleGroupName extends ResourceGroupVirtualAttributesModuleAbstract implements ResourceGroupVirtualAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Attribute googleGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getGoogleGroupNameNamespaceAttributeWithNotNullValue(sess, resource);
		Attribute groupNameAttribute;
		try {
			groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":googleGroupName-namespace:" + googleGroupNameNamespaceAttribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		groupNameAttribute.setValue(attribute.getValue());
		try {
			sess.getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, group, groupNameAttribute);
		} catch(WrongAttributeValueException ex) {
			throw new WrongAttributeValueException(attribute, ex.getMessage(), ex);
		} catch(WrongReferenceAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, ex.getAttribute(), ex);
		}

	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute attribute = new Attribute(attributeDefinition);

		Attribute googleGroupNameNamespaceAttribute;
		try {
			googleGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getGoogleGroupNameNamespaceAttributeWithNotNullValue(sess, resource);
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
		Attribute groupNameAttribute;
		try {
			groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":googleGroupName-namespace:" + googleGroupNameNamespaceAttribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		try {
			sess.getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, group, groupNameAttribute);
			//check passed, we can use value from this physical attribute
			attribute.setValue(groupNameAttribute.getValue());
			return attribute;
		} catch(WrongAttributeValueException ex) {
			//Physical attribute have wrong value, let's find a new one
			groupNameAttribute.setValue(null);
			groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, group, groupNameAttribute);
			attribute.setValue(groupNameAttribute.getValue());
			return attribute;
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		Attribute googleGroupNameNamespaceAttribute;
		try {
			googleGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getGoogleGroupNameNamespaceAttributeWithNotNullValue(sess, resource);
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}

		try {
			Attribute groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":googleGroupName-namespace:" + googleGroupNameNamespaceAttribute.getValue());
			attribute = Utils.copyAttributeToVirtualAttributeWithValue(groupNameAttribute, attribute);
			return attribute;
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute googleGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getGoogleGroupNameNamespaceAttributeWithNotNullValue(sess, resource);

		try {
			Attribute groupNameAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":googleGroupName-namespace:" + googleGroupNameNamespaceAttribute.getValue()));
			groupNameAttribute.setValue(attribute.getValue());
			return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, group, groupNameAttribute);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeValueException ex) {
			throw new InternalErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttributeValue(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		return false;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> dependecies = new ArrayList<String>();
		dependecies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":googleGroupName-namespace" + ":*");
		dependecies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":googleGroupNameNamespace");
		return dependecies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT);
		attr.setFriendlyName("googleGroupName");
		attr.setDisplayName("Google group name");
		attr.setType(String.class.getName());
		attr.setDescription("Name of this group in google groups represented by the resource.");
		return attr;
	}

}
