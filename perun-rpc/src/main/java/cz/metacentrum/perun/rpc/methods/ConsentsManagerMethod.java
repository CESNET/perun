package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.ArrayList;
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
   * Returns all existing Consents
   *
   * @return List<Consent> list of Consents
   */
  getAllConsents {
    @Override
    public List<Consent> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getConsentsManager().getAllConsents(ac.getSession());
    }
  },

  /*#
   * Returns Consent with the corresponding id
   *
   * @param id int Consent <code>id</code>
   * @throw ConsentNotExistsException when Consent specified by <code>id</code> doesn't exist
   * @return List<Consent> list of Consents
   */
  getConsentById {
    @Override
    public Consent call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getConsentsManager().getConsentById(ac.getSession(), parms.readInt("id"));
    }
  },

  /*#
   * Returns all consents for a User
   *
   * @param user int User <code>id</code>
   * @throw UserNotExistsException when User specified by <code>id</code> doesn't exist
   * @return List<Consent> Consents of the User
   */
  /*#
   * Returns all consents with a specific status for a User.
   *
   * @param user int User <code>id</code>
   * @param status String UNSIGNED | GRANTED | REVOKED
   * @throw UserNotExistsException when User specified by <code>id</code> doesn't exist
   * @return List<Consent> Consents of the User
   */
  getConsentsForUser {
    @Override
    public List<Consent> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("status")) {
        return ac.getConsentsManager().getConsentsForUser(ac.getSession(), parms.readInt("user"),
            ConsentStatus.valueOf(parms.readString("status")));
      } else {
        return ac.getConsentsManager().getConsentsForUser(ac.getSession(), parms.readInt("user"));
      }
    }
  },

  /*#
   * Returns all consents for a ConsentHub
   *
   * @param consentHub int ConsentHub <code>id</code>
   * @throw ConsentHubNotExistsException when Consent Hub specified by <code>id</code> doesn't exist
   * @return List<Consent> Consents of the ConsentHub
   */
  /*#
   * Returns all consents with a specific status for a ConsentHub.
   *
   * @param consentHub int ConsentHub <code>id</code>
   * @param status String UNSIGNED | GRANTED | REVOKED
   * @throw ConsentHubNotExistsException when Consent Hub specified by <code>id</code> doesn't exist
   * @return List<Consent> Consents of the ConsentHub
   */
  getConsentsForConsentHub {
    @Override
    public List<Consent> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("status")) {
        return ac.getConsentsManager().getConsentsForConsentHub(ac.getSession(), parms.readInt("consentHub"),
            ConsentStatus.valueOf(parms.readString("status")));
      } else {
        return ac.getConsentsManager().getConsentsForConsentHub(ac.getSession(), parms.readInt("consentHub"));
      }
    }
  },

  /*#
   * Returns consents for members assigned to the chosen resource.
   *
   * @param resource int resource <code>id</code>
   * @throw ResourceNotExistsException when resource specified by <code>id</code> doesn't exist
   * @throw FacilityNotExistsException when facility doesn't exist
   * @throw ConsentHubNotExistsException when Consent Hub doesn't exist
   * @return List<Consent> Consents of the ConsentHub
   */
  getConsentsForConsentHubByResource {
    @Override
    public List<Consent> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getConsentsManager().getConsentsForConsentHubByResource(ac.getSession(), parms.readInt("resource"));
    }
  },

  /*#
   * Returns all consents for a user in specified consent hub
   *
   * @param user int User <code>id</code>
   * @param consentHub int ConsentHub <code>id</code>
   * @throw ConsentHubNotExistsException when Consent Hub specified by <code>id</code> doesn't exist
   * @throw UserNotExistsException when User specified by <code>id</code> doesn't exist
   * @return List<Consent> Consents of the User in the ConsentHub
   */
  getConsentsForUserAndConsentHub {
    @Override
    public List<Consent> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getConsentsManager()
          .getConsentsForUserAndConsentHub(ac.getSession(), parms.readInt("user"), parms.readInt("consentHub"));
    }
  },

  /*#
   * Returns consent for a user in consent hub with specified status
   *
   * @param user int User <code>id</code>
   * @param consentHub int ConsentHub <code>id</code>
   * @param status String UNSIGNED | GRANTED | REVOKED
   * @throw ConsentHubNotExistsException when Consent Hub specified by <code>id</code> doesn't exist
   * @throw UserNotExistsException when User specified by <code>id</code> doesn't exist
   * @return Consent consent of User in the ConsentHub with specified ConsentStatus
   */
  getConsentForUserAndConsentHub {
    @Override
    public Consent call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getConsentsManager()
          .getConsentForUserAndConsentHub(ac.getSession(), parms.readInt("user"), parms.readInt("consentHub"),
              ConsentStatus.valueOf(parms.readString("status")));
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
   * Returns a Consent Hub for facility to which resource belongs.
   *
   * @param resourceId resource id
   * @throw ConsentHubNotExistsException If Consent Hub for facility with given id doesn't exist.
   * @throw FacilityNotExistsException If facility with given id does not exist.
   * @throw ResourceNotExistsException If resource with given id does not exist.
   * @return ConsentHub Found Consent Hub
   */
  getConsentHubByResource {
    @Override
    public ConsentHub call(ApiCaller ac, Deserializer params) throws PerunException {
      return ac.getConsentsManager().getConsentHubByResource(ac.getSession(), params.readInt("resource"));
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

      return ac.getConsentsManager().updateConsentHub(ac.getSession(), parms.read("consentHub", ConsentHub.class));
    }
  },

  /*#
   * Returns consent with changed status.
   *
   * @param consent int Consent <code>id</code>
   * @param status String UNSIGNED | GRANTED | REVOKED
   *
   * @throw ConsentNotExistsException if consent hub does not exist
   * @throw UserNotExistsException if user does not exist
   * @throw InvalidConsentStatusException if passed status value can not be set
   *
   * @return Consent
   */
  changeConsentStatus {
    @Override
    public Consent call(ApiCaller ac, Deserializer params) throws PerunException {
      return ac.getConsentsManager().changeConsentStatus(ac.getSession(), ac.getConsentById(params.readInt("consent")),
          ConsentStatus.valueOf(params.readString("status")));
    }
  },

  /*#
   * Evaluates consents for given consent hub.
   *
   * @param consentHub Consent Hub
   * @throw ConsentNotExistsException if consent hub does not exist
   */
  evaluateConsentsForConsentHub {
    @Override
    public Void call(ApiCaller ac, Deserializer params) throws PerunException {
      ac.getConsentsManager().evaluateConsents(ac.getSession(), ac.getConsentHubById(params.readInt("consentHub")));
      return null;
    }
  },

  /*#
   * Evaluates consents for given consent hubs.
   *
   * @param consentHubs List<Integer> <code>id</code> of consent hubs
   * @throw ConsentNotExistsException if consent hub does not exist
   */
  evaluateConsentsForConsentHubs {
    @Override
    public Void call(ApiCaller ac, Deserializer params) throws PerunException {

      List<ConsentHub> consentHubs = new ArrayList<>();
      for (int id : params.readList("consentHubs", Integer.class)) {
        consentHubs.add(ac.getConsentHubById(id));
      }
      ac.getConsentsManager().evaluateConsents(ac.getSession(), consentHubs);
      return null;
    }
  },

  /*#
   * Corresponding consent hubs (containing the service) will have consents evaluated ONLY for selected service.
   * Service defines whether only active users will be evaluated or expired ones as well.
   *
   * @param service used for consents evaluation
   * @throw ConsentNotExistsException if consent hub does not exist
   */
  evaluateConsentsForService {
    @Override
    public Void call(ApiCaller ac, Deserializer params) throws PerunException {
      ac.getConsentsManager().evaluateConsents(ac.getSession(), ac.getServiceById(params.readInt("service")));
      return null;
    }
  };

}
