package cz.metacentrum.perun.engine.service;

import cz.metacentrum.perun.engine.exceptions.DispatcherNotConfiguredException;
import cz.metacentrum.perun.engine.exceptions.EngineNotConfiguredException;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.rpclib.api.RpcCaller;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface EngineManager {

	void startMessaging();

	void loadSchedulingPool();

	void switchUnfinishedTasksToERROR();

	RpcCaller getRpcCaller();
}
