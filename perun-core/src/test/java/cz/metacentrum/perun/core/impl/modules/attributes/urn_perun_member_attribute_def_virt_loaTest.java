package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests attribute module.
 */
public class urn_perun_member_attribute_def_virt_loaTest {

	private urn_perun_member_attribute_def_virt_loa classInstance;
	private PerunSessionImpl session;
	private Attribute attributeToCheck;
	private Member member;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_member_attribute_def_virt_loa();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		attributeToCheck.setId(101);
		session = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		UsersManagerBl um = mock(UsersManagerBl.class);
		User user = mock(User.class);
		member = mock(Member.class);
		when(session.getPerunBl()).thenReturn(perunBl);
		when(session.getPerunBl().getUsersManagerBl()).thenReturn(um);
		when(session.getPerunBl().getUsersManagerBl().getUserById(session, member.getUserId())).thenReturn(user);
		UserExtSource userExtSource = mock(UserExtSource.class);
		when(userExtSource.getLoa()).thenReturn(1);
		when(session.getPerunBl().getUsersManagerBl().getActiveUserExtSources(session, user)).thenReturn(Collections.singletonList(userExtSource));
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckNotHighestValue() throws Exception {
		System.out.println("testCheckNotHighestValue()");
		attributeToCheck.setValue("0");
		classInstance.checkAttributeSemantics(session, member, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("1");
		classInstance.checkAttributeSemantics(session, member, attributeToCheck);
	}
}
