package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
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
	 * @param owner int Owner <code>id</code>
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
	 * Returns an owner by its <code>id</code>.
	 *
	 * @param id int Owner <code>id</code>
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
