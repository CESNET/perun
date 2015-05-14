package cz.metacentrum.perun.dispatcher.dao;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface RulesDao {

	Map<Integer, List<String>> loadRoutingRules();

	List<String> loadRoutingRulesForEngine(int clientID);
}
