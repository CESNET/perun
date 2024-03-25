package cz.metacentrum.perun.core.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityCreated;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Facility;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AuditerImplIntegrationTest extends AbstractPerunIntegrationTest {

  private Facility facility;      // uses creation of facility to store same system message in Auditer

  @Before
  public void checkAuditerExists() {

    assertNotNull("unable to get auditer", perun.getAuditer());
  }

  @Test
  @Ignore
  public void flushAllMessages() throws Exception {
    System.out.println("AuditerTest.flushAllMessages");

    setUpFacility();
    assertTrue("auditer should contain at least one message", perun.getAuditer().getMessages().size() >= 1);
    perun.getAuditer().flush();
    assertTrue("auditer should be empty after flush", perun.getAuditer().getMessages().isEmpty());
  }

  @Test
  public void getCorrectMessages() throws Exception {
    System.out.println("AuditerTest.getCorrectMessages");
    perun.getAuditer().clean();

    Facility testFacility = new Facility(0, "AuditorTestFacility");
    FacilityCreated facilityCreatedEvent = new FacilityCreated(testFacility);

    perun.getAuditer().log(sess, facilityCreatedEvent);
    perun.getAuditer().flush();

    List<AuditMessage> messages = perun.getAuditMessagesManagerBl().getMessagesByCount(sess, 1);

    assertEquals("Invalid number of messages.", 1, messages.size());
    assertEquals(facilityCreatedEvent, messages.get(0).getEvent());
    //assertTrue("Invalid message received.", messages.get(0).getEvent().getMessage().contains
    // ("\"message\":\"Facility created Facility:"));
  }

  @Test
  public void logMessage() throws Exception {
    System.out.println("AuditerTest.logMessage");

    perun.getAuditer().clean();

    Facility testFacility = new Facility(0, "AuditorTestFacility");
    FacilityCreated facilityCreatedEvent = new FacilityCreated(testFacility);

    perun.getAuditer().log(sess, facilityCreatedEvent);
    perun.getAuditer().flush();

    List<AuditMessage> messages = perun.getAuditMessagesManagerBl().getMessagesByCount(sess, 1);

    assertEquals("Invalid number of messages.", 1, messages.size());
    assertEquals("Invalid number of messages.", facilityCreatedEvent, messages.get(0).getEvent());

  }

  // ------------- private methods ----------------------------------

  private void setUpFacility() throws Exception {

    facility =
        new Facility(0, "AuditorTestFacility", "test description", "testCreatedAt", "testCreatedBy", "testModifiedAt",
            "testModifiedBy", 0, 0);
    facility = perun.getFacilitiesManager().createFacility(sess, facility);
  }
}
