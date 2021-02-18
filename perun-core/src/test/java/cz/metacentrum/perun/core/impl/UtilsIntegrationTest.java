package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidDestinationException;
import cz.metacentrum.perun.core.api.exceptions.InvalidHostnameException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests of methods from Utils class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class UtilsIntegrationTest extends AbstractPerunIntegrationTest {

	private User user;           // our User
	private Vo vo;
	String userFirstName = "";
	String userLastName = "";
	String extLogin = "";
	String extLogin2 = "";
	final String extSourceName = "UserManagerEntryIntegrationTest";
	final UserExtSource userExtSource = new UserExtSource();   // create new User Ext Source

	final String extSourceName2 = "UserManagerEntryIntegrationTest2";
	final UserExtSource userExtSource2 = new UserExtSource();

	private UsersManager usersManager;

	@Before
	public void setUp() throws Exception {
		usersManager = perun.getUsersManager();
		// set random name and logins during every setUp method
		userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));   // his login in external source
		extLogin2 = Long.toHexString(Double.doubleToLongBits(Math.random()));
		vo = setUpVo();
		setUpUser();
		setUpUserExtSource();
	}

	@Test
	public void parseCommonNameTest() {
		System.out.println("Utils.parseCommonName");

		String titleBeforeString = "titleBefore";
		String titleAfterString = "titleAfter";
		String firstNameString = "firstName";
		String lastNameString = "lastName";

		String text1 = "RNDr. David Kurka Ph.D.";
		String text2 = "RNDr. David Kurka Ph. D.";
		String text3 = "Mgr. David Něco Kurka CSc. Ph.D.";
		String text4 = "David Kurka CSc. Ph.D.";
		String text5 = "Mgr. et Mgr. David Kurka Ph.D.";
		String text6 = "David Kurka CSc.";
		String text7 = "David Kurka";
		String text8 = "Dr. David Kurka";
		String text9 = "Ali H. Reshak Al-jaary";
		String text10 = "Ali H. Reshak Al-jaary CSc.";
		String text11 = "prof. Ali H. Reshak Al-jaary";
		String text12 = "RNDr. Petr Kulhánek, PhD.";
		String text13 = "prof. RNDr. David Kurka, CSc.";
		String text14 = "prof.,RNDr. David  Kurka_mladsi, CSc.";
		String text15 = "Kurka";

		Map<String,String> parsedRawName;

		parsedRawName = Utils.parseCommonName(text1);
		assertEquals("RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("Ph.D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text2);
		assertEquals("RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("Ph. D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text3);
		assertEquals("Mgr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Něco Kurka", parsedRawName.get(lastNameString));
		assertEquals("CSc. Ph.D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text4);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("CSc. Ph.D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text5);
		assertEquals("Mgr. et Mgr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("Ph.D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text6);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("CSc.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text7);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text8);
		assertEquals("Dr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text9);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("Ali", parsedRawName.get(firstNameString));
		assertEquals("H. Reshak Al-jaary", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text10);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("Ali", parsedRawName.get(firstNameString));
		assertEquals("H. Reshak Al-jaary", parsedRawName.get(lastNameString));
		assertEquals("CSc.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text11);
		assertEquals("prof.", parsedRawName.get(titleBeforeString));
		assertEquals("Ali", parsedRawName.get(firstNameString));
		assertEquals("H. Reshak Al-jaary", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text12);
		assertEquals("RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("Petr", parsedRawName.get(firstNameString));
		assertEquals("Kulhánek", parsedRawName.get(lastNameString));
		assertEquals("PhD.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text13);
		assertEquals("prof. RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("CSc.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text14);
		assertEquals("prof. RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka mladsi", parsedRawName.get(lastNameString));
		assertEquals("CSc.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text15);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals(null, parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));
	}

	@Test
	public void extendDateByPeriod() {
		System.out.println("Utils.extendDateByPeriod");
		LocalDate localDate = Utils.extendDateByPeriod(LocalDate.of(2019,2,8), "+1d");
		assertEquals(LocalDate.of(2019, 2, 9), localDate);

		localDate = Utils.extendDateByPeriod(LocalDate.of(2019,2,8), "+1m");
		assertEquals(LocalDate.of(2019, 3, 8), localDate);

		localDate = Utils.extendDateByPeriod(LocalDate.of(2019,2,8), "+1y");
		assertEquals(LocalDate.of(2020, 2, 8), localDate);
	}

	@Test(expected = InternalErrorException.class)
	public void extendDateByPeriodInBadFormat() {
		System.out.println("Utils.extendDateByPeriodInBadFormat");
		LocalDate localDate = Utils.extendDateByPeriod(LocalDate.of(2019,2,8), "+1day");
	}

	@Test
	public void extendDateByStaticDate() {
		System.out.println("Utils.extendDateByStaticDate");
		String period = "1.1.";
		Pattern p = Pattern.compile("([0-9]+).([0-9]+).");
		Matcher m = p.matcher(period);
		m.matches();
		LocalDate localDate = Utils.getClosestExpirationFromStaticDate(m);
		assertEquals(LocalDate.of(LocalDate.now().getYear()+1, 1, 1), localDate);
	}

	@Test
	public void prepareGracePeriodDate() {
		System.out.println("Utils.prepareGracePeriodDate");
		String gracePeriod = "5d";
		Pattern p = Pattern.compile("([0-9]+)([dmy]?)");
		Matcher m = p.matcher(gracePeriod);
		Pair<Integer, TemporalUnit> fieldAmount = Utils.prepareGracePeriodDate(m);
		assertEquals(new Integer(5), fieldAmount.getLeft());
		assertEquals(ChronoUnit.DAYS, fieldAmount.getRight());
	}

	@Test
	public void extractAdditionalUserExtSourcesTest() throws Exception {
		System.out.println("Utils.extractAdditionalUserExtSources");

		Map<String, String> map = new HashMap<>();
		map.put("additionalues_a", extSourceName + "|cz.metacentrum.perun.core.impl.ExtSourceInternal|" + extLogin);
		map.put("additionalues_b", extSourceName2 + "|cz.metacentrum.perun.core.impl.ExtSourceInternal|" + extLogin2);

		List<RichUserExtSource> list = Utils.extractAdditionalUserExtSources(sess, map);
		assertEquals(list.size(), 2);
		assertTrue(list.contains(new RichUserExtSource(userExtSource2, new ArrayList<>())));
		assertTrue(list.contains(new RichUserExtSource(userExtSource, new ArrayList<>())));
	}

	@Test
	public void extractAdditionalUserExtSourcesTestWithEmptyValue() throws Exception {
		System.out.println("Utils.extractAdditionalUserExtSourcesTestWithEmptyValue");

		Map<String, String> map = new HashMap<>();
		map.put("additionalues_a", extSourceName + "||" + extLogin + "|2");

		List<RichUserExtSource> list = Utils.extractAdditionalUserExtSources(sess, map);
		assertEquals(list.size(), 0);
	}

	@Test(expected = InternalErrorException.class)
	public void extractAdditionalUserExtSourcesTestWithNotEnoughValues() throws Exception {
		System.out.println("Utils.extractAdditionalUserExtSourcesTestWithNotEnoughValues");

		Map<String, String> map = new HashMap<>();
		map.put("additionalues_a", extSourceName);

		List<RichUserExtSource> list = Utils.extractAdditionalUserExtSources(sess, map);
		assertEquals(list.size(), 0);
	}

	@Test
	public void extractAdditionalUserExtSourcesWithAttributeTest() throws Exception {
		System.out.println("Utils.extractAdditionalUserExtSourcesWithAttributeTest");

		Map<String, String> map = new HashMap<>();
		map.put("additionalues_b", extSourceName2 + "|cz.metacentrum.perun.core.impl.ExtSourceInternal|" + extLogin2 + ";urn:perun:ues:attribute-def:def:eppn=" + extLogin2 + "|2");

		AttributeDefinition attributeDefinition = new AttributeDefinition();
		attributeDefinition.setNamespace("urn:perun:ues:attribute-def:def");
		attributeDefinition.setFriendlyName("eppn");
		attributeDefinition.setDescription("login value");
		attributeDefinition.setType(String.class.getName());
		sess.getPerun().getAttributesManager().createAttribute(sess, attributeDefinition);

		List<RichUserExtSource> list = Utils.extractAdditionalUserExtSources(sess, map);
		assertEquals(list.size(), 1);
		assertTrue(list.contains(new RichUserExtSource(userExtSource2, Arrays.asList(new Attribute(attributeDefinition, extLogin2)))));
	}

	@Test
	public void extractAdditionalUserExtSourcesWithAttributeListTest() throws Exception {
		System.out.println("Utils.extractAdditionalUserExtSourcesWithAttributeTest");

		Map<String, String> map = new HashMap<>();
		map.put("additionalues_a", extSourceName + "|cz.metacentrum.perun.core.impl.ExtSourceInternal|" + extLogin + ";urn:perun:ues:attribute-def:def:eppn=" + extLogin
			+ ";urn:perun:ues:attribute-def:def:eppnList=" + extLogin + "," + extLogin2);

		AttributeDefinition attributeDefinition = new AttributeDefinition();
		attributeDefinition.setNamespace("urn:perun:ues:attribute-def:def");
		attributeDefinition.setFriendlyName("eppn");
		attributeDefinition.setDescription("login value");
		attributeDefinition.setType(String.class.getName());
		sess.getPerun().getAttributesManager().createAttribute(sess, attributeDefinition);

		AttributeDefinition attributeDefinition2 = new AttributeDefinition();
		attributeDefinition2.setNamespace("urn:perun:ues:attribute-def:def");
		attributeDefinition2.setFriendlyName("eppnList");
		attributeDefinition2.setDescription("login value as list");
		attributeDefinition2.setType(ArrayList.class.getName());
		sess.getPerun().getAttributesManager().createAttribute(sess, attributeDefinition2);

		List<RichUserExtSource> list = Utils.extractAdditionalUserExtSources(sess, map);
		assertEquals(list.size(), 1);
		assertTrue(list.contains(new RichUserExtSource(userExtSource, Arrays.asList(new Attribute(attributeDefinition, extLogin), new Attribute(attributeDefinition2, Arrays.asList(extLogin, extLogin2))))));
	}

	@Test
	public void extractAdditionalUserExtSourcesWithAttributeWrongValueTest() throws Exception {
		System.out.println("Utils.extractAdditionalUserExtSourcesWithAttributeWrongValueTest");

		Map<String, String> map = new HashMap<>();
		map.put("additionalues_b", extSourceName2 + "|cz.metacentrum.perun.core.impl.ExtSourceInternal|" + extLogin2 + ";urn:perun:ues:attribute-def:def:eppn");

		AttributeDefinition attributeDefinition = new AttributeDefinition();
		attributeDefinition.setNamespace("urn:perun:ues:attribute-def:def");
		attributeDefinition.setFriendlyName("eppn");
		attributeDefinition.setDescription("login value");
		attributeDefinition.setType(String.class.getName());
		sess.getPerun().getAttributesManager().createAttribute(sess, attributeDefinition);

		List<RichUserExtSource> list = Utils.extractAdditionalUserExtSources(sess, map);
		assertEquals(list.size(), 1);
		assertTrue(list.contains(new RichUserExtSource(userExtSource2, new ArrayList<>())));
	}

	@Test
	public void validateGroupNameThrowsACorrectExceptionForInvalidRegex() {
		System.out.println("Utils.validateGroupNameThrowsACorrectExceptionForInvalidRegex");

		String invalidPattern = "[a-";
		assertThatExceptionOfType(InternalErrorException.class)
				.isThrownBy(() -> Utils.validateGroupName("members", invalidPattern));
	}

	@Test
	public void checkHostDestination() throws Exception {
		System.out.println("Utils.checkHostDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONHOSTTYPE);

		destination.setDestination("192.168.1.1");
		Utils.checkDestination(destination);

		destination.setDestination("hostname.test");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkMailDestination() throws Exception {
		System.out.println("Utils.checkHostDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONEMAILTYPE);

		destination.setDestination("testing@host.test");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkUrlDestination() throws Exception {
		System.out.println("Utils.checkHostDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://www.url.test/");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkUserHostDestination() throws Exception {
		System.out.println("Utils.checkHostDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONUSERHOSTTYPE);

		destination.setDestination("test@192.168.1.1");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkUserHostPortDestination() throws Exception {
		System.out.println("Utils.checkHostDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONUSERHOSTPORTTYPE);

		destination.setDestination("test@192.168.1.1:6060");
		Utils.checkDestination(destination);

		destination.setDestination("test@jakse.mas:6060");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkServiceSpecificDestination() throws Exception {
		System.out.println("Utils.checkHostDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONSERVICESPECIFICTYPE);

		destination.setDestination("test");
		Utils.checkDestination(destination);
	}

	@Test(expected = InvalidDestinationException.class)
	public void checkDestinationInvalid() throws Exception {
		System.out.println("Utils.checkHostDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONUSERHOSTPORTTYPE);

		destination.setDestination("test@192.168.1.1");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkHostname() throws Exception {
		System.out.println("Utils.checkhostname");

		Host host = new Host();
		host.setHostname("192.168.1.1");
		Utils.checkHostname(host);

		host.setHostname("hostname.test");
		Utils.checkHostname(host);
	}

	@Test(expected = InvalidHostnameException.class)
	public void checkHostnameInvalid() throws Exception {
		System.out.println("Utils.checkHostnameInvalid");

		Host host = new Host();
		host.setHostname("invalid");
		Utils.checkHostname(host);
	}

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);
		assertEquals("both VOs should be the same",newVo,returnedVo);
		ExtSource newExtSource = new ExtSource(extSourceName, ExtSourcesManager.EXTSOURCE_INTERNAL);
		ExtSource es = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);
		// get and create real external source from DB
		perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
		// add real ext source to our VO

		ExtSource newExtSource2 = new ExtSource(extSourceName2, ExtSourcesManager.EXTSOURCE_INTERNAL);
		ExtSource es2 = perun.getExtSourcesManager().createExtSource(sess, newExtSource2, null);
		// get and create real external source from DB
		perun.getExtSourcesManager().addExtSource(sess, returnedVo, es2);
		return returnedVo;
	}

	private void setUpUserExtSource() throws Exception {

		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		userExtSource.setExtSource(externalSource);
		userExtSource.setLogin(extLogin);
		assertNotNull(usersManager.addUserExtSource(sess, user, userExtSource));

		ExtSource externalSource2 = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName2);
		userExtSource2.setExtSource(externalSource2);
		userExtSource2.setLogin(extLogin2);
		assertNotNull(usersManager.addUserExtSource(sess, user, userExtSource2));
	}

	private void setUpUser() throws Exception {

		user = new User();
		user.setFirstName(userFirstName);
		user.setMiddleName("");
		user.setLastName(userLastName);
		user.setTitleBefore("");
		user.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user));
		// create new user in database
		usersForDeletion.add(user);
		// save user for deletion after testing
	}
}
