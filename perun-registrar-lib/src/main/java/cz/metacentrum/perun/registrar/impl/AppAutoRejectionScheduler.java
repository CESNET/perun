package cz.metacentrum.perun.registrar.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SearcherBl;
import cz.metacentrum.perun.core.impl.Synchronizer;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcPerunTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class handling auto rejection of expired applications for VOs and Groups
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class AppAutoRejectionScheduler {

	private final static Logger log = LoggerFactory.getLogger(Synchronizer.class);

	private JdbcPerunTemplate jdbc;
	private PerunSession sess;
	private PerunBl perun;
	private RegistrarManager registrarManager;
	private final SearcherBl searcherBl;

	private static final String A_VO_APP_EXP_RULES = "urn:perun:vo:attribute-def:def:applicationExpirationRules";
	private static final String A_GROUP_APP_EXP_RULES = "urn:perun:group:attribute-def:def:applicationExpirationRules";

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbc = new JdbcPerunTemplate(dataSource);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	public PerunBl getPerun() {
		return perun;
	}

	@Autowired
	public void setPerun(PerunBl perun) {
		this.perun = perun;
	}

	public RegistrarManager getRegistrarManager() { return registrarManager; }

	@Autowired
	public void setRegistrarManager(RegistrarManager registrarManager) { this.registrarManager = registrarManager; }

	/**
	 * Constructor for unit tests
	 *
	 * @param perun PerunBl bean
	 */
	public AppAutoRejectionScheduler(PerunBl perun, SearcherBl searcherBl) {
		this.perun = perun;
		this.searcherBl = searcherBl;
		initialize();
	}

	public void initialize() {
		this.sess = perun.getPerunSession(
			new PerunPrincipal("perunRegistrar", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
			new PerunClient());
	}

	/**
	 * Perform check on applications state and expiration attribute and reject them, if it is necessary.
	 * Rejection is based on current date and their value of application expiration.
	 *
	 * Method is triggered by Spring scheduler (at midnight everyday).
	 *
	 * @throws VoNotExistsException if vo not exist (it shouldn't happen)
	 * @throws GroupNotExistsException if group not exist (it shouldn't happen)
	 */
	public void checkApplicationsExpiration() throws VoNotExistsException, GroupNotExistsException {
		List<Vo> vos = getAllEligibleVos();
		// check applications expiration in eligible vos
		try {
			voApplicationsAutoRejection(vos);
		} catch(InternalErrorException | PerunException e){
			log.error("Synchronizer: voApplicationsAutoRejection", e);
		}

		List<Group> groups = getAllEligibleGroups();
		// check applications expiration in eligible groups
		try {
			groupApplicationsAutoRejection(groups);
		} catch (InternalErrorException | PerunException e){
			log.error("Synchronizer: groupApplicationsAutoRejection", e);
		}
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
	 * Checks all applications for given vos and if some application is expired (according to expiration rules set by
	 * VO Manager), then rejects this application.
	 *
	 * @param vos eligible virtual organizations
	 * @throws PerunException perun exception
	 */
	private void voApplicationsAutoRejection(List<Vo> vos) throws PerunException {
		List<String> states = new ArrayList<>();
		states.add("NEW");
		states.add("VERIFIED");

		for (Vo vo : vos) {
			Attribute expiration = perun.getAttributesManagerBl().getAttribute(sess, vo, A_VO_APP_EXP_RULES);
			if (expiration.getValue() != null) {
				List<Application> applications = registrarManager.getApplicationsForVo(sess, vo, states, false);
				rejectExpiredApplications(applications, expiration);
			}
		}
	}

	/**
	 * Gets all existing groups and then checks all applications for this groups and if some application is expired
	 * (according to expiration rules set by VO Manager), then rejects this application.
	 *
	 * @param groups eligible groups
	 * @throws PerunException perun exception
	 */
	private void groupApplicationsAutoRejection(List<Group> groups) throws PerunException {
		List<String> states = new ArrayList<>();
		states.add("NEW");
		states.add("VERIFIED");

		for (Group group : groups) {
			Attribute expiration = perun.getAttributesManagerBl().getAttribute(sess, group, A_GROUP_APP_EXP_RULES);
			if (expiration.getValue() != null) {
				List<Application> applications = registrarManager.getApplicationsForGroup(sess, group, states);
				rejectExpiredApplications(applications, expiration);
			}
		}
	}

	/**
	 * Compares date of last modification of application to values in expiration attribute and if finds expired application, then
	 * rejects it.
	 *
	 * @param applications applications
	 * @param expiration attribute with number of days to application expiration
	 */
	private void rejectExpiredApplications (List<Application> applications, Attribute expiration) {
		Map<String, String> attrValue = expiration.valueAsMap();
		for(Application application : applications) {
			String date = application.getModifiedAt();
			LocalDate modifiedAt = LocalDate.parse(date.substring(0, 10));
			LocalDate now = getCurrentLocalDate();
			if (application.getState() == Application.AppState.NEW && attrValue.containsKey("emailVerification")) {
				int expirationAppWaitingForEmail = Integer.parseInt(attrValue.get("emailVerification"));
				if (now.minusDays(expirationAppWaitingForEmail).isAfter(modifiedAt)) {
					String reasonForVo = "Your application to VO " + application.getVo().getName() + " was auto rejected, because you didn't verify your email address.";
					String reasonForGroup = application.getGroup() == null ? "" : "Your application to group " + application.getGroup().getName() + " was auto rejected, because you didn't verify your email address.";
					rejectWithReason(application, reasonForVo, reasonForGroup);
					continue;
				}
			} else if (attrValue.containsKey("ignoredByAdmin")) {
				int expirationAppIgnoredByAdmin = Integer.parseInt(attrValue.get("ignoredByAdmin"));
				if (now.minusDays(expirationAppIgnoredByAdmin).isAfter(modifiedAt)) {
					String reasonForVo = "Your application to VO " + application.getVo().getName() + " was auto rejected, because admin didn't approve your application in a timely manner.";
					String reasonForGroup = application.getGroup() == null ? "" : "Your application to group " + application.getGroup().getName() + " was auto rejected, because admin didn't approve your application in a timely manner.";
					rejectWithReason(application, reasonForVo, reasonForGroup);
				}
			}
		}
	}

	/**
	 * Rejects given application to Vo or group due to given reason.
	 *
	 * @param application application to reject
	 * @param reasonForVo reason for reject VO application
	 * @param reasonForGroup reason for reject group application
	 */
	private void rejectWithReason (Application application, String reasonForVo, String reasonForGroup) {
		try {
			if (application.getGroup() == null) {
				registrarManager.rejectApplication(sess, application.getId(), reasonForVo);
			} else {
				registrarManager.rejectApplication(sess, application.getId(), reasonForGroup);
			}
		} catch (PerunException e) {
			log.error("Failed to reject expired application: {}", application, e);
		}

	}

	/**
	 * Selects all vos from database, in which could be some expired applications acceptable for auto rejection.
	 *
	 * @return list of vos
	 * @throws VoNotExistsException if vo not exist (it shouldn't happen)
	 */
	private List<Vo> getAllEligibleVos() throws VoNotExistsException {
		List<Integer> vosIds = searcherBl.getVosIdsForAppAutoRejection();
		List<Vo> vos = new ArrayList<>();
		for(int voId : vosIds){
			vos.add(perun.getVosManagerBl().getVoById(sess, voId));
		}
		return vos;

	}

	/**
	 * Selects all groups from database, in which could be some expired applications acceptable for auto rejection.
	 *
	 * @return list of groups
	 * @throws GroupNotExistsException if group not exist (it shouldn't happen)
	 */
	private List<Group> getAllEligibleGroups() throws GroupNotExistsException {
		List<Integer> groupsIds = searcherBl.getGroupsIdsForAppAutoRejection();
		List<Group> groups = new ArrayList<>();
		for(int groupId : groupsIds){
			groups.add(perun.getGroupsManagerBl().getGroupById(sess, groupId));
		}
		return groups;

	}
}
