package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

import java.util.List;

public enum ConsentsManagerMethod implements ManagerMethod {

	/*#
	 * Return list of all Consent Hubs.
	 *
	 * @return List<ConsentHub> list of Consent Hubs
	 */
	getAllConsentHubs {
		@Override
		public List<ConsentHub> call(ApiCaller ac, Deserializer params) throws PerunException {
			return ac.getConsentsManager().getAllConsentHubs(ac.getSession());
		}
	},

	/*#
	 * Returns a Consent Hub by its <code>id</code>.
	 *
	 * @param id int Consent Hub <code>id</code>
	 * @throw ConsentHubNotExistsException When Consent Hub specified by <code>id</code> doesn't exist.
	 * @return ConsentHub Found Consent Hub
	 */
	getConsentHubById {
		@Override
		public ConsentHub call(ApiCaller ac, Deserializer params) throws PerunException {
			return ac.getConsentsManager().getConsentHubById(ac.getSession(), params.readInt("id"));
		}
	},

	/*#
	 * Returns a Consent Hub by its name.
	 *
	 * @param name String Consent Hub name
	 * @throw ConsentHubNotExistsException When Consent Hub specified by name doesn't exist.
	 * @return ConsentHub Found Consent Hub
	 */
	getConsentHubByName {
		@Override
		public ConsentHub call(ApiCaller ac, Deserializer params) throws PerunException {
			return ac.getConsentsManager().getConsentHubByName(ac.getSession(), params.readString("name"));
		}
	},

	/*#
	 * Returns a Consent Hub by facility id.
	 *
	 * @param facilityId facility id
	 * @throw ConsentHubNotExistsException When Consent Hub for facility with given id doesn't exist.
	 * @throw FacilityNotExistsException if facility with given id does not exist
	 * @return ConsentHub Found Consent Hub
	 */
	getConsentHubByFacility {
		@Override
		public ConsentHub call(ApiCaller ac, Deserializer params) throws PerunException {
			return ac.getConsentsManager().getConsentHubByFacility(ac.getSession(), params.readInt("facility"));
		}
	},

	/*#
	 * Updates a consent hub.
	 *
	 * @param consentHub ConsentHub JSON object
	 * @throw ConsentHubNotExistsException if consent hub with given id does not exist
	 * @throw ConsentHubExistsException if consent hub with the same name already exists
	 * @return updated consent hub
	 */
	updateConsentHub {
		@Override
		public ConsentHub call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			return ac.getConsentsManager().updateConsentHub(ac.getSession(),
				parms.read("consentHub", ConsentHub.class));
		}
	};
}
