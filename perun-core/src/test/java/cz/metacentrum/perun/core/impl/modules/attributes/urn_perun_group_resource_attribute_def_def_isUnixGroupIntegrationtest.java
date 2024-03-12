package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_group_resource_attribute_def_def_isUnixGroupIntegrationtest
    extends AbstractPerunIntegrationTest {

  private static final String A_GR_unixGroupName = AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT + ":unixGroupName";
  private static final String A_GR_unixGID = AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT + ":unixGID";
  private static final String A_GR_isUnixGroup = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":isUnixGroup";
  final String extSourceName = "LDAPMETA";        // real ext source with his login
  final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
  final UserExtSource userExtSource = new UserExtSource();   // create new User Ext Source
  String userFirstName = "";
  String userLastName = "";
  String extLogin = "";              // his login in external source
  String extLogin2 = "";
  private User user;                             // our User
  private UsersManager usersManager;
  private Attribute groupGID;
  private Attribute groupGroupName;
  private Attribute groupIsUnixGroup;
  private Group group;
  private Resource resource;
  private Facility facility;
  private Vo vo;
  private Member member;

  @Ignore
  @Test
  public void automaticGenerationOfGID() throws Exception {
    System.out.println("IsUnixGroup.automaticGenerationOfGID");
    groupGID.setValue(null);
    groupGroupName.setValue("Test");
    perun.getAttributesManagerBl().setAttribute(sess, resource, group, groupGID);
    perun.getAttributesManagerBl().setAttribute(sess, resource, group, groupGroupName);
    groupIsUnixGroup.setValue(1);
    perun.getAttributesManagerBl().setAttribute(sess, resource, group, groupIsUnixGroup);
    assertNotNull(perun.getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_unixGID).getValue());

  }

  @Ignore
  @Test(expected = WrongReferenceAttributeValueException.class)
  public void notAbleToSetIfNotHaveGroupName() throws Exception {
    System.out.println("IsUnixGroup.notAbleToSetIfNotHaveGroupName");
    groupGID.setValue(null);
    groupGroupName.setValue(null);
    perun.getAttributesManagerBl().setAttribute(sess, resource, group, groupGID);
    perun.getAttributesManagerBl().setAttribute(sess, resource, group, groupGroupName);
    groupIsUnixGroup.setValue(1);
    perun.getAttributesManagerBl().setAttribute(sess, resource, group, groupIsUnixGroup);
  }

  @Before
  public void setUp() throws Exception {


    usersManager = perun.getUsersManager();
    // set random name and logins during every setUp method
    userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));
    extLogin2 = Long.toHexString(Double.doubleToLongBits(Math.random()));
    setUpUser();
    setUpUserExtSource();
    this.vo = setUpVo();
    this.facility = setUpFacility();
    this.member = setUpMember(vo);
    this.group = setUpGroup(vo, member);
    this.resource = setUpResource(vo, facility);
    setUpAttributes(resource, group);
  }

  // --PRIVATE METHODS ---------------------------------------------------
  private void setUpAttributes(Resource resource, Group group) throws Exception {
    groupGroupName = perun.getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_unixGroupName);
    groupGID = perun.getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_unixGID);
    groupIsUnixGroup = perun.getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_isUnixGroup);
  }

  private Candidate setUpCandidate() {

    Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
    candidate.setFirstName(userFirstName);
    candidate.setId(0);
    candidate.setMiddleName("");
    candidate.setLastName(userLastName);
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());
    return candidate;

  }

  private Facility setUpFacility() throws Exception {

    Facility facility = new Facility();
    facility.setName("IsUnixGroupTestFacility");
    facility = perun.getFacilitiesManager().createFacility(sess, facility);

    return facility;

  }

  private Group setUpGroup(Vo vo, Member member) throws Exception {

    Group group = new Group("IsUnixGroupTestGroup", "");
    group = perun.getGroupsManager().createGroup(sess, vo, group);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    perun.getGroupsManager().addMember(sess, group, member);
    perun.getGroupsManager().addAdmin(sess, group, user);
    return group;

  }

  private Member setUpMember(Vo vo) throws Exception {

    Candidate candidate = setUpCandidate();
    Member member = perun.getMembersManager().createMember(sess, vo, candidate); // candidates.get(0)
    // set first candidate as member of test VO
    assertNotNull("No member created", member);
    usersForDeletion.add(usersManager.getUserByMember(sess, member));
    // save user for deletion after test
    return member;

  }

  private Resource setUpResource(Vo vo, Facility facility) throws Exception {

    Resource resource = new Resource();
    resource.setName("IsUnixGroupTestResource");
    resource.setDescription("Testovaci");
    resource = perun.getResourcesManagerBl().createResource(sess, resource, vo, facility);
    return resource;

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

  private void setUpUserExtSource() throws Exception {

    ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
    // gets real external source object from database
    userExtSource.setExtSource(externalSource);
    // put real external source into user's external source
    userExtSource.setLogin(extLogin);
    // set users login in his ext source
    assertNotNull(usersManager.addUserExtSource(sess, user, userExtSource));
    // create new user ext source in database

  }

  private Vo setUpVo() throws Exception {

    Vo newVo = new Vo(0, "IsUnixGroupTestVo", "IUGTestVo");
    Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
    // create test VO in database
    assertNotNull("unable to create testing Vo", returnedVo);
    newVo.setId(returnedVo.getId());
    assertEquals("both VOs should be the same", newVo, returnedVo);

    ExtSource es = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
    // get real external source from DB
    perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
    // add real ext source to our VO

    return returnedVo;

  }
}
