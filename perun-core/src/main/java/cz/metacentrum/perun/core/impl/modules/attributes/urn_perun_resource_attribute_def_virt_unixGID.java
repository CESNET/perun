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
public class urn_perun_resource_attribute_def_virt_unixGID extends ResourceVirtualAttributesModuleAbstract implements ResourceVirtualAttributesModuleImplApi {

	private static final String A_F_unixGID_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace";
	private static final String A_R_unixGID_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:";

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Attribute unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);
		Attribute gidAttribute;
		try {
			gidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGID_namespace + unixGIDNamespaceAttribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		gidAttribute.setValue(attribute.getValue());
		try {
			sess.getPerunBl().getAttributesManagerBl().forceCheckAttributeSemantics(sess, resource, gidAttribute);
		} catch(WrongAttributeValueException ex) {
			throw new WrongAttributeValueException(attribute, ex.getMessage(), ex);
		} catch(WrongReferenceAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, ex.getReferenceAttribute(), ex);
		}

	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute attribute = new Attribute(attributeDefinition);

		Attribute unixGIDNamespaceAttribute;
		try {
			unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
		Attribute gidAttribute;
		try {
			gidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGID_namespace + unixGIDNamespaceAttribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		try {
			sess.getPerunBl().getAttributesManagerBl().forceCheckAttributeSemantics(sess, resource, gidAttribute);
			//check passed, we can use value from this physical attribute
			attribute.setValue(gidAttribute.getValue());
			return attribute;
		} catch(WrongAttributeValueException ex) {
			//Physical attribute have wrong value, let's find a new one
			gidAttribute.setValue(null);
			gidAttribute = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, resource, gidAttribute);
			attribute.setValue(gidAttribute.getValue());
			return attribute;
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		Attribute unixGIDNamespaceAttribute;
		try {
			unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}

		try {
			Attribute gidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGID_namespace + unixGIDNamespaceAttribute.getValue());
			return Utils.copyAttributeToVirtualAttributeWithValue(gidAttribute, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);

		try {
			Attribute gidAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGID_namespace + unixGIDNamespaceAttribute.getValue()));
			gidAttribute.setValue(attribute.getValue());
			return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, resource, gidAttribute);
		} catch(WrongAttributeValueException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public boolean removeAttributeValue(PerunSessionImpl sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Attribute unixGIDNamespaceAttribute = sess.getPerunBl().getModulesUtilsBl().getUnixGIDNamespaceAttributeWithNotNullValue(sess, resource);

		try {
			AttributeDefinition groupGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGID_namespace + unixGIDNamespaceAttribute.getValue());
			return sess.getPerunBl().getAttributesManagerBl().removeAttribute(sess, resource, groupGidAttribute);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_R_unixGID_namespace + "*");
		dependencies.add(A_F_unixGID_namespace);
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(A_R_unixGID_namespace + "*");
		strongDependencies.add(A_F_unixGID_namespace);
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_VIRT);
		attr.setFriendlyName("unixGID");
		attr.setDisplayName("Unix GID");
		attr.setType(Integer.class.getName());
		attr.setDescription("Unix GID");
		return attr;
	}
}
