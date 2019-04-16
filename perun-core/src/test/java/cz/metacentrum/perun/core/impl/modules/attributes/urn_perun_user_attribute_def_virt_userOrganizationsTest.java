package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class urn_perun_user_attribute_def_virt_userOrganizationsTest extends AbstractPerunIntegrationTest {

	private User user;
	private Member member;
	private Member member2;
	private Vo vo;
	private Vo vo2;
	private urn_perun_user_attribute_def_virt_userOrganizations classInstance;
	private static final String A_M_organization = AttributesManager.NS_MEMBER_ATTR_DEF + ":organization";

	@Before
	public void SetUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_virt_userOrganizations();
		user = setUpUser();
		vo = setUpVo();
		member = setUpMember(vo);
		vo2 = setUpVo2();
		member2 = setUpMember(vo2);
	}

	@Test
	public void getAttributeValue() throws Exception {
		// Set values of member organization attribute
		Attribute organization = perun.getAttributesManagerBl().getAttribute(sess, member, A_M_organization);
		organization.setValue("some value");
		perun.getAttributesManager().setAttribute(sess, member, organization);
		organization = perun.getAttributesManagerBl().getAttribute(sess, member2, A_M_organization);
		organization.setValue("different value");
		perun.getAttributesManager().setAttribute(sess, member2, organization);

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("userOrganizations");
		attr.setType(HashMap.class.getName());

		Attribute attribute = new Attribute(attr);

		perun.getAttributesManager().createAttribute(sess, attribute);

		Map<String, String> value = classInstance.getAttributeValue((PerunSessionImpl) sess, user, attribute).valueAsMap();

		assertEquals("Attribute should have 2 elements.", 2, value.size());
		assertTrue("Attribute should contain short name of first vo as a key.", value.containsKey(vo.getShortName()));
		assertTrue("Attribute should contain short name of second vo as a key.", value.containsKey(vo2.getShortName()));
		assertEquals("some value should be a value to short name of first vo.", "some value", value.get(vo.getShortName()));
		assertEquals("different value should be a value to short name of second vo.", "different value", value.get(vo2.getShortName()));
	}

	private User setUpUser() throws InternalErrorException {
		User newUser = new User();
		return perun.getUsersManagerBl().createUser(sess, newUser);
	}

	private Vo setUpVo() throws Exception {
		Vo newVo = new Vo(0, "TestVo", "TestVo");
		return perun.getVosManagerBl().createVo(sess, newVo);

	}

	private Vo setUpVo2() throws Exception {
		Vo newVo = new Vo(1, "TestVo2", "TestVo2");
		return perun.getVosManagerBl().createVo(sess, newVo);

	}

	private Member setUpMember(Vo vo) throws Exception {
		return perun.getMembersManagerBl().createMember(sess, vo, user);
	}
}