package cz.metacentrum.perun.rpclib.api;

import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Interface for Java RPC Client
 *
 * @author Michal Šťava
 * @author Pavel Zlámal
 */
public interface RpcCaller {

	/**
	 * Call RPC API method without params
	 *
	 * @param managerName called manager
	 * @param methodName called method
	 * @return JsonDeserializer with response
	 * @throws PerunException If anything fails
	 */
	public Deserializer call(String managerName, String methodName) throws PerunException;

	/**
	 * Call RPC API method with params
	 *
	 * @param managerName called manager
	 * @param methodName called method
	 * @param params method params passed with the call
	 * @return JsonDeserializer with response
	 * @throws PerunException If anything fails
	 */
	public Deserializer call(String managerName, String methodName, Map<String, Object> params) throws PerunException;

}
