package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SearcherBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_resource_attribute_def_def_adNameTest {

  private urn_perun_group_resource_attribute_def_def_adName classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group();
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_resource_attribute_def_def_adName();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);
    reqAttribute = new Attribute();

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(attributesManagerBl.getAttribute(sess, resource,
        AttributesManager.NS_RESOURCE_ATTR_DEF + ":adOuName")).thenReturn(reqAttribute);

    SearcherBl searcherBl = mock(SearcherBl.class);
    when(perunBl.getSearcherBl()).thenReturn(searcherBl);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue("correctValue");
    reqAttribute.setValue("correctValue2");

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("correctValue");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithSameADNameAlreadySetElsewhere() throws Exception {
    System.out.println("testSemanticsWithSameADNameAlreadySetElsewhere()");
    attributeToCheck.setValue("correctValue");
    reqAttribute.setValue("correctValue2");
    Group group2 = new Group();
    List<Group> groups = new ArrayList<>();
    groups.add(group2);
    groups.add(group);
    when(sess.getPerunBl().getSearcherBl()
        .getGroupsByGroupResourceSetting(sess, attributeToCheck, reqAttribute)).thenReturn(groups);

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongValue() throws Exception {
    System.out.println("testWrongValue()");
    attributeToCheck.setValue("bad@value");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }
}
