package cz.metacentrum.perun.registrar.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.bl.InvitationsManagerBl;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcPerunTemplate;

public class InvitationExpirationScheduler {

  static final Logger LOG = LoggerFactory.getLogger(InvitationExpirationScheduler.class);

  private InvitationsManagerBl invitationsManagerBl;
  private JdbcPerunTemplate jdbc;
  private PerunSession session;
  private PerunBl perun;

  /**
   * Constructor for unit tests
   *
   * @param perun PerunBl bean
   */
  public InvitationExpirationScheduler(PerunBl perun, InvitationsManagerBl invitationsManagerBl) {
    this.perun = perun;
    this.invitationsManagerBl = invitationsManagerBl;
    initialize();
  }

  public void initialize() {
    this.session = perun.getPerunSession(new PerunPrincipal("perunRegistrar", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());
  }

  public void setSession(PerunSession session) {
    this.session = session;
  }

  @Autowired
  public void setDataSource(DataSource dataSource) {
    this.jdbc = new JdbcPerunTemplate(dataSource);
    this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  @Autowired
  public void setPerun(PerunBl perun) {
    this.perun = perun;
  }

  @Autowired
  public void setInvitationsManagerBl(InvitationsManagerBl invitationsManagerBl) {
    this.invitationsManagerBl = invitationsManagerBl;
  }

  /**
   * Returns current system time.
   *
   * @return current time.
   */
  public LocalDate getCurrentLocalDate() {
    return LocalDate.now();
  }

  /**
   * Checks all pending invitations and expires the ones that have their expiration in the past or today
   *
   * @return List of invitations that were expired in the current call
   * @throws InvalidInvitationStatusException
   */
  public List<Invitation> checkInvitationsExpiration() {
    if (perun.isPerunReadOnly()) {
      LOG.debug("This instance is just read only so skip checking invitations.");
      return new ArrayList<>();
    }
    LOG.info("Automatic check for expiration of invitations initiated on: {}", getCurrentLocalDate());

    List<Invitation> invitations = invitationsManagerBl.getAllInvitations(session, InvitationStatus.PENDING);
    List<Invitation> newlyExpiredInvitations = new ArrayList<>();

    for (Invitation invitation : invitations) {
      LocalDate expiration = invitation.getExpiration();
      LocalDate now = getCurrentLocalDate();
      boolean isExpired = expiration.isBefore(now) || expiration.isEqual(now);

      if (isExpired && invitation.getStatus() == InvitationStatus.PENDING) {
        try {
          Invitation updatedInvitation = invitationsManagerBl.expireInvitation(session, invitation);
          newlyExpiredInvitations.add(updatedInvitation);
        } catch (PerunException e) {
          LOG.error("Failed to expire the invitation: '{}' due to the error: '{}'", invitation, e.getMessage());
        }
      }
    }

    return newlyExpiredInvitations;
  }
}
