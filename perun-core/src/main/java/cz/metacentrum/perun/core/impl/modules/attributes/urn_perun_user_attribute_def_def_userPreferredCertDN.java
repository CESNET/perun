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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_userPreferredCertDN extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Attribute userCertDNs = null;
		try {
			userCertDNs = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":userCertDNs");
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		Map<String, String> certDNsValue = null;
		if(userCertDNs.getValue() != null) {
			certDNsValue = (Map<String, String>) userCertDNs.getValue();
		} else {
			if(attribute.getValue() != null) throw new WrongReferenceAttributeValueException(attribute, userCertDNs, "There is no certificates for this user so preferred certificate can't be choose.");
			else return;
		}
		if(attribute.getValue() == null) {
			if(certDNsValue != null || !certDNsValue.isEmpty()) throw new WrongAttributeValueException(attribute, user, "This attribute value can't be null because of notNull attribute userCertDNs");
		} else {
			String preferredCertDNValue = (String) attribute.getValue();
			if(!certDNsValue.containsKey(preferredCertDNValue)) throw new WrongAttributeValueException(attribute, "This attribute value must be one of exsiting keys in userCertDNs.");
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute userCertDNs = null;
		try {
			userCertDNs = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":userCertDNs");
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		Map<String, String> certDNsValue = null;
		if(userCertDNs.getValue() != null) {
			certDNsValue = (Map<String, String>) userCertDNs.getValue();
			Set<String> keys = certDNsValue.keySet();
			for(String key: keys) {
				if(key != null && !key.isEmpty()) {
					Attribute attr = new Attribute(attribute);
					attr.setValue(key);
					return attr;
				}
			}
		}
		return new Attribute(attribute);
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":userCertDNs");
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("userPreferredCertDN");
		attr.setDisplayName("DN of preferred certificate");
		attr.setType(String.class.getName());
		attr.setDescription("One preferredUser Certificate DN without certificate Authority.");
		return attr;
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		if(attribute.getValue() == null) {
			Attribute userCertDNs = null;
			try {
				userCertDNs = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":userCertDNs");
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}
			Map<String, String> certDNsValue = null;
			if(userCertDNs.getValue() != null) {
				certDNsValue = (Map<String, String>) userCertDNs.getValue();
			}

			if(certDNsValue != null && !certDNsValue.isEmpty()) {
				throw new WrongReferenceAttributeValueException(attribute, "Can't remove preferredCert if there is any existing certDNs for the user.");
			}
		}
	}
}
