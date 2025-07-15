package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_resource_attribute_def_def_defaultBucketQuotaTest extends AbstractPerunIntegrationTest {
  private static urn_perun_resource_attribute_def_def_defaultBucketQuota classInstance;
  private static PerunSessionImpl session;
  private static Resource resource;
  private static Attribute maxAttr;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_defaultBucketQuota();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    resource = new Resource();
    maxAttr = new Attribute();
    attributeToCheck = new Attribute();

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), eq(resource),
        eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":maxUserBucketQuota"))).thenReturn(maxAttr);
  }

  @Test
  public void checkAttributeSemantic() throws Exception {
    System.out.println("urn_perun_resource_attribute_def_def_defaultBucketQuotaTest.checkAttributeSemantic()");
    maxAttr.setValue("0:0");
    attributeToCheck.setValue("100:200");

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(maxAttr), any(Resource.class),
        nullable(PerunBean.class))).thenReturn(new Pair<>(0, 0));
    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(attributeToCheck), any(Resource.class),
        nullable(PerunBean.class))).thenReturn(new Pair<>(100, 200));


    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test
  public void checkAttributeSemanticNull() throws Exception {
    System.out.println("urn_perun_resource_attribute_def_def_defaultBucketQuotaTest.checkAttributeSemanticNull()");
    maxAttr.setValue("0:0");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test
  public void checkAttributeSemanticSame() throws Exception {
    System.out.println("urn_perun_resource_attribute_def_def_defaultBucketQuotaTest.checkAttributeSemanticSame()");
    maxAttr.setValue("100:200");
    attributeToCheck.setValue("100:200");

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(maxAttr), any(Resource.class),
        nullable(PerunBean.class))).thenReturn(new Pair<>(100, 200));
    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(attributeToCheck), any(Resource.class),
        nullable(PerunBean.class))).thenReturn(new Pair<>(100, 200));


    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void checkAttributeSemanticSoftLarger() throws Exception {
    System.out.println(
        "urn_perun_resource_attribute_def_def_defaultBucketQuotaTest.checkAttributeSemanticSoftLarger()");
    maxAttr.setValue("10:200");
    attributeToCheck.setValue("100:200");

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(maxAttr), any(Resource.class),
        nullable(PerunBean.class))).thenReturn(new Pair<>(10, 200));
    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(attributeToCheck), any(Resource.class),
        nullable(PerunBean.class))).thenReturn(new Pair<>(100, 200));

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void checkAttributeSemanticHardLarger() throws Exception {
    System.out.println(
        "urn_perun_resource_attribute_def_def_defaultBucketQuotaTest.checkAttributeSemanticHardLarger()");
    maxAttr.setValue("1:200");
    attributeToCheck.setValue("100:2000");

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(maxAttr), any(Resource.class),
        nullable(PerunBean.class))).thenReturn(new Pair<>(1, 200));
    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(attributeToCheck), any(Resource.class),
        nullable(PerunBean.class))).thenReturn(new Pair<>(100, 2000));

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }
}
