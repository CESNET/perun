package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Templates, for identity changes alerts.
 *
 * @author vojtech.sassmann@gmail.com
 */
public class urn_perun_entityless_attribute_def_def_identityAlertsTemplates extends EntitylessAttributesModuleAbstract {

	public static final String UES_ADDED_PREFERRED_MAIL = "uesAddedPreferredMail";
	public static final String UES_ADDED_PREFERRED_MAIL_SUBJECT = "uesAddedPreferredMailSubject";
	public static final String UES_ADDED_UES_MAIL = "uesAddedUESMail";
	public static final String UES_ADDED_UES_MAIL_SUBJECT = "uesAddedUESMailSubject";

	public static final String UES_REMOVED_PREF_MAIL = "uesRemovedPreferredMail";
	public static final String UES_REMOVED_PREF_MAIL_SUBJECT = "uesRemovedPreferredMailSubject";
	public static final String UES_REMOVED_UES_MAIL = "uesRemovedUESMail";
	public static final String UES_REMOVED_UES_MAIL_SUBJECT = "uesRemovedUESMailSubject";

	public static final String LOGIN_PLACEHOLDER = "{login}";
	public static final String ORG_PLACEHOLDER = "{organization}";
	public static final String TIME_PLACEHOLDER = "{time}";

	public static final String ORG_UNKNOWN_TEXT = "<unknown>";
	public static final DateTimeFormatter ALERT_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss z");

	public static final String KEY_EN = "en";

	public static final String DEFAULT_IDENTITY_ADDED_PREF_MAIL_SUBJECT = "New identity added";
	public static final String DEFAULT_IDENTITY_ADDED_UES_MAIL_SUBJECT = "Account linked";
	public static final String DEFAULT_IDENTITY_ADDED_PREF_MAIL = """
			A new identity has been added to your account. If you don't recognize this activity, please contact our support at perun@cesnet.cz.
			
			Identity organization: {organization} 
			Identity login: {login}
			
			Time of change: {time}
			
			Message is automatically generated.
			----------------------------------------------------------------
			Perun - Identity & Access Management System""";
	public static final String DEFAULT_IDENTITY_ADDED_UES_MAIL = """
			Your account has been linked with an account in the Perun system. If you don't recognize this activity, please contact our support at perun@cesnet.cz.
			
			Identity organization: {organization}
			Identity login: {login}
			
			Time of change: {time}
			
			Message is automatically generated.
			----------------------------------------------------------------
			Perun - Identity & Access Management System""";

	public static final String DEFAULT_IDENTITY_REMOVED_PREF_MAIL_SUBJECT = "Identity removed";
	public static final String DEFAULT_IDENTITY_REMOVED_UES_MAIL_SUBJECT = "Account unlinked";
	public static final String DEFAULT_IDENTITY_REMOVED_PREF_MAIL = """
			An identity has been removed from your account. If you don't recognize this activity, please contact our support at perun@cesnet.cz.
			
			Identity organization: {organization} 
			Identity login: {login}
			
			Time of change: {time}
			
			Message is automatically generated.
			----------------------------------------------------------------
			Perun - Identity & Access Management System""";
	public static final String DEFAULT_IDENTITY_REMOVED_UES_MAIL = """
			Your account has been unlinked from an account in the Perun system. If you don't recognize this activity, please contact our support at perun@cesnet.cz.
			
			Identity organization: {organization}
			Identity login: {login}
			
			Time of change: {time}
			
			Message is automatically generated.
			----------------------------------------------------------------
			Perun - Identity & Access Management System""";

	public static final Set<String> ALLOWED_TEMPLATES = Set.of(
			UES_ADDED_PREFERRED_MAIL,
			UES_ADDED_PREFERRED_MAIL_SUBJECT,
			UES_ADDED_UES_MAIL,
			UES_ADDED_UES_MAIL_SUBJECT,
			UES_REMOVED_PREF_MAIL,
			UES_REMOVED_PREF_MAIL_SUBJECT,
			UES_REMOVED_UES_MAIL,
			UES_REMOVED_UES_MAIL_SUBJECT
			);

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, String key, Attribute attribute) throws WrongAttributeValueException {
		if (!KEY_EN.equals(key)) {
			throw new WrongAttributeValueException("Invalid key. The only allowed key is 'en'.");
		}
		Map<String, String> value = attribute.valueAsMap();
		if (value == null) return;


		for (String templateName : value.keySet()) {
			if (ALLOWED_TEMPLATES.stream()
					.noneMatch(templateName::equals)) {
				throw new WrongAttributeValueException("The given template name '" + templateName + "' is not allowed.");
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setType(LinkedHashMap.class.getName());
		attr.setFriendlyName("identityAlertsTemplates");
		attr.setDisplayName("Identity alerts templates");
		attr.setDescription("Templates for identity alerts. Use 'en' key to set the values. Allowed values are " +
				"'identityAddedPreferredMail', 'identityAddedPreferredMailSubject', 'identityAddedUESMail', 'identityAddedUESMailSubject', " +
				"'identityRemovedPreferredMail', 'identityRemovedPreferredMailSubject', 'identityRemovedUESMail', and 'identityRemovedUESMailSubject'. " +
				"You can use placeholders {organization}, {login}, {time} in the templates that will be replaced with actual values.");
		return attr;
	}
}
