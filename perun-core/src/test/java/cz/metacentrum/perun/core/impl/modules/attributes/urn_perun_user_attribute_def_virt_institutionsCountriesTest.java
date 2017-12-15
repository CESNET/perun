package cz.metacentrum.perun.core.impl.modules.attributes;

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

	private urn_perun_user_attribute_def_virt_institutionsCountries classInstance = new urn_perun_user_attribute_def_virt_institutionsCountries();
	private AttributeDefinition institutionCountriesAttrDef = classInstance.getAttributeDefinition();

	private User user = new User(10, "Joe", "Doe", "W.", "", "");
	private UserExtSource userExtSource = new UserExtSource(1, new ExtSource(100, "es_name", "es_type"), "joe", user.getId());
	private List<UserExtSource> userExtSources = Collections.singletonList(userExtSource);

	private Map<String, String> dnsMap = new HashMap<>();
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
		Attribute countries = classInstance.getAttributeValue(sess, user, institutionCountriesAttrDef);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess,user,"urn:perun:user:attribute-def:virt:institutionsCountries"))
				.thenReturn(countries);

		String uesSet = schacHomeOrg.serializeToString() + " set for "+userExtSource.serializeToString()+".";
		List<String> msgs = classInstance.resolveVirtualAttributeValueChange(sess, uesSet);
		assertTrue("audit should contain change of institutionsCountries",msgs.get(0).contains("friendlyName=<institutionsCountries>"));
	}

	@Test
	public void resolveVirtualAttributeValueChange2() throws Exception {
		setSchacHomeOrgs("muni.cz;cesnet.cz");
		String czech_republic = "Czech Republic";
		dnsMap.put(".cz", czech_republic);
		Attribute countries = classInstance.getAttributeValue(sess, user, institutionCountriesAttrDef);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:virt:institutionsCountries"))
				.thenReturn(countries);
		Attribute newval = new Attribute(new urn_perun_entityless_attribute_def_def_dnsStateMapping().getAttributeDefinition());
		newval.setValue(czech_republic);

		when(sess.getPerunBl().getUsersManagerBl().findUsersWithExtSourceAttributeValueEnding(eq(sess), eq(SCHAC_HOME_ATTR_NAME), eq(".cz"), any()))
				.thenReturn(Collections.singletonList(user));
		String message = newval.serializeToString() + " set for .cz.";
		List<String> msgs = classInstance.resolveVirtualAttributeValueChange(sess, message);
		assertTrue("audit should contain change of institutionsCountries",msgs.get(0).contains("friendlyName=<institutionsCountries>"));
	}
}
