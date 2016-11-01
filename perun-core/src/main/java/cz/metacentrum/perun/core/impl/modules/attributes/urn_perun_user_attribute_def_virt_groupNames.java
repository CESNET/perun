package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all group names of the user
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_groupNames extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		List<String> groupNames = new ArrayList<>();

		List<Member> members = sess.getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member member : members) {
			Vo vo = sess.getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

			List<Group> groups = sess.getPerunBl().getGroupsManagerBl().getMemberGroups(sess, member);
			for (Group group : groups) {
				groupNames.add(vo.getShortName() +":"+ group.getName());
			}
		}

		attribute.setValue(groupNames);
		return attribute;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("groupNames");
		attr.setDisplayName("Group names");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Full names of groups which the user is a member.");
		return attr;
	}

}
