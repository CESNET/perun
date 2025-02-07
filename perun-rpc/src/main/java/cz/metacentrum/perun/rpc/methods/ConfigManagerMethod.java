package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.PersonalDataChangeConfig;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum ConfigManagerMethod implements ManagerMethod {

  /*#
   * Reloads configuration of the perun-apps-config.yml file.
   *
   */
  reloadAppsConfig {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getConfigManager().reloadAppsConfig(ac.getSession());
      return null;
    }
  }, /*#
   * Returns Oidc Configuration for this Perun instance (to be used for CLI communication).
   * @throw OidcConfigNotExistsException when a configuration under the name does not exist.
   * @throw OidcConfigFileNotExistsException when configuration file for oidc configs doesn't exist.
   */
  getPerunOidcConfig {
    @Override
    public OidcConfig call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getConfigManager()
          .getPerunOidcConfig(ac.getSession(), parms.getServletRequest().getRequestURL().toString());
    }
  },
  /*#
   * Checks whether archiving of files generated by services is enabled.
   */
  isArchiveSpoolEnabled {
    @Override
    public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getConfigManager()
          .isArchiveSpoolEnabled(ac.getSession());
    }
  },
  /*#
   * Gets personal data change configuration with all related core config properties.
   */
  getPersonalDataChangeConfig {
    @Override
    public PersonalDataChangeConfig call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getConfigManager().getPersonalDataChangeConfig(ac.getSession());
    }
  },
}
