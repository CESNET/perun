package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.PerunPrincipal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.bl.PerunBl;

/**
 * Synchronizer, general tool for synchronization tasks.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
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

	/**
	 * Start synchronization of groups if not running.
	 *
	 * Method is triggered by Spring scheduler (every 5 minutes).
	 */
	public void synchronizeGroups() {
		if(perunBl.isPerunReadOnly()) {
			log.debug("This instance is just read only so skip synchronization of groups.");
			return;
		}

		if (synchronizeGroupsRunning.compareAndSet(false, true)) {
			try {
				log.debug("Synchronizer starting synchronizing the groups");
				this.perunBl.getGroupsManagerBl().synchronizeGroups(this.sess);
				if (!synchronizeGroupsRunning.compareAndSet(true, false)) {
					log.error("Synchronizer: group synchronization out of sync, resetting.");
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
	 * Perform check on members status and switch it between VALID and EXPIRED (if necessary).
	 * Switching is based on current date and their value of membership expiration.
	 *
	 * Method also log audit messages, that membership will expired in X days or expired X days ago.
	 *
	 * Method is triggered by Spring scheduler (at midnight everyday).
	 */
	public void checkMembersState() {
		if (perunBl.isPerunReadOnly()) {
			log.debug("This instance is just read only so skip checking members states.");
			return;
		}

		try {

			log.debug("Processing checkMemberState() on (to be) expired members.");

			// Only members with following statuses will be notified
			List<Status> allowedStatuses = new ArrayList<Status>();
			allowedStatuses.add(Status.VALID);
			allowedStatuses.add(Status.SUSPENDED);

			// get all available VOs
			List<Vo> vos = perunBl.getVosManagerBl().getVos(sess);
			Map<Integer, Vo> vosMap = new HashMap<Integer, Vo>();
			for (Vo vo : vos) {
				vosMap.put(vo.getId(), vo);
			}

			Calendar monthBefore = Calendar.getInstance();
			monthBefore.add(Calendar.MONTH, 1);

			// log message for all members which will expire in 30 days

			List<Member> expireInAMonth = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", monthBefore);
			for (Member m : expireInAMonth) {
				try {
					if (allowedStatuses.contains(m.getStatus())) {
						perunBl.getMembersManagerBl().canExtendMembershipWithReason(sess, m);
						getPerun().getAuditer().log(sess, "{} will expire in a month in {}.", m, vosMap.get(m.getVoId()));
					} else {
						log.debug("{} not notified about expiration, is not in VALID or SUSPENDED state.", m);
					}
				} catch (ExtendMembershipException ex) {
					if (!Objects.equals(ex.getReason(), ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD)) {
						// we don't care about other reasons (LoA), user can update it later
						getPerun().getAuditer().log(sess, "{} will expire in a month in {}.", m, vosMap.get(m.getVoId()));
					}
				}
			}

			// log message for all members which will expire in 14 days
			Calendar expireInA14Days = Calendar.getInstance();
			expireInA14Days.add(Calendar.DAY_OF_MONTH, 14);
			List<Member> expireIn14Days = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", expireInA14Days);
			for (Member m : expireIn14Days) {
				try {
					if (allowedStatuses.contains(m.getStatus())) {
						perunBl.getMembersManagerBl().canExtendMembershipWithReason(sess, m);
						getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 14, vosMap.get(m.getVoId()));
					} else {
						log.debug("{} not notified about expiration, is not in VALID or SUSPENDED state.", m);
					}
				} catch (ExtendMembershipException ex) {
					if (!Objects.equals(ex.getReason(), ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD)) {
						// we don't care about other reasons (LoA), user can update it later
						getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 14, vosMap.get(m.getVoId()));
					}
				}
			}

			// log message for all members which will expire in 7 days
			Calendar expireInA7Days = Calendar.getInstance();
			expireInA7Days.add(Calendar.DAY_OF_MONTH, 7);
			List<Member> expireIn7Days = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", expireInA7Days);
			for (Member m : expireIn7Days) {
				try {
					if (allowedStatuses.contains(m.getStatus())) {
						perunBl.getMembersManagerBl().canExtendMembershipWithReason(sess, m);
						getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 7, vosMap.get(m.getVoId()));
					} else {
						log.debug("{} not notified about expiration, is not in VALID or SUSPENDED state.", m);
					}
				} catch (ExtendMembershipException ex) {
					if (!Objects.equals(ex.getReason(), ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD)) {
						// we don't care about other reasons (LoA), user can update it later
						getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 7, vosMap.get(m.getVoId()));
					}
				}
			}

			// log message for all members which will expire tomorrow
			Calendar expireInADay = Calendar.getInstance();
			expireInADay.add(Calendar.DAY_OF_MONTH, 1);
			List<Member> expireIn1Days = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", expireInADay);
			for (Member m : expireIn1Days) {
				try {
					if (allowedStatuses.contains(m.getStatus())) {
						perunBl.getMembersManagerBl().canExtendMembershipWithReason(sess, m);
						getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 1, vosMap.get(m.getVoId()));
					} else {
						log.debug("{} not notified about expiration, is not in VALID or SUSPENDED state.", m);
					}
				} catch (ExtendMembershipException ex) {
					if (!Objects.equals(ex.getReason(), ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD)) {
						// we don't care about other reasons (LoA), user can update it later
						getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 1, vosMap.get(m.getVoId()));
					}
				}
			}

			// log message for all members which expired 7 days ago
			Calendar expiredWeekAgo = Calendar.getInstance();
			expiredWeekAgo.add(Calendar.DAY_OF_MONTH, -7);
			List<Member> expired7DaysAgo = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", expiredWeekAgo);
			// include expired in this case
			allowedStatuses.add(Status.EXPIRED);
			for (Member m : expired7DaysAgo) {
				if (allowedStatuses.contains(m.getStatus())) {
					getPerun().getAuditer().log(sess, "{} has expired {} days ago in {}.", m, 7, vosMap.get(m.getVoId()));
				} else {
					log.debug("{} not notified about expiration, is not in VALID, SUSPENDED or EXPIRED state.", m);
				}
			}

			// switch members, which expire today
			Calendar expireToday = Calendar.getInstance();
			List<Member> shouldBeExpired = perunBl.getSearcherBl().getMembersByExpiration(sess, "<=", expireToday);
			for (Member member : shouldBeExpired) {
				if (member.getStatus().equals(Status.VALID)) {
					try {
						perunBl.getMembersManagerBl().expireMember(sess, member);
						log.info("Switching {} to EXPIRE state, due to expiration {}.", member, (String) perunBl.getAttributesManagerBl().getAttribute(sess, member, "urn:perun:member:attribute-def:def:membershipExpiration").getValue());
					} catch (MemberNotValidYetException e) {
						log.error("Consistency error while trying to expire member {}, exception {}", member, e);
					}
				}
			}

			// switch members, which shouldn't be expired
			List<Member> shouldntBeExpired = perunBl.getSearcherBl().getMembersByExpiration(sess, ">", expireToday);
			for (Member member : shouldntBeExpired) {
				if (member.getStatus().equals(Status.EXPIRED)) {
					try {
						perunBl.getMembersManagerBl().validateMember(sess, member);
						log.info("Switching {} to VALID state, due to changed expiration {}.", member, (String) perunBl.getAttributesManagerBl().getAttribute(sess, member, "urn:perun:member:attribute-def:def:membershipExpiration").getValue());
					} catch (WrongAttributeValueException e) {
						log.error("Error during validating member {}, exception {}", member, e);
					} catch (WrongReferenceAttributeValueException e) {
						log.error("Error during validating member {}, exception {}", member, e);
					}
				}
			}

		} catch(InternalErrorException e){
			log.error("Synchronizer: checkMembersState, exception {}", e);
		} catch(AttributeNotExistsException e){
			log.warn("Synchronizer: checkMembersState, attribute definition for membershipExpiration doesn't exist, exception {}", e);
		} catch(WrongAttributeAssignmentException e){
			log.error("Synchronizer: checkMembersState, attribute name is from wrong namespace, exception {}", e);
		}
	}

	public void removeAllExpiredBans() {
		if(perunBl.isPerunReadOnly()) {
			log.debug("This instance is just read only so skip removing expired bans.");
			return;
		}

		try {
			getPerun().getResourcesManagerBl().removeAllExpiredBansOnResources(sess);
			getPerun().getFacilitiesManagerBl().removeAllExpiredBansOnFacilities(sess);
		} catch (InternalErrorException ex) {
			log.error("Synchronizer: removeAllExpiredBans, exception {}", ex);
		}
	}

	public void initialize() throws InternalErrorException {
		String synchronizerPrincipal = "perunSynchronizer";
		this.sess = perunBl.getPerunSession(
				new PerunPrincipal(synchronizerPrincipal, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());
	}

	public PerunBl getPerun() {
		return perunBl;
	}

	public void setPerun(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

}
