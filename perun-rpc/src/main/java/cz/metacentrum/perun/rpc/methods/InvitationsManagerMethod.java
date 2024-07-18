package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.List;

public enum InvitationsManagerMethod implements ManagerMethod {

  getInvitationById {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  },

  getInvitationsForSender {
    @Override
    public List<Invitation> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  },

  getInvitationsForGroup {
    @Override
    public List<Invitation> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  },

  getInvitationsForVo {
    @Override
    public List<Invitation> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  },

  //TODO most likely delete this method and use better ways to submit invitation, wanted to try RPC
  createInvitation {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  }
}
