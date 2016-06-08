package cz.metacentrum.perun.dispatcher.dao;

import cz.metacentrum.perun.dispatcher.exceptions.EngineNotConfiguredException;


/**
 * EngineDao
 * 
 * @author Michal Karm Babacek
 * 
 */
public interface EngineDao {

	void registerEngine(int engineId, String engineIpAddress, int enginePort) throws EngineNotConfiguredException;

	void checkIn(int engineId);
}
