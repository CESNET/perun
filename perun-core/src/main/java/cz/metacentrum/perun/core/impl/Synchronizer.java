package cz.metacentrum.perun.core.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Synchronizer, general tool for synchronization tasks.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 *
 */
public class Synchronizer {

  private final static Logger log = LoggerFactory.getLogger(Synchronizer.class);
  private PerunSession sess;

  private PerunBl perunBl;
  private AtomicBoolean synchronizeGroupsRunning = new AtomicBoolean(false);

  public Synchronizer() {
  }

  public Synchronizer(PerunBl perunBl) throws InternalErrorException {
    this.perunBl = perunBl;

    initialize();
  }

  public void synchronizeGroups() {
    if (synchronizeGroupsRunning.compareAndSet(false, true)) {
      try {
        log.debug("Synchronizer starting synchronizing the groups");
        this.perunBl.getGroupsManagerBl().synchronizeGroups(this.sess);
        if (!synchronizeGroupsRunning.compareAndSet(true, false)) {
          log.error("Synchonizer: group synchronization out of sync, reseting.");
          synchronizeGroupsRunning.set(false);
        }
      } catch (InternalErrorException e) {
        log.error("Cannot synchronize groups:", e);
      }
    } else {
      log.debug("Synchronizer: group synchronization currently running.");
    }
  }

  /**
   * Iterate through all VALID members and check whether they are still in valid period.
   */
  public void checkMembersState() {
	  Date now = new Date();

	  // Get all VO's
	  try {
		  for (Vo vo: perunBl.getVosManagerBl().getVos(this.sess)) {
			  // Get all members
			  for (Member member: perunBl.getMembersManagerBl().getMembers(sess, vo)) {
				  // Read membershipExpiration and check it
				  Attribute membersExpiration = perunBl.getAttributesManagerBl().getAttribute(sess, member, "urn:perun:member:attribute-def:def:membershipExpiration");
				  if (membersExpiration.getValue() != null) {
					  Date currentMembershipExpirationDate = BeansUtils.DATE_FORMATTER.parse((String) membersExpiration.getValue());
					  if (currentMembershipExpirationDate.before(now)) {
						  // Expired membership, set status to expired only if the user hasn't been in suspended state
						  if (!member.getStatus().equals(Status.EXPIRED) && !member.getStatus().equals(Status.SUSPENDED)) {
							  try {
								  perunBl.getMembersManagerBl().expireMember(sess, member);
								  log.info("Switching {} to EXPIRE state, due to expiration {}.", member, (String) membersExpiration.getValue());
							  } catch (MemberNotValidYetException e) {
								  log.warn("Trying to switch invalid member {} into the expire state.", member);
							  }
						  }
					  } else if (member.getStatus().equals(Status.EXPIRED) || member.getStatus().equals(Status.DISABLED)) {
						  // Validate member only if the previous status was EXPIRED or DISABLED
						  try {
							  perunBl.getMembersManagerBl().validateMember(sess, member);
							  log.info("Switching {} to VALID state, due to changed expiration {}.", member, (String) membersExpiration.getValue());
						  } catch (WrongAttributeValueException e) {
							  log.error("Error during validating member {}, exception {}", member, e);
						  } catch (WrongReferenceAttributeValueException e) {
							  log.error("Error during validating member {}, exception {}", member, e);
						  }
					  }
				  }
			  }

		  }
	  } catch (InternalErrorException e) {
		  log.error("Synchronizer: checkMembersState", e);
	  } catch (AttributeNotExistsException e) {
		  log.warn("Synchronizer: checkMembersState, member doesn't have membershipExpiration attribute set.");
	  } catch (WrongAttributeAssignmentException e) {
		  log.error("Synchronizer: checkMembersState", e);
	  } catch (ParseException e) {
		  log.error("Synchronizer: checkMembersState", e);
	  }
  }

  public void initialize() throws InternalErrorException {
    String synchronizerPrincipal = "Synchronizer";
    this.sess = perunBl.getPerunSession(new PerunPrincipal(synchronizerPrincipal, ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
  }

  public PerunBl getPerun() {
    return perunBl;
  }

  public void setPerun(PerunBl perunBl) {
    this.perunBl = perunBl;
  }
}
