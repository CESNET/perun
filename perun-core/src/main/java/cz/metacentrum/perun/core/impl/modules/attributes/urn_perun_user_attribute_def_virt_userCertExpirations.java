package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Get and set specified user certificate expiration
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_userCertExpirations extends UserVirtualAttributesModuleAbstract
    implements UserVirtualAttributesModuleImplApi {

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("userCertExpirations");
    attr.setDisplayName("Certificates expirations");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Expiration of user certificate.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);
    Map<String, String> certsExpiration = new LinkedHashMap<>();

    Attribute userCertsAttribute = getUserCertsAttribute(sess, user);
    Map<String, String> certs = userCertsAttribute.valueAsMap();

    if (certs != null) {
      certsExpiration = ModulesUtilsBlImpl.retrieveCertificatesExpiration(certs);
      Utils.copyAttributeToViAttributeWithoutValue(userCertsAttribute, attribute);
    }
    attribute.setValue(certsExpiration);
    return attribute;
  }

  @Override
  public List<String> getStrongDependencies() {
    return Collections.singletonList(AttributesManager.NS_USER_ATTR_DEF + ":userCertificates");
  }

  private Attribute getUserCertsAttribute(PerunSessionImpl sess, User user) {
    try {
      return sess.getPerunBl().getAttributesManagerBl()
          .getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":userCertificates");
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    } catch (AttributeNotExistsException e) {
      throw new ConsistencyErrorException(e);
    }
  }
}
