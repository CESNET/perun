package cz.metacentrum.perun.registrar.impl;

import cz.metacentrum.perun.audit.events.ExpirationNotifScheduler.MembershipExpirationInDays;
import cz.metacentrum.perun.audit.events.ExpirationNotifScheduler.MembershipExpirationInMonthNotification;
import cz.metacentrum.perun.audit.events.ExpirationNotifScheduler.MembershipExpired;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberExpired;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.Synchronizer;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ExpirationNotifScheduler {

	private final static Logger log = LoggerFactory.getLogger(Synchronizer.class);
	private PerunSession sess;

	private PerunBl perun;
	private JdbcPerunTemplate jdbc;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbc = new JdbcPerunTemplate(dataSource);
	}

	public PerunBl getPerun() {
		return perun;
	}

	@Autowired
	public void setPerun(PerunBl perun) {
		this.perun = perun;
	}

	public ExpirationNotifScheduler() {
	}

	/**
	 * Constructor for unit tests
	 *
	 * @param perun
	 * @throws Exception
	 */
	public ExpirationNotifScheduler(PerunBl perun) throws Exception {
		this.perun = perun;
		initialize();
	}

	public void initialize() throws InternalErrorException {
		String synchronizerPrincipal = "perunSynchronizer";
		this.sess = perun.getPerunSession(
				new PerunPrincipal(synchronizerPrincipal, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());
	}

	/**
	 * Functional interface defining audit action when members will expire in given time
	 */
	@FunctionalInterface
	private interface ExpirationAuditAction<TA extends Auditer, TS extends PerunSession, TM extends Member, TV extends Vo> {
		void callOn(TA auditer, TS session, TM member, TV vo) throws InternalErrorException;
	}

	/**
	 * Enum defining time before member expiration and action that should be logged in auditer when it happens
	 */
	private enum ExpirationPeriod {
		MONTH(((auditer, sess, member, vo) ->
			auditer.log(sess, new MembershipExpirationInMonthNotification(member, vo))
		)),
		DAYS_14(((auditer, sess, member, vo) ->
			auditer.log(sess, new MembershipExpirationInDays(member, 14, vo))
		)),
		DAYS_7(((auditer, sess, member, vo) ->
			auditer.log(sess, new MembershipExpirationInDays(member, 7, vo))
		)),
		DAYS_1(((auditer, sess, member, vo) ->
			auditer.log(sess, new MembershipExpirationInDays(member, 1, vo))
		));

		private ExpirationAuditAction<Auditer, PerunSession, Member, Vo> expirationAuditAction;

		public ExpirationAuditAction<Auditer, PerunSession, Member, Vo> getExpirationAuditAction() {
			return this.expirationAuditAction;
		}

		ExpirationPeriod(ExpirationAuditAction<Auditer, PerunSession, Member, Vo> expirationAuditAction) {
			this.expirationAuditAction = expirationAuditAction;
		}
	}

	/**
	 * Finds members who should expire in given time, and if they did not submit an extension
	 * application, notification is logged in auditer
	 *
	 * @param allowedStatuses members that has one of this statuses will be checked
	 * @param vosMap map containing all Vos from perun
	 * @param timeBeforeExpiration time used for check
	 * @param expirationPeriod Expiration period, should correspond with given Calendar
	 * @throws InternalErrorException internal error
	 */
	private void auditInfoAboutIncomingMembersExpirationInGivenTime(List<Status> allowedStatuses, Map<Integer, Vo> vosMap, Calendar timeBeforeExpiration, ExpirationPeriod expirationPeriod) throws InternalErrorException {
		List<Member> expireInTime = perun.getSearcherBl().getMembersByExpiration(sess, "=", timeBeforeExpiration);
		for (Member m : expireInTime) {
			try {
				if (allowedStatuses.contains(m.getStatus())) {
					perun.getMembersManagerBl().canExtendMembershipWithReason(sess, m);
					if (didntSubmitExtensionApplication(m)) {
						// still didn't apply for extension

						expirationPeriod.getExpirationAuditAction().callOn(getPerun().getAuditer(), sess, m, vosMap.get(m.getId()));
					} else {
						log.debug("{} not notified about expiration, has submitted - pending application.", m);
					}
				} else {
					log.debug("{} not notified about expiration, is not in VALID or SUSPENDED state.", m);
				}
			} catch (ExtendMembershipException ex) {
				if (!Objects.equals(ex.getReason(), ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD)) {
					// we don't care about other reasons (LoA), user can update it later
					if (didntSubmitExtensionApplication(m)) {
						// still didn't apply for extension
						ExpirationPeriod.MONTH.getExpirationAuditAction().callOn(getPerun().getAuditer(), sess, m, vosMap.get(m.getVoId()));
					} else {
						log.debug("{} not notified about expiration, has submitted - pending application.", m);
					}
				}
			}
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
		if (perun.isPerunReadOnly()) {
			log.debug("This instance is just read only so skip checking members states.");
			return;
		}

		List<Vo> vos;
		try {
			// get all available VOs
			vos = perun.getVosManagerBl().getVos(sess);
		} catch (InternalErrorException e) {
			log.error("Synchronizer: checkMembersState, failed to get all vos exception {}", e);
			return;
		}

		log.debug("Processing checkMemberState() on (to be) expired members.");

		// check group expiration in vos
		try {
			checkGroupMembersState(vos);
		} catch (InternalErrorException e) {
			log.error("checkGroupMembersState failed", e);
		}

		// check vo membership expiration
		try {
			checkVoMembersState(vos);
		} catch(InternalErrorException e){
			log.error("Synchronizer: checkMembersState, exception {}", e);
		} catch(AttributeNotExistsException e){
			log.warn("Synchronizer: checkMembersState, attribute definition for membershipExpiration doesn't exist, exception {}", e);
		} catch(WrongAttributeAssignmentException e){
			log.error("Synchronizer: checkMembersState, attribute name is from wrong namespace, exception {}", e);
		}
	}

	/**
	 * Checks state of members in given vos
	 *
	 * @param vos vos
	 * @throws InternalErrorException internal error
	 * @throws WrongAttributeAssignmentException error
	 * @throws AttributeNotExistsException error
	 */
	private void checkVoMembersState(List<Vo> vos) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		// Only members with following statuses will be notified
		List<Status> allowedStatuses = new ArrayList<>();
		allowedStatuses.add(Status.VALID);
		allowedStatuses.add(Status.SUSPENDED);

		Map<Integer, Vo> vosMap = new HashMap<>();
		for (Vo vo : vos) {
			vosMap.put(vo.getId(), vo);
		}

		auditIncomingExpirations(allowedStatuses, vosMap);

		auditOldExpirations(allowedStatuses, vosMap);

		Calendar today = Calendar.getInstance();
		expireMembers(today);
		validateMembers(today);
	}

	/**
	 * Validates member whose expiration is set after the given date
	 *
	 * @param date date
	 * @throws InternalErrorException internal error
	 * @throws WrongAttributeAssignmentException error
	 * @throws AttributeNotExistsException error
	 */
	private void validateMembers(Calendar date) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		List<Member> shouldntBeExpired = perun.getSearcherBl().getMembersByExpiration(sess, ">", date);
		for (Member member : shouldntBeExpired) {
			if (member.getStatus().equals(Status.EXPIRED)) {
				try {
					perun.getMembersManagerBl().validateMember(sess, member);
					log.info("Switching {} to VALID state, due to changed expiration {}.", member, perun.getAttributesManagerBl().getAttribute(sess, member, "urn:perun:member:attribute-def:def:membershipExpiration").getValue());
				} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
					log.error("Error during validating member {}, exception {}", member, e);
				}
			}
		}
	}

	/**
	 * Expires members whose expiration is set to given date or before it.
	 *
	 * @throws InternalErrorException internal error
	 * @throws WrongAttributeAssignmentException error
	 * @throws AttributeNotExistsException error
	 */
	private void expireMembers(Calendar date) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		List<Member> shouldBeExpired = perun.getSearcherBl().getMembersByExpiration(sess, "<=", date);
		for (Member member : shouldBeExpired) {
			if (member.getStatus().equals(Status.VALID)) {
				try {
					perun.getMembersManagerBl().expireMember(sess, member);
					log.info("Switching {} to EXPIRE state, due to expiration {}.", member, perun.getAttributesManagerBl().getAttribute(sess, member, "urn:perun:member:attribute-def:def:membershipExpiration").getValue());
				} catch (MemberNotValidYetException e) {
					log.error("Consistency error while trying to expire member {}, exception {}", member, e);
				}
			}
		}
	}

	/**
	 * Logs expirations that happened a week ago
	 *
	 * @param allowedStatuses allowed Statuses
	 * @param vosMap vos
	 * @throws InternalErrorException internal error
	 */
	private void auditOldExpirations(List<Status> allowedStatuses, Map<Integer, Vo> vosMap) throws InternalErrorException {
		// log message for all members which expired 7 days ago
		Calendar expiredWeekAgo = Calendar.getInstance();
		expiredWeekAgo.add(Calendar.DAY_OF_MONTH, -7);
		List<Member> expired7DaysAgo = perun.getSearcherBl().getMembersByExpiration(sess, "=", expiredWeekAgo);
		// include expired in this case
		allowedStatuses.add(Status.EXPIRED);
		for (Member m : expired7DaysAgo) {
			if (allowedStatuses.contains(m.getStatus())) {
				if (didntSubmitExtensionApplication(m)) {
					// still didn't apply for extension
					getPerun().getAuditer().log(sess, new MembershipExpired(m, 7, vosMap.get(m.getVoId())));
				} else {
					log.debug("{} not notified about expiration, has submitted - pending application.", m);
				}
			} else {
				log.debug("{} not notified about expiration, is not in VALID, SUSPENDED or EXPIRED state.", m);
			}
		}

	}

	/**
	 * Logs incoming expirations into auditer
	 *
	 * @param allowedStatuses allowed statues
	 * @param vosMap vos
	 * @throws InternalErrorException internal error
	 */
	private void auditIncomingExpirations(List<Status> allowedStatuses, Map<Integer, Vo> vosMap) throws InternalErrorException {
		Calendar monthBefore = Calendar.getInstance();
		monthBefore.add(Calendar.MONTH, 1);

		// log message for all members which will expire in 30 days
		auditInfoAboutIncomingMembersExpirationInGivenTime(allowedStatuses, vosMap, monthBefore, ExpirationPeriod.MONTH);

		// log message for all members which will expire in 14 days
		Calendar expireInA14Days = Calendar.getInstance();
		expireInA14Days.add(Calendar.DAY_OF_MONTH, 14);
		auditInfoAboutIncomingMembersExpirationInGivenTime(allowedStatuses, vosMap, expireInA14Days, ExpirationPeriod.DAYS_14);

		// log message for all members which will expire in 7 days
		Calendar expireInA7Days = Calendar.getInstance();
		expireInA7Days.add(Calendar.DAY_OF_MONTH, 7);
		auditInfoAboutIncomingMembersExpirationInGivenTime(allowedStatuses, vosMap, expireInA7Days, ExpirationPeriod.DAYS_7);

		// log message for all members which will expire tomorrow
		Calendar expireInADay = Calendar.getInstance();
		expireInADay.add(Calendar.DAY_OF_MONTH, 1);
		auditInfoAboutIncomingMembersExpirationInGivenTime(allowedStatuses, vosMap, expireInADay, ExpirationPeriod.DAYS_1);
	}

	/**
	 * Finds members in given group and if they expire on given date and they have
	 * VALID MemberGroupState, switch them to EXPIRED
	 * @param group given date
	 * @param calendar current date
	 * @throws InternalErrorException internal error
	 */
	private void checkGroupMemberExpiration(Group group, Calendar calendar) throws InternalErrorException {
		List<Member> shouldBeExpired = perun.getSearcherBl().getMembersByGroupExpiration(sess, group, "<=", calendar);
		shouldBeExpired.stream()
				//read members current group status
				.filter(member -> {
					try {
						return perun.getGroupsManagerBl().getDirectMemberGroupStatus(sess, member, group).equals(MemberGroupStatus.VALID);
					} catch (InternalErrorException e) {
						log.error("Synchronizer: checkGroupMemberExpiration failed to read member's state in group. Member: {}, Group: {}, Exception: {}", member, group, e);
						return false;
					}
				})
				.forEach(member -> {
					try {
						perun.getGroupsManagerBl().expireMemberInGroup(sess, member, group);
						log.info("Switching {} in {} to EXPIRED state, due to expiration {}.", member, group, perun.getAttributesManagerBl().getAttribute(sess, member, group, "urn:perun:member_group:attribute-def:def:membershipExpiration").getValue());
					} catch (InternalErrorException e) {
						log.error("Consistency error while trying to expire member {} in {}, exception {}", member, group, e);
					} catch (AttributeNotExistsException e) {
						log.warn("Synchronizer: checkGroupMembersState, attribute definition for membershipExpiration in group doesn't exist, exception {}", e);
					} catch(WrongAttributeAssignmentException e){
						log.error("Synchronizer: checkMembersState, attribute name is from wrong namespace, exception {}", e);
					}
				});
	}

	/**
	 * Finds members in given group which should be valid and if they are expired,
	 * switch them to VALID state in given group
	 *
	 * @param group group where members are searched
	 * @param calendar current date
	 * @throws InternalErrorException internal error
	 */
	private void checkGroupMemberValidation(Group group, Calendar calendar) throws InternalErrorException {
		List<Member> shouldNotBeExpired = perun.getSearcherBl().getMembersByGroupExpiration(sess, group, ">", calendar);
		shouldNotBeExpired.stream()
				//read members current group status
				.filter(member -> {
					try {
						return perun.getGroupsManagerBl().getDirectMemberGroupStatus(sess, member, group).equals(MemberGroupStatus.EXPIRED);
					} catch (InternalErrorException e) {
						log.error("Synchronizer: checkGroupMemberExpiration failed to read member's state in group. Member: {}, Group: {}, Exception: {}", member, group, e);
						return false;
					}
				})
				.forEach(member -> {
					try {
						perun.getGroupsManagerBl().validateMemberInGroup(sess, member, group);
						log.info("Switching {} in {} to VALID state, due to changed expiration {}.", member, group, perun.getAttributesManagerBl().getAttribute(sess, member, group, "urn:perun:member_group:attribute-def:def:membershipExpiration").getValue());
					} catch (InternalErrorException e) {
						log.error("Error during validating member {} in {}, exception {}", member, group, e);
					} catch (AttributeNotExistsException e) {
						log.warn("Synchronizer: checkGroupMemberValidation, attribute definition for membershipExpiration in group doesn't exist, exception {}", e);
					} catch(WrongAttributeAssignmentException e){
						log.error("Synchronizer: checkGroupMemberValidation, attribute name is from wrong namespace, exception {}", e);
					}
				});
	}

	/**
	 * Check members states in groups from given Vos.
	 *
	 * @param vos vos
	 * @throws InternalErrorException internal error
	 */
	private void checkGroupMembersState(List<Vo> vos) throws InternalErrorException {

		List<Group> allGroups = new ArrayList<>();

		for (Vo vo : vos) {
			allGroups.addAll(perun.getGroupsManagerBl().getGroups(sess, vo));
		}

		Calendar today = Calendar.getInstance();

		// remove member groups
		allGroups = allGroups.stream()
				.filter(group -> !group.getName().equals("members"))
				.collect(Collectors.toList());

		for (Group group : allGroups) {

			// check members which should expire today
			checkGroupMemberExpiration(group, today);

			// check members which should be validated today
			checkGroupMemberValidation(group, today);
		}
	}

	/**
	 * Check if member didn't submit new extension application - in such case, do not send expiration notifications
	 *
	 * @param member Member to check applications for
	 * @return TRUE = didn't submit application / FALSE = otherwise
	 */
	private boolean didntSubmitExtensionApplication(Member member) {

		try {
			Application application = jdbc.queryForObject(RegistrarManagerImpl.APP_SELECT + " where a.id=(select max(id) from application where vo_id=? and apptype=? and user_id=? )", RegistrarManagerImpl.APP_MAPPER, member.getVoId(), String.valueOf(Application.AppType.EXTENSION), member.getUserId());
			return !Arrays.asList(Application.AppState.NEW, Application.AppState.VERIFIED).contains(application.getState());
		} catch (EmptyResultDataAccessException ex) {
			// has no application submitted
			return true;
		} catch (Exception ex) {
			log.error("Unable to check if {} has submitted pending application: {}.", member, ex);
			return true;
		}

	}

}
