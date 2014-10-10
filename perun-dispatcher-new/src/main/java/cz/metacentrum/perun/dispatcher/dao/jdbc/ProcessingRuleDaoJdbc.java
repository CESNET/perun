package cz.metacentrum.perun.dispatcher.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ErrorManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.dao.ProcessingRuleDao;
import cz.metacentrum.perun.dispatcher.model.ProcessingRule;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@Transactional
public class ProcessingRuleDaoJdbc extends JdbcDaoSupport implements
		ProcessingRuleDao {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(ProcessingRuleDaoJdbc.class);

	@Autowired
	private GeneralServiceManager generalServiceManager;

	public static final RowMapper<ProcessingRule> PROCESSING_RULE_ROWMAPPER = new RowMapper<ProcessingRule>() {

		public ProcessingRule mapRow(ResultSet rs, int i) throws SQLException {

			ProcessingRule processingRule = new ProcessingRule();
			processingRule.setId(rs.getInt("id"));
			processingRule.setRule(rs.getString("processing_rule"));

			return processingRule;
		}

	};

	@Override
	public Map<ProcessingRule, List<ExecService>> getRules(
			PerunSession perunSession) throws ServiceNotExistsException,
			InternalErrorException, PrivilegeException {
		Map<ProcessingRule, List<ExecService>> rulesExecServices = new HashMap<ProcessingRule, List<ExecService>>();
		for (ProcessingRule processingRule : listProcessingRules()) {
			rulesExecServices.put(processingRule,
					getExecServices(perunSession, processingRule));
		}
		return rulesExecServices;
	}

	private List<ProcessingRule> listProcessingRules() {
		return this.getJdbcTemplate().query(
				"select id, processing_rule from processing_rules",
				PROCESSING_RULE_ROWMAPPER);
	}

	private List<ExecService> getExecServices(PerunSession perunSession,
			ProcessingRule processingRule) throws ServiceNotExistsException,
			InternalErrorException, PrivilegeException {
		List<ExecService> execServicesTiedToTheProcessingRule = new ArrayList<ExecService>();
		List<Integer> services = this.getJdbcTemplate().queryForList(
				"select service_id from service_processing_rule where "
						+ "service_processing_rule.processing_rule_id = ?",
				new Integer[] { processingRule.getId() }, Integer.class);
		for (Integer serviceId : services) {
			execServicesTiedToTheProcessingRule.addAll(generalServiceManager
					.listExecServices(perunSession, serviceId));
		}
		return execServicesTiedToTheProcessingRule;
	}

	public GeneralServiceManager getGeneralServiceManager() {
		return generalServiceManager;
	}

	public void setGeneralServiceManager(
			GeneralServiceManager generalServiceManager) {
		this.generalServiceManager = generalServiceManager;
	}
}
