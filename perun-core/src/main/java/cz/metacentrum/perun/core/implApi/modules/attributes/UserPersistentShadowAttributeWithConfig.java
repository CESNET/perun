package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.impl.modules.ModulesConfigLoader;
import cz.metacentrum.perun.core.impl.modules.ModulesYamlConfigLoader;

/**
 * Abstract class that can be used to created persistent shadow modules.
 * This class lazy loads information about extSourceName and domainName from
 * attribute's module's configuration file.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public abstract class UserPersistentShadowAttributeWithConfig extends UserPersistentShadowAttribute {

  private final ModulesConfigLoader loader = new ModulesYamlConfigLoader();
  private String extSourceName = null;
  private String domainName = null;

  @Override
  public String getExtSourceName() {
    if (extSourceName == null) {
      extSourceName = loader.loadString(getClass().getSimpleName(), getExtSourceConfigName());
    }
    return extSourceName;
  }

  @Override
  public String getDomainName() {
    if (domainName == null) {
      domainName = loader.loadString(getClass().getSimpleName(), getDomainConfigName());
    }
    return domainName;
  }

  /**
   * Get path in the config file to the extSource.
   *
   * @return path in the config file to the extSource name.
   */
  public abstract String getExtSourceConfigName();

  /**
   * Get path in the config file to the domain name.
   *
   * @return path in the config file to the domain name.
   */
  public abstract String getDomainConfigName();
}
