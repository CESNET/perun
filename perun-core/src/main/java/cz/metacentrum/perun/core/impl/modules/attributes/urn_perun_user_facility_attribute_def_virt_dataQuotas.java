package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Virtual attribute to count all data quotas for user on facility.
 *
 * For every resource-member combination of user on this facility prepare merged quotas (unique just one, not unique - bigger is better)
 * Merge all combination for the user on facility together (use addition on them where paths are not unique)
 *
 * @author Michal Stava stavamichal@gmail.com
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_facility_attribute_def_virt_dataQuotas extends UserFacilityVirtualAttributesModuleAbstract {
	public static final String A_MR_V_dataQuotas = AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":dataQuotas";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		//merge attribute settings for every allowed resource
		List<Map<String,Pair<BigDecimal, BigDecimal>>> mergedMemberResourceQuotas = new ArrayList<>();

		List<Resource> allowedResources = sess.getPerunBl().getResourcesManagerBl().getAllowedResources(sess, facility, user);
		for(Resource resource: allowedResources) {
			//get allowed member of this user on this resource (using his VO)
			Vo membersVo;
			try {
				membersVo = sess.getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId());
			} catch (VoNotExistsException ex) {
				throw new ConsistencyErrorException("Vo should exists, because resource with this id exists " + resource);
			}
			Member memberOnResource;
			try{
				memberOnResource = sess.getPerunBl().getMembersManagerBl().getMemberByUser(sess, membersVo, user);
			} catch (MemberNotExistsException ex) {
				throw new ConsistencyErrorException("User should have member in this VO, because he was listed in allowed assigned resources " + user + ", " + membersVo + " , " + resource);
			}

			//Get member-resource final counted quotas for the member on the resource
			Map<String, Pair<BigDecimal, BigDecimal>> memberResourceFinalDataQuotas;
			Attribute memberResourceFinalDataQuotasAttribute;
			try {
				memberResourceFinalDataQuotasAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, memberOnResource, resource, A_MR_V_dataQuotas);
			} catch (MemberResourceMismatchException | WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}

			if(memberResourceFinalDataQuotasAttribute == null || memberResourceFinalDataQuotasAttribute.getValue() == null) memberResourceFinalDataQuotas = new HashMap<>();
			else {
				try {
					memberResourceFinalDataQuotas = sess.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(memberResourceFinalDataQuotasAttribute, resource, memberOnResource, true);
				} catch (WrongAttributeValueException ex) {
					throw new ConsistencyErrorException("Final counted quotas on " + resource + " for member " + memberOnResource + " are in bad format.", ex);
				}
			}

			//Add merged quotas to the big map by resources
			mergedMemberResourceQuotas.add(memberResourceFinalDataQuotas);
		}

		//now we have all resource and member merged quotas, so we need to create 1 transfered map with sum of values
		Map<String, Pair<BigDecimal, BigDecimal>> finalTransferredQuotas = sess.getPerunBl().getModulesUtilsBl().countUserFacilityQuotas(mergedMemberResourceQuotas);

		//transfer map back to attribute value
		attribute.setValue(sess.getPerunBl().getModulesUtilsBl().transferQuotasBackToAttributeValue(finalTransferredQuotas, true));
		//return attribute
		return attribute;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(A_MR_V_dataQuotas);
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("dataQuotas");
		attr.setDisplayName("Computed data quotas for a user on a facility");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Every record is the path (to volume) and the quota in format 'SoftQuota:HardQuota' in (M, G, T, ...), G is default. Example: '10G:20T'. Is counted from all member-resource and resource settings of the user on the facility.");
		return attr;
	}
}
