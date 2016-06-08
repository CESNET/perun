package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleAbstract;
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
public class urn_perun_user_facility_attribute_def_virt_dataQuotas extends FacilityUserVirtualAttributesModuleAbstract {
	public static final String A_R_defaultDataQuotas = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataQuotas";
	public static final String A_MR_dataQuotas = AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":dataQuotas";
	
	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		Map<String, String> countedQuotas = new HashMap<>();

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
			
			//get resource quotas
			Map<String, Pair<BigDecimal, BigDecimal>> resourceTransferedQuotas;
			Attribute resourceQuotas;
			try {
				resourceQuotas = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_defaultDataQuotas);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}
			if(resourceQuotas == null || resourceQuotas.getValue() == null) resourceTransferedQuotas = new HashMap<>();
			else {
				try {
					resourceTransferedQuotas = sess.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(resourceQuotas, resource, null, true);
				} catch (WrongAttributeValueException ex) {
					throw new ConsistencyErrorException("Quotas on resource " + resource + " are in bad format.", ex);
				}
			}

			//get members quotas
			Map<String, Pair<BigDecimal, BigDecimal>> memberTransferedQuotas;
			Attribute memberQuotas;
			try {
				memberQuotas = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, memberOnResource, A_MR_dataQuotas);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}
			if(memberQuotas == null || memberQuotas.getValue() == null) memberTransferedQuotas = new HashMap<>();
			else {
				try {
					memberTransferedQuotas = sess.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(memberQuotas, resource, memberOnResource, true);
				} catch (WrongAttributeValueException ex) {
					throw new ConsistencyErrorException("Quotas on resource " + resource + " for member " + memberOnResource + " are in bad format.", ex);
				}
			}

			//merge quotas and add them to the big map by resources
			mergedMemberResourceQuotas.add(sess.getPerunBl().getModulesUtilsBl().mergeMemberAndResourceTransferedQuotas(memberTransferedQuotas, resourceTransferedQuotas));
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
		List<String> strongDependencies = new ArrayList<String>();
		strongDependencies.add(A_R_defaultDataQuotas);
		strongDependencies.add(A_MR_dataQuotas);
		return strongDependencies;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("dataQuotas");
		attr.setDisplayName("Computed data quotas");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Every record is the path (to volume) and the quota in format 'SoftQuota:HardQuota' in (M, G, T, ...), G is default. Example: '10G:20T'. Is counted from all member-resource and resource settings of the user on the facility.");
		return attr;
	}
}
