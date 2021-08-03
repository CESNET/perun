package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Ľuboslav Halama lubo.halama@gmail.com
 */
public class urn_perun_user_attribute_def_virt_studentIdentifiersTest {
	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_studentIdentifiersTest.class);

	private static final String studentIdentifiersValuePrefix = "urn:schac:personalUniqueCode:int:esi:";

	private final urn_perun_user_attribute_def_virt_studentIdentifiers classInstance = new urn_perun_user_attribute_def_virt_studentIdentifiers();
	private final AttributeDefinition studentIdentifiersAttrDef = classInstance.getAttributeDefinition();

	private final String organizationNamespaceAttrFriendlyName = "organizationNamespace";
	private final String organizationScopeAttrFriendlyName = "organizationScope";

	private final String organizationNamespaceAttrName = AttributesManager.NS_GROUP_ATTR_DEF + ":" + organizationNamespaceAttrFriendlyName;
	private final String organizationScopeAttrName = AttributesManager.NS_GROUP_ATTR_DEF + ":" + organizationScopeAttrFriendlyName;

	private final String loginNamespaceAttrPrefix = AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":";

	private final User user = new User(1, "Joe", "Doe", "W.", "", "");
	private final Member member1 = new Member(1, 1, 1, Status.VALID);
	private final Member member2 = new Member(2, 1, 2, Status.VALID);
	private final Group group1 = new Group("group1", "group1 description");
	private final Group group2 = new Group("group2", "group2 description");

	private final String organizationNamespaceValue1 = "muni-cz-identifier";
	private final String organizationNamespaceValue2 = "einfra-cz-identifier";
	private final String organizationScopeValue1 = "muni.cz";
	private final String organizationScopeValue2 = "einfra.cz";

	private final String loginNamespaceAttrName1 = loginNamespaceAttrPrefix + organizationNamespaceValue1;
	private final String loginNamespaceAttrName2 = loginNamespaceAttrPrefix + organizationNamespaceValue2;
	private final String loginNamespaceAttrFriendlyName1 = AttributesManager.LOGIN_NAMESPACE + ":" + organizationNamespaceValue1;
	private final String loginNamespaceAttrFriendlyName2 = AttributesManager.LOGIN_NAMESPACE + ":" + organizationNamespaceValue2;
	private final String loginNamespaceAttrValue1 = "123456";
	private final String loginNamespaceAttrValue2 = "987654";

	private final Attribute groupNamespaceAttr1 = setUpGroupAttribute(101, organizationNamespaceAttrFriendlyName, String.class.getName(), organizationNamespaceValue1);
	private final Attribute groupNamespaceAttr2 = setUpGroupAttribute(101, organizationNamespaceAttrFriendlyName, String.class.getName(), organizationNamespaceValue2);

	private final Attribute groupScopeAttr1 = setUpGroupAttribute(201, organizationScopeAttrFriendlyName, String.class.getName(), organizationScopeValue1);
	private final Attribute groupScopeAttr2 = setUpGroupAttribute(201, organizationScopeAttrFriendlyName, String.class.getName(), organizationScopeValue2);

	private final Attribute userLoginNamespaceAttr1 = setUpUserAttribute(301, loginNamespaceAttrFriendlyName1, String.class.getName(), loginNamespaceAttrValue1);
	private final Attribute userLoginNamespaceAttr2 = setUpUserAttribute(302, loginNamespaceAttrFriendlyName2, String.class.getName(), loginNamespaceAttrValue2);

	// Expected (returned) values
	private final String studentIdentifierValue1 = studentIdentifiersValuePrefix + organizationScopeValue1 + ":" + loginNamespaceAttrValue1;
	private final String studentIdentifierValue2 = studentIdentifiersValuePrefix + organizationScopeValue2 + ":" + loginNamespaceAttrValue2;

	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		studentIdentifiersAttrDef.setId(100);

		group1.setId(1);
		group2.setId(2);

		//prepare mocks
		sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);


		// Members Manager
		when(sess.getPerunBl().getMembersManagerBl().getMembersByUserWithStatus(sess, user, Status.VALID))
			.thenReturn(Arrays.asList(member1, member2));

		// Groups Manager
		when(sess.getPerunBl().getGroupsManagerBl().getGroupsWhereMemberIsActive(sess, member1))
			.thenReturn(Arrays.asList(group1));
		when(sess.getPerunBl().getGroupsManagerBl().getGroupsWhereMemberIsActive(sess, member2))
			.thenReturn(Arrays.asList(group2));

		// Attributes manager
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group1, organizationNamespaceAttrName))
			.thenReturn(groupNamespaceAttr1);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group2, organizationNamespaceAttrName))
			.thenReturn(groupNamespaceAttr2);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group1, organizationScopeAttrName))
			.thenReturn(groupScopeAttr1);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group2, organizationScopeAttrName))
			.thenReturn(groupScopeAttr2);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, loginNamespaceAttrName1))
			.thenReturn(userLoginNamespaceAttr1);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, loginNamespaceAttrName2))
			.thenReturn(userLoginNamespaceAttr2);
	}

	private Attribute setUpGroupAttribute(int id, String friendlyName, String type, Object value) {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setId(id);
		attrDef.setFriendlyName(friendlyName);
		attrDef.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attrDef.setType(type);
		Attribute attr = new Attribute(attrDef);
		attr.setValue(value);
		return attr;
	}

	private Attribute setUpUserAttribute(int id, String friendlyName, String type, Object value) {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setId(id);
		attrDef.setFriendlyName(friendlyName);
		attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrDef.setType(type);
		Attribute attr = new Attribute(attrDef);
		attr.setValue(value);
		return attr;
	}

	@Test
	public void getAttributeValue() {
		List<String> attributeValue = classInstance.getAttributeValue(sess, user, studentIdentifiersAttrDef).valueAsList();

		assertNotNull(attributeValue);
		assertEquals(2, attributeValue.size());
		assertTrue(attributeValue.contains(studentIdentifierValue1));
		assertTrue(attributeValue.contains(studentIdentifierValue2));
	}
}
