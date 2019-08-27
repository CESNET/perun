package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map of vos where user is member to member attribute of organization.
 *
 * @author Metodej Klang
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_userOrganizations extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String A_M_organization = AttributesManager.NS_MEMBER_ATTR_DEF + ":organization";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Map<String, String> userOrganizations = new LinkedHashMap<>();
		List<Vo> vos = perunSession.getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(perunSession, user);

		for (Vo vo : vos) {
			try {
				Member member = perunSession.getPerunBl().getMembersManagerBl().getMemberByUser(perunSession, vo, user);
				String value = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, member, A_M_organization).valueAsString();
				if (value != null) userOrganizations.put(vo.getShortName(), value);
			} catch (MemberNotExistsException e) {
				throw new ConsistencyErrorException(e);
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
				throw new InternalErrorException(e);
			}
		}

		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(userOrganizations);
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("userOrganizations");
		attr.setDisplayName("User's Organizations");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Map of vos where user is member to member attribute of organization.");
		return attr;
	}
}
