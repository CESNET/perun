package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.PerunPrincipal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		if(perunBl.isPerunReadOnly()) {
			log.debug("This instance is just read only so skip checking members states.");
			return;
		}

		try {

			// we must retrieve current date only once per method run
			Calendar compareDate = Calendar.getInstance();

			// get all available VOs
			List<Vo> vos = perunBl.getVosManagerBl().getVos(sess);
			Map<Integer, Vo> vosMap = new HashMap<Integer, Vo>();
			for (Vo vo : vos) {
				vosMap.put(vo.getId(), vo);
			}

			// log message for all members which will expire in 30 days
			compareDate.add(Calendar.DAY_OF_MONTH, 30);
			List<Member> expireIn30Days = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", compareDate);
			for (Member m : expireIn30Days) {
				getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 30, vosMap.get(m.getVoId()));
			}

			// log message for all members which will expire in 14 days
			compareDate.add(Calendar.DAY_OF_MONTH, -16);
			List<Member> expireIn14Days = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", compareDate);
			for (Member m : expireIn14Days) {
				getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 14, vosMap.get(m.getVoId()));
			}

			// log message for all members which will expire in 7 days
			compareDate.add(Calendar.DAY_OF_MONTH, -7);
			List<Member> expireIn7Days = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", compareDate);
			for (Member m : expireIn7Days) {
				getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 7, vosMap.get(m.getVoId()));
			}

			// log message for all members which will expire tomorrow
			compareDate.add(Calendar.DAY_OF_MONTH, -6);
			List<Member> expireIn1Days = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", compareDate);
			for (Member m : expireIn1Days) {
				getPerun().getAuditer().log(sess, "{} will expire in {} days in {}.", m, 1, vosMap.get(m.getVoId()));
			}

			// log message for all members which expired 7 days ago
			compareDate.add(Calendar.DAY_OF_MONTH, -8);
			List<Member> expired7DaysAgo = perunBl.getSearcherBl().getMembersByExpiration(sess, "=", compareDate);
			for (Member m : expired7DaysAgo) {
				getPerun().getAuditer().log(sess, "{} has expired {} days ago in {}.", m, 7, vosMap.get(m.getVoId()));
			}

			// switch members, which expire today
			compareDate.add(Calendar.DAY_OF_MONTH, 7);
			List<Member> shouldBeExpired = perunBl.getSearcherBl().getMembersByExpiration(sess, "<=", compareDate);
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
			List<Member> shouldntBeExpired = perunBl.getSearcherBl().getMembersByExpiration(sess, ">", compareDate);
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

		} catch (InternalErrorException e) {
			log.error("Synchronizer: checkMembersState, exception {}", e);
		} catch (AttributeNotExistsException e) {
			log.warn("Synchronizer: checkMembersState, attribute definition for membershipExpiration doesn't exist, exception {}", e);
		} catch (WrongAttributeAssignmentException e) {
			log.error("Synchronizer: checkMembersState, attribute name is from wrong namespace, exception {}", e);
		}

	}

	public void initialize() throws InternalErrorException {
		String synchronizerPrincipal = "perunSynchronizer";
		this.sess = perunBl.getPerunSession(new PerunPrincipal(synchronizerPrincipal, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
	}

	public PerunBl getPerun() {
		return perunBl;
	}

	public void setPerun(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

}
