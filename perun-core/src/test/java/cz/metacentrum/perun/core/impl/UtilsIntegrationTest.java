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
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SSHKeyNotValidException;
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
import static org.assertj.core.api.Assertions.assertThatNoException;
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
		System.out.println("Utils.checkMailDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONEMAILTYPE);

		destination.setDestination("testing@host.test");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkUrlDestination() throws Exception {
		System.out.println("Utils.checkUrlDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://www.url.test/");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkUserHostDestination() throws Exception {
		System.out.println("Utils.checkUserHostDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONUSERHOSTTYPE);

		destination.setDestination("test@192.168.1.1");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkUserHostPortDestination() throws Exception {
		System.out.println("Utils.checkUserHostPortDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONUSERHOSTPORTTYPE);

		destination.setDestination("test@192.168.1.1:6060");
		Utils.checkDestination(destination);

		destination.setDestination("test@jakse.mas:6060");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkServiceSpecificDestination() throws Exception {
		System.out.println("Utils.checkServiceSpecificDestination");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONSERVICESPECIFICTYPE);

		destination.setDestination("test");
		Utils.checkDestination(destination);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkDestinationInvalid() throws Exception {
		System.out.println("Utils.checkDestinationInvalid");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONUSERHOSTPORTTYPE);

		destination.setDestination("test@192.168.1.1");
		Utils.checkDestination(destination);
	}

	@Test
	public void checkDestinationValid_hyphen() {
		System.out.println("Utils.checkDestinationValid_hyphen");
		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);
		
		destination.setDestination("https://dudo-du.dudo.du.do/perun/upload");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

  @Test
  public void checkDestinationValidTilde() {
		System.out.println("Utils.checkDestinationValidTilde");
		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/~name");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationValidParentheses() {
		System.out.println("Utils.checkDestinationValidParentheses");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/name_(my)");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("https://my.url/name_(my)_is");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationValidUnderscore() {
		System.out.println("Utils.checkDestinationValidUnderscore");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/blah_blah");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("https://my_name.url/bleh");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationValidQuestionMark() {
		System.out.println("Utils.checkDestinationValidQuestionMark");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/?name=john");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationValidAmpersand() {
		System.out.println("Utils.checkDestinationValidAmpersand");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/?name=john&surname=doe");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationValidPercentSign() {
		System.out.println("Utils.checkDestinationValidPercentSign");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/?name=john%20doe");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationValidIP() {
		System.out.println("Utils.checkDestinationValidIP");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://42.42.1.1/");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationValidIPPort() {
		System.out.println("Utils.checkDestinationValidIPPort");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://42.42.1.1:8080/");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationDomainNumbers() {
		System.out.println("Utils.checkDestinationDomainNumbers");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://2187.net");

		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationHashtag() {
		System.out.println("Utils.checkDestinationHashtag");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/name#page1");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationDash() {
		System.out.println("Utils.checkDestinationDash");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/my-name");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("https://my-url.com/");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationLogin() {
		System.out.println("Utils.checkDestinationLogin");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://myid@my.url:2187/");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("https://myid:mypassword@my.url/");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationProtocol() {
		System.out.println("Utils.checkDestinationProtocol");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("http://my.url/");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("ftp://my.url/");
		assertThatNoException().isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationInvalidProtocol() {
		System.out.println("Utils.checkDestinationInvalidProtocol");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("my.url");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("h://my.url");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("ftps://my.url/");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationInvalidBeginning() {
		System.out.println("Utils.checkDestinationInvalidBeginning");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("https://.");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("https://?");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkDestinationInvalidSpaces() {
		System.out.println("Utils.checkDestinationInvalidSpaces");

		Destination destination = new Destination();
		destination.setType(Destination.DESTINATIONURLTYPE);

		destination.setDestination("https://my.url/?name=john doe");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.checkDestination(destination));

		destination.setDestination("https:// my.url");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.checkDestination(destination));
	}

	@Test
	public void checkHostname() throws Exception {
		System.out.println("Utils.checkHostname");

		Host host = new Host();
		host.setHostname("192.168.1.1");
		Utils.checkHostname(host);

		host.setHostname("hostname.test");
		Utils.checkHostname(host);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkHostnameInvalid() throws Exception {
		System.out.println("Utils.checkHostnameInvalid");

		Host host = new Host();
		host.setHostname("invalid");
		Utils.checkHostname(host);
	}

	@Test
	public void validateSSHPublicKey() {
		System.out.println("Utils.validateSShKeyInvalid");
		String invalid1 = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCYZTcdI8iZFJ2c63iN0kMhpcEGuE054DCJh8gCBhyOKQn6LH3wBX/U6RERh+1UmWkblEnQM3B2vEnSGRgNfG7KQgi2xSMHlb4KO1wNB6mOwNV4a+rX115ncWxHwR+7UZPYEmafXX5WZWzT3mzvHWRLZvw87uD7FLWqFGAbEwdvinHIB4tLvCcnLSc+O9xmdZVMKbiuCIO/odhqmfUM4RD7htaBL/ZSFZn5fen5wo9xhTd2Z7fTOALPbRkG5uIWMo7TiiLNWlo9f1sao1zNmNxZrUpgbL7mUJwWz1Wor8hlOCIbjHYySFK8vz6ziqbOHh2/8DVEqAh/dEJMhVhY9rHUDjbfjOrCMswF9NWRO4Gmsn9ARHRwXN2Gq3bu6cJ6L7h5YuBH93+QtZZhYm34JfNNsZnCsaz4g0aTUwD4UtZ7kxqMMf0xE7ndc7y4wCI7+kHn/nPamtSCFT8Pgg8WfDF22S4ouZcRVS9eU1O8a/fn0dpL77wmY8rvCDyzX3VhUAHfp9YHYPB1rVRN/9tLR2wpwHHhDz758750bin/tkp7QHCJ+27vqLU3RX/ZTFjUeNX2HeHpfQEy5jyptSZgfmbnmljqOfVpDgyQ2Wvc+prN6iTjDmsaTZrY0AIQq9EUVYFLFXhqo2x3tYcC7bmlFfRE8Dl5klpfniRNOWLnMdvXcw";
		String invalid2 = "ssh-rsa 2048 10:e7:0a:ff:be:c3:c9:fb:2b:06:f3:07:ac:68:43:02";
		String validECDSA = "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBGgt1/rkRvQJp92tP8uxLJfy340lJGSSxsPp3+W1JdMbk+S2qIPwM5o/oblTjGhVRzKcas4pLrBz7L/Mxn6D6qw= martin@martin-ThinkPad-T480";
		String validED25519 = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIJhGU1cLG0UldPhYxbEjKcZmFSZsGznmAYvra2QPls7a martin@martin-ThinkPad-T480";
		String validRSA = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC7FPq20sXf+83P/mvfEBntaGUkVJu36X2gLIi5TioYPSqGVIPV+ztnhNUuJHQZ3HYRDhGw/5c32mIYKQvsAB0T/WT6hgs9zVHU1s5ieJSduxx9DqbEkHaZUirmukd8uF97QJm6Ve/cvS3YUb3yxWXcRiJX5jy1aRazoJgm/Vocgz/1PHInq46IQUN6I62ge7u5YrpSxym6Ehw8ZGCr7QyIyg5TdNVbK4flkf6LM/uKh0JuODfm+/R/3TjzbR/7oDzfkQR4TZE3sCHXpSEwaHbb4SM6if1di2PKefhlx9m7w0oMwaE6Epoq/US1FHxR0up+PQYqqwE+/fi9C88byT1Kjz7xpC3IV0bOdeP6nDcLDYsKssgotqU0YIrBCTes/an1efe1jrYZQvr54XvKNFWUnJsMJLosT2ZCWkNCyyrnL9V+KEJ07Qb4NAXfgcrVakP/6647FAXCgyY8Len9c/0aTn7SVd1aC3aTGRvLtvPNPzhbDJGKzjPs90So0GZ+q7s= martin@martin-ThinkPad-T480";

		assertThatExceptionOfType(SSHKeyNotValidException.class).isThrownBy(() -> Utils.validateSSHPublicKey(invalid1));

		assertThatExceptionOfType(SSHKeyNotValidException.class).isThrownBy(() -> Utils.validateSSHPublicKey(invalid2));

		assertThatNoException().isThrownBy(() -> Utils.validateSSHPublicKey(validECDSA));
		assertThatNoException().isThrownBy(() -> Utils.validateSSHPublicKey(validED25519));
		assertThatNoException().isThrownBy(() -> Utils.validateSSHPublicKey(validRSA));
	}

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);
		newVo.setId(returnedVo.getId());
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
