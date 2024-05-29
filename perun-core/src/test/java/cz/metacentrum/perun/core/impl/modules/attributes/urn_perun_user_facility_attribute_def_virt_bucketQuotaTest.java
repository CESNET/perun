package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_facility_attribute_def_virt_bucketQuotaTest extends AbstractPerunIntegrationTest {
  private static urn_perun_user_facility_attribute_def_virt_bucketQuota classInstance;
  private static PerunSessionImpl session;
  private static Attribute resourceAttr1;
  private static Attribute resourceAttr2;
  private static User user;
  private static Facility facility;
  private static Member member;
  private static Vo vo;
  private static Resource resource1;
  private static Resource resource2;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_facility_attribute_def_virt_bucketQuota();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    user = new User();
    facility = new Facility();
    member = new Member();
    vo = new Vo();
    resource1 = new Resource();
    // have to set id for resources not to equal when argument matching
    resource1.setId(-1);
    resource2 = new Resource();
    resourceAttr1 = new Attribute();
    resourceAttr2 = new Attribute();

    when(session.getPerunBl().getResourcesManagerBl().getAllowedResources(any(PerunSessionImpl.class),
        any(Facility.class), any(User.class))).thenReturn(List.of(resource1, resource2));
    when(session.getPerunBl().getMembersManagerBl().getMemberByUser(any(PerunSessionImpl.class), any(Vo.class),
        any(User.class))).thenReturn(member);
    when(session.getPerunBl().getVosManagerBl().getVoById(any(PerunSessionImpl.class), any(int.class))).thenReturn(vo);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Member.class),
        eq(resource1), eq(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":bucketQuota"))).thenReturn(resourceAttr1);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Member.class),
        eq(resource2), eq(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":bucketQuota"))).thenReturn(resourceAttr2);
  }

  @Test
  public void getAttributeValueAllMemberResourceNull() throws Exception {
    System.out.println("urn_perun_user_facility_attribute_def_virt_bucketQuota.getAttributeValueAllMemberResourceNull" +
                           "()");
    resourceAttr1.setValue(null);
    resourceAttr2.setValue(null);

    Attribute testAttr = classInstance.getAttributeValue(session, user, facility,
        session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
            AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ":bucketQuota"));
    assertNull(testAttr.getValue());
  }

  @Test
  public void getAttributeValueNormalSum() throws Exception {
    System.out.println("urn_perun_user_facility_attribute_def_virt_bucketQuota.getAttributeValueNormalSum" +
                           "()");
    resourceAttr1.setValue("100:200");
    resourceAttr2.setValue("200:300");

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(resourceAttr1), eq(resource1),
        any(Member.class))).thenReturn(new Pair<>(100, 200));
    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(resourceAttr2), eq(resource2),
        any(Member.class))).thenReturn(new Pair<>(200, 300));

    Attribute testAttr = classInstance.getAttributeValue(session, user, facility,
        session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
            AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ":bucketQuota"));
    assertEquals("300:500", testAttr.getValue());
  }

  @Test
  public void getAttributeValueSomeNull() throws Exception {
    System.out.println("urn_perun_user_facility_attribute_def_virt_bucketQuota.getAttributeValueSomeNull" +
                           "()");
    resourceAttr1.setValue("100:200");
    resourceAttr2.setValue(null);

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(resourceAttr1), eq(resource1),
        any(Member.class))).thenReturn(new Pair<>(100, 200));

    Attribute testAttr = classInstance.getAttributeValue(session, user, facility,
        session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
            AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ":bucketQuota"));
    assertEquals("100:200", testAttr.getValue());
  }

  @Test
  public void getAttributeValueZeroSum() throws Exception {
    System.out.println("urn_perun_user_facility_attribute_def_virt_bucketQuota.getAttributeValueZeroSum" +
                           "()");
    resourceAttr1.setValue("0:0");
    resourceAttr2.setValue("0:0");

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(resourceAttr1), eq(resource1),
        any(Member.class))).thenReturn(new Pair<>(0, 0));
    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(resourceAttr2), eq(resource2),
        any(Member.class))).thenReturn(new Pair<>(0, 0));

    Attribute testAttr = classInstance.getAttributeValue(session, user, facility,
        session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
            AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ":bucketQuota"));
    assertEquals("0:0", testAttr.getValue());
  }

    @Test
  public void getAttributeValueSomeZeroSum() throws Exception {
    System.out.println("urn_perun_user_facility_attribute_def_virt_bucketQuota.getAttributeValueSomeZeroSum" +
                           "()");
    resourceAttr1.setValue("100:200");
    resourceAttr2.setValue("0:0");

    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(resourceAttr1), eq(resource1),
        any(Member.class))).thenReturn(new Pair<>(100, 200));
    when(session.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(eq(resourceAttr2), eq(resource2),
        any(Member.class))).thenReturn(new Pair<>(0, 0));

    Attribute testAttr = classInstance.getAttributeValue(session, user, facility,
        session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
            AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ":bucketQuota"));
    assertEquals("0:0", testAttr.getValue());
  }
}
