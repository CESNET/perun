package cz.metacentrum.perun.auditparser;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.TaskResult.TaskResultStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class AuditParserTest {

	private static final String NS_FACILITY_ATTR_DEF= "urn:perun:facility:attribute-def:def";
	private static final String NS_GROUP_RESOURCE_ATTR_DEF = "urn:perun:group_resource:attribute-def:def";

	private final String textMismatch = "!@#$%^<<&*()_+<\\><:{[}][]{>} sd";
	private final String CLASS_NAME = "AuditMessagesManagerEntry";
	private AuditMessage createdAuditMessage = new AuditMessage();
	private final User user = new User(5, textMismatch,textMismatch, textMismatch, textMismatch, textMismatch, false, false);
	private final ExtSource extSource = new ExtSource(9, textMismatch, textMismatch);
	private final UserExtSource userExtSource1 = new UserExtSource(12, extSource, textMismatch, user.getId(), 133);
	private final UserExtSource userExtSource2 = new UserExtSource(15, extSource, textMismatch, -1, 156);
	private final Vo vo = new Vo(15, textMismatch, textMismatch);
	private final Facility facility = new Facility(13, textMismatch);
	private final Resource resource = new Resource(19, textMismatch, textMismatch, facility.getId(), vo.getId());
	private final Group group = new Group(35, textMismatch, textMismatch);
	private Member member = new Member(13, user.getId(), vo.getId(), Status.VALID);
	private Candidate candidate;
	private final Destination destination = new Destination(32, textMismatch, textMismatch);
	private final Host host = new Host(32, textMismatch);
	private final Owner owner = new Owner(39, textMismatch, textMismatch, OwnerType.administrative);
	private final Owner owner1 = new Owner(12, null, textMismatch, null);
	private final Owner owner2 = new Owner(23, textMismatch, textMismatch, OwnerType.technical);
	private final Service service = new Service(29, textMismatch, null);
	private final AttributeDefinition attributeDefinition1 = new AttributeDefinition(getAttributeDefinition1());
	private final Attribute attribute1 = new Attribute(attributeDefinition1);
	private final AttributeDefinition attributeDefinition2 = new AttributeDefinition(getAttributeDefinition2());
	private final Attribute attribute2 = new Attribute(attributeDefinition2);
	private final AttributeDefinition attributeDefinition3 = new AttributeDefinition(getAttributeDefinition3());
	private final Attribute attribute3 = new Attribute(attributeDefinition3);
	private final AttributeDefinition attributeDefinition4 = new AttributeDefinition(getAttributeDefinition4());
	private final Attribute attribute4 = new Attribute(attributeDefinition4);
	private RichMember richMember;
	private final RichDestination richDestination = new RichDestination(destination, facility, service);
	private final RichResource richResource = new RichResource(resource);
	private RichUser richUser;
	private RichGroup richGroup;
	private RichFacility richFacility;
	private ResourceTag resourceTag1 = new ResourceTag(5, "cosi" , 2);
	private ResourceTag resourceTag2 = new ResourceTag(8, null, 5);
	private SecurityTeam securityTeam1 = new SecurityTeam(1, "jmeno", "popis");
	private SecurityTeam securityTeam2 = new SecurityTeam(2, null, null);
	private TaskResult taskResult1 = new TaskResult();
	private BanOnResource banOnResource1 = new BanOnResource(3, new Date(), "neco" , 10, 12);
	private BanOnResource banOnResource2 = new BanOnResource(4, null, null, 10, 12);
	private BanOnFacility banOnFacility1 = new BanOnFacility(5, new Date(), "neco", 10, 12);
	private BanOnFacility banOnFacility2 = new BanOnFacility(6, null, null, 10, 12);

	@Before
	public void setUp() throws Exception {
		member.setMembershipType(MembershipType.DIRECT);
		member.setSourceGroupId(5);
		facility.setDescription(textMismatch);
		Map<String, String> attributesMap = new HashMap<String, String>();
		attributesMap.put("test1", textMismatch);
		attributesMap.put("test", textMismatch);
		candidate = new Candidate(userExtSource1, attributesMap);
		attribute1.setValue(15);
		attribute2.setValue(textMismatch);
		attribute3.setValue(new ArrayList<String>(Arrays.asList(new String[] {"a", "b", "c"})));
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("a", "b");
		map.put("c", "d");
		attribute4.setValue(map);
		List<Attribute> listOfAttributes = new ArrayList<Attribute>();
		listOfAttributes.add(attribute2);
		listOfAttributes.add(attribute1);
		List<UserExtSource> userExtSources = new ArrayList<UserExtSource>();
		userExtSources.add(userExtSource1);
		userExtSources.add(userExtSource2);
		richMember = new RichMember(user, member, userExtSources, listOfAttributes, listOfAttributes);
		richMember.setSponsored(true);
		richUser = new RichUser(user, userExtSources, listOfAttributes);
		richGroup = new RichGroup(group, listOfAttributes);
		richResource.setFacility(facility);
		richResource.setVo(vo);
		richResource.addResourceTag(resourceTag1);
		List<Owner> owners = new ArrayList<Owner>();
		owners.add(owner);
		owners.add(owner1);
		owners.add(owner2);
		richFacility = new RichFacility(facility, owners);
		candidate.setAdditionalUserExtSources(userExtSources);
		taskResult1.setId(1);
		taskResult1.setDestinationId(2);
		taskResult1.setErrorMessage("error");
		taskResult1.setReturnCode(3);
		taskResult1.setService(service);
		taskResult1.setStandardMessage("nothing");
		taskResult1.setTaskId(10);
		taskResult1.setStatus(TaskResultStatus.DONE);
		taskResult1.setTimestamp(new Date());
	}

	@Test
	public void testParseLogOnExamples() throws Exception {
		System.out.println(CLASS_NAME + ":testParseLogOnExamples()");

		String log = "Hosts ["
			+ "Host:[id=<982>, hostname=<konos37.fav.zcu.cz>], "
			+ "Host:[id=<981>, hostname=<konos36.fav.zcu.cz>], "
			+ "Host:[id=<980>, hostname=<konos34.fav.zcu.cz>], "
			+ "Host:[id=<979>, hostname=<konos33.fav.zcu.cz>], "
			+ "Host:[id=<978>, hostname=<konos30.fav.zcu.cz>], "
			+ "Host:[id=<977>, hostname=<konos28.fav.zcu.cz>], "
			+ "Host:[id=<976>, hostname=<konos27.fav.zcu.cz>], "
			+ "Host:[id=<975>, hostname=<konos26.fav.zcu.cz>], "
			+ "Host:[id=<974>, hostname=<konos24.fav.zcu.cz>], "
			+ "Host:[id=<973>, hostname=<konos22.fav.zcu.cz>], "
			+ "Host:[id=<972>, hostname=<konos20.fav.zcu.cz>], "
			+ "Host:[id=<971>, hostname=<konos19.fav.zcu.cz>], "
			+ "Host:[id=<970>, hostname=<konos18.fav.zcu.cz>], "
			+ "Host:[id=<969>, hostname=<konos17.fav.zcu.cz>], "
			+ "Host:[id=<968>, hostname=<konos16.fav.zcu.cz>], "
			+ "Host:[id=<967>, hostname=<konos15.fav.zcu.cz>]] "
			+ "removed from cluster "
			+ "Facility:[id=<371>, name=<konos.fav.zcu.cz>, type=<cluster>]";

		String log2 = "RichMember:[id=<12521>, userId=<9181>, voId=<21>, status=<DISABLED>, sourceGroupId=<\\0>, sponsored=<true>, "
			+ "user=<User:[id=<9181>,titleBefore=<null>,firstName=<Gracian>,lastName=<Tejral>,middleName=<null>,titleAfter=<null>]>, "
			+ "userExtSources=<[UserExtSource:[id=<13621>, login=<8087>, source=<ExtSource:[id=<2>, name=<PERUNPEOPLE>, type=<cz.metacentrum.perun.core.impl.ExtSourceSql>]>, userId=<-1> loa=<0>]]>, "
			+ "userAttributes=<[Attribute:[id=<800>, friendlyName=<kerberosLogins>, namespace=<urn:perun:user:attribute-def:def>, type=<java.util.ArrayList>, value=<[tejral@META, tejral@EINFRA]>], "
			+ "Attribute:[id=<49>, friendlyName=<id>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.Integer>, value=<9181>], "
			+ "Attribute:[id=<50>, friendlyName=<firstName>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<Gracian>], "
			+ "Attribute:[id=<51>, friendlyName=<lastName>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<Tejral>], "
			+ "Attribute:[id=<52>, friendlyName=<middleName>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<null>], "
			+ "Attribute:[id=<53>, friendlyName=<titleBefore>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<null>], "
			+ "Attribute:[id=<54>, friendlyName=<titleAfter>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<null>], "
			+ "Attribute:[id=<221>, friendlyName=<uid-namespace:ruk>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.Integer>, value=<12762>], "
			+ "Attribute:[id=<222>, friendlyName=<uid-namespace:ics>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.Integer>, value=<62434>], "
			+ "Attribute:[id=<1140>, friendlyName=<displayName>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<Gracian Tejral>], "
			+ "Attribute:[id=<220>, friendlyName=<uid-namespace:zcu>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.Integer>, value=<62433>], "
			+ "Attribute:[id=<146>, friendlyName=<login-namespace:einfra>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.String>, value=<tejral>]]>, "
			+ "memberAttributes=<[Attribute:[id=<32>, friendlyName=<id>, namespace=<urn:perun:member:attribute-def:core>, type=<java.lang.Integer>, value=<12521>], "
			+ "Attribute:[id=<860>, friendlyName=<membershipExpiration>, namespace=<urn:perun:member:attribute-def:def>, type=<java.lang.String>, value=<2010-12-31>], "
			+ "Attribute:[id=<880>, friendlyName=<status>, namespace=<urn:perun:member:attribute-def:core>, type=<java.lang.String>, value=<DISABLED>], "
			+ "Attribute:[id=<60>, friendlyName=<mail>, namespace=<urn:perun:member:attribute-def:def>, type=<java.lang.String>, value=<gracian.tejral@centrum.cz>], "
			+ "Attribute:[id=<122>, friendlyName=<phone>, namespace=<urn:perun:member:attribute-def:def>, type=<java.lang.String>, value=<605469950>], "
			+ "Attribute:[id=<123>, friendlyName=<organization>, namespace=<urn:perun:member:attribute-def:def>, type=<java.lang.String>, value=<Univerzita Karlova>]]>] "
			+ "validated";

		String log3 = "Group synchronization Group:[id=<21>, parentGroupId=<35>, name=<members>, description=<Group containing VO members>, voId=<21>]: "
			+ "Member RichMember:[id=<11523>, userId=<3242>, voId=<21>, status=<DISABLED>, sourceGroupId=<\\0>, sponsored=<true>, user=<User:[id=<3242>,titleBefore=<null>,firstName=<Jiri>,lastName=<Novacek>,middleName=<null>,titleAfter=<null>]>, userExtSources=<[UserExtSource:[id=<6083>, login=<novej>, source=<ExtSource:[id=<3>, name=<META>, type=<cz.metacentrum.perun.core.impl.ExtSourceKerberos>]>, loa=<0>], UserExtSource:[id=<4534>, login=<16143>, source=<ExtSource:[id=<2>, name=<PERUNPEOPLE>, type=<cz.metacentrum.perun.core.impl.ExtSourceSql>]>, loa=<0>], UserExtSource:[id=<4916>, login=<novej>, source=<ExtSource:[id=<2>, name=<PERUNPEOPLE>, type=<cz.metacentrum.perun.core.impl.ExtSourceSql>]>, loa=<0>], UserExtSource:[id=<9542>, login=<151132@muni.cz>, source=<ExtSource:[id=<142>, name=<https://idp2.ics.muni.cz/idp/shibboleth>, type=<cz.metacentrum.perun.core.impl.ExtSourceIdp>]>, loa=<0>], UserExtSource:[id=<9543>, login=<151132>, source=<ExtSource:[id=<1>, name=<LDAPMU>, type=<cz.metacentrum.perun.core.impl.ExtSourceLdap>]>, loa=<0>]]>, userAttributes=<[Attribute:[id=<800>, friendlyName=<kerberosLogins>, namespace=<urn:perun:user:attribute-def:def>, type=<java.util.ArrayList>, value=<[novej@META, novej@EINFRA]>], Attribute:[id=<49>, friendlyName=<id>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.Integer>, value=<3242>], Attribute:[id=<50>, friendlyName=<firstName>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<Jiri>], Attribute:[id=<51>, friendlyName=<lastName>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<Novacek>], Attribute:[id=<52>, friendlyName=<middleName>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<null>], Attribute:[id=<53>, friendlyName=<titleBefore>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<null>], Attribute:[id=<54>, friendlyName=<titleAfter>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<null>], Attribute:[id=<221>, friendlyName=<uid-namespace:ruk>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.Integer>, value=<13191>], Attribute:[id=<222>, friendlyName=<uid-namespace:ics>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.Integer>, value=<62861>], Attribute:[id=<1140>, friendlyName=<displayName>, namespace=<urn:perun:user:attribute-def:core>, type=<java.lang.String>, value=<Jiri Novacek>], Attribute:[id=<202>, friendlyName=<perunPeopleId>, namespace=<urn:perun:user:attribute-def:opt>, type=<java.lang.String>, value=<16143>], Attribute:[id=<220>, friendlyName=<uid-namespace:zcu>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.Integer>, value=<62863>], Attribute:[id=<146>, friendlyName=<login-namespace:einfra>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.String>, value=<novej>], Attribute:[id=<440>, friendlyName=<userCertificates>, namespace=<urn:perun:user:attribute-def:def>, type=<java.util.LinkedHashMap>, value=<{/C=CZ/O=Masarykova univerzita/CN=Ji\\\\xC5\\\\x99\\\\xC3\\\\xAD Nov\\\\xC3\\\\xA1\\\\xC4\\\\x8Dek/unstructuredName=151132=-----BEGIN CERTIFICATE-----\nMIIElTCCA32gAwIBAgIQUAJAYK+ap+hI8GV/zSI4CjANBgkqhkiG9w0BAQUFADA7\n"
			+ "MQswCQYDVQQGEwJOTDEPMA0GA1UEChMGVEVSRU5BMRswGQYDVQQDExJURVJFTkEg\n"
			+ "UGVyc29uYWwgQ0EwHhcNMTIwMjAyMDAwMDAwWhcNMTUwMjAxMjM1OTU5WjBfMQsw\n"
			+ "CQYDVQQGEwJDWjEeMBwGA1UEChMVTWFzYXJ5a292YSB1bml2ZXJ6aXRhMRkwFwYD\n"
			+ "VQQDDBBKacWZw60gTm92w6HEjWVrMRUwEwYJKoZIhvcNAQkCFgYxNTExMzIwggEi\n"
			+ "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDcovQPyApqR3NLp0Ald8VpbQ2f\n"
			+ "k2QoxW/sKznL39QPcxkNo/0APU5bOMYWIezx9l1FYaZ6gNQwdiwuiNJLaCoCkMJU\n"
			+ "/A8xtCpfuZPU3VOYhtflOzNX3ilnKNN/rDkdTBPQZD1oJTxEKNsZ5nBQ5ni2OlRI\n"
			+ "8uVQYw0RGvgZwb6wxgVqgClAN3NI4M0PVzzqTVx/pdXN+R/ECHcrR5Jn+mRJwVP8\n"
			+ "uFfgkG5wEgom537rNHaDGBWPq5W1bd63ibM7F4toUgKZ7RIJZzZK/EWbS4g7dx42\n"
			+ "aZ4V+B+eEkrVsDJcXhCutfEDHfEjSvJ855EVxzeWo1TYmPnzo1eybBGg4Tb1AgMB\n"
			+ "AAGjggFvMIIBazAfBgNVHSMEGDAWgBRjTUNaGUg/xEbBArq/7g7lgrdmpjAdBgNV\n"
			+ "HQ4EFgQU5fe4bHQuhHC4WXM8JaHCLg3NXLkwDgYDVR0PAQH/BAQDAgWgMAwGA1Ud\n"
			+ "EwEB/wQCMAAwHQYDVR0lBBYwFAYIKwYBBQUHAwQGCCsGAQUFBwMCMBgGA1UdIAQR\n"
			+ "MA8wDQYLKwYBBAGyMQECAh0wPwYDVR0fBDgwNjA0oDKgMIYuaHR0cDovL2NybC50\n"
			+ "Y3MudGVyZW5hLm9yZy9URVJFTkFQZXJzb25hbENBLmNybDByBggrBgEFBQcBAQRm\n"
			+ "MGQwOgYIKwYBBQUHMAKGLmh0dHA6Ly9jcnQudGNzLnRlcmVuYS5vcmcvVEVSRU5B\n"
			+ "UGVyc29uYWxDQS5jcnQwJgYIKwYBBQUHMAGGGmh0dHA6Ly9vY3NwLnRjcy50ZXJl\n"
			+ "bmEub3JnMB0GA1UdEQQWMBSBEm5vdmVqQG1haWwubXVuaS5jejANBgkqhkiG9w0B\n"
			+ "AQUFAAOCAQEAO7XXTdWNc6Bm5tCzFVi3QR75hQoJJP4mkW5vNHe4z+XWPmGp4aS1\n"
			+ "ye3Co4oQTzF2zmeEsNVArbii9OTFmTZXakGsH/WhFG4trGqW3AbCL28FGoz8I4JW\n"
			+ "vkJMKmzZHvP/mdJFfQZVB5OaB5mSZVLt9kOvnb0aAYApLQZb8zbyQ4up96avOXyQ\n"
			+ "7zvGwLIn2O9S+yNPShe39lMVPqb5mkAgOdUA3KcqlNJQtwS+p5lZXzDuBzJZY+Gv\n"
			+ "/bJtJWtvSfVlRReDTfKW5qzJkFR/YqnGYU6R5Xq70zMdKtdjGgDkFiybJB9619lj\n"
			+ "rLwA+iL1DZr6jWGINA2ROsOwwSYqTRF7AQ==\n"
			+ "-----END CERTIFICATE-----\n"
			+ "}>], Attribute:[id=<441>, friendlyName=<userCertDNs>, namespace=<urn:perun:user:attribute-def:def>, type=<java.util.LinkedHashMap>, value=<{/C=CZ/O=Masarykova univerzita/CN=Ji\\\\xC5\\\\x99\\\\xC3\\\\xAD Nov\\\\xC3\\\\xA1\\\\xC4\\\\x8Dek/unstructuredName=151132=/C=NL/O=TERENA/CN=TERENA Personal CA}>]]>, memberAttributes=<[Attribute:[id=<32>, friendlyName=<id>, namespace=<urn:perun:member:attribute-def:core>, type=<java.lang.Integer>, value=<11523>], Attribute:[id=<860>, friendlyName=<membershipExpiration>, namespace=<urn:perun:member:attribute-def:def>, type=<java.lang.String>, value=<2012-01-31>], Attribute:[id=<880>, friendlyName=<status>, namespace=<urn:perun:member:attribute-def:core>, type=<java.lang.String>, value=<DISABLED>], Attribute:[id=<60>, friendlyName=<mail>, namespace=<urn:perun:member:attribute-def:def>, type=<java.lang.String>, value=<novej@ncbr.chemi.muni.cz>], Attribute:[id=<122>, friendlyName=<phone>, namespace=<urn:perun:member:attribute-def:def>, type=<java.lang.String>, value=<+420-549 492 674>], Attribute:[id=<123>, friendlyName=<organization>, namespace=<urn:perun:member:attribute-def:def>, type=<java.lang.String>, value=<Masarykova univerzita>]]>] removed.";

		String log4 = "Attribute:[id=<146>, friendlyName=<login-namespace:einfra>, namespace=<urn:perun:user:attribute-def:def>, type=<java.lang.String>, value=<tejral>]";

		String log5 = "Member:[id=<3899>, userId=<3199>, voId=<21>, status=<VALID>, sourceGroupId=<\\0>, sponsored=<true>] Cokoliv:[]";

		//Long start = System.currentTimeMillis();
		List<PerunBean> list = AuditParser.parseLog(log);
		//Long end = System.currentTimeMillis()-start;
		//System.out.println("Trvani 1 v case = " + end.toString());
		//start = System.currentTimeMillis();
		List<PerunBean> list2 = AuditParser.parseLog(log2);
		//end = System.currentTimeMillis()-start;
		//System.out.println("Trvani 2 v case = " + end.toString());
		//start = System.currentTimeMillis();
		List<PerunBean> list3 = AuditParser.parseLog(log3);
		//end = System.currentTimeMillis()-start;
		//System.out.println("Trvani 3 v case = " + end.toString());
		//start = System.currentTimeMillis();
		List<PerunBean> list4 = AuditParser.parseLog(log4);
		//end = System.currentTimeMillis()-start;
		assertEquals(17, list.size());
		assertEquals(1, list2.size());
		assertEquals(2, list3.size());
		assertEquals(1, list4.size());

		List<PerunBean> list5 = AuditParser.parseLog(log5);
	}

	@Test
	public void sameObjectBeforeAndAfterSerializing() throws Exception {
		System.out.println(CLASS_NAME + ":sameObjectBeforeAndAfterSerializing");

		//FOR USER
		User user = new User(8, null, textMismatch, null, textMismatch, null, true, true);

		List<PerunBean> userInList = AuditParser.parseLog(user.serializeToString());
		assertEquals(user.toString(), ((User) userInList.get(0)).toString());
		assertEquals(user.getFirstName(), ((User) userInList.get(0)).getFirstName());

		//FOR EXTSOURCE
		ExtSource extSource = new ExtSource(11, null, textMismatch);
		List<PerunBean> extSourceInList = AuditParser.parseLog(extSource.serializeToString());
		assertEquals(extSource.toString(), ((ExtSource) extSourceInList.get(0)).toString());
		assertEquals(extSource.getName(), ((ExtSource) extSourceInList.get(0)).getName());

		//FOR USEREXTSOURCE
		UserExtSource userExtSource1 = new UserExtSource(15, extSource, null , 8, 15);
		UserExtSource userExtSource2 = new UserExtSource(15, null, textMismatch, 8, 15);
		List<PerunBean> userExtSource1InList = AuditParser.parseLog(userExtSource1.serializeToString());
		List<PerunBean> userExtSource2InList = AuditParser.parseLog(userExtSource2.serializeToString());
		assertEquals(userExtSource1.toString(), ((UserExtSource) userExtSource1InList.get(0)).toString());
		assertEquals(userExtSource2.toString(), ((UserExtSource) userExtSource2InList.get(0)).toString());
		assertEquals(userExtSource1.getLogin(), ((UserExtSource) userExtSource1InList.get(0)).getLogin());
		assertEquals(userExtSource2.getExtSource(), ((UserExtSource) userExtSource2InList.get(0)).getExtSource());

		//FOR VO (VO MUST HAVE ALL ATTRIBUTE NOT NULL)
		Vo vo = new Vo(18, textMismatch, textMismatch);
		List<PerunBean> voInList = AuditParser.parseLog(vo.serializeToString());
		assertEquals(vo.toString(), ((Vo) voInList.get(0)).toString());
		assertEquals(vo.getName(), ((Vo) voInList.get(0)).getName());

		//FOR FACILITY
		Facility facility = new Facility(15, null);
		List<PerunBean> facilityInList = AuditParser.parseLog(facility.serializeToString());
		assertEquals(facility.toString(), ((Facility) facilityInList.get(0)).toString());
		assertEquals(facility.getName(), ((Facility) facility).getName());

		//FOR RESOURCE
		Resource resource = new Resource(15, textMismatch, null, 10, 10);
		List<PerunBean> resourceInList = AuditParser.parseLog(resource.serializeToString());
		assertEquals(resource.toString(), ((Resource) resourceInList.get(0)).toString());
		assertEquals(resource.getDescription(), ((Resource) resourceInList.get(0)).getDescription());

		//FOR GROUP
		Group group = new Group(textMismatch, null);
		group.setId(15);
		group.setParentGroupId(320);
		Group group2 = new Group(textMismatch, null);
		group2.setId(36);
		group2.setParentGroupId(null);
		List<PerunBean> groupInList = AuditParser.parseLog(group.serializeToString());
		List<PerunBean> groupInList2 = AuditParser.parseLog(group2.serializeToString());
		assertEquals(group.toString(), ((Group) groupInList.get(0)).toString());
		assertEquals(group.getDescription(), ((Group) groupInList.get(0)).getDescription());
		assertEquals(group2.toString(), ((Group) groupInList2.get(0)).toString());
		assertEquals(group2.getParentGroupId(), ((Group) groupInList2.get(0)).getParentGroupId());

		//FOR RESOURCE TAG
		List<PerunBean> resourceTagInList1 = AuditParser.parseLog(resourceTag1.serializeToString());
		List<PerunBean> resourceTagInList2 = AuditParser.parseLog(resourceTag2.serializeToString());
		assertEquals(resourceTag1.toString(), ((ResourceTag) resourceTagInList1.get(0)).toString());
		assertEquals(resourceTag1.getTagName(), ((ResourceTag) resourceTagInList1.get(0)).getTagName());
		assertEquals(resourceTag2.toString(), ((ResourceTag) resourceTagInList2.get(0)).toString());
		assertEquals(resourceTag2.getTagName(), ((ResourceTag) resourceTagInList2.get(0)).getTagName());

		//FOR MEMBER
		Member member = new Member(6, 8, 8, null);
		member.setSourceGroupId(null);
		List<PerunBean> memberInList = AuditParser.parseLog(member.serializeToString());
		assertEquals(member.toString(), ((Member) memberInList.get(0)).toString());
		assertEquals(member.getStatus(), ((Member) memberInList.get(0)).getStatus());

		//FOR DESTINATION
		Destination destination = new Destination(7, null, textMismatch);
		List<PerunBean> destinationInList = AuditParser.parseLog(destination.serializeToString());
		assertEquals(destination.toString(), ((Destination) destinationInList.get(0)).toString());
		assertEquals(destination.getDestination(), ((Destination) destinationInList.get(0)).getDestination());

		//FOR HOST
		Host host = new Host(5, null);
		List<PerunBean> hostInList = AuditParser.parseLog(host.serializeToString());
		assertEquals(host.toString(), ((Host) hostInList.get(0)).toString());
		assertEquals(host.getHostname(), ((Host) hostInList.get(0)).getHostname());

		//FOR OWNER
		Owner owner = new Owner(5, null, textMismatch, OwnerType.administrative);
		List<PerunBean> ownerInList = AuditParser.parseLog(owner.serializeToString());
		assertEquals(owner.toString(), ((Owner) ownerInList.get(0)).toString());
		assertEquals(owner.getName(), ((Owner) ownerInList.get(0)).getName());

		//FOR SERVICE
		Service service = new Service(8, null, null);
		List<PerunBean> serviceInList = AuditParser.parseLog(service.serializeToString());
		assertEquals(service.toString(), ((Service) serviceInList.get(0)).toString());
		assertEquals(service.getName(), ((Service) serviceInList.get(0)).getName());
		assertEquals(service.getDescription(), ((Service) serviceInList.get(0)).getDescription());
		assertEquals(service.getDelay(), ((Service) serviceInList.get(0)).getDelay());
		assertEquals(service.getRecurrence(), ((Service) serviceInList.get(0)).getRecurrence());
		assertEquals(service.isEnabled(), ((Service) serviceInList.get(0)).isEnabled());
		assertEquals(service.getScript(), ((Service) serviceInList.get(0)).getScript());

		//FOR ATTRIBUTE DEFINITION
		AttributeDefinition attributeDefinition1 = new AttributeDefinition(getAttributeDefinition1());
		AttributeDefinition attributeDefinition2 = new AttributeDefinition(getAttributeDefinition2());
		AttributeDefinition attributeDefinition3 = new AttributeDefinition(getAttributeDefinition3());
		AttributeDefinition attributeDefinition4 = new AttributeDefinition(getAttributeDefinition4());
		attributeDefinition1.setType(null);
		attributeDefinition1.setDescription(null);
		attributeDefinition1.setFriendlyName(null);
		attributeDefinition1.setNamespace(null);
		List<PerunBean> attributeDefinition1InList = AuditParser.parseLog(attributeDefinition1.serializeToString());
		assertEquals(attributeDefinition1.toString(), ((AttributeDefinition) attributeDefinition1InList.get(0)).toString());
		assertEquals(attributeDefinition1.getNamespace(), ((AttributeDefinition) attributeDefinition1InList.get(0)).getNamespace());

		//FOR ATTRIBUTE
		Attribute attribute1 = new Attribute(getAttributeDefinition1());
		Attribute attribute2 = new Attribute(getAttributeDefinition2());
		Attribute attribute3 = new Attribute(getAttributeDefinition3());
		Attribute attribute4 = new Attribute(getAttributeDefinition4());
		Attribute attribute5 = new Attribute(getAttributeDefinition3());
		Attribute attribute6 = new Attribute(getAttributeDefinition4());
		attribute5.setValue(null);
		attribute6.setValue(null);
		attribute1.setValue(null);
		attribute2.setValue(null);
		attribute3.setValue(new ArrayList<String>(Arrays.asList(new String[] {"a", null, null})));
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("a", null);
		map.put(null, "d");
		attribute4.setValue(map);
		List<PerunBean> attribute1InList = AuditParser.parseLog(attribute1.serializeToString());
		List<PerunBean> attribute2InList = AuditParser.parseLog(attribute2.serializeToString());
		List<PerunBean> attribute3InList = AuditParser.parseLog(attribute3.serializeToString());
		List<PerunBean> attribute4InList = AuditParser.parseLog(attribute4.serializeToString());
		List<PerunBean> attribute5InList = AuditParser.parseLog(attribute5.serializeToString());
		List<PerunBean> attribute6InList = AuditParser.parseLog(attribute6.serializeToString());
		assertEquals(attribute1.toString(), ((Attribute) attribute1InList.get(0)).toString());
		assertEquals(attribute2.toString(), ((Attribute) attribute2InList.get(0)).toString());
		assertEquals(attribute3.toString(), ((Attribute) attribute3InList.get(0)).toString());
		assertEquals(attribute4.toString(), ((Attribute) attribute4InList.get(0)).toString());
		assertEquals(attribute5.toString(), ((Attribute) attribute5InList.get(0)).toString());
		assertEquals(attribute6.toString(), ((Attribute) attribute6InList.get(0)).toString());
		assertEquals(attribute3.getValue(), ((Attribute) attribute3InList.get(0)).getValue());
		assertEquals(attribute4.getValue(), ((Attribute) attribute4InList.get(0)).getValue());
		assertEquals(attribute5.getValue(), ((Attribute) attribute5InList.get(0)).getValue());
		assertEquals(attribute6.getValue(), ((Attribute) attribute6InList.get(0)).getValue());

		//FOR CANDIDATE
		Map<String, String> attributesMap1 = new HashMap<String, String>();
		attributesMap1.put("test1", null);
		attributesMap1.put(null, null);
		Candidate candidate1 = new Candidate(userExtSource1, attributesMap1);
		Candidate candidate2 = new Candidate();
		candidate2.setUserExtSource(null);
		candidate2.setAttributes(null);
		candidate1.setId(5);
		candidate2.setId(6);
		candidate1.setAdditionalUserExtSources(null);
		List<UserExtSource> userExtSources = new ArrayList<UserExtSource>();
		userExtSources.add(userExtSource1);
		userExtSources.add(userExtSource2);
		candidate2.setAdditionalUserExtSources(userExtSources);
		List<PerunBean> candidate1InList = AuditParser.parseLog(candidate1.serializeToString());
		List<PerunBean> candidate2InList = AuditParser.parseLog(candidate2.serializeToString());
		assertEquals(candidate1.toString(), ((Candidate) candidate1InList.get(0)).toString());
		assertEquals(candidate2.toString(), ((Candidate) candidate2InList.get(0)).toString());
		assertEquals(candidate1.getAttributes(), ((Candidate) candidate1InList.get(0)).getAttributes());
		assertEquals(candidate2.getAttributes(), ((Candidate) candidate2InList.get(0)).getAttributes());

		//FOR SECURITY TEAM
		SecurityTeam securityTeam = new SecurityTeam(18, textMismatch, textMismatch);
		List<PerunBean> scsInList = AuditParser.parseLog(securityTeam.serializeToString());
		assertEquals(securityTeam.toString(), ((SecurityTeam) scsInList.get(0)).toString());
		assertEquals(securityTeam.getName(), ((SecurityTeam) scsInList.get(0)).getName());
		assertEquals(securityTeam.getDescription(), ((SecurityTeam) scsInList.get(0)).getDescription());

		//FOR TASK RESULT
		List<PerunBean> trList = AuditParser.parseLog(taskResult1.serializeToString());
		TaskResult taskResult2 = (TaskResult)trList.get(0);
		assertEquals(taskResult1.toString(), taskResult2.toString());


		//FOR BAN ON RESOURCE
		List<PerunBean> banOnResourceInList = AuditParser.parseLog(banOnResource1.serializeToString());
		assertEquals(banOnResource1.toString(), ((BanOnResource) banOnResourceInList.get(0)).toString());
		assertEquals(banOnResource1.getMemberId(), ((BanOnResource) banOnResourceInList.get(0)).getMemberId());
		assertEquals(banOnResource1.getResourceId(), ((BanOnResource) banOnResourceInList.get(0)).getResourceId());
		assertEquals(banOnResource1.getDescription(), ((BanOnResource) banOnResourceInList.get(0)).getDescription());
		assertEquals(banOnResource1.getValidityTo(), ((BanOnResource) banOnResourceInList.get(0)).getValidityTo());

		//FOR BAN ON FACILITY
		List<PerunBean> banOnFacilityInList = AuditParser.parseLog(banOnFacility1.serializeToString());
		assertEquals(banOnFacility1.toString(), ((BanOnFacility) banOnFacilityInList.get(0)).toString());
		assertEquals(banOnFacility1.getUserId(), ((BanOnFacility) banOnFacilityInList.get(0)).getUserId());
		assertEquals(banOnFacility1.getFacilityId(), ((BanOnFacility) banOnFacilityInList.get(0)).getFacilityId());
		assertEquals(banOnFacility1.getDescription(), ((BanOnFacility) banOnFacilityInList.get(0)).getDescription());
		assertEquals(banOnFacility1.getValidityTo(), ((BanOnFacility) banOnFacilityInList.get(0)).getValidityTo());

		//FOR RICHMEMBER
		RichMember richMember1 = new RichMember(null, member, null);
		//List<UserExtSource> userExtSources = new ArrayList<UserExtSource>();
		//userExtSources.add(userExtSource1);
		//userExtSources.add(null);
		//userExtSources.add(userExtSource2);
		//RichMember richMember2 = new RichMember(null, member, userExtSources);
		List<Attribute> listOfAttributes = new ArrayList<Attribute>();
		listOfAttributes.add(attribute1);
		listOfAttributes.add(attribute2);
		listOfAttributes.add(attribute3);
		listOfAttributes.add(attribute4);
		listOfAttributes.add(attribute5);
		//TODO: Same problem like with userExtSources, what about null between attributes? Not Supported Now!
		//listOfAttributes.add(null);
		listOfAttributes.add(attribute6);
		RichMember richMember3 = new RichMember(null, member, null, listOfAttributes, listOfAttributes);
		List<PerunBean> richMember1InList = AuditParser.parseLog(richMember1.serializeToString());

		//TODO: What about null pointers between userExtSources? Not Supported yet

		//List<PerunBean> richMember2InList = AuditParser.parseLog(richMember2.serializeToString());
		List<PerunBean> richMember3InList = AuditParser.parseLog(richMember3.serializeToString());
		assertEquals(richMember1.toString(), ((RichMember) richMember1InList.get(0)).toString());
		assertEquals(richMember1.isSponsored(), ((RichMember) richMember1InList.get(0)).isSponsored());
		//assertEquals(richMember2, ((RichMember) richMember2InList.get(0)));
		assertEquals(richMember3.toString(), ((RichMember) richMember3InList.get(0)).toString());
		assertEquals(richMember3.isSponsored(), ((RichMember) richMember3InList.get(0)).isSponsored());
		assertEquals(richMember1.getUser(), ((RichMember) richMember1InList.get(0)).getUser());
		assertEquals(richMember1.getUserExtSources(), ((RichMember) richMember1InList.get(0)).getUserExtSources());

		//FOR RICHUSER
		RichUser richUser1 = new RichUser(user, null, null);
		RichUser richUser2 = new RichUser(user, null, listOfAttributes);
		List<PerunBean> richUserInList = AuditParser.parseLog(richUser.serializeToString());
		List<PerunBean> richUser1InList = AuditParser.parseLog(richUser1.serializeToString());
		List<PerunBean> richUser2InList = AuditParser.parseLog(richUser2.serializeToString());
		assertEquals(richUser.toString(), ((RichUser) richUserInList.get(0)).toString());
		assertEquals(richUser1.toString(), ((RichUser) richUser1InList.get(0)).toString());
		assertEquals(richUser2.toString(), ((RichUser) richUser2InList.get(0)).toString());

		//FOR RICHGROUP
		RichGroup richGroup1 = new RichGroup(group, null);
		List<PerunBean> richGroupInList = AuditParser.parseLog(richGroup.serializeToString());
		List<PerunBean> richGroup1InList = AuditParser.parseLog(richGroup1.serializeToString());
		assertEquals(richGroup.toString(), ((RichGroup) richGroupInList.get(0)).toString());
		assertEquals(richGroup1.toString(), ((RichGroup) richGroup1InList.get(0)).toString());

		//FOR RICHFACILITY
		RichFacility richFacility1 = new RichFacility(facility, null);
		List<Owner> owners = new ArrayList<Owner>();
		owners.add(owner);
		owners.add(owner1);
		owners.add(owner2);
		RichFacility richFacility2 = new RichFacility(facility, owners);
		List<PerunBean> richFacility1InList = AuditParser.parseLog(richFacility1.serializeToString());
		List<PerunBean> richFacility2InList = AuditParser.parseLog(richFacility2.serializeToString());
		assertEquals(richFacility1.toString(), ((RichFacility) richFacility1InList.get(0)).toString());
		assertEquals(richFacility2.toString(), ((RichFacility) richFacility2InList.get(0)).toString());

		//FOR RICHRESOURCE
		RichResource richResource = new RichResource(resource);
		richResource.setFacility(null);
		richResource.setVo(null);
		richResource.addResourceTag(resourceTag1);
		List<PerunBean> richResourceInList = AuditParser.parseLog(richResource.serializeToString());
		assertEquals(richResource.toString(), ((RichResource) richResourceInList.get(0)).toString());
		assertEquals(richResource.getFacility(), ((RichResource) richResourceInList.get(0)).getFacility());

		//FOR RICHDESTINATION
		RichDestination richDestination = new RichDestination(destination, null, null);
		List<PerunBean> richDestinationInList = AuditParser.parseLog(richDestination.serializeToString());
		assertEquals(richDestination.toString(), ((RichDestination) richDestinationInList.get(0)).toString());
		assertEquals(richDestination.getFacility(), ((RichDestination) richDestinationInList.get(0)).getFacility());

		//FOR AUTHORSHIP
		Authorship authorship1 = new Authorship();
		authorship1.setId(1);
		authorship1.setPublicationId(3);
		authorship1.setUserId(18);
		authorship1.setCreatedBy(textMismatch);
		authorship1.setCreatedDate(new Date());
		authorship1.setCreatedByUid(10);
		Authorship authorship2 = new Authorship();
		authorship2.setId(1);
		authorship2.setPublicationId(3);
		authorship2.setUserId(18);
		authorship2.setCreatedBy(null);
		authorship2.setCreatedDate(null);
		authorship2.setCreatedByUid(0);
		List<PerunBean> authorship1InList = AuditParser.parseLog(authorship1.serializeToString());
		List<PerunBean> authorship2InList = AuditParser.parseLog(authorship2.serializeToString());
		assertEquals(authorship1.toString(), ((Authorship) authorship1InList.get(0)).toString());
		assertEquals(authorship2.toString(), ((Authorship) authorship2InList.get(0)).toString());

	}

	@Test
	public void serializeToStringEqualsToString() throws Exception {
		System.out.println(CLASS_NAME + ":serializeToStringEqualsToString()");
		assertEquals(user.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(user.serializeToString())));
		assertEquals(attribute1.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(attribute1.serializeToString())));
		Attribute testAttribute3 = new Attribute(attribute3);
		testAttribute3.setValue(BeansUtils.stringToAttributeValue(BeansUtils.attributeValueToString(attribute3), attribute3.getType()));
		assertEquals(attribute3.getValue(), testAttribute3.getValue());
		Attribute testAttribute4 = new Attribute(attribute4);
		testAttribute4.setValue(BeansUtils.stringToAttributeValue(BeansUtils.attributeValueToString(attribute4), attribute4.getType()));
		assertEquals(attribute4.getValue(), testAttribute4.getValue());
		assertEquals(attributeDefinition1.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(attributeDefinition1.serializeToString())));
		assertEquals(candidate.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(candidate.serializeToString())));
		assertEquals(destination.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(destination.serializeToString())));
		assertEquals(extSource.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(extSource.serializeToString())));
		assertEquals(facility.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(facility.serializeToString())));
		assertEquals(group.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(group.serializeToString())));
		assertEquals(host.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(host.serializeToString())));
		assertEquals(member.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(member.serializeToString())));
		assertEquals(owner.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(owner.serializeToString())));
		assertEquals(resource.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(resource.serializeToString())));
		assertEquals(richDestination.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(richDestination.serializeToString())));
		assertEquals(richMember.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(richMember.serializeToString())));
		assertEquals(richUser.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(richUser.serializeToString())));
		assertEquals(richGroup.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(richGroup.serializeToString())));
		assertEquals(richFacility.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(richFacility.serializeToString())));
		assertEquals(richResource.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(richResource.serializeToString())));
		assertEquals(service.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(service.serializeToString())));
		assertEquals(vo.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(vo.serializeToString())));
		assertEquals(userExtSource1.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(userExtSource1.serializeToString())));
		assertEquals(resourceTag1.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(resourceTag1.serializeToString())));
		assertEquals(securityTeam1.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(securityTeam1.serializeToString())));
		assertEquals(securityTeam2.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(securityTeam2.serializeToString())));
		assertEquals(taskResult1.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(taskResult1.serializeToString())));
		assertEquals(banOnResource1.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(banOnResource1.serializeToString())));
		assertEquals(banOnResource2.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(banOnResource2.serializeToString())));
		assertEquals(banOnFacility1.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(banOnFacility1.serializeToString())));
		assertEquals(banOnFacility2.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(banOnFacility2.serializeToString())));
		//test also some null serializing
		Resource newResource = new Resource(20, null, null, 5);
		assertEquals(newResource.toString(), BeansUtils.eraseEscaping(BeansUtils.replacePointyBracketsByApostrophe(newResource.serializeToString())));
	}

	@Test
	public void testParseLogCreatedBeans() throws Exception {
		System.out.println(CLASS_NAME + ":testParseLogCreatedBeans()");
		richMember.setMembershipType(MembershipType.INDIRECT);
		String bigLog = user.serializeToString() + extSource.serializeToString() + userExtSource1.serializeToString() +
			vo.serializeToString() + facility.serializeToString() +
			resource.serializeToString() + group.serializeToString() + member.serializeToString() +
			candidate.serializeToString() + destination.serializeToString() + host.serializeToString() +
			owner.serializeToString() + service.serializeToString() + attributeDefinition1.serializeToString() +
			attribute1.serializeToString() + richMember.serializeToString() + richDestination.serializeToString() +
			richResource.serializeToString() + richUser.serializeToString() + richGroup.serializeToString() +
			richFacility.serializeToString() + resourceTag1.serializeToString() +
			securityTeam1.serializeToString() + taskResult1.serializeToString() + banOnResource1.serializeToString() +
			banOnResource2.serializeToString() + banOnFacility1.serializeToString()+ banOnFacility2.serializeToString();

		List<PerunBean> perunBeans = new ArrayList<PerunBean>();
		perunBeans = AuditParser.parseLog(bigLog);
		assertEquals(28, perunBeans.size());

		assertTrue(perunBeans.contains(user));
		assertTrue(perunBeans.contains(attribute1));
		assertTrue(perunBeans.contains(attributeDefinition1));
		assertTrue(perunBeans.contains(candidate));
		assertTrue(perunBeans.contains(destination));
		assertTrue(perunBeans.contains(extSource));
		assertTrue(perunBeans.contains(facility));
		assertTrue(perunBeans.contains(group));
		assertTrue(perunBeans.contains(host));
		assertTrue(perunBeans.contains(member));
		assertTrue(perunBeans.contains(owner));
		assertTrue(perunBeans.contains(resource));
		assertTrue(perunBeans.contains(richDestination));
		assertTrue(perunBeans.contains(richMember));
		assertTrue(perunBeans.contains(richResource));
		assertTrue(perunBeans.contains(service));
		assertTrue(perunBeans.contains(vo));
		assertTrue(perunBeans.contains(userExtSource1));
		assertTrue(perunBeans.contains(richUser));
		assertTrue(perunBeans.contains(richGroup));
		assertTrue(perunBeans.contains(richFacility));
		assertTrue(perunBeans.contains(resourceTag1));
		assertTrue(perunBeans.contains(securityTeam1));
		assertTrue(perunBeans.contains(taskResult1));
	}

	private AttributeDefinition getAttributeDefinition1() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("isUnixGroup");
		attr.setType(Integer.class.getName());
		attr.setDescription("Does this group represents unix group on the resource?");
		return attr;
	}

	private AttributeDefinition getAttributeDefinition2() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("shell_passwd-scp");
		attr.setType(String.class.getName());
		attr.setDescription("Shell for passwd-scp service");
		return attr;
	}

	private AttributeDefinition getAttributeDefinition3() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("myTest1");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("");
		attr.setUnique(true);
		return attr;
	}

	private AttributeDefinition getAttributeDefinition4() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("myTest2");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("");
		return attr;
	}
}
