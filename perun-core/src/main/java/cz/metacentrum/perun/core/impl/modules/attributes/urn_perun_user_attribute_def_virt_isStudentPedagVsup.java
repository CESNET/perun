package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.List;
import java.util.Objects;

/**
 * Get value as true if user is student and a teacher too.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_virt_isStudentPedagVsup extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {

		Attribute attribute = new Attribute(attributeDefinition);
		PerunBl perun = sess.getPerunBl();

		try {
			Vo vo = perun.getVosManagerBl().getVoByShortName(sess, "vsup");
			Member member = perun.getMembersManagerBl().getMemberByUser(sess, vo, user);
			List<Group> groups = perun.getGroupsManagerBl().getMemberGroups(sess, member);

			boolean isStudent = false;
			boolean isTeacher = false;
			for (Group group : groups) {
				if (Objects.equals(group.getName(), "Studenti")) isStudent = true;
				if (Objects.equals(group.getName(), "Zamestnanci:Pedagog")) isTeacher = true;
			}
			attribute.setValue(isStudent && isTeacher);
			return attribute;

		} catch(Exception ex) {
			return attribute;
		}

	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("isStudentPedagVsup");
		attr.setDisplayName("Student&Teacher");
		attr.setType(Boolean.class.getName());
		attr.setDescription("TRUE if student is also a teacher (user is a member of 'Studenti' group and 'Zamestnanci:Pedagog' group on VŠUP VO.");
		return attr;
	}
}
