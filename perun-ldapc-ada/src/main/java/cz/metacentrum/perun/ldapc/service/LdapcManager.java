package cz.metacentrum.perun.ldapc.service;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.rpclib.api.RpcCaller;

public interface LdapcManager {

	/**
	 * Start processing incommming events from Perun Auditer.
	 */
	void startProcessingEvents();

	/**
	 * Stop processing incommming events from Perun Auditer.
	 */
	void stopProcessingEvents();

	void synchronize();
	
	/**
	 * Sets RPCCaller.
	 *
	 * @param rpcCaller
	 */
	void setRpcCaller(RpcCaller rpcCaller);

	/**
	 * Gets RPCCaller.
	 *
	 * @return rpcCaller
	 */
	RpcCaller getRpcCaller();
	
	public Perun getPerunBl();
	
	public void setPerunBl(Perun perunBl);

	public PerunSession getPerunSession() throws InternalErrorException;

	public PerunPrincipal getPerunPrincipal();

	public void setPerunPrincipal(PerunPrincipal perunPrincipal);
	
}
