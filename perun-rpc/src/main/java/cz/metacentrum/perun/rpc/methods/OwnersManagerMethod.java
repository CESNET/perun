package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.*;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.List;

public enum OwnersManagerMethod implements ManagerMethod {

	/*#
	 * Creates a new owner.
	 *
	 * @param owner Owner JSON object
	 * @return Owner Created object
	 */
    createOwner {
        @Override
        public Owner call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            return ac.getOwnersManager().createOwner(ac.getSession(),
                    parms.read("owner", Owner.class));
        }
    },

    /*#
     * Deletes an owner.
     *
     * @param owner int Owner ID
     * @return Object Always null
     */
    deleteOwner {
        @Override
        public Owner call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getOwnersManager().deleteOwner(ac.getSession(),
                    ac.getOwnerById(parms.readInt("owner")));

            return null;
        }
    },

    /*#
     * Returns an owner by its ID.
     *
     * @param id int Owner ID
     * @return Owner Found Owner
     */
    getOwnerById {
        @Override
        public Owner call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getOwnerById(parms.readInt("id"));
        }
    },

    /*#
     * Returns all owners.
     *
     * @return List<Owner> All owners
     */
    getOwners {
        @Override
        public List<Owner> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getOwnersManager().getOwners(ac.getSession());
        }
    };
}
