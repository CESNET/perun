package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_virt_scopedLogin_namespace_muTest {
	private static urn_perun_user_attribute_def_virt_scopedLogin_namespace_mu classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;
	private final String muNamespace = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu";

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_virt_scopedLogin_namespace_mu();
		session = mock(PerunSessionImpl.class);

		attributeToCheck = new Attribute();
		attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attributeToCheck.setFriendlyName("login-namespace:mu");

		when(session.getPerunBl()).thenReturn(mock(PerunBl.class));
		when(session.getPerunBl().getAttributesManagerBl()).thenReturn(mock(AttributesManagerBl.class));
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, muNamespace)).thenReturn(attributeToCheck);
	}

	@Test
	public void testCheckWithAttribute() throws Exception{
		System.out.println("testCheckWithAttribute()");

		attributeToCheck.setValue("test");
		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("scopedLogin-namespace:mu");

		assertEquals(attributeToCheck.getValue() + "@muni.cz", classInstance.getAttributeValue(session, user, attr).getValue());
	}

	@Test
	public void testCheckNull() throws Exception {
		System.out.println("testCheckNull()");
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, muNamespace)).thenThrow(AttributeNotExistsException.class);

		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("scopedLogin-namespace:mu");

		assertNull(classInstance.getAttributeValue(session, user, attr).getValue());
	}

	@Test
	public void testCheckNullValue() throws Exception {
		System.out.println("testCheckNull()");
		Attribute nullValueAttrToCheck = new Attribute();
		Utils.copyAttributeToViAttributeWithoutValue(nullValueAttrToCheck, attributeToCheck);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, muNamespace)).thenReturn(nullValueAttrToCheck);

		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("scopedLogin-namespace:mu");

		assertNull(classInstance.getAttributeValue(session, user, attr).getValue());
	}

}
