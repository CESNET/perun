package cz.metacentrum.perun.dispatcher.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.dao.ProcessingRuleDao;
import cz.metacentrum.perun.dispatcher.model.ProcessingRule;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
@Transactional
public class ProcessingRuleDaoJdbc extends JdbcDaoSupport implements ProcessingRuleDao {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(ProcessingRuleDaoJdbc.class);

	public static final RowMapper<ProcessingRule> PROCESSING_RULE_ROWMAPPER = new RowMapper<ProcessingRule>() {

		public ProcessingRule mapRow(ResultSet rs, int i) throws SQLException {

			ProcessingRule processingRule = new ProcessingRule();
			processingRule.setId(rs.getInt("id"));
			processingRule.setRule(rs.getString("processing_rule"));

			return processingRule;
		}

	};

	@Override
	public Map<ProcessingRule, List<Service>> getRules(PerunSession perunSession) throws ServiceNotExistsException,InternalErrorException, PrivilegeException {
		Map<ProcessingRule, List<Service>> rulesServices = new HashMap<ProcessingRule, List<Service>>();
		for (ProcessingRule processingRule : listProcessingRules()) {
			rulesServices.put(processingRule,getServices(perunSession, processingRule));
		}
		return rulesServices;
	}

	private List<ProcessingRule> listProcessingRules() {
		return this.getJdbcTemplate().query("select id, processing_rule from processing_rules",PROCESSING_RULE_ROWMAPPER);
	}

	private List<Service> getServices(PerunSession perunSession, ProcessingRule processingRule) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {

		List<Service> services = this.getJdbcTemplate().query(
				"select " + ServicesManagerImpl.serviceMappingSelectQuery + " from service_processing_rule " +
						"left join services on services.id=service_processing_rule.service_id where "
						+ "service_processing_rule.processing_rule_id = ?", ServicesManagerImpl.SERVICE_MAPPER, processingRule.getId());
		return services;
	}

}
