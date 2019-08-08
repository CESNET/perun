package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_userCertDNs extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final Pattern certPattern = Pattern.compile("^/");

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "This attribute value can't be null");
		Map<String, String> value = (Map) attribute.getValue();
		if(value.isEmpty()) throw new WrongAttributeValueException(attribute, "This attribute value can't be empty");

		Set<String> valueKeys = value.keySet();
		for(String k: valueKeys) {
			Matcher certKeyMatcher = certPattern.matcher(k);
			if(!certKeyMatcher.find()) throw new WrongAttributeValueException(attribute, "There is wrong value for key " + k + " in hashMap of userCertDNs.");
			String valueOfKey = value.get(k);
			Matcher certValueOfKeyMatcher = certPattern.matcher(valueOfKey);
			if(!certValueOfKeyMatcher.find()) throw new WrongAttributeValueException(attribute, "There is wrong value for key's value " + valueOfKey + " in hashMap of UserCertDns for key " + k);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute userPreferredCertDN;
		try {
			userPreferredCertDN = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":userPreferredCertDN");
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
		String preferredCertDNValue = null;
		if(userPreferredCertDN.getValue() != null) preferredCertDNValue = (String) userPreferredCertDN.getValue();
		Map<String, String> certDNs = null;
		if(attribute.getValue() != null) certDNs = (Map<String, String>) attribute.getValue();

		if(certDNs == null || certDNs.isEmpty()) {
			try {
				session.getPerunBl().getAttributesManagerBl().removeAttribute(session, user, userPreferredCertDN);
			} catch (WrongAttributeAssignmentException | WrongAttributeValueException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			Set<String> certDNsKeys = certDNs.keySet();
			String newPossibleCertDN = null;
			for(String key: certDNsKeys) {
				if(key != null && !key.isEmpty()) {
					newPossibleCertDN = key;
					break;
				}
			}
			if(preferredCertDNValue == null) {
				userPreferredCertDN.setValue(newPossibleCertDN);
				try {
					session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPreferredCertDN);
				} catch (WrongAttributeAssignmentException | WrongAttributeValueException ex) {
					throw new InternalErrorException(ex);
				}
			} else {
				if(!certDNsKeys.contains(preferredCertDNValue)) {
					userPreferredCertDN.setValue(newPossibleCertDN);
					try {
						session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPreferredCertDN);
					} catch (WrongAttributeAssignmentException | WrongAttributeValueException ex) {
						throw new InternalErrorException(ex);
					}
				}
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("userCertDNs");
		attr.setDisplayName("DN of certificates");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Hash map for all users certificates DN.");
		return attr;
	}
}
