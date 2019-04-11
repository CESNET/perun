package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_resource_attribute_def_virt_unixGroupName extends ResourceVirtualAttributesModuleAbstract implements ResourceVirtualAttributesModuleImplApi {

	private static final String A_F_unixGroupName_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace";
	private static final String A_R_unixGroupName_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Attribute unixGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGroupNameNamespaceAttributeWithNotNullValue(sess, resource);
		Attribute groupNameAttribute;
		try {
			groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGroupName_namespace + unixGroupNameNamespaceAttribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		groupNameAttribute.setValue(attribute.getValue());
		try {
			sess.getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, resource, groupNameAttribute);
		} catch(WrongAttributeValueException ex) {
			throw new WrongAttributeValueException(attribute, ex.getMessage(), ex);
		} catch(WrongReferenceAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, ex.getAttribute(), ex);
		}

	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute attribute = new Attribute(attributeDefinition);

		Attribute unixGroupNameNamespaceAttribute;
		try {
			unixGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGroupNameNamespaceAttributeWithNotNullValue(sess, resource);
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
		Attribute groupNameAttribute;
		try {
			groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGroupName_namespace + unixGroupNameNamespaceAttribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		try {
			sess.getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, resource, groupNameAttribute);
			//check passed, we can use value from this physical attribute
			attribute.setValue(groupNameAttribute.getValue());
			return attribute;
		} catch(WrongAttributeValueException ex) {
			//Physical attribute have wrong value, let's find a new one
			groupNameAttribute.setValue(null);
			groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, resource, groupNameAttribute);
			attribute.setValue(groupNameAttribute.getValue());
			return attribute;
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		Attribute unixGroupNameNamespaceAttribute;
		try {
			unixGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGroupNameNamespaceAttributeWithNotNullValue(sess, resource);
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}

		try {
			Attribute groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGroupName_namespace + unixGroupNameNamespaceAttribute.getValue());
			return Utils.copyAttributeToVirtualAttributeWithValue(groupNameAttribute, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute unixGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGroupNameNamespaceAttributeWithNotNullValue(sess, resource);

		try {
			Attribute groupNameAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGroupName_namespace + unixGroupNameNamespaceAttribute.getValue()));
			groupNameAttribute.setValue(attribute.getValue());
			return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, resource, groupNameAttribute);
		} catch(WrongAttributeValueException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public boolean removeAttributeValue(PerunSessionImpl sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Attribute unixGroupNameNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGroupNameNamespaceAttributeWithNotNullValue(sess, resource);
		try {
			AttributeDefinition groupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGroupName_namespace + unixGroupNameNamespaceAttribute.getValue());
			return sess.getPerunBl().getAttributesManagerBl().removeAttribute(sess, resource, groupNameAttribute);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_F_unixGroupName_namespace);
		dependencies.add(A_R_unixGroupName_namespace + "*");
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(A_R_unixGroupName_namespace + "*");
		strongDependencies.add(A_F_unixGroupName_namespace);
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_VIRT);
		attr.setFriendlyName("unixGroupName");
		attr.setDisplayName("Unix group name");
		attr.setType(String.class.getName());
		attr.setDescription("Unix group name");
		return attr;
	}
}
