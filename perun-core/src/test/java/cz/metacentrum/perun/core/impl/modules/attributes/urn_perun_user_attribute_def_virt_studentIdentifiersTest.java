package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberRemovedFromGroupTotally;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Ľuboslav Halama lubo.halama@gmail.com
 */
public class urn_perun_user_attribute_def_virt_studentIdentifiersTest extends AbstractPerunIntegrationTest {

	private static final String studentIdentifiersValuePrefix = "urn:schac:personalUniqueCode:int:esi:";

	private final urn_perun_user_attribute_def_virt_studentIdentifiers classInstance = new urn_perun_user_attribute_def_virt_studentIdentifiers();

	private final String organizationNamespaceAttrFriendlyName = "organizationNamespace";
	private final String organizationScopeAttrFriendlyName = "organizationScope";

	private final String schacPersonalUniqueCodeFriendlyName = "schacPersonalUniqueCode";
	private final String voPersonExternalAffiliationFriendlyName = "affiliation";
	private final String schacHomeOrganizationFriendlyName = "schacHomeOrganization";

	private final String schacPersonalUniqueCodeAttrName = AttributesManager.NS_UES_ATTR_DEF + ":" + schacPersonalUniqueCodeFriendlyName;
	private final String voPersonExternalAffiliationAttrName = AttributesManager.NS_UES_ATTR_DEF + ":" + voPersonExternalAffiliationFriendlyName;
	private final String schacHomeOrganizationAttrName = AttributesManager.NS_UES_ATTR_DEF + ":" + schacHomeOrganizationFriendlyName;

	private User user;
	private Member member;
	private Vo vo;
	private Group group;

	private final String organizationNamespaceValue = "muni-cz-identifier";
	private final String organizationScopeValue = "muni.cz";

	private final String loginNamespaceAttrFriendlyName = AttributesManager.LOGIN_NAMESPACE + ":" + organizationNamespaceValue;
	private final String loginNamespaceAttrValue = "123456";

	// Expected (returned) values
	private final String schacPersonalUniqueCodeValue = studentIdentifiersValuePrefix + organizationScopeValue + ":" + loginNamespaceAttrValue;
	private final String voPersonExternalAffiliationValue = "student@" + organizationScopeValue;
	private final String schacHomeOrganizationValue = organizationScopeValue;

	private DirectMemberAddedToGroup memberAddedEvent;
	private MemberRemovedFromGroupTotally memberRemovedEvent;
	private ExtSource extSource;

	@Before
	public void setUp() throws Exception {
		setUpUser();
		setUpVo();
		setUpMember();
		setUpGroup();
		setUpExtSource();
		createUserExtSourceAttributes();
		memberAddedEvent = new DirectMemberAddedToGroup(member, group);
		memberRemovedEvent = new MemberRemovedFromGroupTotally(member, group);
	}

	@Test
	public void memberAddedEvent() throws PerunException {
		classInstance.resolveVirtualAttributeValueChange((PerunSessionImpl) sess, memberAddedEvent);

		UserExtSource ues = perun.getUsersManagerBl().getUserExtSourceByExtLogin(sess, extSource, loginNamespaceAttrValue);

		String shoValue = perun.getAttributesManagerBl().getAttribute(sess, ues, schacHomeOrganizationAttrName).valueAsString();
		String epsaValue = perun.getAttributesManagerBl().getAttribute(sess, ues, voPersonExternalAffiliationAttrName).valueAsString();
		List<String> spucValue = perun.getAttributesManagerBl().getAttribute(sess, ues, schacPersonalUniqueCodeAttrName).valueAsList();

		assertEquals(shoValue, schacHomeOrganizationValue);
		assertEquals(epsaValue, voPersonExternalAffiliationValue);
		assertTrue(spucValue.contains(schacPersonalUniqueCodeValue));

	}

	@Test (expected = UserExtSourceNotExistsException.class)
	public void memberRemovedEvent() throws PerunException {
		UserExtSource ues = new UserExtSource(extSource, loginNamespaceAttrValue);
		perun.getUsersManagerBl().addUserExtSource(sess, user, ues);
		classInstance.resolveVirtualAttributeValueChange((PerunSessionImpl) sess, memberRemovedEvent);

		perun.getUsersManagerBl().getUserExtSourceByExtLogin(sess, extSource, loginNamespaceAttrValue);
	}

	private void setUpUser() throws Exception {
		User newUser = new User();
		user = perun.getUsersManagerBl().createUser(sess, newUser);
		setUpUserAttribute(loginNamespaceAttrFriendlyName, String.class.getName(), loginNamespaceAttrValue);
	}

	private void setUpVo() throws Exception {
		Vo newVo = new Vo(0, "TestVo", "TestVo");
		vo = perun.getVosManagerBl().createVo(sess, newVo);
	}

	private void setUpMember() throws Exception {
		member = perun.getMembersManagerBl().createMember(sess, vo, user);
	}

	private void setUpGroup() throws Exception {
		Group g = new Group("group", "group description");
		group = perun.getGroupsManagerBl().createGroup(sess, vo, g);
		setUpGroupAttribute(organizationScopeAttrFriendlyName, String.class.getName(), organizationScopeValue);
		setUpGroupAttribute(organizationNamespaceAttrFriendlyName, String.class.getName(), organizationNamespaceValue);
	}

	private void setUpExtSource() throws Exception {
		extSource = new ExtSource(organizationScopeValue, ExtSourcesManager.EXTSOURCE_IDP);
		extSource = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, new HashMap<>());
	}

	private void createUserExtSourceAttributes() throws Exception {
		setUpUserExtSourceAttribute(schacHomeOrganizationFriendlyName, String.class.getName());
		setUpUserExtSourceAttribute(voPersonExternalAffiliationFriendlyName, String.class.getName());
		setUpUserExtSourceAttribute(schacPersonalUniqueCodeFriendlyName, ArrayList.class.getName());
	}

	private Attribute setUpGroupAttribute(String friendlyName, String type, Object value) throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName(friendlyName);
		attrDef.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attrDef.setType(type);
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attr = perun.getAttributesManagerBl().getAttribute(sess, group, attrDef.getName());
		attr.setValue(value);
		perun.getAttributesManagerBl().setAttribute(sess, group, attr);
		return attr;
	}

	private Attribute setUpUserAttribute(String friendlyName, String type, Object value) throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName(friendlyName);
		attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrDef.setType(type);
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attr = perun.getAttributesManagerBl().getAttribute(sess, user, attrDef.getName());
		attr.setValue(value);
		perun.getAttributesManagerBl().setAttribute(sess, user, attr);
		return attr;
	}

	private void setUpUserExtSourceAttribute(String friendlyName, String type) throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName(friendlyName);
		attrDef.setNamespace(AttributesManager.NS_UES_ATTR_DEF);
		attrDef.setType(type);
		perun.getAttributesManagerBl().createAttribute(sess, attrDef);
	}
}
