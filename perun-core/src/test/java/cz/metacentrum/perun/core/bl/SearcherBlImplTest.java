package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({@ContextConfiguration(locations = {"classpath:perun-base.xml", "classpath:perun-core.xml"})})
@Transactional(transactionManager = "springTransactionManager")
public class SearcherBlImplTest {

  @Autowired
  private PerunBl perun;

  private PerunSession sess;
  private Vo vo1;
  private Group group1InVo1;
  private Group group2InVo1;

  private Vo vo2;
  private Group group1InVo2;
  private Group group2InVo2;

  @Test
  public void getGroupsByAttribute() throws Exception {
    System.out.println("SearcherBlImplTest.getGroupsByAttribute");

    Attribute attr1 = setUpGroupAttribute(1, "stringAttribute", String.class.getName(), "hodnota");
    perun.getAttributesManagerBl().createAttribute(sess, attr1);
    perun.getAttributesManagerBl().setAttribute(sess, group1InVo1, attr1);
    perun.getAttributesManagerBl().setAttribute(sess, group2InVo2, attr1);

    Attribute attr2 =
        setUpGroupAttribute(2, "arrayAttribute", ArrayList.class.getName(), new ArrayList<>(Arrays.asList("hodnota")));
    perun.getAttributesManagerBl().createAttribute(sess, attr2);
    perun.getAttributesManagerBl().setAttribute(sess, group2InVo1, attr2);

    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(attr1.getName(), attr1.valueAsString());

    List<Group> returnedGroups = perun.getSearcherBl().getGroups(sess, attributesWithSearchingValues);

    Assert.assertEquals(2, returnedGroups.size());
    Assert.assertTrue(returnedGroups.contains(group1InVo1));
    Assert.assertTrue(returnedGroups.contains(group2InVo2));
  }

  @Test
  public void getGroupsByAttributes() throws Exception {
    System.out.println("SearcherBlImplTest.getGroupsByAttributes");

    String value1 = "hodnota";

    Attribute attr1 = setUpGroupAttribute(1, "stringAttribute", String.class.getName(), value1);
    perun.getAttributesManagerBl().createAttribute(sess, attr1);
    perun.getAttributesManagerBl().setAttribute(sess, group1InVo1, attr1);
    perun.getAttributesManagerBl().setAttribute(sess, group1InVo2, attr1);
    perun.getAttributesManagerBl().setAttribute(sess, group2InVo1, attr1);

    Attribute attr2 =
        setUpGroupAttribute(2, "arrayAttribute", ArrayList.class.getName(), new ArrayList<>(Arrays.asList(value1)));
    perun.getAttributesManagerBl().createAttribute(sess, attr2);
    perun.getAttributesManagerBl().setAttribute(sess, group1InVo2, attr2);
    perun.getAttributesManagerBl().setAttribute(sess, group2InVo1, attr2);
    perun.getAttributesManagerBl().setAttribute(sess, group2InVo2, attr2);

    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(attr1.getName(), value1);
    attributesWithSearchingValues.put(attr2.getName(), value1);

    List<Group> returnedGroups = perun.getSearcherBl().getGroups(sess, attributesWithSearchingValues);

    Assert.assertEquals(2, returnedGroups.size());
    Assert.assertTrue(returnedGroups.contains(group1InVo2));
    Assert.assertTrue(returnedGroups.contains(group2InVo1));
  }

  @Test
  public void getGroupsByVoAndAttribute() throws Exception {
    System.out.println("SearcherBlImplTest.getGroupsByVoAndAttribute");

    Attribute attr1 = setUpGroupAttribute(1, "stringAttribute", String.class.getName(), "hodnota");
    perun.getAttributesManagerBl().createAttribute(sess, attr1);
    perun.getAttributesManagerBl().setAttribute(sess, group1InVo1, attr1);
    perun.getAttributesManagerBl().setAttribute(sess, group2InVo2, attr1);

    Attribute attr2 =
        setUpGroupAttribute(2, "arrayAttribute", ArrayList.class.getName(), new ArrayList<>(Arrays.asList("hodnota")));
    perun.getAttributesManagerBl().createAttribute(sess, attr2);
    perun.getAttributesManagerBl().setAttribute(sess, group2InVo1, attr2);

    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(attr1.getName(), attr1.valueAsString());

    List<Group> returnedGroups = perun.getSearcherBl().getGroups(sess, vo1, attributesWithSearchingValues);

    Assert.assertEquals(1, returnedGroups.size());
    Assert.assertTrue(returnedGroups.contains(group1InVo1));
  }

