package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;

/**
 * Entityless attribute for mapping values of DNS address to specific state (or empty string). The DNS domain is in key,
 * state is in String value.
 * <p>
 * Examples:
 * <BR>.cz = “Czech Republic”
 * <BR>.uk = “United Kingdom”
 * <BR>.ebi.ac.uk = “”
 *
 * @author Vladimir Mecko vladimir.mecko@gmail.com
 * @author Martin Kuba makub@ics.muni.cz
 */
public class urn_perun_entityless_attribute_def_def_dnsStateMapping extends EntitylessAttributesModuleAbstract
    implements EntitylessAttributesModuleImplApi {

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
    attr.setFriendlyName("dnsStateMapping");
    attr.setDisplayName("mapping of DNS address to specific state");
    attr.setType(String.class.getName());
    attr.setDescription("Maps together pairs of DNS address and state.");
    return attr;
  }
}
