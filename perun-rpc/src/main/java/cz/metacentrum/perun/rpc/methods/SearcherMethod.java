package cz.metacentrum.perun.rpc.methods;

import java.util.List;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.HashMap;
import java.util.LinkedHashMap;

public enum SearcherMethod implements ManagerMethod {
	
  getUsers {

    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.stateChangingCheck();
      
      return ac.getSearcher().getUsers(ac.getSession(),
              //FIXME this parameter maybe not correct
              parms.read("attributesWithSearchingValues", LinkedHashMap.class));
    } 
  };
}
