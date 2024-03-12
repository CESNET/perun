package cz.metacentrum.perun.core.impl.modules.attributes;

import static cz.metacentrum.perun.core.blImpl.VosManagerBlImpl.A_MEMBER_DEF_MEMBER_ORGANIZATIONS;
import static org.junit.Assert.assertEquals;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_member_attribute_def_virt_isLifecycleAlterableTest extends AbstractPerunIntegrationTest {

  private static Vo vo;
  private static Vo vo2;
  private static User user;
  private static Member member;
  private static Attribute attribute;
  private static AttributeDefinition attrDef;
  private static AttributeDefinition memberOrgsAttrDef;
  private static urn_perun_member_attribute_def_virt_isLifecycleAlterable classInstance;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_member_attribute_def_virt_isLifecycleAlterable();
    attrDef = classInstance.getAttributeDefinition();
    attribute = new Attribute(attrDef);

    vo = setUpVo(1);
    vo2 = setUpVo(2);
    user = setUpUser();
    memberOrgsAttrDef = setUpMemberOrgsAttrDef();
  }

  private Member setUpMember(Vo vo) throws Exception {
    return perun.getMembersManagerBl().createMember(sess, vo, user);
  }

  public AttributeDefinition setUpMemberOrgsAttrDef() throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
    attrDef.setDescription("Member organizations");
    attrDef.setFriendlyName("memberOrganizations");
    attrDef.setType(ArrayList.class.getName());

    return perun.getAttributesManagerBl().getAttributeDefinition(sess, A_MEMBER_DEF_MEMBER_ORGANIZATIONS);
  }

  private Attribute setUpMemberOrgsAttribute(ArrayList<String> voNames) {
    Attribute attribute = new Attribute(memberOrgsAttrDef);
    attribute.setValue(voNames);
    return attribute;
  }

  private User setUpUser() {
    User newUser = new User(1, "Jo", "Do", "", "", "");
    return perun.getUsersManagerBl().createUser(sess, newUser);
  }

  private Vo setUpVo(int id) throws Exception {
    Vo newVo = new Vo(id, "TestVo" + id, "TestVo" + id);
    return perun.getVosManagerBl().createVo(sess, newVo);
  }

  @Test
  public void testGetAttributeValueExpiredMemberOfHierarchicalMemberVo() throws Exception {
    System.out.println("testGetAttributeValue() - expiredMemberOfHierarchicalMemberVo");

    ArrayList<String> memberOrganizations = new ArrayList<>(List.of());
    member = setUpMember(vo);
    Attribute orgAttribute = setUpMemberOrgsAttribute(memberOrganizations);
    perun.getAttributesManagerBl().setAttribute(sess, member, orgAttribute);

    attribute.setValue(true);
    Attribute testAttr = classInstance.getAttributeValue((PerunSessionImpl) sess, member, attrDef);
    assertEquals(attribute, testAttr);
  }

  @Test
  public void testGetAttributeValueMemberOfHierarchicalMemberVo() throws Exception {
    System.out.println("testGetAttributeValue() - memberOfHierarchicalMemberVo");

    ArrayList<String> memberOrganizations = new ArrayList<>(List.of(vo2.getShortName()));
    member = setUpMember(vo);
    Attribute orgAttribute = setUpMemberOrgsAttribute(memberOrganizations);
    perun.getAttributesManagerBl().setAttribute(sess, member, orgAttribute);

    attribute.setValue(false);
    Attribute testAttr = classInstance.getAttributeValue((PerunSessionImpl) sess, member, attrDef);
    assertEquals(attribute, testAttr);
  }

  @Test
  public void testGetAttributeValueMemberOfHierarchicalParentVo() throws Exception {
    System.out.println("testGetAttributeValue() - memberOfHierarchicalParentVo");

    ArrayList<String> memberOrganizations = new ArrayList<>(List.of(vo.getShortName()));
    member = setUpMember(vo);
    Attribute orgAttribute = setUpMemberOrgsAttribute(memberOrganizations);
    perun.getAttributesManagerBl().setAttribute(sess, member, orgAttribute);

    attribute.setValue(true);
    Attribute testAttr = classInstance.getAttributeValue((PerunSessionImpl) sess, member, attrDef);
    assertEquals(attribute, testAttr);
  }

  @Test
  public void testGetAttributeValueNotMemberOfHierarchicalVo() throws Exception {
    System.out.println("testGetAttributeValue() - notMemberOfHierarchicalVo");

    member = setUpMember(vo);
    attribute.setValue(true);
    Attribute testAttr = classInstance.getAttributeValue((PerunSessionImpl) sess, member, attrDef);
    assertEquals(attribute, testAttr);
  }
}
