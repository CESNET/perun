package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleImplApi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_member_resource_attribute_def_def_dataQuota extends MemberResourceAttributesModuleAbstract implements MemberResourceAttributesModuleImplApi {

	private static final String A_R_defaultDataLimit = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataLimit";
	private static final String A_R_defaultDataQuota = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataQuota";
	private static final String A_MR_dataLimit = AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":dataLimit";
	private static final Pattern numberPattern = Pattern.compile("[0-9]+([.,])?[0-9]*");
	private static final Pattern letterPattern = Pattern.compile("[A-Z]");
	final long K = 1024;
	final long M = K * 1024;
	final long G = M * 1024;
	final long T = G * 1024;
	final long P = T * 1024;
	final long E = P * 1024;

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrDataLimit;
		String dataQuota;
		String dataLimit;

		String dataQuotaNumber = null;
		String dataQuotaLetter = null;
		String dataLimitNumber = null;
		String dataLimitLetter = null;

		//Get attrDataLimit attribute
		try {
			attrDataLimit = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, member, resource, A_MR_dataLimit);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute with dataLimit from member " + member.getId() + " and resource " + resource.getId() + " could not obtained.", ex);
		} catch (MemberResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}

		//Get dataQuota value
		if (attribute.getValue() == null) {
			try {
				attribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultDataQuota);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute with defaultDataQuota from resource " + resource.getId() + " could not obtained.", ex);
			}
		}
		if (attribute.getValue() != null) {
			dataQuota = (String) attribute.getValue();
			Matcher numberMatcher = numberPattern.matcher(dataQuota);
			Matcher letterMatcher = letterPattern.matcher(dataQuota);
			numberMatcher.find();
			letterMatcher.find();
			try {
				dataQuotaNumber = dataQuota.substring(numberMatcher.start(), numberMatcher.end());
			} catch (IllegalStateException ex) {
				dataQuotaNumber = null;
			}
			try {
				dataQuotaLetter = dataQuota.substring(letterMatcher.start(), letterMatcher.end());
			} catch (IllegalStateException ex) {
				dataQuotaLetter = "G";
			}
		}
		BigDecimal quotaNumber;
		if(dataQuotaNumber != null) quotaNumber = new BigDecimal(dataQuotaNumber.replace(',', '.'));
		else quotaNumber = new BigDecimal("0");

		if (quotaNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongAttributeValueException(attribute, resource, member, attribute + " cant be less than 0.");
		}

		//Get dataLimit value
		if (attrDataLimit == null || attrDataLimit.getValue() == null) {
			try {
				attrDataLimit = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultDataLimit);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute with defaultDataLimit from resource " + resource.getId() + " could not obtained.", ex);
			}
		}
		if (attrDataLimit != null && attrDataLimit.getValue() != null) {
			dataLimit = (String) attrDataLimit.getValue();
			Matcher numberMatcher = numberPattern.matcher(dataLimit);
			Matcher letterMatcher = letterPattern.matcher(dataLimit);
			numberMatcher.find();
			letterMatcher.find();
			try {
				dataLimitNumber = dataLimit.substring(numberMatcher.start(), numberMatcher.end());
			} catch (IllegalStateException ex) {
				dataLimitNumber = null;
			}
			try {
				dataLimitLetter = dataLimit.substring(letterMatcher.start(), letterMatcher.end());
			} catch (IllegalStateException ex) {
				dataLimitLetter = "G";
			}
		}
		BigDecimal limitNumber;
		if(dataLimitNumber != null) limitNumber = new BigDecimal(dataLimitNumber.replace(',', '.'));
		else limitNumber = new BigDecimal("0");

		if (limitNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongReferenceAttributeValueException(attribute, attrDataLimit, resource, member, resource, null, attrDataLimit + " cant be less than 0.");
		}

		//Compare dataQuota with dataLimit
		if (quotaNumber.compareTo(BigDecimal.valueOf(0)) == 0) {
			if (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDataLimit, resource, member, resource, null, "Try to set unlimited quota, but limit is still " + dataLimitNumber + dataLimitLetter);
			}
		} else if (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0 && dataLimitLetter != null && dataQuotaLetter != null) {

			switch (dataLimitLetter) {
				case "K":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(K));
					break;
				case "M":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(M));
					break;
				case "T":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(T));
					break;
				case "P":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(P));
					break;
				case "E":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(E));
					break;
				default:
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(G));
					break;
			}

			switch (dataQuotaLetter) {
				case "K":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(K));
					break;
				case "M":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(M));
					break;
				case "T":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(T));
					break;
				case "P":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(P));
					break;
				case "E":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(E));
					break;
				default:
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(G));
					break;
			}

			if (limitNumber.compareTo(quotaNumber) < 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDataLimit, resource, member, resource, null, attribute + " must be less than or equals to " + attrDataLimit);
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependecies = new ArrayList<>();
		dependecies.add(A_MR_dataLimit);
		dependecies.add(A_R_defaultDataLimit);
		dependecies.add(A_R_defaultDataQuota);
		return dependecies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("dataQuota");
		attr.setDisplayName("Data quota");
		attr.setType(String.class.getName());
		attr.setDescription("Soft quota including units (M, G, T, ...), G is default.");
		return attr;
	}
}
