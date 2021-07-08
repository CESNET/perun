package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class urn_perun_member_group_attribute_def_virt_o365SendOnBehalf extends MemberGroupVirtualAttributesModuleAbstract implements MemberGroupVirtualAttributesModuleImplApi {

	private static final String A_MG_o365SendOnBehalf = AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":o365SendOnBehalf";
	private static final String A_G_o365SendOnBehalfGroups = AttributesManager.NS_GROUP_ATTR_DEF + ":o365SendOnBehalfGroups";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute) {
		boolean value = sess.getPerunBl().getModulesUtilsBl().getSendRightFromAttributes(sess, member, group, A_MG_o365SendOnBehalf, A_G_o365SendOnBehalfGroups);

		return new Attribute(attribute, value);
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(A_MG_o365SendOnBehalf);
		strongDependencies.add(A_G_o365SendOnBehalfGroups);
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
		attr.setFriendlyName("o365SendOnBehalf");
		attr.setDisplayName("O365 Send on behalf");
		attr.setType(Boolean.class.getName());
		attr.setDescription("Whether member has a right to send on behalf of a group.");
		return attr;
	}
}
