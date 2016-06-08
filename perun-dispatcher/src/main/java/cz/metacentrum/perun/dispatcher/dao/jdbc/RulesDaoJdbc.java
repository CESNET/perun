package cz.metacentrum.perun.dispatcher.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.dispatcher.dao.RulesDao;

/**
 * DAO layer for loading routing rules which map events to engines.
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 */
@Transactional
public class RulesDaoJdbc extends JdbcDaoSupport implements RulesDao {

	private RowMapper<EngineRules> allRulesMapper = new RowMapper<EngineRules>() {

		public EngineRules mapRow(ResultSet rs, int i) throws SQLException {
			EngineRules engineRules = new EngineRules();
			Integer clientID = rs.getInt("engine_id");
			engineRules.setEngineID(clientID);
			engineRules.setRoutingRules(loadRoutingRulesForEngine(clientID));
			return engineRules;
		}

	};

	@Override
	public Map<Integer, List<String>> loadRoutingRules() {
		List<EngineRules> results = this.getJdbcTemplate().query(
				"select engines.id as engine_id from engines", allRulesMapper);
		Map<Integer, List<String>> allRules = new HashMap<Integer, List<String>>();
		for (EngineRules engineRules : results) {
			allRules.put(engineRules.getEngineID(),
					engineRules.getRoutingRules());
		}
		return allRules;

	}

	@Override
	public List<String> loadRoutingRulesForEngine(int clientID) {
		return this
				.getJdbcTemplate()
				.queryForList(
						"select routing_rules.routing_rule from routing_rules, engine_routing_rule where routing_rules.id = engine_routing_rule.routing_rule_id and engine_routing_rule.engine_id = ?",
						new Integer[] { clientID }, String.class);
	}

	private class EngineRules {
		private Integer engineID;
		private List<String> routingRules;

		public void setEngineID(Integer engineID) {
			this.engineID = engineID;
		}

		public Integer getEngineID() {
			return engineID;
		}

		public void setRoutingRules(List<String> routingRules) {
			this.routingRules = routingRules;
		}

		public List<String> getRoutingRules() {
			return routingRules;
		}

	}

}
