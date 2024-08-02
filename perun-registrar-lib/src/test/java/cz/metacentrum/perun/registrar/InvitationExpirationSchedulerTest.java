package cz.metacentrum.perun.registrar;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.bl.InvitationsManagerBl;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.impl.InvitationExpirationScheduler;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-core.xml", "classpath:perun-registrar-lib.xml"})
@Transactional(transactionManager = "perunTransactionManager")
public class InvitationExpirationSchedulerTest {
  @Autowired
  PerunBl perun;

  PerunSession session;

  @Mock
  InvitationsManagerBl invitationsManagerBl;

  private static final String CLASS_NAME = "InvitationExpirationSchedulerTest.";

  private static List<Invitation> mixedInvitations;
  private static List<Invitation> validFutureInvitations;
  private static List<Invitation> expiredMarkedInvitations;
  private static List<Invitation> expiredUnmarkedInvitations;

  private static LocalDate today;
  private static LocalDate tomorrow;
  private static LocalDate yesterday;
  private static LocalDate plusSevenDays;
  private static LocalDate plusTenDays;
  private static LocalDate minusSevenDays;
  private static LocalDate minusTenDays;

  private InvitationExpirationScheduler scheduler;
  private InvitationExpirationScheduler spyScheduler;

  @Autowired
  public void setScheduler(InvitationExpirationScheduler scheduler) {
    scheduler.setInvitationsManagerBl(invitationsManagerBl);
    this.scheduler = scheduler;
    this.spyScheduler = spy(scheduler);
  }

  @BeforeClass
  public static void initDates() {
    today = LocalDate.of(2020, 2, 1);
    tomorrow = today.plusDays(1);
    yesterday = today.minusDays(1);
    plusSevenDays = today.plusDays(7);
    plusTenDays = today.plusDays(10);
    minusSevenDays = today.minusDays(7);
    minusTenDays = today.minusDays(10);

  }

