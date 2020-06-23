package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Attribute represents CN (common name) of user in a CEITECs ActiveDirectory server.
 *
 * It's value must be unique and have form of "lastName firstName [number]"
 * where number is optional and starts with 2 when more than one user
 * has same name.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_cnCeitecAD extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute) throws WrongReferenceAttributeValueException {

		if (attribute.getValue() == null) {
			throw new WrongReferenceAttributeValueException(attribute, null, user, null, "Value can't be null");
		}

		// check existing DN
		Set<User> usersWithSameCN = new HashSet<>(perunSession.getPerunBl().getUsersManagerBl().getUsersByAttribute(perunSession, attribute));
		// check existing DN without accents
		String normalizedValue = java.text.Normalizer.normalize((String)attribute.getValue(), java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		if (!Objects.equals(normalizedValue, attribute.getValue())) {
			Attribute changedAttribute = new Attribute(attribute);
			changedAttribute.setValue(normalizedValue);
			usersWithSameCN.addAll(perunSession.getPerunBl().getUsersManagerBl().getUsersByAttribute(perunSession, changedAttribute));
		}
		usersWithSameCN.remove(user); //remove self
		if (!usersWithSameCN.isEmpty()) {
			if(usersWithSameCN.size() > 1) throw new ConsistencyErrorException("FATAL ERROR: Duplicated CN detected." +  attribute + " " + usersWithSameCN);
			throw new WrongReferenceAttributeValueException(attribute, attribute, user, null, usersWithSameCN.iterator().next(), null, "This CN " + attribute.getValue() + " is already occupied for CEITEC AD");
		}

	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, User user, AttributeDefinition attribute) {

		Attribute filledAttribute = new Attribute(attribute);
		String firstName = user.getFirstName();
		String lastName = user.getLastName();

		if (firstName == null || lastName == null) {
			// unable to fill
			return filledAttribute;
		}

		firstName = java.text.Normalizer.normalize(firstName, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		lastName = java.text.Normalizer.normalize(lastName, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");

		int iterator = 1;
		while (iterator >= 1) {
			if (iterator > 1) {
				filledAttribute.setValue(lastName + " " + firstName + " " + iterator);
			} else {
				filledAttribute.setValue(lastName + " " + firstName);
			}
			try {
				checkAttributeSemantics(session, user, filledAttribute);
				return filledAttribute;
			} catch (WrongReferenceAttributeValueException ex) {
				// continue in a WHILE cycle
				iterator++;
			}
		}

		return filledAttribute;

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("cnCeitecAD");
		attr.setDisplayName("CN in CEITEC AD");
		attr.setType(String.class.getName());
		attr.setDescription("Users CN in CEITEC AD, it must have form of \"lastName firstName [number]\" where number starts with 2 for users with same name.");
		return attr;
	}

}
