package cz.metacentrum.perun.ldapc.service;

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
}
