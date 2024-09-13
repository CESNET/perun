package cz.metacentrum.perun.registrar.blImpl;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.bl.InvitationsManagerBl;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.exceptions.InvitationAlreadyAssignedToAnApplicationException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.implApi.InvitationsManagerImplApi;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import cz.metacentrum.perun.registrar.model.InvitationWithSender;
import cz.metacentrum.perun.registrar.model.InvitationsPageQuery;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvitationsManagerBlImpl implements InvitationsManagerBl {

  static final Logger LOG = LoggerFactory.getLogger(InvitationsManagerBlImpl.class);

  private final InvitationsManagerImplApi invitationsManagerImpl;
  private final RegistrarManager registrarManager;
  private PerunBl perun;


  public InvitationsManagerBlImpl(InvitationsManagerImplApi invitationsManagerImpl, RegistrarManager registrarManager) {
    this.invitationsManagerImpl = invitationsManagerImpl;
    this.registrarManager = registrarManager;
  }

  public void setPerun(PerunBl perunBl) {
    this.perun = perunBl;
  }

  @Override
  public Invitation getInvitationByApplication(PerunSession sess, Application application) {
    Invitation invitation;
    try {
      invitation = invitationsManagerImpl.getInvitationByApplication(sess, application);
    } catch (InvitationNotExistsException ex) {
      invitation = null;
    }
    return invitation;
  }

  @Override
  public Invitation getInvitationById(PerunSession sess, int id) throws InvitationNotExistsException {
    return invitationsManagerImpl.getInvitationById(sess, id);
  }

  @Override
  public Invitation getInvitationByToken(PerunSession sess, UUID token) throws InvitationNotExistsException {
    return invitationsManagerImpl.getInvitationByToken(sess, token);
  }

  @Override
  public List<Invitation> getInvitationsForSender(PerunSession sess, Group group, User user) {
    return invitationsManagerImpl.getInvitationsForSender(sess, group, user);
  }

  @Override
  public List<Invitation> getInvitationsForGroup(PerunSession sess, Group group) {
    return invitationsManagerImpl.getInvitationsForGroup(sess, group);
  }

  @Override
  public List<Invitation> getInvitationsForVo(PerunSession sess, Vo vo) {
    return invitationsManagerImpl.getInvitationsForVo(sess, vo);
  }

  @Override
  public List<Invitation> getAllInvitations(PerunSession sess, InvitationStatus status) {
    return invitationsManagerImpl.getAllInvitations(sess, status);
  }

  @Override
  public Invitation createInvitation(PerunSession sess, Invitation invitation) {
    if (!Utils.EMAIL_PATTERN.matcher(invitation.getReceiverEmail()).matches()) {
      throw new IllegalArgumentException("Invalid email address: " + invitation.getReceiverEmail());
    }

    return invitationsManagerImpl.createInvitation(sess, invitation);
  }

  @Override
  public String createInvitationUrl(PerunSession sess, String token)
      throws InvitationNotExistsException {
    UUID tokenUuid;
    try {
      tokenUuid = UUID.fromString(token);
    } catch (java.lang.IllegalArgumentException e) {
      throw new IllegalArgumentException("Invitation token '" + token + "' is not in correct UUID format.");
    }

    Invitation invitation = invitationsManagerImpl.getInvitationByToken(sess, tokenUuid);

    Group group;
    Vo vo;
    try {
      group = perun.getGroupsManagerBl().getGroupById(sess, invitation.getGroupId());
      vo = perun.getVosManagerBl().getVoById(sess, invitation.getVoId());
    } catch (VoNotExistsException | GroupNotExistsException ex) {
      throw new ConsistencyErrorException("Entities related to invitation were removed but invitation still remains.",
          ex);
    }

    return registrarManager.getMailManager().buildInviteURLForInvitation(vo, group, tokenUuid);
  }

  @Override
  public Invitation inviteToGroup(PerunSession sess, Vo vo, Group group, String receiverName, String receiverEmail,
      String language, LocalDate expiration, String redirectUrl) throws RegistrarException {
    return inviteToGroup(sess, vo, group, receiverName, receiverEmail, language, expiration, redirectUrl, false);
  }

  private Invitation inviteToGroup(PerunSession sess, Vo vo, Group group, String receiverName, String receiverEmail,
      String language, LocalDate expiration, String redirectUrl, boolean csv)
      throws RegistrarException {
    if (!Utils.EMAIL_PATTERN.matcher(receiverEmail).matches()) {
      throw new RegistrarException("Invalid email address: " + receiverEmail);
    }

    if (expiration == null) {
      expiration = LocalDate.now().plusMonths(1);
    }

    if (expiration.isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Expiration '" + expiration + "' is in the past.");
    }

    Invitation invitation = new Invitation(vo.getId(), group.getId(), receiverName,
        receiverEmail, redirectUrl, new Locale(language), expiration);
    // set sender to current principal
    invitation.setSenderId(sess.getPerunPrincipal().getUserId());
    // created in db, also generates UUID
    invitation = createInvitation(sess, invitation);

    String url;
    try {
      url = createInvitationUrl(sess, invitation.getToken().toString());
    } catch (InvitationNotExistsException e) {
      throw new ConsistencyErrorException("Invitation created during invite process does not exist.", e);
    }

    try {
      registrarManager.getMailManager().sendInvitationPreApproved(vo, group, invitation, url);
    } catch (RegistrarException ex) {
      invitation.setStatus(InvitationStatus.UNSENT);
      invitationsManagerImpl.setInvitationStatus(sess, invitation, InvitationStatus.UNSENT);
      LOG.error("Invitation: {} failed to be sent and was set to UNSENT due to {}", invitation, ex.getMessage());
      if (csv) {
        throw ex;
      }
    }
    return invitation;
  }

  @Override
  public Map<String, String> inviteToGroupFromCsv(PerunSession sess, Vo vo, Group group, List<String> data,
      String language, LocalDate expiration, String redirectUrl) {
    List<List<String>> parsedLines;
    Map<String, String> result = new HashMap<>();
    try {
      parsedLines = parseInvitationCsv(data);
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid CSV format of Invitations", e);
    }
    checkInvitationCsvData(parsedLines);
    for (List<String> row : parsedLines) {
      if (row.isEmpty()) {
        continue;
      }
      String receiverEmail = row.get(0);
      String receiverName = row.get(1);
      try {
        inviteToGroup(sess, vo, group, receiverName, receiverEmail, language, expiration, redirectUrl, true);
        result.put(receiverEmail + " - " + receiverName, "OK");
      } catch (RegistrarException ex) {
        result.put(receiverEmail + " - " + receiverName, "ERROR: " + ex.getMessage());
      }
    }
    return result;
  }

  @Override
  public Invitation expireInvitation(PerunSession sess, Invitation invitation) throws InvalidInvitationStatusException {
    if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
      throw new InvalidInvitationStatusException("Invitation: " + invitation + "cannot be expired when in status: " +
          invitation.getStatus());
    }
    invitationsManagerImpl.setInvitationStatus(sess, invitation, InvitationStatus.EXPIRED);
    invitation.setStatus(InvitationStatus.EXPIRED);
    LOG.info("Invitation: {} was set to EXPIRED.", invitation);
    return invitation;
  }

  @Override
  public Invitation revokeInvitation(PerunSession sess, Invitation invitation) throws InvalidInvitationStatusException {
    if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
      throw new InvalidInvitationStatusException("Invitation: " + invitation + "cannot be revoked when in status: " +
          invitation.getStatus());
    }
    invitationsManagerImpl.setInvitationStatus(sess, invitation, InvitationStatus.REVOKED);
    invitation.setStatus(InvitationStatus.REVOKED);
    LOG.info("Invitation: {} was set to REVOKED.", invitation);
    return invitation;
  }

  @Override
  public Invitation markInvitationAccepted(PerunSession sess, Invitation invitation)
      throws InvalidInvitationStatusException {
    if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
      throw new InvalidInvitationStatusException("Invitation: " + invitation + "cannot be accepted when in status: " +
          invitation.getStatus());
    }
    invitationsManagerImpl.setInvitationStatus(sess, invitation, InvitationStatus.ACCEPTED);
    invitation.setStatus(InvitationStatus.ACCEPTED);
    LOG.info("Invitation: {} was set to ACCEPTED.", invitation);
    return invitation;
  }

  @Override
  public Invitation canInvitationBeAccepted(PerunSession sess, UUID uuid, Group group)
      throws InvalidInvitationStatusException, InvitationNotExistsException,
                 InvitationAlreadyAssignedToAnApplicationException {
    Invitation invitation = invitationsManagerImpl.getInvitationByToken(sess, uuid);

    if (invitation.getGroupId() != group.getId()) {
      throw new IllegalArgumentException("Wrong group! You are trying to apply to group " +
                                             group.getId() + " with invitation for group " + invitation.getGroupId());
    }
    if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
      throw new InvalidInvitationStatusException(
          "Expected the invitation in state " + InvitationStatus.PENDING + " got " + invitation.getStatus());
    }
    if (invitation.getApplicationId() != null) {
      throw new InvitationAlreadyAssignedToAnApplicationException(
          "Invitation with uuid " + invitation.getToken() + " is already assigned to a different application.");
    }

    return invitation;
  }

  @Override
  public Invitation extendInvitationExpiration(PerunSession sess, Invitation invitation, LocalDate newExpirationDate)
      throws InvalidInvitationStatusException {
    if (newExpirationDate == null) {
      newExpirationDate = invitation.getExpiration().plusMonths(1);
    }

    if (newExpirationDate.isBefore(invitation.getExpiration())) {
      throw new IllegalArgumentException("New expiration date '" +
                                             newExpirationDate + "' is earlier than the current one.");
    }

    if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
      throw new InvalidInvitationStatusException("Invitation: " + invitation + "cannot be extended when in status: " +
                                                     invitation.getStatus());
    }

    Invitation invitationToReturn = invitationsManagerImpl.setInvitationExpiration(sess, invitation, newExpirationDate);
    LOG.debug("Expiration date for invitation {} was extended to {}.",
        invitationToReturn.getId(), invitationToReturn.getExpiration().toString());
    return invitationToReturn;
  }

  @Override
  public Paginated<InvitationWithSender> getInvitationsPage(PerunSession sess, Group group,
                                                            InvitationsPageQuery query) {
    return invitationsManagerImpl.getInvitationsPage(sess, group, query);
  }

  private void checkInvitationCsvData(List<List<String>> parsedData) {
    Set<String> emails = new HashSet<>();
    for (List<String> row : parsedData) {
      if (row.isEmpty()) {
        continue;
      }
      if (row.size() != 2) {
        throw new IllegalArgumentException("Invalid CSV format of Invitation: " + row);
      }
      String email = row.get(0);

      if (!emails.add(email)) {
        throw new IllegalArgumentException("ERROR: duplicated email found: " + email);
      }
    }
  }

  /**
   * Parse semicolon ';' separated String into list of Strings
   *
   * @param data list of row data
   * @return list of list of row values
   */
  private List<List<String>> parseInvitationCsv(List<String> data) throws IOException {
    CsvMapper csvMapper = new CsvMapper();
    csvMapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
    MappingIterator<List<String>> rows = csvMapper.readerFor(List.class)
        .with(CsvSchema.emptySchema().withColumnSeparator(';'))
        .readValues(String.join("\n", data));
    return rows.readAll();
  }
}
