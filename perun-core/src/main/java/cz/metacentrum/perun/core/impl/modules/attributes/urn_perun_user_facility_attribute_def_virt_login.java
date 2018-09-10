package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks and fills at specified facility users login.
 *
 * @date 22.4.2011 10:43:48
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 */
public class urn_perun_user_facility_attribute_def_virt_login extends FacilityUserVirtualAttributesModuleAbstract implements FacilityUserVirtualAttributesModuleImplApi {

	/**
	 * Calls checkAttribute on u:login-namespace:[login-namespace]
	 */
	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Facility facility, User user, Attribute attribute) throws WrongAttributeValueException, WrongReferenceAttributeValueException, InternalErrorException, WrongAttributeAssignmentException {
		try  {
			Attribute loginNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace");
			Attribute loginAttribute = null;
			if (loginNamespaceAttribute.getValue() != null) {
				loginAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess,
						user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + (String) loginNamespaceAttribute.getValue());
				if(attribute.getValue() == null) throw new WrongAttributeValueException(loginAttribute, user, facility, "Login can't be null");
				loginAttribute.setValue(attribute.getValue());
				sess.getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, user, loginAttribute);
			} else {
				throw new WrongAttributeValueException(loginNamespaceAttribute, user, facility, "Login-namespace for facility can not be empty.");
			}
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}
	}

	/**
	 * Calls fillAttribute on u:login-namespace:[login-namespace]
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute virtLoginAttribute = new Attribute(attributeDefinition);

		try {
			Attribute loginNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace");

			if (loginNamespaceAttribute.getValue() != null) {
				// Get the u:login-namespace[loginNamespaceAttribute]
				Attribute loginAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + (String) loginNamespaceAttribute.getValue());
				loginAttribute.setValue(loginAttribute.getValue());
				loginAttribute = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, loginAttribute);

				virtLoginAttribute.setValue(loginAttribute.getValue());
			} else {
				virtLoginAttribute.setValue(null);
			}
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}
		return virtLoginAttribute;
	}

	/**
	 * Gets the value of the attribute f:login-namespace and then finds the value of the attribute u:login-namespace:[login-namespace]
	 */
	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attr = new Attribute(attributeDefinition);

		Attribute loginAttribute = null;

		try {
			// Get the f:login-namespace attribute
			Attribute loginNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace");

			if (loginNamespaceAttribute.getValue() != null) {
				// Get the u:login-namespace[loginNamespaceAttribute]
				loginAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + (String) loginNamespaceAttribute.getValue());
				attr = Utils.copyAttributeToVirtualAttributeWithValue(loginAttribute, attr);
			} else {
				attr.setValue(null);
			}
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new ConsistencyErrorException(e);
		}

		return attr;
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		AttributeDefinition userLoginAttributeDefinition;
		try {
			// Get the f:login-namespace attribute
			Attribute loginNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace");

			if (loginNamespaceAttribute.getValue() == null) {
				throw new WrongReferenceAttributeValueException(attribute, loginNamespaceAttribute, user, facility,  "Facility need to have nonempty login-namespace attribute.");
			}

			userLoginAttributeDefinition = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + (String) loginNamespaceAttribute.getValue());
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new ConsistencyErrorException(e);
		}

		Attribute userLoginAttribute = new Attribute(userLoginAttributeDefinition);
		userLoginAttribute.setValue(attribute.getValue());

		try {
			return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, user, userLoginAttribute);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new ConsistencyErrorException(e);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace");
		dependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:*");
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> StrongDependencies = new ArrayList<String>();
		StrongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace");
		StrongDependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":login-namespace" + ":*");
		return StrongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("login");
		attr.setDisplayName("Login");
		attr.setType(String.class.getName());
		attr.setDescription("Login if is set.");
		return attr;
	}
}
