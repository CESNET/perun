package cz.metacentrum.perun.core.entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.ExpirationNotifScheduler.SponsorshipExpired;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityCreated;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupSyncFailed;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupSyncStarted;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.SponsorshipEstablished;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoCreated;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.EnrichedSponsorship;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.MessagesPageQuery;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.SortingOrder;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongRangeOfCountException;
import cz.metacentrum.perun.core.impl.AuditMessagesManagerImpl;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Integration tests of AuditMessagesManager.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class AuditMessagesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

  private final String CLASS_NAME = "AuditMessagesManager.";
  private final AuditMessage createdAuditMessage = new AuditMessage();

  public AuditMessagesManagerEntryIntegrationTest() {
    super();
  }

  @Test
  public void findAllPossibleEvents() throws Exception {
    System.out.println(CLASS_NAME + "findAllPossibleEvents");

    List<String> events = perun.getAuditMessagesManager().findAllPossibleEvents(sess);

    assertThat(events.size()).isGreaterThan(200);
    assertThat(events).contains("MemberCreated");
    assertThat(events).contains("VoCreated");
    assertThat(events).contains("UserCreated");
  }

  @Test
  public void getMessagesPage_allEntries() throws Exception {
    System.out.println(CLASS_NAME + "getMessagesPage_allEntries");

    List<AuditEvent> events = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      AuditEvent event = new VoCreated(new Vo(i, "testVo" + i, "shortName" + i));
      events.add(event);
      perun.getAuditer().logWithoutTransaction(sess, event);
    }

    MessagesPageQuery query = new MessagesPageQuery(5, 0, SortingOrder.DESCENDING, new ArrayList<>());
    // descending so have to reverse
    events = Lists.reverse(events);

    Paginated<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesPage(sess, query);
    assertThat(messages.getData().size()).isEqualTo(5);
    assertThat(messages.getData().stream().map(AuditMessage::getEvent).collect(Collectors.toList())).isEqualTo(
        events);
  }

  @Test
  public void getMessagesPage_ascendingOrder() throws Exception {
    System.out.println(CLASS_NAME + "getMessagesPage_ascendingOrder");

    AuditEvent voEvent = new VoCreated(new Vo(0, "test1", "testVo1"));
    perun.getAuditer().logWithoutTransaction(sess, voEvent);
    AuditEvent newerVoEvent = new VoCreated(new Vo(1, "test", "testVo"));
    perun.getAuditer().logWithoutTransaction(sess, newerVoEvent);

    MessagesPageQuery query = new MessagesPageQuery(2, 0, SortingOrder.ASCENDING, new ArrayList<>());

    Paginated<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesPage(sess, query);
    assertThat(messages.getData().get(0).getId()).isLessThan(messages.getData().get(1).getId());
  }

  @Test
  public void getMessagesPage_filteredByEventName() throws Exception {
    System.out.println(CLASS_NAME + "getMessagesPage_filteredByEventName");

    Vo vo = perun.getVosManager().createVo(sess, new Vo(0, "TestVo", "Test"));
    Group group = perun.getGroupsManager().createGroup(sess, vo, new Group("GroupTest", "Test"));

    perun.getAuditer().logWithoutTransaction(sess, new GroupSyncStarted(group));
    perun.getAuditer().logWithoutTransaction(sess, new GroupSyncFailed(group));

    MessagesPageQuery query = new MessagesPageQuery(2, 0, SortingOrder.ASCENDING, List.of("GroupSyncStarted"));
    Paginated<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesPage(sess, query);

    assertThat(messages.getData().size()).isEqualTo(1);
    assertThat(messages.getTotalCount()).isEqualTo(1);
  }

  @Test
  public void getMessagesPage_oneOfMany() throws Exception {
    System.out.println(CLASS_NAME + "getMessagesPage_oneOfMany");

    AuditEvent voEvent = new VoCreated(new Vo(0, "test1", "testVo1"));
    perun.getAuditer().logWithoutTransaction(sess, voEvent);
    AuditEvent newerVoEvent = new VoCreated(new Vo(1, "test", "testVo"));
    perun.getAuditer().logWithoutTransaction(sess, newerVoEvent);

    MessagesPageQuery query = new MessagesPageQuery(1, 0, SortingOrder.DESCENDING, new ArrayList<>());

    Paginated<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesPage(sess, query);
    assertThat(messages.getData().size()).isEqualTo(1);
    assertThat(messages.getData().get(0).getEvent()).isEqualTo(newerVoEvent);
  }

  @Test
  public void getMessagesPage_secondPage() throws Exception {
    System.out.println(CLASS_NAME + "getMessagesPage_secondPage");

    AuditEvent voEvent = new VoCreated(new Vo(0, "test1", "testVo1"));
    perun.getAuditer().logWithoutTransaction(sess, voEvent);
    AuditEvent newerVoEvent = new VoCreated(new Vo(1, "test", "testVo"));
    perun.getAuditer().logWithoutTransaction(sess, newerVoEvent);

    MessagesPageQuery query = new MessagesPageQuery(1, 1, SortingOrder.DESCENDING, new ArrayList<>());

    Paginated<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesPage(sess, query);
    assertThat(messages.getData().size()).isEqualTo(1);
    assertThat(messages.getData().get(0).getEvent()).isEqualTo(voEvent);
  }

  /**
   *
   */
  @Before
  public void setUp() {
    Facility testFacility = new Facility(0, "AuditMessageManagerEntryIntegrationTestFacility");
    FacilityCreated facilityCreatedEvent = new FacilityCreated(testFacility);
    createdAuditMessage.setEvent(facilityCreatedEvent);
  }

  private void testAuditEventMapper(ObjectMapper mapper, AuditEvent event) throws Exception {
    String value = mapper.writeValueAsString(event);

    AuditEvent deserializedEvent = mapper.readValue(value, AuditEvent.class);

    assertThat(deserializedEvent).isEqualTo(event);
  }

  /**
   * Check if method getMessages(sess) return right number of messages
   */
  @Test
  public void testGetFixedNumberOfMessages() throws Exception {
    System.out.println(CLASS_NAME + "testGetFixedNumberOfMessages");
    int count = AuditMessagesManager.COUNTOFMESSAGES;

    List<AuditEvent> events = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      AuditEvent event = new VoCreated(new Vo(i, "testVo" + i, "shortName" + i));
      events.add(event);
      perun.getAuditer().logWithoutTransaction(sess, event);
    }

    List<AuditMessage> messages = perun.getAuditMessagesManager().getMessages(sess);
    assertEquals("getMessage(sess) returns wrong count of messages", count, messages.size());
    assertThat(messages.stream().map(AuditMessage::getEvent).collect(Collectors.toList()))
        .containsExactlyInAnyOrderElementsOf(events);
  }

  @Test
  public void testGetMessagesByIdAndCount() throws Exception {
    System.out.println(CLASS_NAME + "testGetMessagesByIdAndCount");
    int total = 40;
    int idToChoose = total / 2;
    int count = 10;

    for (int i = 0; i < total; i++) {
      AuditEvent event = new VoCreated(new Vo(i, "testVo" + i, "shortName" + i));
      perun.getAuditer().logWithoutTransaction(sess, event);
    }

    List<AuditMessage> allNewMessages = perun.getAuditMessagesManager().getMessages(sess, total);
    int id = allNewMessages.get(idToChoose).getId();
    List<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesByIdAndCount(sess, id, count);
    assertEquals("getMessagesByIdAndCount(sess, id, count) returns wrong count of messages", count, messages.size());
    assertEquals(messages.get(0).getId(), id);
    assertEquals(messages.get(count - 1), allNewMessages.get(idToChoose - count + 1));
  }

  @Test
  public void testGetMessagesByIdAndCountSkipNonexistent() throws Exception {
    System.out.println(CLASS_NAME + "testGetMessagesByIdAndCountSkipNonexistent");
    int total = 40;
    int idToChoose = total / 2;
    int count = 10;

    for (int i = 0; i < total; i++) {
      AuditEvent event = new VoCreated(new Vo(i, "testVo" + i, "shortName" + i));
      perun.getAuditer().logWithoutTransaction(sess, event);
    }

    AuditEvent nonExistentEvent = new TestNonExistentEvent(new Vo(1, "test", "testVo"));
    perun.getAuditer().logWithoutTransaction(sess, nonExistentEvent);

    List<AuditMessage> allNewMessages = perun.getAuditMessagesManager().getMessages(sess, total);
    int id = allNewMessages.get(idToChoose).getId();
    List<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesByIdAndCount(sess, id, count);
    assertEquals("getMessagesByIdAndCount(sess, id, count) returns wrong count of messages", count, messages.size());
    assertEquals(messages.get(0).getId(), id);
    assertEquals(messages.get(count - 1), allNewMessages.get(idToChoose - count + 1));
  }

  /**
   * Check if method getMessages(sess, count) return right number of messages
   */
  @Test
  public void testGetVariableNumberOfMessages() throws Exception {
    System.out.println(CLASS_NAME + "testGetVariableNumberOfMessages");
    int count = 33;

    for (int i = 0; i < count; i++) {
      AuditEvent event = new VoCreated(new Vo(i, "testVo" + i, "shortName" + i));
      perun.getAuditer().logWithoutTransaction(sess, event);
    }
    List<AuditMessage> messages = perun.getAuditMessagesManager().getMessages(sess, count);
    assertEquals("getMessage(sess, count) returns wrong count of messages", count, messages.size());
  }

  @Test
  public void testGetMessagesSkipNonExistent() throws Exception {
    System.out.println(CLASS_NAME + "testGetMessagesSkipNonExistent");
    int count = 33;

    for (int i = 0; i < count; i++) {
      AuditEvent event = new VoCreated(new Vo(i, "testVo" + i, "shortName" + i));
      perun.getAuditer().logWithoutTransaction(sess, event);
    }

    AuditEvent nonExistentEvent = new TestNonExistentEvent(new Vo(1, "test", "testVo"));
    perun.getAuditer().logWithoutTransaction(sess, nonExistentEvent);

    List<AuditMessage> messages = perun.getAuditMessagesManager().getMessages(sess, count);
    assertEquals("getMessage(sess, count) returns wrong count of messages", count, messages.size());
    assertThat(messages.stream().map((AuditMessage::getEvent)).toList()).doesNotContain(nonExistentEvent);
  }

  @Test
  public void testGetMessagesByCountSkipNonExistent() throws Exception {
    System.out.println(CLASS_NAME + "testGetMessagesByCountSkipNonExistent");
    int count = 33;

    for (int i = 0; i < count; i++) {
      AuditEvent event = new VoCreated(new Vo(i, "testVo" + i, "shortName" + i));
      perun.getAuditer().logWithoutTransaction(sess, event);
    }

    AuditEvent nonExistentEvent = new TestNonExistentEvent(new Vo(1, "test", "testVo"));
    perun.getAuditer().logWithoutTransaction(sess, nonExistentEvent);

    List<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesByCount(sess, count);
    assertThat(messages.stream().map((AuditMessage::getEvent)).toList()).doesNotContain(nonExistentEvent);
  }

  /*
   * Wrong Range of count exception if count is less than 1 message
   */
  @Test(expected = WrongRangeOfCountException.class)
  public void testLessThanZeroCountOfMessages() throws Exception {
    System.out.println(CLASS_NAME + "testLessThanZeroCountOfMessages");
    perun.getAuditMessagesManager().getMessages(sess, -1);
  }

  @Test
  public void testLocalDate() throws Exception {
    System.out.println(CLASS_NAME + "testLocalDate");

    AuditMessagesManagerImpl auditMessagesManagerImpl =
        (AuditMessagesManagerImpl) ReflectionTestUtils.getField(perun.getAuditMessagesManagerBl(),
            "auditMessagesManagerImpl");
    assertThat(auditMessagesManagerImpl).isNotNull();

    ObjectMapper mapper = (ObjectMapper) ReflectionTestUtils.getField(auditMessagesManagerImpl, "MAPPER");
    assertThat(mapper).isNotNull();

    AuditEvent event = new SponsorshipEstablished(null, null, LocalDate.MIN);

    testAuditEventMapper(mapper, event);

    EnrichedSponsorship enrichedSponsorship = new EnrichedSponsorship();
    enrichedSponsorship.setValidityTo(LocalDate.MAX);
    AuditEvent event2 = new SponsorshipExpired();

    testAuditEventMapper(mapper, event2);
  }

  @Test
  public void skipNonExistentEventGetMessagesPage() throws Exception {
    System.out.println(CLASS_NAME + "skipNonExistentEventGetMessagesPage");

    AuditEvent voEvent = new VoCreated(new Vo(0, "test1", "testVo1"));
    perun.getAuditer().logWithoutTransaction(sess, voEvent);
    AuditEvent newerVoEvent = new TestNonExistentEvent(new Vo(1, "test", "testVo"));
    perun.getAuditer().logWithoutTransaction(sess, newerVoEvent);

    MessagesPageQuery query = new MessagesPageQuery(1, 0, SortingOrder.DESCENDING, new ArrayList<>());

    Paginated<AuditMessage> messages = perun.getAuditMessagesManager().getMessagesPage(sess, query);
    assertThat(messages.getData().size()).isEqualTo(1);
    // normally newest should be returned (see getMessagesPage_oneOfMany), however since it's skipped, voEvent should be returned here
    assertThat(messages.getData().get(0).getEvent()).isEqualTo(voEvent);
  }

  // This won't be found by Reflections since it's in a different package (I'm pretty sure?)
  private class TestNonExistentEvent extends AuditEvent {

    private final Vo vo;

    public TestNonExistentEvent(Vo vo) {
      this.vo = vo;
    }

    @Override
    public String getMessage() {
      return formatMessage("This is just a message which shouldn't be found %s", vo);
    }
  }

}
