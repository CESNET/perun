package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Get all emails from Perun for purpose of TCS.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_tcsMails_mu extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String preferredMailFriendlyName = "preferredMail";
	private static final String isMailFriendlyName = "ISMail";
	private static final String publicMailsFriendlyName = "publicAliasMails";
	private static final String privateMailsFriendlyName = "privateAliasMails";
	private static final String o365MailsFriendlyName = "o365EmailAddresses:mu";

	private static final String A_U_D_preferredMail = AttributesManager.NS_USER_ATTR_DEF + ":" + preferredMailFriendlyName;
	private static final String A_U_D_ISMail = AttributesManager.NS_USER_ATTR_DEF + ":" + isMailFriendlyName;
	private static final String A_U_D_publicAliasMails = AttributesManager.NS_USER_ATTR_DEF + ":" + publicMailsFriendlyName;
	private static final String A_U_D_privateAliasMails = AttributesManager.NS_USER_ATTR_DEF + ":" + privateMailsFriendlyName;
	private static final String A_U_D_o365EmailAddressesMU = AttributesManager.NS_USER_ATTR_DEF + ":" + o365MailsFriendlyName;

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_tcsMails_mu.class);

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {

		Attribute attribute = new Attribute(attributeDefinition);
		SortedSet<String> tcsMailsValue = new TreeSet<>();

		tcsMailsValue.addAll(getEmailValues(sess, user, A_U_D_preferredMail));
		tcsMailsValue.addAll(getEmailValues(sess, user, A_U_D_ISMail));
		tcsMailsValue.addAll(getEmailValues(sess, user, A_U_D_o365EmailAddressesMU));
		tcsMailsValue.addAll(getEmailValues(sess, user, A_U_D_publicAliasMails));
		tcsMailsValue.addAll(getEmailValues(sess, user, A_U_D_privateAliasMails));

		attribute.setValue(new ArrayList<>(tcsMailsValue));

		return attribute;
	}

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) throws AttributeNotExistsException {
		List<AuditEvent> resolvingMessages = new ArrayList<>();

		// handle source user attributes changes
		if (message instanceof AttributeSetForUser && isAffectedAttribute(((AttributeSetForUser) message).getAttribute().getFriendlyName())) {
			AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, this.getAttributeDefinition().getName());
			resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition), ((AttributeSetForUser) message).getUser()));

		} else if (message instanceof AttributeRemovedForUser && isAffectedAttribute(((AttributeRemovedForUser) message).getAttribute().getFriendlyName())) {
			AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, this.getAttributeDefinition().getName());
			resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition), ((AttributeRemovedForUser) message).getUser()));

		} else if (message instanceof AllAttributesRemovedForUser) {
			AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, this.getAttributeDefinition().getName());
			resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition), ((AllAttributesRemovedForUser) message).getUser()));

		}

		return resolvingMessages;
	}

	/**
	 * Return true if attribute name is one of affected attributes in this virtual module.
	 *
	 * @param nameOfAttribute name of attribute to check
	 * @return true if attribute is one of affected, false otherwise
	 */
	private boolean isAffectedAttribute(String nameOfAttribute) {
		if(preferredMailFriendlyName.equals(nameOfAttribute)) return true;
		else if(isMailFriendlyName.equals(nameOfAttribute)) return true;
		else if(o365MailsFriendlyName.equals(nameOfAttribute)) return true;
		else if(publicMailsFriendlyName.equals(nameOfAttribute)) return true;
		else if(privateMailsFriendlyName.equals(nameOfAttribute)) return true;

		return false;
	}

	/**
	 * Return email values as sorted set from attribute by name.
	 *
	 * It works only for String and List attributes. If attribute has different type of value, it will be logged and skipped.
	 * If attribute has empty value, it will return empty set.
	 * If attribute not exists, it will be logged and skipped.
	 *
	 * @param sess perun session
	 * @param user user to get values for
	 * @param nameOfAttribute name of attribute for which values should be returned
	 *
	 * @return sorted set of values of attribute defined by name
	 */
	private SortedSet<String> getEmailValues(PerunSessionImpl sess, User user, String nameOfAttribute) {
		SortedSet<String> valuesToAdd = new TreeSet<>();
		try {
			Attribute sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, nameOfAttribute);
			if (sourceAttribute.getType().equals(String.class.getName())) {
				if (sourceAttribute.getValue() != null) valuesToAdd.add(sourceAttribute.valueAsString());
			} else if (sourceAttribute.getType().equals(ArrayList.class.getName())) {
				if (sourceAttribute.getValue() != null) valuesToAdd.addAll(sourceAttribute.valueAsList());
			} else {
				//unexpected type of value, log it and skip the attribute
				log.error("Unexpected type of attribute (should be String or ArrayList) {}. It will be skipped.", sourceAttribute);
			}
		} catch (AttributeNotExistsException ex) {
			//we can log this situation and skip the attribute from computing
			log.warn("When counting value of attribute {} we are missing source attribute {}. Exception: {}. It will be skipped.", this.getAttributeDefinition(), nameOfAttribute, ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		return valuesToAdd;
	}

	@Override
	public List<String> getStrongDependencies() {
		return Arrays.asList(
			A_U_D_preferredMail,
			A_U_D_ISMail,
			A_U_D_publicAliasMails,
			A_U_D_privateAliasMails,
			A_U_D_o365EmailAddressesMU);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("tcsMails:mu");
		attr.setDisplayName("Computed TCS mails for MU");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("All mails for TCS. Computed from different emails in Perun.");
		return attr;
	}

}