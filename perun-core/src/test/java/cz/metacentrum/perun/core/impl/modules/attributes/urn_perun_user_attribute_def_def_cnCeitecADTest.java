package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_cnCeitecADTest {

	private static urn_perun_user_attribute_def_def_cnCeitecAD classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static User secondUser;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_cnCeitecAD();
		session = mock(PerunSessionImpl.class);
		user = new User(0, "John", "Doe", "", "", "");
		secondUser = new User(1, "Jane", "Doe", "", "", "");
		attributeToCheck = new Attribute();
		attributeToCheck.setValue("něco");

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
		when(session.getPerunBl().getUsersManagerBl()).thenReturn(usersManagerBl);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testNullValue() throws Exception {
		System.out.println("testNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckAlreadyOccupiedValue() throws Exception {
		System.out.println("testCheckAlreadyOccupiedValue()");
		when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck)).thenReturn(Arrays.asList(user, secondUser));

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck)).thenReturn(Collections.singletonList(user));

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
