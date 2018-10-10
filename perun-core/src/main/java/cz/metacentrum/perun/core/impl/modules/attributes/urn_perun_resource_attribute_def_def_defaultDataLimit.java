package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;

/**
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_resource_attribute_def_def_defaultDataLimit extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final String A_R_defaultDataQuota = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataQuota";
	private static final Pattern numberPattern = Pattern.compile("[0-9]+[.]?[0-9]*");
	private static final Pattern letterPattern = Pattern.compile("[A-Z]");
	private static final Pattern testingPattern = Pattern.compile("^[0-9]+([.][0-9]+)?[KMGTPE]$");

	//Definition of K = KB, M = MB etc.
	long K = 1024;
	long M = K * 1024;
	long G = M * 1024;
	long T = G * 1024;
	long P = T * 1024;
	long E = P * 1024;

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrDefaultDataQuota = null;
		String defaultDataQuota = null;
		String defaultDataLimit = null;

		String defaultDataQuotaNumber = null;
		String defaultDataQuotaLetter = null;
		String defaultDataLimitNumber = null;
		String defaultDataLimitLetter = null;

		//Check if attribute value has the right exp pattern (can be null)
		if(attribute.getValue() != null) {
			Matcher testMatcher = testingPattern.matcher((String) attribute.getValue());
			if(!testMatcher.find()) throw new WrongAttributeValueException(attribute, resource, "Format of quota must be something like ex.: 1.30M or 2500K, but it is " + attribute.getValue());
		} else return;

		//Get DefaultDataQuota attribute
		try {
			attrDefaultDataQuota = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultDataQuota);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute with defaultDataQuota from resource " + resource.getId() + " could not obtained.", ex);
		}

		//Get DefaultDataLimit value
		if (attribute.getValue() != null) {
			defaultDataLimit = (String) attribute.getValue();
			Matcher numberMatcher = numberPattern.matcher(defaultDataLimit);
			Matcher letterMatcher = letterPattern.matcher(defaultDataLimit);
			numberMatcher.find();
			letterMatcher.find();
			try {
				defaultDataLimitNumber = defaultDataLimit.substring(numberMatcher.start(), numberMatcher.end());
			} catch (IllegalStateException ex) {
				defaultDataLimitNumber = null;
			}
			try {
				defaultDataLimitLetter = defaultDataLimit.substring(letterMatcher.start(), letterMatcher.end());
			} catch (IllegalStateException ex) {
				defaultDataLimitLetter = "G";
			}
		}
		BigDecimal limitNumber;
		if(defaultDataLimitNumber != null) limitNumber = new BigDecimal(defaultDataLimitNumber.replace(',', '.'));
		else limitNumber = new BigDecimal("0");
		if (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongAttributeValueException(attribute, resource, attribute + " can't be less than 0.");
		}

		//Get DefaultDataQuota value
		if (attrDefaultDataQuota != null && attrDefaultDataQuota.getValue() != null) {
			defaultDataQuota = (String) attrDefaultDataQuota.getValue();
			Matcher numberMatcher = numberPattern.matcher(defaultDataQuota);
			Matcher letterMatcher = letterPattern.matcher(defaultDataQuota);
			numberMatcher.find();
			letterMatcher.find();
			try {
				defaultDataQuotaNumber = defaultDataQuota.substring(numberMatcher.start(), numberMatcher.end());
			} catch (IllegalStateException ex) {
				defaultDataQuotaNumber = null;
			}
			try {
				defaultDataQuotaLetter = defaultDataQuota.substring(letterMatcher.start(), letterMatcher.end());
			} catch (IllegalStateException ex) {
				defaultDataQuotaLetter = "G";
			}
		}
		BigDecimal quotaNumber;
		if(defaultDataQuotaNumber != null) quotaNumber = new BigDecimal(defaultDataQuotaNumber.replace(',', '.'));
		else quotaNumber = new BigDecimal("0");

		if (quotaNumber != null && quotaNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongReferenceAttributeValueException(attribute, attrDefaultDataQuota, resource, null, resource, null, attrDefaultDataQuota + " cant be less than 0.");
		}

		//Compare DefaultDataLimit with DefaultDataQuota
		if (quotaNumber == null || quotaNumber.compareTo(BigDecimal.valueOf(0)) == 0) {
			if (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDefaultDataQuota, resource, null, resource, null, "Try to set limited limit, but there is still set unlimited Quota.");
			}
		} else if ((quotaNumber != null && quotaNumber.compareTo(BigDecimal.valueOf(0)) != 0) && (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) != 0)) {

			if(defaultDataLimitLetter.equals("K")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(K));
			else if(defaultDataLimitLetter.equals("M")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(M));
			else if(defaultDataLimitLetter.equals("T")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(T));
			else if(defaultDataLimitLetter.equals("P")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(P));
			else if(defaultDataLimitLetter.equals("E")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(E));
			else limitNumber = limitNumber.multiply(BigDecimal.valueOf(G));

			if(defaultDataQuotaLetter.equals("K")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(K));
			else if(defaultDataQuotaLetter.equals("M")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(M));
			else if(defaultDataQuotaLetter.equals("T")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(T));
			else if(defaultDataQuotaLetter.equals("P")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(P));
			else if(defaultDataQuotaLetter.equals("E")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(E));
			else quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(G));

			if (limitNumber.compareTo(quotaNumber) < 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDefaultDataQuota, resource, null, resource, null, attribute + " must be more than or equals to " + attrDefaultDataQuota);
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_R_defaultDataQuota);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("defaultDataLimit");
		attr.setDisplayName("Default data limit");
		attr.setType(String.class.getName());
		attr.setDescription("Hard quota including units (M,G,T, ...), G is default.");
		return attr;
	}
}
