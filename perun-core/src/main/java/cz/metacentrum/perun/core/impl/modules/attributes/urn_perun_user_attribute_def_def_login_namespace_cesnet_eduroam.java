package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Class for filling logins in the cesnet-eduroam namespace.
 *
 * @author Jan Zvěřina <zverina@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_cesnet_eduroam extends urn_perun_user_attribute_def_def_login_namespace {

	public static final String CESNET_EDUROAM_IDS_COUNTER = "urn:perun:entityless:attribute-def:def:cesnetEduroamIdsCounter";
	public static final String cesnet_eduroam_key = "cesnet-eduroam";

	/**
	 * Fills login based on counter of IDs for cesnet-eduroam namespace.
	 *
	 * @param perunSession PerunSession
	 * @param user User to fill attribute for
	 * @param attribute Attribute to fill value to
	 * @return Filled attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {

		// Get attribute with login of user in namespace cesnet-eduroam
		Attribute filledAttribute = new Attribute(attribute);

		// Get IDs counter and lock its value
		Attribute idsCounter;
		try {
			idsCounter = perunSession.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(perunSession, cesnet_eduroam_key, CESNET_EDUROAM_IDS_COUNTER);
		} catch (AttributeDefinitionNotExistsException e) {
			throw new ConsistencyErrorException("Attribute doesn't exists.", e);
		}

		Integer counterValue;
		// Get value of counter to integer or set it to 1000, if counter is empty
		if (idsCounter.getValue() == null) {
			counterValue = new Integer(1000);
		} else {
			counterValue = (Integer) idsCounter.getValue();
		}

		// Set login of user in namespace cesnet-eduroam with value of counter
		filledAttribute.setValue(counterValue.toString());

		// Increment value of counter
		counterValue++;

		// Save changes in entityless attribute and unlock it
		try {
			idsCounter.setValue(counterValue);
			perunSession.getPerunBl().getAttributesManagerBl().setAttribute(perunSession, cesnet_eduroam_key, idsCounter);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException | WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
		}

		return filledAttribute;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:cesnet-eduroam");
		attr.setDisplayName("Login in namespace: cesnet-eduroam");
		attr.setType(String.class.getName());
		attr.setDescription("Logname in namespace 'cesnet-eduroam'");
		return attr;
	}
}
