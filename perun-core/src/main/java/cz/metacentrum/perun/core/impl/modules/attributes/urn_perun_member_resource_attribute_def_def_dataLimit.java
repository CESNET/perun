package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
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
public class urn_perun_member_resource_attribute_def_def_dataLimit extends MemberResourceAttributesModuleAbstract implements MemberResourceAttributesModuleImplApi {

	private static final String A_R_defaultDataLimit = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataLimit";
	private static final String A_R_defaultDataQuota = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataQuota";
	private static final String A_MR_dataQuota = AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":dataQuota";
	private static final String A_F_readyForNewQuotas = AttributesManager.NS_FACILITY_ATTR_DEF + ":readyForNewQuotas";
	private static final Pattern numberPattern = Pattern.compile("[0-9]+([.,])?[0-9]*");
	private static final Pattern letterPattern = Pattern.compile("[A-Z]");
	final long K = 1024;
	final long M = K * 1024;
	final long G = M * 1024;
	final long T = G * 1024;
	final long P = T * 1024;
	final long E = P * 1024;

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrDataQuota;
		String dataQuota;
		String dataLimit;

		String dataQuotaNumber = null;
		String dataQuotaLetter = null;
		String dataLimitNumber = null;
		String dataLimitLetter = null;

		//Get attrDataQuota attribute
		try {
			attrDataQuota = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, member, resource, A_MR_dataQuota);
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
		if(dataLimitNumber != null) limitNumber = new BigDecimal(dataLimitNumber.replace(',', '.'));
		else limitNumber = new BigDecimal("0");

		if (limitNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
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

		if (quotaNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongReferenceAttributeValueException(attribute, attrDataQuota, resource, member, resource, null,  attrDataQuota + " cant be less than 0.");
		}

		//Compare dataLimit with dataQuota
		if (quotaNumber.compareTo(BigDecimal.valueOf(0)) == 0) {
			if (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDataQuota, resource, member, resource, null, "Try to set limited limit, but there is still set unlimited Quota.");
			}
		} else if ((quotaNumber.compareTo(BigDecimal.valueOf(0)) != 0) && (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) && dataLimitLetter != null && dataQuotaLetter != null) {
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
				throw new WrongReferenceAttributeValueException(attribute, attrDataQuota, resource, member, resource, null, attribute + " must be more than or equals to " + attrDataQuota);
			}
		}
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		//if this is setting of the new attribute value, check if old quota attributes are supported on the facility
		if(attribute.getValue() != null) {
			try {
				Facility facility = session.getPerunBl().getResourcesManagerBl().getFacility(session, resource);
				Attribute readyForNewQuotasAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, facility, A_F_readyForNewQuotas);
				//You shouldn't be allowed to set old quota attributes if facility is set for new quotas attributes (to prevent wrong setting of quotas)
				if(readyForNewQuotasAttribute.getValue() != null && readyForNewQuotasAttribute.valueAsBoolean()) {
					throw new WrongReferenceAttributeValueException(attribute, readyForNewQuotasAttribute, member, resource, facility, null, "For this facility the new quotas attributes are used! You are trying to set the old ones.");
				}
			} catch (AttributeNotExistsException ex) {
				//if attribute not exists, it is the same like it was set on false, which is ok
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
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
		List<String> dependecies = new ArrayList<>();
		dependecies.add(A_MR_dataQuota);
		dependecies.add(A_R_defaultDataLimit);
		dependecies.add(A_R_defaultDataQuota);
		return dependecies;
	}
}
