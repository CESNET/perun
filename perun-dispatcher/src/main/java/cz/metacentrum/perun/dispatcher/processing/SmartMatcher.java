package cz.metacentrum.perun.dispatcher.processing;

import java.util.Set;

import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer;
import cz.metacentrum.perun.dispatcher.model.Event;

/**
 * SmartMatcher is used to match Event header to EngineMessageProducer (queue).
 * Basically it decide to which Engine should Event match the Task using MatchingRules.
 *
 * @see cz.metacentrum.perun.dispatcher.processing.impl.SmartMatcherImpl
 * @see cz.metacentrum.perun.dispatcher.processing.EventProcessor
 * @see cz.metacentrum.perun.dispatcher.model.Event
 * @see cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer
 * @see cz.metacentrum.perun.dispatcher.model.MatchingRule
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface SmartMatcher {

	/**
	 * Return TRUE if Events header matches EngineMessageProducer queue by available MatchingRules.
	 *
	 * @param event Event to check match
	 * @param engineMessageProducer EngineMessageProducer queue to check match
	 * @return TRUE if matches / FALSE otherwise
	 */
	boolean doesItMatch(Event event, EngineMessageProducer engineMessageProducer);

	/**
	 * Load all MatchingRules from DB
	 */
	void loadAllRulesFromDB();

	/**
	 * Reload MatchingRules for specified Engine
	 *
	 * @param clientID ID of Engine to load MatchingRules
	 */
	void reloadRulesFromDBForEngine(Integer clientID);

	/**
	 * Return IDs of Engines we have any MatchingRules.
	 *
	 * @return Set of Engine IDs
	 */
	Set<Integer> getClientsWeHaveRulesFor();

}
