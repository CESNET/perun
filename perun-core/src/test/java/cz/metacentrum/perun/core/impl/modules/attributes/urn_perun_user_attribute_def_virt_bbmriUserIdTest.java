package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test module for urn:perun:user:attribute-def:virt:eduPersonEntitlement
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class urn_perun_user_attribute_def_virt_bbmriUserIdTest {

	private static urn_perun_user_attribute_def_virt_bbmriUserId classInstance;
	private PerunSessionImpl session;
	private User user;
	private UserExtSource ues1;
	private UserExtSource ues2;
	private UserExtSource correctUes;

	@Before
	public void setVariables() {
		classInstance = new urn_perun_user_attribute_def_virt_bbmriUserId();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		user = new User();
		user.setId(1);

		ues1 = new UserExtSource(10, new ExtSource(100, "name1", ExtSourcesManager.EXTSOURCE_INTERNAL), "login1");
		ues2 = new UserExtSource(20, new ExtSource(200, "nameľ", ExtSourcesManager.EXTSOURCE_IDP), "login1");
		int correctValue = 123;
		correctUes = new UserExtSource(
			30,
			new ExtSource(300, urn_perun_user_attribute_def_virt_bbmriUserId.BBMRI_ES_NAME, ExtSourcesManager.EXTSOURCE_INTERNAL),
			Integer.toString(correctValue)
		);
		ues1.setUserId(user.getId());
		ues2.setUserId(user.getId());
		correctUes.setUserId(user.getId());
	}

	@Test
	public void getAttributeValue() {
		// test for correct behavior
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
			Arrays.asList(ues1, ues2, correctUes)
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof Integer);
		assertEquals("received attribute name is wrong",
			classInstance.getAttributeDefinition().getFriendlyName(), receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		Integer actual = (Integer) receivedAttr.getValue();
		Integer expected = Integer.parseInt(correctUes.getLogin());
		assertEquals("received value is incorrect", expected, actual);
	}

	@Test
	public void getAttributeValueNoExtSource() {
		// UES not present, should return null
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
			Arrays.asList(ues1, ues2)
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertEquals("received attribute name is wrong",
			classInstance.getAttributeDefinition().getFriendlyName(), receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		Integer actual = (Integer) receivedAttr.getValue();
		assertNull("received value is not null", actual);
	}

	@Test
	public void getAttributeValueNoIntLoginValue() {
		// set invalid UES extLogin - should be parseable as number
		correctUes.setLogin("testLogin");
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
			Arrays.asList(ues1, ues2, correctUes)
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertEquals("received attribute name is wrong",
			classInstance.getAttributeDefinition().getFriendlyName(), receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		Integer actual = (Integer) receivedAttr.getValue();
		assertNull("received value is not null", actual);
	}

	@Test
	public void getAttributeValueWrongExtSourceType() {
		// set wrong ES type - should be Internal
		correctUes.getExtSource().setType(ExtSourcesManager.EXTSOURCE_IDP);
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
			Arrays.asList(ues1, ues2, correctUes)
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertEquals("received attribute name is wrong",
			classInstance.getAttributeDefinition().getFriendlyName(), receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		Integer actual = (Integer) receivedAttr.getValue();
		assertNull("received value is not null", actual);
	}

	@Test
	public void getAttributeValueWrongExtSourceName() {
		// set wrong ES Name - should match constant from the module
		correctUes.getExtSource().setName("randomWrongName");
		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
			Arrays.asList(ues1, ues2, correctUes)
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertEquals("received attribute name is wrong",
			classInstance.getAttributeDefinition().getFriendlyName(), receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		Integer actual = (Integer) receivedAttr.getValue();
		assertNull("received value is not null", actual);
	}

}