  @Test
  public void getMembersByLastExtension() throws Exception {
    System.out.println("SearcherBlImplTest.getMembersByLastExtension");

    Member member1 = createMember("searcherTestLE1");
    Member member2 = createMember("searcherTestLE2");
    // a member of another VO, must never be returned
    Member memberInOtherVo = createMemberInVo(vo2, "searcherTestLE3");
    perun.getGroupsManagerBl().addMember(sess, group1InVo1, member1);
    perun.getGroupsManagerBl().addMember(sess, group1InVo1, member2);
    perun.getGroupsManagerBl().addMember(sess, group1InVo2, memberInOtherVo);

    String lastExtensionUrn = AttributesManager.NS_MEMBER_ATTR_DEF + ":lastExtension";
    LocalDate date = LocalDate.now();

    Attribute lastExtension1 = perun.getAttributesManagerBl().getAttribute(sess, member1, lastExtensionUrn);
    lastExtension1.setValue(date.toString());
    perun.getAttributesManagerBl().setAttribute(sess, member1, lastExtension1);

    Attribute lastExtension2 = perun.getAttributesManagerBl().getAttribute(sess, member2, lastExtensionUrn);
    lastExtension2.setValue(date.minusDays(1).toString());
    perun.getAttributesManagerBl().setAttribute(sess, member2, lastExtension2);

    // exact match, only member1 was extended on the given date
    List<Member> returnedMembers = perun.getSearcherBl().getMembersByLastExtension(sess, vo1, "=", date);
    Assert.assertEquals(1, returnedMembers.size());
    Assert.assertEquals(member1.getId(), returnedMembers.get(0).getId());

    // both members were extended on or before the given date
    returnedMembers = perun.getSearcherBl().getMembersByLastExtension(sess, vo1, "<=", date);
    Assert.assertEquals(2, returnedMembers.size());
    Assert.assertTrue(returnedMembers.containsAll(Arrays.asList(member1, member2)));

    // no member was extended on or before this date
    returnedMembers = perun.getSearcherBl().getMembersByLastExtension(sess, vo1, "<=", date.minusDays(2));
    Assert.assertTrue(returnedMembers.isEmpty());
  }

  @Test
  public void getMembersByGroupLastExtension() throws Exception {
    System.out.println("SearcherBlImplTest.getMembersByGroupLastExtension");

    Member member1 = createMember("searcherTestGLE1");
    Member member2 = createMember("searcherTestGLE2");
    // a member of another group in the same VO, must not be returned
    Member memberInOtherGroup = createMember("searcherTestGLE3");
    perun.getGroupsManagerBl().addMember(sess, group1InVo1, member1);
    perun.getGroupsManagerBl().addMember(sess, group1InVo1, member2);
    perun.getGroupsManagerBl().addMember(sess, group2InVo1, memberInOtherGroup);

    String lastExtensionUrn = AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":lastExtension";
    LocalDate date = LocalDate.now();

    Attribute lastExtension1 = perun.getAttributesManagerBl()
        .getAttribute(sess, member1, group1InVo1, lastExtensionUrn);
    lastExtension1.setValue(date.toString());
    perun.getAttributesManagerBl().setAttribute(sess, member1, group1InVo1, lastExtension1);

    Attribute lastExtension2 = perun.getAttributesManagerBl()
        .getAttribute(sess, member2, group1InVo1, lastExtensionUrn);
    lastExtension2.setValue(date.minusDays(1).toString());
    perun.getAttributesManagerBl().setAttribute(sess, member2, group1InVo1, lastExtension2);

    // exact match, only member1 was extended in the group on the given date
    List<Member> returnedMembers =
        perun.getSearcherBl().getMembersByGroupLastExtension(sess, group1InVo1, "=", date);
    Assert.assertEquals(1, returnedMembers.size());
    Assert.assertEquals(member1.getId(), returnedMembers.get(0).getId());

    // both members were extended in the group on or before the given date
    returnedMembers = perun.getSearcherBl().getMembersByGroupLastExtension(sess, group1InVo1, "<=", date);
    Assert.assertEquals(2, returnedMembers.size());
    Assert.assertTrue(returnedMembers.containsAll(Arrays.asList(member1, member2)));
  }

  @Before
  public void setUp() throws Exception {

    sess = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());

    // Create the ext source in the transaction of each test, the in-memory cache of ext sources
    // survives rolled-back test transactions and a stale entry would break member creation.
    perun.getExtSourcesManagerBl().createExtSource(
        sess, new ExtSource("SearcherBlImplTestExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL), null);

    // first branch

    vo1 = new Vo(0, "GroupsManagerBlImplTestVo", "GrpMgrBlImplTestVo");
    vo1 = perun.getVosManagerBl().createVo(sess, vo1);

    group1InVo1 = new Group("testGroup11", "testGroup");
    group1InVo1 = perun.getGroupsManagerBl().createGroup(sess, vo1, group1InVo1);

    group2InVo1 = new Group("testGroup12", "testGroup");
    group2InVo1 = perun.getGroupsManagerBl().createGroup(sess, vo1, group2InVo1);

    // second branch

    vo2 = new Vo(0, "FacilitiesManagerBlImplTestVo2", "FacMgrBlImplTestVo2");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    group1InVo2 = new Group("testGroup21", "testGroup");
    group1InVo2 = perun.getGroupsManagerBl().createGroup(sess, vo2, group1InVo2);

    group2InVo2 = new Group("testGroup22", "testGroup");
    group2InVo2 = perun.getGroupsManagerBl().createGroup(sess, vo2, group2InVo2);

  }

  private Attribute setUpGroupAttribute(int id, String friendlyName, String type, Object value) {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setId(id);
    attrDef.setFriendlyName(friendlyName);
    attrDef.setDisplayName("");
    attrDef.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attrDef.setDisplayName("");
    attrDef.setType(type);
    Attribute attr = new Attribute(attrDef);
    attr.setValue(value);
    return attr;
  }

  private Member createMember(String lastName) throws Exception {
    return createMemberInVo(vo1, lastName);
  }

  private Member createMemberInVo(Vo vo, String lastName) throws Exception {
    Candidate candidate = new Candidate();
    candidate.setFirstName("some");
    candidate.setMiddleName("");
    candidate.setLastName(lastName);
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    candidate.setUserExtSource(new UserExtSource(
        new ExtSource(0, "SearcherBlImplTestExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL),
        "extLogin" + lastName));
    candidate.setAttributes(new HashMap<>());
    return perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
  }
}
