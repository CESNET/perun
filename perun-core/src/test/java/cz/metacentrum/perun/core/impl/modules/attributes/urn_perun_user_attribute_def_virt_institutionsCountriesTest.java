package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForKey;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUes;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class urn_perun_user_attribute_def_virt_institutionsCountriesTest {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_institutionsCountriesTest.class);
	public static final String SCHAC_HOME_ATTR_NAME = "urn:perun:ues:attribute-def:def:schacHomeOrganization";

	private final urn_perun_user_attribute_def_virt_institutionsCountries classInstance = new urn_perun_user_attribute_def_virt_institutionsCountries();
	private final AttributeDefinition institutionCountriesAttrDef = classInstance.getAttributeDefinition();

	private final User user = new User(10, "Joe", "Doe", "W.", "", "");
	private final UserExtSource userExtSource = new UserExtSource(1, new ExtSource(100, "es_name", "es_type"), "joe", user.getId());
	private final List<UserExtSource> userExtSources = Collections.singletonList(userExtSource);

	private final Map<String, String> dnsMap = new HashMap<>();
	private PerunSessionImpl sess;
	private Attribute schacHomeOrg;

	@Before
	public void setUp() throws Exception {
		dnsMap.put(".cz", "Czech Rep");
		dnsMap.put("muni.cz", "MU");
		dnsMap.put("ics.muni.cz", "UVT");

		AttributeDefinition schacHomeOrgDef = new AttributeDefinition();
		schacHomeOrgDef.setId(5);
		schacHomeOrgDef.setFriendlyName("schacHomeOrganization");
		schacHomeOrgDef.setNamespace(AttributesManager.NS_UES_ATTR_DEF);
		schacHomeOrgDef.setType("java.lang.String");
		schacHomeOrg = new Attribute(schacHomeOrgDef);

		//prepare mocks
		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		AttributesManagerBl am = mock(AttributesManagerBl.class);
		UsersManagerBl um = mock(UsersManagerBl.class);
		ModulesUtilsBl mu = mock(ModulesUtilsBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);
		when(perunBl.getAttributesManagerBl()).thenReturn(am);
		when(perunBl.getUsersManagerBl()).thenReturn(um);
		when(perunBl.getModulesUtilsBl()).thenReturn(mu);
		when(mu.getUserFromMessage(eq(sess), any(String.class))).thenReturn(user);
		when(am.getEntitylessStringAttributeMapping(sess, "urn:perun:entityless:attribute-def:def:dnsStateMapping")).thenReturn(dnsMap);
		when(um.getUserExtSources(sess, user)).thenReturn(userExtSources);

	}

	private void setSchacHomeOrgs(String domains) throws WrongAttributeAssignmentException, InternalErrorException, AttributeNotExistsException {
		schacHomeOrg.setValue(domains);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, userExtSource, SCHAC_HOME_ATTR_NAME)).thenReturn(schacHomeOrg);
	}

	@Test
	public void getAttributeValue() throws Exception {
		setSchacHomeOrgs("muni.cz;cesnet.cz");

		@SuppressWarnings("unchecked") ArrayList<String> attributeValue
				= (ArrayList<String>) classInstance.getAttributeValue(sess, user, institutionCountriesAttrDef).getValue();
		assertThat(attributeValue, is(notNullValue()));
		assertThat(attributeValue, CoreMatchers.hasItem("Czech Rep"));
		assertThat(attributeValue, CoreMatchers.hasItem("MU"));
		assertThat(attributeValue.size(), is(2));
	}

	@Test
	public void getAttributeValue2() throws Exception {
		setSchacHomeOrgs(".sk;google.com");

		@SuppressWarnings("unchecked") ArrayList<String> attributeValue
				= (ArrayList<String>) classInstance.getAttributeValue(sess, user, institutionCountriesAttrDef).getValue();
		assertThat(attributeValue, is(notNullValue()));
		assertThat(attributeValue.size(), is(0));
	}

	@Test
	public void getAttributeValue3() throws Exception {
		setSchacHomeOrgs(null);
		@SuppressWarnings("unchecked") ArrayList<String> attributeValue
				= (ArrayList<String>) classInstance.getAttributeValue(sess, user, institutionCountriesAttrDef).getValue();
		assertThat(attributeValue, is(notNullValue()));
		assertThat(attributeValue.size(), is(0));
	}

	@Test
	public void resolveVirtualAttributeValueChange() throws Exception {
		setSchacHomeOrgs("muni.cz;cesnet.cz");
		AttributeDefinition countries = classInstance.getAttributeDefinition();
		when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess,"urn:perun:user:attribute-def:virt:institutionsCountries"))
			.thenReturn(countries);
		when(sess.getPerunBl().getUsersManagerBl().getUserById(sess, userExtSource.getUserId())).thenReturn(user);

		AuditEvent uesSet = new AttributeSetForUes(schacHomeOrg, userExtSource);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, uesSet);
		assertTrue("audit should contain change of institutionsCountries",msgs.get(0).getMessage().contains("friendlyName=<institutionsCountries>"));
	}

	@Test
	public void resolveVirtualAttributeValueChange2() throws Exception {
		setSchacHomeOrgs("muni.cz;cesnet.cz");
		String czech_republic = "Czech Republic";
		dnsMap.put(".cz", czech_republic);
		AttributeDefinition countries = classInstance.getAttributeDefinition();
		when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:user:attribute-def:virt:institutionsCountries"))
			.thenReturn(countries);
		Attribute newval = new Attribute(new urn_perun_entityless_attribute_def_def_dnsStateMapping().getAttributeDefinition());
		newval.setValue(czech_republic);

		when(sess.getPerunBl().getUsersManagerBl().findUsersWithExtSourceAttributeValueEnding(eq(sess), eq(SCHAC_HOME_ATTR_NAME), eq(".cz"), any()))
			.thenReturn(Collections.singletonList(user));
		AuditEvent event = new AttributeSetForKey(newval, ".cz");
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, event);
		assertTrue("audit should contain change of institutionsCountries",msgs.get(0).getMessage().contains("friendlyName=<institutionsCountries>"));
	}
}