  @Before
  public void setUp() throws Exception {
    session = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());
    scheduler.setSession(session);
  }

  @Before
  public void setUpMocks() throws InvalidInvitationStatusException {
    MockitoAnnotations.openMocks(this);
    setScheduler(scheduler);
    when(spyScheduler.getCurrentLocalDate()).thenReturn(today);
    when(invitationsManagerBl.expireInvitation(eq(session), any(Invitation.class))).thenAnswer(
        (Answer<Invitation>) invocation -> {
          Invitation inv = invocation.getArgument(1);
          inv.setStatus(InvitationStatus.EXPIRED);
          return inv;
        });

  }

  @Before
  public void initInvitations() {
    mixedInvitations = new ArrayList<>();
    mixedInvitations.add(new Invitation(0, 0, 0, 0,
        "expires today", "receiver@email.com", Locale.ENGLISH, today));
    mixedInvitations.add(new Invitation(0, 0, 0, 0,
        "expires tomorrow", "receiver@email.com", Locale.ENGLISH, tomorrow));

    Invitation expiredInvitationYesterday = new Invitation(0, 0, 0, 0,
        "expired yesterday", "receiver@email.com", Locale.ENGLISH, yesterday);
    expiredInvitationYesterday.setStatus(InvitationStatus.EXPIRED);

    mixedInvitations.add(expiredInvitationYesterday);

    validFutureInvitations = new ArrayList<>();
    validFutureInvitations.add(new Invitation(0, 0, 0, 0,
        "expires tomorrow", "receiver@email.com", Locale.ENGLISH, tomorrow));
    validFutureInvitations.add(new Invitation(0, 0, 0, 0,
        "expires in 7 days", "receiver@email.com", Locale.ENGLISH, plusSevenDays));
    validFutureInvitations.add(new Invitation(0, 0, 0, 0,
        "expires in 10 days", "receiver@email.com", Locale.ENGLISH, plusTenDays));

    expiredMarkedInvitations = new ArrayList<>();
    expiredMarkedInvitations.add(expiredInvitationYesterday);

    Invitation expiredInvitationMinusSevenDays = new Invitation(0, 0, 0, 0,
        "expired 7 days ago", "receiver@email.com", Locale.ENGLISH, minusSevenDays);
    expiredInvitationMinusSevenDays.setStatus(InvitationStatus.EXPIRED);

    expiredMarkedInvitations.add(expiredInvitationMinusSevenDays);

    Invitation expiredInvitationMinusTenDays = new Invitation(0, 0, 0, 0,
        "expired 10 days ago", "receiver@email.com", Locale.ENGLISH, minusTenDays);
    expiredInvitationMinusTenDays.setStatus(InvitationStatus.EXPIRED);

    expiredMarkedInvitations.add(expiredInvitationMinusTenDays);

    expiredUnmarkedInvitations = new ArrayList<>();
    expiredUnmarkedInvitations.add(new Invitation(0, 0, 0, 0,
        "expires today", "receiver@email.com", Locale.ENGLISH, today));
    expiredUnmarkedInvitations.add(new Invitation(0, 0, 0, 0,
        "expired yesterday", "receiver@email.com", Locale.ENGLISH, yesterday));
    expiredUnmarkedInvitations.add(new Invitation(0, 0, 0, 0,
        "expired 7 days ago", "receiver@email.com", Locale.ENGLISH, minusSevenDays));
    expiredUnmarkedInvitations.add(new Invitation(0, 0, 0, 0,
        "expired 10 days ago", "receiver@email.com", Locale.ENGLISH, minusTenDays));
  }

  @Test
  public void checkInvitationsOneShouldExpire() throws Exception {
    System.out.println(CLASS_NAME + "checkInvitationsOneShouldExpire");

    when(invitationsManagerBl.getAllInvitations(session, InvitationStatus.PENDING)).thenReturn(mixedInvitations);

    List<Invitation> newlyExpiredInvitations = spyScheduler.checkInvitationsExpiration();
    assertEquals("Exactly one invitation should expire.", 1, newlyExpiredInvitations.size());

    Optional<Invitation> newlyExpiredInvitation = newlyExpiredInvitations.stream().findFirst();
    Optional<Invitation> expectedExpiredInvitation =
        mixedInvitations.stream().filter(inv -> inv.getExpiration() == today).findFirst();
    assertEquals(expectedExpiredInvitation, newlyExpiredInvitation);
  }

  @Test
  public void checkInvitationsNoneShouldExpireFutureDates() throws Exception {
    System.out.println(CLASS_NAME + "checkInvitationsNoneShouldExpireFutureDates");

    when(invitationsManagerBl.getAllInvitations(session, InvitationStatus.PENDING)).thenReturn(validFutureInvitations);

    List<Invitation> newlyExpiredInvitations = spyScheduler.checkInvitationsExpiration();
    assertEquals("No invitations should expire - all have future expiration dates.", 0, newlyExpiredInvitations.size());
  }

  @Test
  public void checkInvitationsNoneShouldExpireAlreadyExpired() throws Exception {
    System.out.println(CLASS_NAME + "checkInvitationsNoneShouldExpireAlreadyExpired");

    when(invitationsManagerBl.getAllInvitations(session, InvitationStatus.PENDING)).thenReturn(
        expiredMarkedInvitations);

    List<Invitation> newlyExpiredInvitations = spyScheduler.checkInvitationsExpiration();
    assertEquals("No invitations should expire - all have already expired.", 0, newlyExpiredInvitations.size());
  }

  @Test
  public void checkInvitationsAllShouldExpire() throws Exception {
    System.out.println(CLASS_NAME + "checkInvitationsAllShouldExpire");

    when(invitationsManagerBl.getAllInvitations(session, InvitationStatus.PENDING)).thenReturn(
        expiredUnmarkedInvitations);

    List<Invitation> newlyExpiredInvitations = spyScheduler.checkInvitationsExpiration();

    assertEquals("All invitations should expire.", expiredUnmarkedInvitations, newlyExpiredInvitations);
  }

}
