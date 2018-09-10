package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.Collections;
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
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_group_resource_attribute_def_virt_unixGID extends ResourceGroupVirtualAttributesModuleAbstract implements ResourceGroupVirtualAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Attribute unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);
		Attribute gidAttribute;
		try {
			gidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + unixGIDNamespaceAttribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		gidAttribute.setValue(attribute.getValue());
		try {
			sess.getPerunBl().getAttributesManagerBl().forceCheckAttributeValue(sess, group, gidAttribute);
		} catch(WrongAttributeValueException ex) {
			throw new WrongAttributeValueException(attribute, ex.getMessage(), ex);
		} catch(WrongReferenceAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, ex.getAttribute(), ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute attribute = new Attribute(attributeDefinition);

		Attribute unixGIDNamespaceAttribute;
		try {
			unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
		Attribute gidAttribute;
		try {
			gidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + unixGIDNamespaceAttribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		try {
			sess.getPerunBl().getAttributesManagerBl().forceCheckAttributeValue(sess, group, gidAttribute);
			//check passed, we can use value from this physical attribute
			attribute.setValue(gidAttribute.getValue());
			return attribute;
		} catch(WrongAttributeValueException ex) {
			//Physical attribute have wrong value, let's find a new one
			gidAttribute.setValue(null);
			gidAttribute = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, group, gidAttribute);
			attribute.setValue(gidAttribute.getValue());
			return attribute;
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		Attribute unixGIDNamespaceAttribute;
		try {
			unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}

		try {
			Attribute gidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + unixGIDNamespaceAttribute.getValue());
			attribute = Utils.copyAttributeToVirtualAttributeWithValue(gidAttribute, attribute);
			return attribute;
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);

		try {
			Attribute gidAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + unixGIDNamespaceAttribute.getValue()));
			gidAttribute.setValue(attribute.getValue());
			return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, group, gidAttribute);
		} catch (WrongAttributeValueException ex) {
			throw new InternalErrorException(ex);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttributeValue(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		return false;
		/* This method remove attribute for Group not only GroupResource (we dont want it)
			 Attribute unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);

			 try {
			 AttributeDefinition groupGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + unixGIDNamespaceAttribute.getValue());
			 sess.getPerunBl().getAttributesManagerBl().removeAttribute(sess, group, groupGidAttribute);
			 } catch (AttributeNotExistsException ex) {
			 throw new InternalErrorException(ex);
			 } catch (WrongAttributeAssignmentException ex) {
			 throw new InternalErrorException(ex);
			 }*/
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:*");
		dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> dependecies = new ArrayList<String>();
		dependecies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace" + ":*");
		dependecies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
		return dependecies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT);
		attr.setFriendlyName("unixGID");
		attr.setDisplayName("GID");
		attr.setType(Integer.class.getName());
		attr.setDescription("Unix GID. It is applied only if isUnixGroup is set.");
		return attr;
	}

}
