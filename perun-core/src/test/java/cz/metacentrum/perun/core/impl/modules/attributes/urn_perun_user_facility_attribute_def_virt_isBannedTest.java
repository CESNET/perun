package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForFacilityAndUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanSetForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanUpdatedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.SecurityTeamAssignedToFacility;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_facility_attribute_def_virt_isBannedTest {

  private static urn_perun_user_facility_attribute_def_virt_isBanned classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Facility facility;
  private static AttributeDefinition attrDef;

  @Before
  public void setUp() {
    classInstance = new urn_perun_user_facility_attribute_def_virt_isBanned();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    facility = new Facility(1, "testFacility");
    user = new User(1, "name", "surname", "middlename", "title", "title");
    attrDef = new Attribute(classInstance.getAttributeDefinition());
  }

  @Test
  public void resolveVirtualAttributeValueChangeTest() throws Exception {
    System.out.println(getClass().getName() + ".resolveVirtualAttributeValueChangeTest()");

    AuditEvent event4 = new BanSetForFacility(new BanOnFacility(), user.getId(), facility.getId());
    AuditEvent event5 = new BanUpdatedForFacility(new BanOnFacility(), user.getId(), facility.getId());
    AuditEvent event6 = new BanRemovedForFacility(new BanOnFacility(), user.getId(), facility.getId());
    AuditEvent wrongEvent = new SecurityTeamAssignedToFacility();

    List<AuditEvent> resolvedEvents;

    when(session.getPerunBl().getUsersManagerBl().getUserById(any(PerunSessionImpl.class), anyInt())).thenReturn(user);
    when(session.getPerunBl().getFacilitiesManagerBl()
        .getFacilityById(any(PerunSessionImpl.class), anyInt())).thenReturn(facility);
    resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event4);
    assertEquals(AttributeChangedForFacilityAndUser.class, resolvedEvents.get(0).getClass());
    resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event5);
    assertEquals(AttributeChangedForFacilityAndUser.class, resolvedEvents.get(0).getClass());
    resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, event6);
    assertEquals(AttributeChangedForFacilityAndUser.class, resolvedEvents.get(0).getClass());
    resolvedEvents = classInstance.resolveVirtualAttributeValueChange(session, wrongEvent);
    assertTrue(resolvedEvents.isEmpty());
  }
}
