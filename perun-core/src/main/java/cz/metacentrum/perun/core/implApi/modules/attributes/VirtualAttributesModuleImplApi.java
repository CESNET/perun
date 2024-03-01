package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichAttribute;

import java.util.List;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface VirtualAttributesModuleImplApi extends AttributesModuleImplApi {

  /**
   * Get list of attributes which this attribute value is computed from.
   * In other words attributes whose values change can also directly affect value of this attribute.
   * <p>
   * An attribute should strongly depend on all attributes which values are used in method "getAttributeValue"
   * defined in attribute module for virtual attributes.
   *
   * @return list of attributes this attribute strongly depends on
   * @see cz.metacentrum.perun.core.bl.AttributesManagerBl#checkAttributeDependencies(PerunSession, RichAttribute)
   */
  List<String> getStrongDependencies();
}
