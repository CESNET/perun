package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.LinkedHashMap;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Prochazka &lt;michalp@ics.muni.cz&gt;
 */
public class urn_perun_vo_attribute_def_def_membershipExpirationRules extends VoAttributesModuleAbstract implements VoAttributesModuleImplApi {

	Pattern extensionDatePattern = Pattern.compile("^[+][0-9]+(d|m|y)$");
	Pattern datePattern = Pattern.compile("^[0-9]+(d|m|y)$");
	Pattern loaPattern = Pattern.compile("^(([0-9]+,)|([0-9]+,[ ]))*[0-9]+$");
	Pattern periodLoaPattern = Pattern.compile("^[0-9]+[|](([0-9]+[.][0-9]+[.])|([+][0-9]+(d|m|y)))[.]?$");

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Map<String, String> attrValue = new LinkedHashMap<String, String>();

		//For no value is correct (it means no rules)
		if(attribute.getValue() == null) return;

		//save value to map attrValue

		attrValue = (Map) attribute.getValue();
		//Same for empty HashList
		if(attrValue.isEmpty()) return;

		//If is not empty, so i will check if all keys are correct first
		Set<String> keys = new LinkedHashSet<String>();
		keys = attrValue.keySet();

		//Only possibilities: period, doNotExtendLoa, gracePeriod, periodLoa
		for(String k: keys) {
			if(!isAllowedParameter(k)) throw new WrongAttributeValueException(attribute, "There is not allowed parameter value: " + k);
		}

		//If all possibilities are correct, so check their values

		//For period (only date like 1.1. or 29.4. without year) or (+xy where x is number and y is d/m/y - +35m or +80d)
		String parameter = MembersManager.membershipPeriodKeyName;
		if(keys.contains(parameter)) {
			DateFormat dateFormatter = new SimpleDateFormat("dd.MM.");
			Date date = null;
			try {
				date = dateFormatter.parse(attrValue.get(parameter));
				//Test if it is valid date format (need to use standardization: 01.01. = 1.1, 29.03 = 29.3 for both in test)
				if (!standardFormatDate(dateFormatter.format(date)).equals(standardFormatDate(attrValue.get(parameter)))) {
					throw new WrongAttributeValueException(attribute, "There is not allowed value (bad date format) for parameter '" + parameter + "': " + attrValue.get(parameter));
				}
			} catch (ParseException ex) {
				//Its not date in format dd.MM. so test for next option exmp: "+18m"
				Matcher extensionDateMatcher = extensionDatePattern.matcher(attrValue.get(parameter));
				if(!extensionDateMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
			}
		}

		//For gracePeriod (xy where x is number and y is d/m/y - 35m or 80d)
		parameter = MembersManager.membershipGracePeriodKeyName;
		if(keys.contains(parameter)) {
			Matcher dateMatcher = datePattern.matcher(attrValue.get(parameter));
			if(!dateMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
		}

		//For doNotExtendLoa (exmp: '3,4,5' or '3, 4 ,5' or '325, 324,336')
		parameter = MembersManager.membershipDoNotExtendLoaKeyName;
		if(keys.contains(parameter)) {
			Matcher loaMatcher = loaPattern.matcher(attrValue.get(parameter));
			if(!loaMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
		}

		//For doNotAllowLoa (exmp: '3,4,5' or '3, 4 ,5' or '325, 324,336')
		parameter = MembersManager.membershipDoNotAllowLoaKeyName;
		if(keys.contains(parameter)) {
			Matcher loaMatcher = loaPattern.matcher(attrValue.get(parameter));
			if(!loaMatcher.find()) throw new WrongAttributeValueException(attribute, "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
		}

		//For periodLoa x|y. or x|y where x is loa format and y is period format and symbol '.' is not mandatory
		parameter = MembersManager.membershipPeriodLoaKeyName;
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
				DateFormat dateFormatter = new SimpleDateFormat("dd.MM.");
				Date date = null;
				try {
					date = dateFormatter.parse(value);
					//Test if it is valid date format (need to use standardization: 01.01. = 1.1, 29.03 = 29.3 for both in test)
					if (!standardFormatDate(dateFormatter.format(date)).equals(standardFormatDate(value))) {
						throw new WrongAttributeValueException(attribute, "There is not allowed value (bad date format) for parameter '" + parameter + "': " + attrValue.get(parameter));
					}
				} catch (ParseException ex) {
					throw new WrongAttributeValueException(attribute, "There is not allowed value (bad date format) for parameter '" + parameter + "': " + attrValue.get(parameter));
				}
			}
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		attr.setFriendlyName("membershipExpirationRules");
		attr.setDisplayName("Membership expiration rules");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Rules which define how the membership is extended.");
		return attr;
	}

	/**
	 * If parameter (key) is allowed in HashMap for membershipExpirationRules
	 *
	 * @param parameter String
	 * @return true if parameter is allowed, false if is not
	 */
	private boolean isAllowedParameter(String parameter) {
		if(parameter == null) return false;
		if(parameter.equals(MembersManager.membershipPeriodKeyName) || parameter.equals(MembersManager.membershipDoNotExtendLoaKeyName)
				|| parameter.equals(MembersManager.membershipGracePeriodKeyName) || parameter.equals(MembersManager.membershipPeriodLoaKeyName)
				|| parameter.equals(MembersManager.membershipDoNotAllowLoaKeyName)) {
			return true;
				}
		else return false;
	}


	/**
	 * Get String of date and try to create standard format for this module.
	 * Standard format is : 1.1.
	 * Standard format is not : 01.01. or 01.1.
	 * IMPORTANT This is only for equals on string dates in this module!
	 *
	 * @param date format of date (1.1. or 01.1. or 01.01. etc)
	 * @return String standard format of date
	 */
	private String standardFormatDate(String date) {
		int position1 = 0;
		int position2 = 3;
		if(date == null || date.length() == 0) return date;
		if(date.charAt(position1) == '0') {
			date = date.substring(position1+1);
			position2--;
		}
		if(date.length() == (position2+3) && date.charAt(position2) == '0') {
			date = date.substring(0,position2) + date.substring(position2+1);
		}
		return date;
	}
}
