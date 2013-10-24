package cz.metacentrum.perun.rpclib.api;

import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

public interface RpcCaller {
  public Deserializer call(String managerName, String methodName) throws PerunException;
  
  public Deserializer call(String managerName, String methodName, Map<String, Object> params) throws PerunException;
}
