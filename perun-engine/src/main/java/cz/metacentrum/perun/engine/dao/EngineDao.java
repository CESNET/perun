package cz.metacentrum.perun.engine.dao;

import cz.metacentrum.perun.engine.exceptions.DispatcherNotConfiguredException;
import cz.metacentrum.perun.engine.exceptions.EngineNotConfiguredException;

/**
 * EngineDao
 * 
 * @author Michal Karm Babacek
 * 
 */
public interface EngineDao {

	void loadDispatcherAddress() throws DispatcherNotConfiguredException;

	void registerEngine() throws EngineNotConfiguredException;

	void checkIn();
}
