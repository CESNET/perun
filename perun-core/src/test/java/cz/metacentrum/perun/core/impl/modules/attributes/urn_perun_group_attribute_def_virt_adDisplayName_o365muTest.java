package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_group_attribute_def_virt_adDisplayName_o365muTest {

  private final urn_perun_group_attribute_def_virt_adDisplayName_o365mu classInstance =
      new urn_perun_group_attribute_def_virt_adDisplayName_o365mu();
  private final AttributeDefinition adDisplayNameO365MuAttrDef = classInstance.getAttributeDefinition();

  private final Group groupA = setUpGroup(1, null, "groupA", "A");
  private final Group groupB = setUpGroup(2, 1, "groupB", "B");
  private final Group groupC = setUpGroup(3, 2, "groupC", "C");
  private final Group groupD = setUpGroup(4, 3, "groupD", "D");
  private final Group groupE = setUpGroup(5, 4, "groupE", "E");
  private final Group groupF = setUpGroup(6, 1, "groupF", "F");
  private final Group groupWithoutAttributes = setUpGroup(7, null, "groupWithoutAttributes", "groupWithoutAttributes");

  private final AttributeDefinition defaultAdDisplayNameAttrDef =
      setUpGroupAttributeDefinition(1, "adDisplayName:o365mu", String.class.getName());
  private final AttributeDefinition inetCisprAttrDef =
      setUpGroupAttributeDefinition(2, "inetCispr", String.class.getName());
  private final AttributeDefinition inetGroupNameCSAttrDef =
      setUpGroupAttributeDefinition(3, "inetGroupNameCS", String.class.getName());
  private final AttributeDefinition inetGroupNameAbbENAttrDef =
      setUpGroupAttributeDefinition(4, "inetGroupNameAbbEN", String.class.getName());
  private final AttributeDefinition inetWorkplacesTypeCSAttrDef =
      setUpGroupAttributeDefinition(5, "inetWorkplacesTypeCS", String.class.getName());

  private final String typeOfWorkplacesA = "typeA";
  private final String typeOfWorkplacesB = "typeB";
  private final String typeOfWorkplacesC = "typeC";
  private final String predefinedDisplayName = "specialTestingPredefinedDisplayName";

  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    //prepare mocks
    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    AttributesManagerBl am = mock(AttributesManagerBl.class);
    GroupsManagerBl gm = mock(GroupsManagerBl.class);

    when(sess.getPerunBl())
        .thenReturn(perunBl);
    when(perunBl.getAttributesManagerBl())
        .thenReturn(am);
    when(perunBl.getGroupsManagerBl())
        .thenReturn(gm);

    when(sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, groupB))
        .thenReturn(groupA);
    when(sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, groupC))
        .thenReturn(groupB);
    when(sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, groupD))
        .thenReturn(groupC);
    when(sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, groupE))
        .thenReturn(groupD);
    when(sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, groupF))
        .thenReturn(groupA);

    //group A is the main group with cispr 000000, it has no default display name
    setWhenClauseByParametersForGroup(sess, groupA, null, "000000", groupA.getName(), groupA.getDescription(),
        typeOfWorkplacesA);
    //group B is special group which should return null on display name
    setWhenClauseByParametersForGroup(sess, groupB, null, "000001", groupB.getName(), groupB.getDescription(),
        typeOfWorkplacesA);
    //group C is other part of university, it has no default display name
    setWhenClauseByParametersForGroup(sess, groupC, null, "110000", groupC.getName(), groupC.getDescription(),
        typeOfWorkplacesB);
    //group D is special group with predefined display name
    setWhenClauseByParametersForGroup(sess, groupD, predefinedDisplayName, null, null, null, null);
    //group E is other type of group, it has no default display name
    setWhenClauseByParametersForGroup(sess, groupE, null, "123456", groupE.getName(), groupE.getDescription(),
        typeOfWorkplacesC);
    //group E is other type of group, it has no default display name, but displayName is missing
    setWhenClauseByParametersForGroup(sess, groupF, null, "654321", groupF.getName(), groupF.getDescription(),
        typeOfWorkplacesB);

    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, groupWithoutAttributes, defaultAdDisplayNameAttrDef.getName()))
        .thenThrow(AttributeNotExistsException.class);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, groupWithoutAttributes, inetCisprAttrDef.getName()))
        .thenThrow(AttributeNotExistsException.class);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, groupWithoutAttributes, inetWorkplacesTypeCSAttrDef.getName()))
        .thenThrow(AttributeNotExistsException.class);
  }

  private void setWhenClauseByParametersForGroup(PerunSessionImpl sess, Group group, String valueOfDisplayName,
                                                 String valueOfCispr, String valueOfNameInCS, String valueOfAbbInEN,
                                                 String valueOfType) throws Exception {
    Attribute adDisplayName = new Attribute(defaultAdDisplayNameAttrDef);
    Attribute inetCispr = new Attribute(inetCisprAttrDef);
    Attribute inetGroupNameCS = new Attribute(inetGroupNameCSAttrDef);
    Attribute inetGroupNameAbbEN = new Attribute(inetGroupNameAbbENAttrDef);
    Attribute inetWorkplacesType = new Attribute(inetWorkplacesTypeCSAttrDef);

    adDisplayName.setValue(valueOfDisplayName);
    inetCispr.setValue(valueOfCispr);
    inetGroupNameCS.setValue(valueOfNameInCS);
    inetGroupNameAbbEN.setValue(valueOfAbbInEN);
    inetWorkplacesType.setValue(valueOfType);

    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, defaultAdDisplayNameAttrDef.getName()))
        .thenReturn(adDisplayName);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, inetCisprAttrDef.getName()))
        .thenReturn(inetCispr);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, inetGroupNameCSAttrDef.getName()))
        .thenReturn(inetGroupNameCS);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, inetGroupNameAbbENAttrDef.getName()))
        .thenReturn(inetGroupNameAbbEN);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, inetWorkplacesTypeCSAttrDef.getName()))
        .thenReturn(inetWorkplacesType);
  }

  private Group setUpGroup(int id, Integer parentGroupId, String name, String description) {
    Group group = new Group(name, description);
    group.setId(id);
    group.setParentGroupId(parentGroupId);
    return group;
  }

  private AttributeDefinition setUpGroupAttributeDefinition(int id, String friendlyName, String type) {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setId(id);
    attrDef.setFriendlyName(friendlyName);
    attrDef.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attrDef.setType(type);
    return attrDef;
  }

  @Test
  public void getAttributeValueForGroupA() {
    String attributeValue = classInstance.getAttributeValue(sess, groupA, adDisplayNameO365MuAttrDef).valueAsString();
    assertNotNull(attributeValue);
    assertEquals("MUNI, typeA", attributeValue);
  }

  @Test
  public void getAttributeValueForGroupB() {
    String attributeValue = classInstance.getAttributeValue(sess, groupB, adDisplayNameO365MuAttrDef).valueAsString();
    assertNull(attributeValue);
  }

  @Test
  public void getAttributeValueForGroupC() {
    String attributeValue = classInstance.getAttributeValue(sess, groupC, adDisplayNameO365MuAttrDef).valueAsString();
    assertNotNull(attributeValue);
    assertEquals(groupC.getName() + ", " + typeOfWorkplacesB, attributeValue);
  }

  @Test
  public void getAttributeValueForGroupD() {
    String attributeValue = classInstance.getAttributeValue(sess, groupD, adDisplayNameO365MuAttrDef).valueAsString();
    assertNotNull(attributeValue);
    assertEquals(predefinedDisplayName, attributeValue);
  }

  @Test
  public void getAttributeValueForGroupE() {
    String attributeValue = classInstance.getAttributeValue(sess, groupE, adDisplayNameO365MuAttrDef).valueAsString();
    assertNotNull(attributeValue);
    assertEquals(groupE.getName() + ", " + groupC.getDescription() + ", " + typeOfWorkplacesC, attributeValue);
  }

  @Test
  public void getAttributeValueForGroupF() {
    String attributeValue = classInstance.getAttributeValue(sess, groupF, adDisplayNameO365MuAttrDef).valueAsString();
    assertNotNull(attributeValue);
    assertEquals(groupF.getName() + ", " + typeOfWorkplacesB, attributeValue);
  }

  @Test
  public void getAttributeValueForUnknownGroup() {
    String attributeValue =
        classInstance.getAttributeValue(sess, groupWithoutAttributes, adDisplayNameO365MuAttrDef).valueAsString();
    assertNull(attributeValue);
  }
}
