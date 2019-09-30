package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_uid_namespaceTest {
	private static urn_perun_user_attribute_def_def_uid_namespace classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;
	private static Attribute minUid;
	private static Attribute maxUid;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_uid_namespace();
		session = mock(PerunSessionImpl.class);
		user = new User(0, "John", "Doe", "", "", "");
		attributeToCheck = new Attribute();
		attributeToCheck.setFriendlyName("name:param");
		minUid = new Attribute();
		maxUid = new Attribute();

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(session.getPerunBl().getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, "param", AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minUID")).thenReturn(minUid);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, "param", AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxUID")).thenReturn(maxUid);

		UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
		when(session.getPerunBl().getUsersManagerBl()).thenReturn(usersManagerBl);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithNullValue() throws Exception {
		System.out.println("testCheckWithNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithNullWithNullMinUid() throws Exception {
		System.out.println("testCheckWithNullWithNullMinUid()");
		minUid.setValue(null);
		maxUid.setValue(6);
		attributeToCheck.setValue(5);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithNullWithNullMaxUid() throws Exception {
		System.out.println("testCheckWithNullWithNullMaxUid()");
		minUid.setValue(1);
		maxUid.setValue(null);
		attributeToCheck.setValue(5);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithUidLesserThanMin() throws Exception {
		System.out.println("testCheckWithUidLesserThanMin()");
		minUid.setValue(2);
		maxUid.setValue(6);
		attributeToCheck.setValue(1);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithUidHigherThanMax() throws Exception {
		System.out.println("testCheckWithUidHigherThanMax()");
		minUid.setValue(2);
		maxUid.setValue(6);
		attributeToCheck.setValue(7);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithDuplicate() throws Exception {
		System.out.println("testCheckWithDuplicate()");
		minUid.setValue(2);
		maxUid.setValue(6);
		attributeToCheck.setValue(5);

		List<User> list = new ArrayList<>();
		list.add(user);
		list.add(new User());
		when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck)).thenReturn(list);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		minUid.setValue(2);
		maxUid.setValue(6);
		attributeToCheck.setValue(5);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
