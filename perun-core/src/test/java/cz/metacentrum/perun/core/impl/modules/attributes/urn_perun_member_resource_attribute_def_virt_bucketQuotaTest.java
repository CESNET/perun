package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_member_resource_attribute_def_virt_bucketQuotaTest extends AbstractPerunIntegrationTest {
  private static urn_perun_member_resource_attribute_def_virt_bucketQuota classInstance;
  private static PerunSessionImpl session;
  private static Attribute defaultQuotaAttr;
  private static Attribute memberQuotaAttr;
  private static Attribute overrideQuotaAttr;
  private static Member member;
  private static Vo vo;
  private static Resource resource;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_member_resource_attribute_def_virt_bucketQuota();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    member = new Member();
    vo = new Vo();
    resource = new Resource();

    defaultQuotaAttr = new Attribute();
    memberQuotaAttr = new Attribute();
    overrideQuotaAttr = new Attribute();


    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class),
        eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultBucketQuota"))).thenReturn(defaultQuotaAttr);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class),
        any(Member.class), any(Resource.class),
        eq(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":bucketQuota"))).thenReturn(memberQuotaAttr);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class),
        any(Member.class), any(Resource.class),
        eq(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":bucketQuotaOverride"))).thenReturn(overrideQuotaAttr);
  }

  @Test
  public void getAttributeValue() throws Exception {
    System.out.println("urn_perun_member_resource_attribute_def_virt_bucketQuota.getAttributeValue()");

    defaultQuotaAttr.setValue("100:200");
    memberQuotaAttr.setValue("200:400");
    overrideQuotaAttr.setValue("0:0");

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(defaultQuotaAttr),
        any(PerunBean.class), nullable(PerunBean.class))).thenReturn(new Pair<>(100,200));

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(memberQuotaAttr),
        any(PerunBean.class), nullable(PerunBean.class))).thenReturn(new Pair<>(200,400));

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(overrideQuotaAttr),
        any(PerunBean.class), nullable(PerunBean.class))).thenReturn(new Pair<>(0,0));

    Attribute testAttr = classInstance.getAttributeValue(session, member, resource,
        session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
            AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":bucketQuota"));

    assertEquals(overrideQuotaAttr.getValue(), testAttr.getValue());
  }

  @Test
  public void getAttributeValueOverrideNull() throws Exception {
    System.out.println("urn_perun_member_resource_attribute_def_virt_bucketQuota.getAttributeValueOverrideNull()");

    defaultQuotaAttr.setValue("100:200");
    memberQuotaAttr.setValue("200:400");
    overrideQuotaAttr.setValue(null);

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(defaultQuotaAttr),
        any(PerunBean.class), nullable(PerunBean.class))).thenReturn(new Pair<>(100,200));

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(memberQuotaAttr),
        any(PerunBean.class), nullable(PerunBean.class))).thenReturn(new Pair<>(200,400));

    Attribute testAttr = classInstance.getAttributeValue(session, member, resource,
        session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
            AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":bucketQuota"));

    assertEquals(memberQuotaAttr.getValue(), testAttr.getValue());
  }

    @Test
  public void getAttributeValueOverrideAndMemberNull() throws Exception {
    System.out.println("urn_perun_member_resource_attribute_def_virt_bucketQuota.getAttributeValueOverrideAndMemberNull()");

    defaultQuotaAttr.setValue("100:200");
    memberQuotaAttr.setValue(null);
    overrideQuotaAttr.setValue(null);

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(defaultQuotaAttr),
        any(PerunBean.class), nullable(PerunBean.class))).thenReturn(new Pair<>(100,200));

    Attribute testAttr = classInstance.getAttributeValue(session, member, resource,
        session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
            AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":bucketQuota"));

    assertEquals(defaultQuotaAttr.getValue(), testAttr.getValue());
  }
}
