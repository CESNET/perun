package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public enum OwnersManagerMethod implements ManagerMethod {

  /*#
   * Creates a new owner.
   *
   * Owner object must contain: name, contact, ownerType which can be: 0 - technical, 1 - administrative.
   * Other parameters are ignored.
   *
   * @deprecated
   * @param owner Owner JSON object
   * @return Owner Created object
   * @exampleParam owner { "name" : "The Owner" , "contact" : "the contact", "ownerType" : 0 }
   */
  /*#
   * Creates a new owner.
   *
   * @deprecated
   * @param name String name of a new owner
   * @param contact String contact of a new owner
   * @param ownerType int ownerType, 0 - technical, 1 - administrative
   * @return Owner Created object
   * @exampleParam name "The Owner"
   * @exampleParam contact "the contact"
   * @exampleParam ownerType 0
   */
  createOwner {
    @Override
    public Owner call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("owner")) {
        return ac.getOwnersManager().createOwner(ac.getSession(), parms.read("owner", Owner.class));
      } else if (parms.contains("name") && parms.contains("contact") && parms.contains("ownerType")) {
        String name = parms.readString("name");
        String contact = parms.readString("contact");
        OwnerType ownerType = OwnerType.getOwnerType(parms.readInt("ownerType"));
        Owner owner = new Owner(0, name, contact, ownerType);
        return ac.getOwnersManager().createOwner(ac.getSession(), owner);
      } else {
        throw new RpcException(RpcException.Type.WRONG_PARAMETER);
      }
    }
  },

  /*#
   * Deletes an owner.
   *
   * @deprecated
   * @param owner int Owner <code>id</code>
   * @return Object Always null
   */
  /*#
   * Forcefully deletes an owner.
   *
   * @deprecated
   * @param owner int Owner <code>id</code>
   * @param force boolean Force must be true
   * @return Object Always null
   */
  deleteOwner {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      boolean force = false;
      if (parms.contains("force")) {
        force = parms.readBoolean("force");
      }
      ac.getOwnersManager().deleteOwner(ac.getSession(), ac.getOwnerById(parms.readInt("owner")), force);
      return null;
    }
  },

  /*#
   * Deletes owners.
   *
   * @deprecated
   * @param owners List<Integer> Owner <code>id</code>
   * @return Object Always null
   */
  /*#
   * Forcefully deletes owners.
   *
   * @deprecated
   * @param owners List<Integer> Owner <code>id</code>
   * @param force boolean Force must be true
   * @return Object Always null
   */
  deleteOwners {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      List<Owner> owners = new ArrayList<>();
      for (Integer ownerId : parms.readList("owners", Integer.class)) {
        owners.add(ac.getOwnerById(ownerId));
      }

      boolean force = false;
      if (parms.contains("force")) {
        force = parms.readBoolean("force");
      }

      ac.getOwnersManager().deleteOwners(ac.getSession(), owners, force);
      return null;
    }
  },

  /*#
   * Returns an owner by its <code>id</code>.
   *
   * @deprecated
   * @param id int Owner <code>id</code>
   * @return Owner Found Owner
   */
  getOwnerById {
    @Override
    public Owner call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getOwnersManager().getOwnerById(ac.getSession(), parms.readInt("id"));
    }
  },

  /*#
   * Returns an owner by its name.
   *
   * @deprecated
   * @param name String Owner name
   * @return Owner Found Owner
   */
  getOwnerByName {
    @Override
    public Owner call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getOwnersManager().getOwnerByName(ac.getSession(), parms.readString("name"));
    }
  },

  /*#
   * Returns all owners.
   *
   * @deprecated
   * @return List<Owner> All owners
   */
  getOwners {
    @Override
    public List<Owner> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getOwnersManager().getOwners(ac.getSession());
    }
  };
}
