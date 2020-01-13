package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
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
	private static final String A_F_readyForNewQuotas = AttributesManager.NS_FACILITY_ATTR_DEF + ":readyForNewQuotas";
	private static final Pattern testingPattern = Pattern.compile("^[0-9]+([.][0-9]+)?[KMGTPE]$");

	final long K = 1024;
	final long M = K * 1024;
	final long G = M * 1024;
	final long T = G * 1024;
	final long P = T * 1024;
	final long E = P * 1024;

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException {
		if (attribute.getValue() == null) return;

		Matcher testMatcher = testingPattern.matcher(attribute.valueAsString());
		if (!testMatcher.find())
			throw new WrongAttributeValueException(attribute, member, resource, "Format of quota must be something like ex.: 1.30M or 2500K, but it is " + attribute.getValue());
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrDataLimit;
		String dataLimit = null;

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

		Pair<BigDecimal, String> quotaNumberAndLetter = ModulesUtilsBlImpl.getNumberAndUnitFromString(attribute.valueAsString());
		BigDecimal quotaNumber = quotaNumberAndLetter.getLeft();
		String dataQuotaLetter = quotaNumberAndLetter.getRight();

		if (quotaNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongReferenceAttributeValueException(attribute, null, resource, member, attribute + " cant be less than 0.");
		}

		//Get dataLimit value
		if (attrDataLimit == null || attrDataLimit.getValue() == null) {
			try {
				attrDataLimit = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultDataLimit);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Attribute with defaultDataLimit from resource " + resource.getId() + " could not obtained.", ex);
			}
		}

		if (attrDataLimit != null) dataLimit = attrDataLimit.valueAsString();

		Pair<BigDecimal, String> limitNumberAndLetter = ModulesUtilsBlImpl.getNumberAndUnitFromString(dataLimit);
		BigDecimal limitNumber = limitNumberAndLetter.getLeft();
		String dataLimitLetter = limitNumberAndLetter.getRight();

		if (limitNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongReferenceAttributeValueException(attribute, attrDataLimit, resource, member, resource, null, attrDataLimit + " cant be less than 0.");
		}

		//Compare dataQuota with dataLimit
		if (quotaNumber.compareTo(BigDecimal.valueOf(0)) == 0) {
			if (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDataLimit, resource, member, resource, null, "Try to set unlimited quota, but limit is still " + limitNumber + dataLimitLetter);
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
