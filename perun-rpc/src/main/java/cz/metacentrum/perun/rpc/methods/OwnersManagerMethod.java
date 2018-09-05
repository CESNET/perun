package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
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
	/*#
	 * Creates a new owner.
	 *
	 * @param name String name of a new owner
	 * @param contact String contact of a new owner
	 * @param ownerType int ownerType
	 * @return Owner Created object
	 */
	createOwner {
		@Override
		public Owner call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("owner")) {
				return ac.getOwnersManager().createOwner(ac.getSession(),
						parms.read("owner", Owner.class));
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
