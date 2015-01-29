package cz.metacentrum.perun.core.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
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
				synchronizeGroupsRunning.set(false);
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
					Date currentMembershipExpirationDate;
					// Read membershipExpiration and check it
					Attribute membersExpiration = perunBl.getAttributesManagerBl().getAttribute(sess, member, "urn:perun:member:attribute-def:def:membershipExpiration");
					if (membersExpiration.getValue() != null) {
						currentMembershipExpirationDate = BeansUtils.getDateFormatter().parse((String) membersExpiration.getValue());
						if (currentMembershipExpirationDate.before(now)) {
							// Expired membership, set status to expired only if the user hasn't been in suspended state
							if (!member.getStatus().equals(Status.EXPIRED) && !member.getStatus().equals(Status.SUSPENDED)) {
								try {
									perunBl.getMembersManagerBl().expireMember(sess, member);
									log.info("Switching {} to EXPIRE state, due to expiration {}.", member, (String) membersExpiration.getValue());
									log.debug("Switching member to EXPIRE state, additional info: membership expiration date='{}', system now date='{}'", currentMembershipExpirationDate, now);
								} catch (MemberNotValidYetException e) {
									log.error("Consistency error while trying to expire member {}, exception {}", member, e);
								}
							}
						} else if (member.getStatus().equals(Status.EXPIRED) || member.getStatus().equals(Status.DISABLED)) {
							// Validate member only if the previous status was EXPIRED or DISABLED
							try {
								perunBl.getMembersManagerBl().validateMember(sess, member);
								log.info("Switching {} to VALID state, due to changed expiration {}.", member, (String) membersExpiration.getValue());
								log.debug("Switching member to VALID state, additional info: membership expiration date='{}', system now date='{}'", currentMembershipExpirationDate, now);
							} catch (WrongAttributeValueException e) {
								log.error("Error during validating member {}, exception {}", member, e);
							} catch (WrongReferenceAttributeValueException e) {
								log.error("Error during validating member {}, exception {}", member, e);
							}
						}

						//check for members' expiration in the future on in the past
						int daysToExpire = (int) TimeUnit.DAYS.convert(currentMembershipExpirationDate.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
						switch(daysToExpire) {
							case 30: case 14: case 7: case 1:
									getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", member, daysToExpire, vo);
									break;
							case -7:
									getPerun().getAuditer().log(sess, "{} has expired {} days ago in {}.", member, daysToExpire*(-1), vo);
									break;
						}
					}
				}

			}
		} catch (InternalErrorException e) {
			log.error("Synchronizer: checkMembersState, exception {}", e);
		} catch (AttributeNotExistsException e) {
			log.warn("Synchronizer: checkMembersState, attribute definition for membershipExpiration doesn't exist, exception {}", e);
		} catch (WrongAttributeAssignmentException e) {
			log.error("Synchronizer: checkMembersState, attribute name is from wrong namespace, exception {}", e);
		} catch (ParseException e) {
			log.error("Synchronizer: checkMembersState, member expiration String cannot be parsed, exception {}", e);
		}
	}

	public void initialize() throws InternalErrorException {
		String synchronizerPrincipal = "Synchronizer";
		this.sess = perunBl.getPerunSession(new PerunPrincipal(synchronizerPrincipal, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
	}

	public PerunBl getPerun() {
		return perunBl;
	}

	public void setPerun(PerunBl perunBl) {
		this.perunBl = perunBl;
	}
}
