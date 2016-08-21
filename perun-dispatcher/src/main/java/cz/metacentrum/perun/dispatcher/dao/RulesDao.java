package cz.metacentrum.perun.dispatcher.dao;

import java.util.List;
import java.util.Map;

/**
 * DAO layer for loading MatchingRules.
 *
 * @see cz.metacentrum.perun.dispatcher.model.MatchingRule
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface RulesDao {

	/**
	 * Load all MatchingRules from DB.
	 *
	 * @return All MatchingRules grouped by Engine ID
	 */
	Map<Integer, List<String>> loadRoutingRules();

	/**
	 * Load MatchingRules for Engine by its ID.
	 *
	 * @param clientID ID of Engine to load MatchingRules
	 * @return List of MatchingRules
	 */
	List<String> loadRoutingRulesForEngine(int clientID);

}
