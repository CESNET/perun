package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public abstract class AbstractMembershipExpirationRulesModule<T extends PerunBean> extends AttributesModuleAbstract implements AttributesModuleImplApi {

	private static final Pattern extensionDatePattern = Pattern.compile("^[+][0-9]+([dmy])$");
	private static final Pattern datePattern = Pattern.compile("^[0-9]+([dmy])$");
	private static final Pattern loaPattern = Pattern.compile("^(([0-9]+,)|([0-9]+,[ ]))*[0-9]+$");
	private static final Pattern periodLoaPattern = Pattern.compile("^[0-9]+[|](([0-9]+[.][0-9]+[.])|([+][0-9]+([dmy])))[.]?$");
	private static final Pattern extSourcesPatter = Pattern.compile("^(\\d+)(,\\d+)*$");
	private static final Pattern expireSponsoredMembersPattern = Pattern.compile("^(true)|(false)$");

	public static final String membershipGracePeriodKeyName = "gracePeriod";
	public static final String membershipPeriodKeyName = "period";
	public static final String membershipDoNotExtendLoaKeyName = "doNotExtendLoa";
	public static final String membershipPeriodLoaKeyName = "periodLoa";
	public static final String membershipDoNotAllowLoaKeyName = "doNotAllowLoa";
	public static final String autoExtensionLastLoginPeriod = "autoExtensionLastLoginPeriod";
	public static final String autoExtensionExtSources = "autoExtensionExtSources";
	public static final String expireSponsoredMembers = "expireSponsoredMembers";

	public void checkAttributeSyntax(PerunSessionImpl sess, T entity, Attribute attribute) throws WrongAttributeValueException {
		Map<String, String> attrValue;

		//For no value is correct (it means no rules)
		if(attribute.getValue() == null) return;

		//save value to map attrValue
		attrValue = attribute.valueAsMap();

		//Same for empty HashList
		if(attrValue.isEmpty()) return;

		//If is not empty, so i will check if all keys are correct first
		Set<String> keys;
		keys = attrValue.keySet();

		//Only possibilities: period, doNotExtendLoa, gracePeriod, periodLoa
		for(String k: keys) {
			if(!isAllowedParameter(k)) throw new WrongAttributeValueException(attribute, "There is not allowed parameter value: " + k);
		}

		//If all possibilities are correct, so check their values

		//For period (only date like 1.1. or 29.4. without year) or (+xy where x is number and y is d/m/y - +35m or +80d)
		String parameter = membershipPeriodKeyName;
		if(keys.contains(parameter)) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.uuuu").withResolverStyle(ResolverStyle.STRICT);
			try {
				//Check if given date is valid date (2000 is a leap year)
				LocalDate.parse(attrValue.get(parameter) + "2000", dateTimeFormatter);
			} catch (DateTimeParseException ex) {
				//It's not date in format d.M. (or dd.MM.) so test for next option, for example: "+18m"
				Matcher extensionDateMatcher = extensionDatePattern.matcher(attrValue.get(parameter));
				if(!extensionDateMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
			}
		}

		//For gracePeriod (xy where x is number and y is d/m/y - 35m or 80d)
		parameter = membershipGracePeriodKeyName;
		if(keys.contains(parameter)) {
			Matcher dateMatcher = datePattern.matcher(attrValue.get(parameter));
			if(!dateMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
		}

		//For doNotExtendLoa (exmp: '3,4,5' or '3, 4 ,5' or '325, 324,336')
		parameter = membershipDoNotExtendLoaKeyName;
		if(keys.contains(parameter)) {
			Matcher loaMatcher = loaPattern.matcher(attrValue.get(parameter));
			if(!loaMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
		}

		//For doNotAllowLoa (exmp: '3,4,5' or '3, 4 ,5' or '325, 324,336')
		parameter = membershipDoNotAllowLoaKeyName;
		if(keys.contains(parameter)) {
			Matcher loaMatcher = loaPattern.matcher(attrValue.get(parameter));
			if(!loaMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
		}

		//For periodLoa x|y. or x|y where x is loa format and y is period format and symbol '.' is not mandatory
		parameter = membershipPeriodLoaKeyName;
		if(keys.contains(parameter)) {
			Matcher periodLoaMatcher = periodLoaPattern.matcher(attrValue.get(parameter));
			if(!periodLoaMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
			String value = attrValue.get(parameter);
			for(int i=0;i<value.length();i++) {
				if(value.charAt(i) == '|') value = value.substring(i+1);
			}
			if(value.charAt(0) != '+') {
				if(value.contains("..")) {
					value = value.substring(0, value.length()-1);
				}
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.uuuu").withResolverStyle(ResolverStyle.STRICT);
				try {
					LocalDate.parse(value + "2000", dateTimeFormatter);
				} catch (DateTimeParseException ex) {
					throw new WrongAttributeValueException(attribute, "There is not allowed value (bad date format) for parameter '" + parameter + "': " + attrValue.get(parameter));
				}
			}
		}

		parameter = autoExtensionLastLoginPeriod;
		if (keys.contains(parameter)) {
			Matcher dateMatcher = datePattern.matcher(attrValue.get(autoExtensionLastLoginPeriod));
			if (!dateMatcher.find()) {
				throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" +
						parameter + "': " + attrValue.get(parameter));
			}
		}

		parameter = autoExtensionExtSources;
		if (keys.contains(parameter)) {
			Matcher extSourcesMatcher = extSourcesPatter.matcher(attrValue.get(autoExtensionExtSources));
			if (!extSourcesMatcher.find()) {
				throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" +
						parameter + "': " + attrValue.get(parameter));
			}
		}

		parameter = expireSponsoredMembers;
		if(keys.contains(parameter)) {
			Matcher expireSponsoredMemberMatcher = expireSponsoredMembersPattern.matcher(attrValue.get(parameter));
			if(!expireSponsoredMemberMatcher.find())
				throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" +
					parameter + "': " + attrValue.get(parameter));
		}
	}

	public void checkAttributeSemantics(PerunSessionImpl sess, T entity, Attribute attribute) throws WrongReferenceAttributeValueException {
		Map<String, String> attrValue;

		//For no value is correct (it means no rules)
		if(attribute.getValue() == null) return;

		//save value to map attrValue
		attrValue = attribute.valueAsMap();

		//Same for empty HashList
		if(attrValue.isEmpty()) return;

		if (attrValue.containsKey(autoExtensionExtSources)) {
			String[] extSourceIds = attrValue.get(autoExtensionExtSources).split(",");
			for (String extSourceId : extSourceIds) {
				try {
					sess.getPerunBl().getExtSourcesManagerBl().getExtSourceById(sess, Integer.parseInt(extSourceId));
				} catch (ExtSourceNotExistsException e) {
					throw new WrongReferenceAttributeValueException("There is no extSource with given id: " + extSourceId, e);
				}
			}
		}
	}

	/**
	 * If parameter (key) is allowed in HashMap for membershipExpirationRules
	 *
	 * @param parameter String
	 * @return true if parameter is allowed, false if is not
	 */
	protected abstract boolean isAllowedParameter(String parameter);
}
