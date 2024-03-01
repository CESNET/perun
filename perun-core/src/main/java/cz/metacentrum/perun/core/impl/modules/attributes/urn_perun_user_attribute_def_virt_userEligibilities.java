package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Virtual attribute for user's eligibilities. Attribute is calculated using all user's ext sources and their
 * corresponding eligibilities attribute so that for each unique key (name) only the highest obtained value is
 * selected.
 *
 * @author Luboslav Halama
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_userEligibilities
    extends UserVirtualAttributeCollectedFromUserExtSource {

  private static final String USER_ELIGIBILITIES_FRIENDLY_NAME = "userEligibilities";
  private static final String A_U_V_USER_ELIGIBILITIES =
      AttributesManager.NS_USER_ATTR_VIRT + ":" + USER_ELIGIBILITIES_FRIENDLY_NAME;

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName(USER_ELIGIBILITIES_FRIENDLY_NAME);
    attr.setDisplayName("Eligibilities");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription(
        "Eligibilities collected from all identities of the user with the most recent timestamp for each eligibility.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

    Map<String, String> collectedEligibilities = new LinkedHashMap<>();

    for (UserExtSource extSource : userExtSources) {
      Attribute attribute;
      try {
        attribute = sess.getPerunBl().getAttributesManagerBl()
            .getAttribute(sess, extSource, AttributesManager.NS_UES_ATTR_DEF + ":" + getSourceAttributeFriendlyName());
      } catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
        throw new RuntimeException(e);
      }

      Map<String, String> eligibilities = attribute.valueAsMap();

      // if no eligibilities were obtained, skip this ext source
      if (eligibilities == null) {
        continue;
      }

      for (String key : eligibilities.keySet()) {

        // if given key is not present yet, store it
        if (!collectedEligibilities.containsKey(key)) {
          collectedEligibilities.put(key, eligibilities.get(key));
          continue;
        }

        // if given is present, check if value should be updated
        int timestamp = Integer.parseInt(eligibilities.get(key));
        if (Integer.parseInt(collectedEligibilities.get(key)) < timestamp) {
          collectedEligibilities.replace(key, eligibilities.get(key));
        }
      }
    }

    Attribute attribute = new Attribute(attributeDefinition);
    attribute.setValue(collectedEligibilities);
    return attribute;
  }

  @Override
  public String getDestinationAttributeFriendlyName() {
    return USER_ELIGIBILITIES_FRIENDLY_NAME;
  }

  @Override
  public String getSourceAttributeFriendlyName() {
    return "eligibilities";
  }
}
