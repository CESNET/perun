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
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberAttributesModuleImplApi;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_member_resource_attribute_def_def_dataLimit extends ResourceMemberAttributesModuleAbstract implements ResourceMemberAttributesModuleImplApi {

	private static final String A_R_defaultDataLimit = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataLimit";
	private static final String A_R_defaultDataQuota = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataQuota";
	private static final String A_MR_dataQuota = AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":dataQuota";
	Pattern numberPattern = Pattern.compile("[0-9]+(\\.|,)?[0-9]*");
	Pattern letterPattern = Pattern.compile("[A-Z]");
	long K = 1024;
	long M = K * 1024;
	long G = M * 1024;
	long T = G * 1024;
	long P = T * 1024;
	long E = P * 1024;

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrDataQuota = null;
		String dataQuota = null;
		String dataLimit = null;

		String dataQuotaNumber = null;
		String dataQuotaLetter = null;
		String dataLimitNumber = null;
		String dataLimitLetter = null;

		//Get attrDataQuota attribute
		try {
			attrDataQuota = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, member, A_MR_dataQuota);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute with dataQuota from member " + member.getId() + " and resource " + resource.getId() + " could not obtained.", ex);
		} catch (MemberResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}

		//Get dataLimit value
		if (attribute.getValue() == null) {
			try {
				attribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultDataLimit);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute with defaultDataLimit from resource " + resource.getId() + " could not obtained.", ex);
			}
		}
		if (attribute.getValue() != null) {
			dataLimit = (String) attribute.getValue();
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
		if(dataLimitNumber != null) limitNumber = new BigDecimal(dataLimitNumber.replace(',', '.').toString());
		else limitNumber = new BigDecimal("0");

		if (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongAttributeValueException(attribute, resource, member, attribute + " cant be less than 0.");
		}

		//Get dataQuota value
		if (attrDataQuota == null || attrDataQuota.getValue() == null) {
			try {
				attrDataQuota = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultDataQuota);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute with defaultDataQuota from resource " + resource.getId() + " could not obtained.", ex);
			}
		}
		if (attrDataQuota != null && attrDataQuota.getValue() != null) {
			dataQuota = (String) attrDataQuota.getValue();
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

		if (quotaNumber != null && quotaNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongReferenceAttributeValueException(attribute, attrDataQuota, resource, member, resource, null,  attrDataQuota + " cant be less than 0.");
		}

		//Compare dataLimit with dataQuota
		if (quotaNumber == null || quotaNumber.compareTo(BigDecimal.valueOf(0)) == 0) {
			if (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDataQuota, resource, member, resource, null, "Try to set limited limit, but there is still set unlimited Quota.");
			}
		} else if ((quotaNumber != null && quotaNumber.compareTo(BigDecimal.valueOf(0)) != 0) && (limitNumber != null && limitNumber.compareTo(BigDecimal.valueOf(0)) != 0)) {
			if(dataLimitLetter.equals("K")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(K));
			else if(dataLimitLetter.equals("M")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(M));
			else if(dataLimitLetter.equals("T")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(T));
			else if(dataLimitLetter.equals("P")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(P));
			else if(dataLimitLetter.equals("E")) limitNumber = limitNumber.multiply(BigDecimal.valueOf(E));
			else limitNumber = limitNumber.multiply(BigDecimal.valueOf(G));

			if(dataQuotaLetter.equals("K")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(K));
			else if(dataQuotaLetter.equals("M")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(M));
			else if(dataQuotaLetter.equals("T")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(T));
			else if(dataQuotaLetter.equals("P")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(P));
			else if(dataQuotaLetter.equals("E")) quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(E));
			else quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(G));

			if (limitNumber.compareTo(quotaNumber) < 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDataQuota, resource, member, resource, null, attribute + " must be more than or equals to " + attrDataQuota);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("dataLimit");
		attr.setDisplayName("Data limit");
		attr.setType(String.class.getName());
		attr.setDescription("Hard quota including units (M, G, T, ...), G is default.");
		return attr;
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependecies = new ArrayList<String>();
		dependecies.add(A_MR_dataQuota);
		dependecies.add(A_R_defaultDataLimit);
		dependecies.add(A_R_defaultDataQuota);
		return dependecies;
	}
}
