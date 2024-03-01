package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;

public class urn_perun_group_attribute_def_def_uniqueIDTest {

  private static final Pattern pattern = Pattern.compile("^[a-f0-9]+$");
  private urn_perun_group_attribute_def_def_uniqueID classInstance;
  private Attribute attributeToCheck;
  private Group group1 = new Group(12345, "group1", "Group 1", null, null, null, null, 0, 0);
  private Group group2 = new Group(34567, "group2", "Group 2", null, null, null, null, 0, 0);
  private PerunSessionImpl sess;
  private CoreConfig oldConfig = BeansUtils.getCoreConfig();

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_uniqueID();
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
    sess = mock(PerunSessionImpl.class);

    //prepare core config for this test
    CoreConfig cfNew = new CoreConfig();
    cfNew.setInstanceId("test");
    BeansUtils.setConfig(cfNew);
  }

  @After
  public void tearDown() throws Exception {
    //return old core config
    BeansUtils.setConfig(oldConfig);
  }

  @Test
  public void testFillValue() throws Exception {
    System.out.println("testCorrectFilling()");

    Attribute filledAttribute1 = classInstance.fillAttribute(sess, group1, attributeToCheck);
    assertNotNull("expected not null filled attribute", filledAttribute1);
    assertNotNull("expected not null value of filled attribute", filledAttribute1.getValue());
    Matcher matcher = pattern.matcher(filledAttribute1.valueAsString());
    assertTrue("expected matching for regular expression", matcher.matches());

    Attribute filledAttribute2 = classInstance.fillAttribute(sess, group2, attributeToCheck);
    assertNotNull("expected not null filled attribute", filledAttribute2);
    assertNotNull("expected not null value of filled attribute", filledAttribute2.getValue());
    matcher = pattern.matcher(filledAttribute2.valueAsString());
    assertTrue("expected matching for regular expression", matcher.matches());

    assertTrue("expected different output for 2 different groups",
        filledAttribute1.getValue() != filledAttribute2.getValue());
    assertEquals("expected the same output for the same group", filledAttribute1,
        classInstance.fillAttribute(sess, group1, attributeToCheck));
  }
}
