package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_virt_optional_login_namespace_muTest {

	private static urn_perun_user_attribute_def_virt_optional_login_namespace_mu classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;
	private static ExtSource extSource;
	private static UserExtSource ues1;
	private static UserExtSource ues2;
	private final String EXTSOURCE_MUNI_IDP2 = "https://idp2.ics.muni.cz/idp/shibboleth";
	private final String muNamespace = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu";

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_virt_optional_login_namespace_mu();
		session = mock(PerunSessionImpl.class);

		attributeToCheck = new Attribute();
		attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attributeToCheck.setFriendlyName("login-namespace:mu");

		extSource = new ExtSource(EXTSOURCE_MUNI_IDP2, ExtSourcesManager.EXTSOURCE_IDP);

		ues1 = new UserExtSource();
		ues1.setExtSource(extSource);
		ues1.setLogin("123456@muni.cz");

		ues2 = new UserExtSource();
		ues2.setExtSource(extSource);
		ues2.setLogin("123456@gmail.com");

		when(session.getPerunBl()).thenReturn(mock(PerunBl.class));
		when(session.getPerunBl().getAttributesManagerBl()).thenReturn(mock(AttributesManagerBl.class));
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, muNamespace)).thenReturn(attributeToCheck);

		when(session.getPerunBl().getUsersManagerBl()).thenReturn(mock(UsersManagerBl.class));
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(List.of(ues1, ues2));
	}

	@Test
	public void testCheckWithAttribute() throws Exception{
		System.out.println("testCheckWithAttribute()");

		attributeToCheck.setValue("test");
		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("optional-login-namespace:mu");

		assertEquals(attributeToCheck.getValue(), classInstance.getAttributeValue(session, user, attr).getValue());
	}
	@Test
	public void testLoginFromExtSource() throws Exception {
		System.out.println("testLoginFromExtSource()");
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, muNamespace)).thenThrow(AttributeNotExistsException.class);

		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("optional-login-namespace:mu");
		assertEquals("123456", classInstance.getAttributeValue(session, user, attr).getValue());
	}

	@Test
	public void testCheckNull() throws Exception {
		System.out.println("testCheckNull()");
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(List.of());
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, muNamespace)).thenThrow(AttributeNotExistsException.class);

		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("optional-login-namespace:mu");

		assertNull(classInstance.getAttributeValue(session, user, attr).getValue());
	}

}
