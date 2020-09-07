package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.*;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.List;

public enum ExtSourcesManagerMethod implements ManagerMethod {

	/*#
	 * Creates an external source.
	 * ExtSource object must contain: name, type. Other parameters are ignored.
	 * @param extSource ExtSource JSON object
	 * @return ExtSource Created ExtSource
	 */
	/*#
	 * Creates an external source.
	 * @param name String name of ExtSource
	 * @param type String type of ExtSource
	 * @return ExtSource Created ExtSource
	 */
	createExtSource {

		@Override
		public ExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("extSource")) {
				return ac.getExtSourcesManager().createExtSource(ac.getSession(), parms.read("extSource", ExtSource.class), null);
			} else if (parms.contains("name") && parms.contains("type")) {
				String name = parms.readString("name");
				String type = parms.readString("type");
				ExtSource extSource = new ExtSource(name, type);
				return ac.getExtSourcesManager().createExtSource(ac.getSession(), extSource, null);
			} else {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER);
			}
		}
	},
	/*#
	 * Delete an external source.
	 * @param id int ExtSource <code>id</code>
	 */
	deleteExtSource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();
			ac.getExtSourcesManager().deleteExtSource(ac.getSession(), ac.getExtSourceById(parms.readInt("id")));
			return null;
		}
	},
	/*#
	 * Returns an external source by its <code>id</code>.
	 *
	 * @param id int ExtSource <code>id</code>
	 * @return ExtSource Found ExtSource
	 */
	getExtSourceById {

		@Override
		public ExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getExtSourcesManager().getExtSourceById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Returns an external source by its name.
	 *
	 * @param name String ExtSource name
	 * @return ExtSource Found ExtSource
	 */
	getExtSourceByName {

		@Override
		public ExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getExtSourcesManager().getExtSourceByName(ac.getSession(),
					parms.readString("name"));
		}
	},
	/*#
	 * Returns the list of external sources associated with a VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @return List<ExtSource> VO external sources
	 */
	getVoExtSources {

		@Override
		public List<ExtSource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getExtSourcesManager().getVoExtSources(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
		}
	},

	/*#
	 * Returns the list of external sources associated with a GROUP.
	 *
	 * @param group int GROUP <code>id</code>
	 * @return List<ExtSource> GROUP external sources
	 */
	getGroupExtSources {

		@Override
		public List<ExtSource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getExtSourcesManager().getGroupExtSources(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns the list of all external sources.
	 *
	 * @return List<ExtSource> all external sources
	 */
	getExtSources {

		@Override
		public List<ExtSource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getExtSourcesManager().getExtSources(ac.getSession());
		}
	},

	/*#
	 * Associate an external source definition with a VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @param source int ExtSource <code>id</code>
	 */
	/*#
	 * Associate an external source definition with a GROUP.
	 *
	 * @param group int GROUP <code>id</code>
	 * @param source int ExtSource <code>id</code>
	 */
	addExtSource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms)
		throws PerunException {
		parms.stateChangingCheck();

		if(parms.contains("vo")) {
			ac.getExtSourcesManager().addExtSource(ac.getSession(),
				ac.getVoById(parms.readInt("vo")),
				ac.getExtSourceById(parms.readInt("source")));
		} else if(parms.contains("group")) {
			ac.getExtSourcesManager().addExtSource(ac.getSession(),
				ac.getGroupById(parms.readInt("group")),
				ac.getExtSourceById(parms.readInt("source")));
		} else {
			throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
		}

		return null;
		}
	},

	/*#
	 * Remove an association of an external source from a VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @param source int ExtSource <code>id</code>
	 */
	/*#
	 * Remove an association of an external source from a GROUP.
	 *
	 * @param group int GROUP <code>id</code>
	 * @param source int ExtSource <code>id</code>
	 */
	removeExtSource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms)
		throws PerunException {
		parms.stateChangingCheck();

		if(parms.contains("vo")) {
			ac.getExtSourcesManager().removeExtSource(ac.getSession(),
				ac.getVoById(parms.readInt("vo")),
				ac.getExtSourceById(parms.readInt("source")));
		} else if(parms.contains("group")) {
			ac.getExtSourcesManager().removeExtSource(ac.getSession(),
				ac.getGroupById(parms.readInt("group")),
				ac.getExtSourceById(parms.readInt("source")));
		} else {
			throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
		}

		
		return null;
		}
	},

	/*#
	 * Loads ext source definitions from the configuration file and updates entries stored in the DB.
	 */
	loadExtSourcesDefinitions {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getExtSourcesManager().loadExtSourcesDefinitions(ac.getSession());
			return null;
		}
	};
}
