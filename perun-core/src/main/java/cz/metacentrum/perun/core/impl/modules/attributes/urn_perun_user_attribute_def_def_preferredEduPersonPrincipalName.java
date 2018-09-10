package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_preferredEduPersonPrincipalName extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final String A_U_eduPersonPrincipalNames = AttributesManager.NS_USER_ATTR_VIRT + ":" + "eduPersonPrincipalNames";

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {

		String value = null;
		try {
			Attribute eppns = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_eduPersonPrincipalNames);
			if (eppns.getValue() != null) {
				List<String> values = (List<String>)eppns.getValue();
				if (!values.isEmpty()) {
					// fill first value available
					value = values.get(0);
				}
			}

		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Can't fill attribute value because source attribute " + A_U_eduPersonPrincipalNames +" not exist!", ex);
		}

		Attribute attributeWithValue = new Attribute(attribute);
		attributeWithValue.setValue(value);
		return attributeWithValue;

	}

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		String value = (String)attribute.getValue();
		try {
			Attribute eppns = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, A_U_eduPersonPrincipalNames);
			if (eppns.getValue() != null) {
				List<String> values = (List<String>)eppns.getValue();
				if (!values.contains(value)) {
					// value is not allowed
					throw new WrongReferenceAttributeValueException(attribute, eppns, user, null, user, null, "Value '"+value+"' is not allowed. Please use one of allowed.");
				}
			} else {
				// available EPPNs are null, only allowed value is null.
				if (value != null) throw new WrongReferenceAttributeValueException(attribute, eppns, user, null, user, null, "Value '"+value+"' is not allowed. Please use one of allowed.");
			}
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Can't check attribute value because source attribute " + A_U_eduPersonPrincipalNames + " not exist!", ex);
		}

	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_U_eduPersonPrincipalNames);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("preferredEduPersonPrincipalName");
		attr.setDisplayName("Preferred EPPN");
		attr.setType(String.class.getName());
		attr.setDescription("Preferred EPPN (login from IDP external source).");
		return attr;
	}
}
