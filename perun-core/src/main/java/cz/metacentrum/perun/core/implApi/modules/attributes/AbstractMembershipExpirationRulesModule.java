package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

	public static final String membershipGracePeriodKeyName = "gracePeriod";
	public static final String membershipPeriodKeyName = "period";
	public static final String membershipDoNotExtendLoaKeyName = "doNotExtendLoa";
	public static final String membershipPeriodLoaKeyName = "periodLoa";
	public static final String membershipDoNotAllowLoaKeyName = "doNotAllowLoa";

	public void checkAttributeValue(PerunSessionImpl sess, T entity, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
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
			DateFormat dateFormatter = new SimpleDateFormat("dd.MM.");
			Date date;
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
				DateFormat dateFormatter = new SimpleDateFormat("dd.MM.");
				Date date;
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

	/**
	 * If parameter (key) is allowed in HashMap for membershipExpirationRules
	 *
	 * @param parameter String
	 * @return true if parameter is allowed, false if is not
	 */
	protected abstract boolean isAllowedParameter(String parameter);


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
