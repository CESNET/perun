package cz.metacentrum.perun.rpc;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public interface ManagerMethod {
	public Object call(ApiCaller ac, Deserializer parms) throws PerunException;
}
