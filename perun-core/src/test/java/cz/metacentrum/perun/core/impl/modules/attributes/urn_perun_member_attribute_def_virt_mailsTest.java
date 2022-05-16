package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class urn_perun_member_attribute_def_virt_mailsTest extends AbstractPerunIntegrationTest {

	private Member member;
	private Vo vo;
	private User user;
	private urn_perun_member_attribute_def_virt_mails classInstance;
	private static final String A_U_PREFERRED_MAIL = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";
	private static final String A_M_MAIL = AttributesManager.NS_MEMBER_ATTR_DEF + ":mail";
	private static final String prefMailTestValue = "pref@perun.cz";
	private static final String memberMailTestValue = "memb@perun.cz";

	@Before
	public void SetUp() throws Exception {
		classInstance = new urn_perun_member_attribute_def_virt_mails();
		vo = setUpVo();
		user = setUpUser();
		member = setUpMember(vo);
	}

	@Test
	public void getAttributeValue() throws Exception {
		Attribute preferredMail = perun.getAttributesManagerBl().getAttribute(sess, user, A_U_PREFERRED_MAIL);
		preferredMail.setValue(prefMailTestValue);
		perun.getAttributesManager().setAttribute(sess, user, preferredMail);

		Attribute memberMail = perun.getAttributesManagerBl().getAttribute(sess, member, A_M_MAIL);
		memberMail.setValue(memberMailTestValue);
		perun.getAttributesManager().setAttribute(sess, member, memberMail);

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_VIRT);
		attr.setFriendlyName("mails");
		attr.setType(ArrayList.class.getName());
		Attribute attribute = new Attribute(attr);

		AttributeDefinition attrDef = perun.getAttributesManager().createAttribute(sess, attribute);
		ArrayList<String> value = classInstance.getAttributeValue((PerunSessionImpl) sess, member, attrDef).valueAsList();

		assertEquals("Attribute should contain 2 mails.", 2, value.size());
		assertTrue("Attribute should contain preferred mail value.", value.contains(prefMailTestValue));
		assertTrue("Attribute should contain member mail value.", value.contains(memberMailTestValue));
	}

	private Vo setUpVo() throws Exception {
		Vo newVo = new Vo(0, "TestVo", "TestVo");
		return perun.getVosManagerBl().createVo(sess, newVo);
	}

	private User setUpUser() {
		User newUser = new User();
		return perun.getUsersManagerBl().createUser(sess, newUser);
	}

	private Member setUpMember(Vo vo) throws Exception {
		return perun.getMembersManagerBl().createMember(sess, vo, user);
	}
}
