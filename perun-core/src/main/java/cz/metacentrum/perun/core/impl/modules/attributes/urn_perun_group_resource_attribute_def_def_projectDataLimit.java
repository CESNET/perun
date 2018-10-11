package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;

/**
 * Project directory hard data quota module
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_group_resource_attribute_def_def_projectDataLimit extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {

	private static final String A_GR_projectDataQuota = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":projectDataQuota";
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
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrProjectDataQuota = null;
		String projectDataQuota = null;
		String projectDataLimit = null;

		String projectDataQuotaNumber = null;
		String projectDataQuotaLetter = null;
		String projectDataLimitNumber = null;
		String projectDataLimitLetter = null;

		//Check if attribute value has the right exp pattern (can be null)
		if(attribute.getValue() != null) {
			Matcher testMatcher = testingPattern.matcher((String) attribute.getValue());
			if(!testMatcher.find()) throw new WrongAttributeValueException(attribute, resource, group, "Format of quota must be something like ex.: 1.30M or 2500K, but it is " + attribute.getValue());
		} else return;

		//Get ProjectDataQuota attribute
		try {
			attrProjectDataQuota = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, group, A_GR_projectDataQuota);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute with projectDataQuota from resource " + resource.getId() + " and group + " + group.getId() + " could not obtain.", ex);
		} catch (GroupResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}

		//Get ProjectDataLimit value
		if (attribute.getValue() != null) {
			projectDataLimit = (String) attribute.getValue();
			Matcher numberMatcher = numberPattern.matcher(projectDataLimit);
			Matcher letterMatcher = letterPattern.matcher(projectDataLimit);
			numberMatcher.find();
			letterMatcher.find();
			try {
				projectDataLimitNumber = projectDataLimit.substring(numberMatcher.start(), numberMatcher.end());
			} catch (IllegalStateException ex) {
				projectDataLimitNumber = null;
			}
			try {
				projectDataLimitLetter = projectDataLimit.substring(letterMatcher.start(), letterMatcher.end());
			} catch (IllegalStateException ex) {
				projectDataLimitLetter = "G";
			}
		}
		BigDecimal limitNumber;
		if(projectDataLimitNumber != null) limitNumber = new BigDecimal(projectDataLimitNumber.replace(',', '.'));
		else limitNumber = new BigDecimal("0");
		if (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongAttributeValueException(attribute, attribute + " can't be less than 0.");
		}

		//Get ProjectDataQuota value
		if (attrProjectDataQuota != null && attrProjectDataQuota.getValue() != null) {
			projectDataQuota = (String) attrProjectDataQuota.getValue();
			Matcher numberMatcher = numberPattern.matcher(projectDataQuota);
			Matcher letterMatcher = letterPattern.matcher(projectDataQuota);
			numberMatcher.find();
			letterMatcher.find();
			try {
				projectDataQuotaNumber = projectDataQuota.substring(numberMatcher.start(), numberMatcher.end());
			} catch (IllegalStateException ex) {
				projectDataQuotaNumber = null;
			}
			try {
				projectDataQuotaLetter = projectDataQuota.substring(letterMatcher.start(), letterMatcher.end());
			} catch (IllegalStateException ex) {
				projectDataQuotaLetter = "G";
			}
		}
		BigDecimal quotaNumber;
		if(projectDataQuotaNumber != null) quotaNumber = new BigDecimal(projectDataQuotaNumber.replace(',', '.'));
		else quotaNumber = new BigDecimal("0");

		if (quotaNumber != null && quotaNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongReferenceAttributeValueException(attribute, attrProjectDataQuota, attrProjectDataQuota + " cant be less than 0.");
		}

		//Compare ProjectDataLimit with ProjectDataQuota
		if (quotaNumber == null || quotaNumber.compareTo(BigDecimal.valueOf(0)) == 0) {
			if (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrProjectDataQuota, "Try to set limited limit, but there is still set unlimited Quota.");
			}
		} else if ((quotaNumber != null && quotaNumber.compareTo(BigDecimal.valueOf(0)) != 0) && (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) != 0)) {

			if(projectDataLimitLetter.equals("K")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(K));
			else if(projectDataLimitLetter.equals("M")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(M));
			else if(projectDataLimitLetter.equals("T")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(T));
			else if(projectDataLimitLetter.equals("P")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(P));
			else if(projectDataLimitLetter.equals("E")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(E));
			else limitNumber = limitNumber.multiply(BigDecimal.valueOf(G));

			if(projectDataQuotaLetter.equals("K")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(K));
			else if(projectDataQuotaLetter.equals("M")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(M));
			else if(projectDataQuotaLetter.equals("T")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(T));
			else if(projectDataQuotaLetter.equals("P")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(P));
			else if(projectDataQuotaLetter.equals("E")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(E));
			else quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(G));

			if (limitNumber.compareTo(quotaNumber) < 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrProjectDataQuota, attribute + " must be more than or equals to " + attrProjectDataQuota);
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_GR_projectDataQuota);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("projectDataLimit");
		attr.setDisplayName("Project soft data quota.");
		attr.setType(String.class.getName());
		attr.setDescription("Project hard quota including units (M,G,T, ...), G is default.");
		return attr;
	}
}
