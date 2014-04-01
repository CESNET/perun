package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.*;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.List;

public enum ExtSourcesManagerMethod implements ManagerMethod {
	/*#
	 * Creates an external source.
	 * @param extSource ExtSource JSON object
	 * @return ExtSource Created ExtSource
	 */
	createExtSource {

		@Override
		public ExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getExtSourcesManager().createExtSource(ac.getSession(), parms.read("extSource", ExtSource.class));
		}
	},
	/*#
	 * Returns an external source by its ID.
	 *
	 * @param id int ExtSource ID
	 * @return ExtSource Found ExtSource
	 */
	getExtSourceById {

		@Override
		public ExtSource call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getExtSourceById(parms.readInt("id"));
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
	 * @param vo int VO ID
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
	 * @param vo int VO ID
	 * @param source int ExtSource ID
	 */
	addExtSource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms)
		throws PerunException {
		ac.stateChangingCheck();

		ac.getExtSourcesManager().addExtSource(ac.getSession(),
				ac.getVoById(parms.readInt("vo")),
				ac.getExtSourceById(parms.readInt("source")));
		return null;
		}
	},

	/*#
	 * Remove an association of an external source from a VO.
	 *
	 * @param vo int VO ID
	 * @param source int ExtSource ID
	 */
	removeExtSource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms)
		throws PerunException {
		ac.stateChangingCheck();

		ac.getExtSourcesManager().removeExtSource(ac.getSession(),
				ac.getVoById(parms.readInt("vo")),
				ac.getExtSourceById(parms.readInt("source")));
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
