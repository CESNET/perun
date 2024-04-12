package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ORCIDs collected from UserExtSources attributes.
 * The behaviour can be further extended by setting the following
 * properties in the eduPersonORCIDConfig entityless attribute:
 * <ul>
 *   <li>
 *    source_attribute - UES attribute name from which to fetch the values
 *   </li>
 *   <li>
 *    get_ext_login - set to "true" or "1" to fetch also the external logins
 *   </li>
 *   <li>
 *    value_filter - regex to filter the collected values by
 *   </li>
 *   <li>
 *    es_type - filter external sources by type
 *   </li>
 *   <li>
 *    es_name - filter external sources by name
 *   </li>
 *   <li>
 *    pattern and replacement - define the transformation to apply to the collected values
 *   </li>
 * </ul>
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_eduPersonORCID extends UserVirtualAttributeCollectedFromUserExtSource {

  private static final String A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE =
      AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":eduPersonORCIDConfig";
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private String sourceAttributeFriendlyName = "eduPersonOrcid";


  @Override
  public String getDestinationAttributeDescription() {
    return "All ORCIDs of a user";
  }

  @Override
  public String getDestinationAttributeFriendlyName() {
    return "eduPersonORCID";
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user,
                                     AttributeDefinition destinationAttributeDefinition) {
    String sourceAttribute = getConfigProperty(sess,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.SOURCE_ATTRIBUTE_KEY);

    if (sourceAttribute != null) {
      this.sourceAttributeFriendlyName = sourceAttribute;
    }

    return super.getAttributeValue(sess, user, destinationAttributeDefinition);
  }

  @Override
  public String getSourceAttributeFriendlyName() {
    return sourceAttributeFriendlyName;
  }

  @Override
  public Predicate<UserExtSource> getExtSourceFilter(PerunSessionImpl sess) {
    String uesFilterName = getConfigProperty(sess,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.ES_NAME_KEY);
    String uesFilterType = getConfigProperty(sess,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.ES_TYPE_KEY);

    if (uesFilterName == null && uesFilterType == null) {
      return super.getExtSourceFilter(sess);
    }

    return ues -> (uesFilterName == null || ues.getExtSource().getName().equals(uesFilterName)) &&
                      (uesFilterType == null || ues.getExtSource().getType().equals(uesFilterType));
  }

  @Override
  public Predicate<String> getValueFilter(PerunSessionImpl sess) {
    String regex = getConfigProperty(sess,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.VALUE_FILTER_KEY);

    return regex == null ? super.getValueFilter(sess) : value -> (value != null && value.matches(regex));
  }

  @Override
  public String modifyValue(PerunSession session, ModifyValueContext ctx, UserExtSource ues, String value) {
    PerunSessionImpl sess = ctx.getSession();
    String pattern = getConfigProperty(sess,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.PATTERN_KEY);
    String replacement = getConfigProperty(sess,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.REPLACEMENT_KEY);

    // if one is null both are null (guarded by the syntax check of the config attribute)
    if (pattern == null) {
      return super.modifyValue(session, ctx, ues, value);
    }

    return value.replaceAll(pattern, replacement);
  }

  @Override
  public boolean getAlsoExtLogin(PerunSessionImpl sess) {
    String getExtLoginStr = getConfigProperty(sess,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.GET_EXT_LOGIN_KEY);

    return getExtLoginStr == null ? super.getAlsoExtLogin(sess) :
               urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.STRING_TO_BOOLEAN_MAPPER.get(getExtLoginStr);
  }

  @Override
  public List<String> getStrongDependencies() {
    return List.of(A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE);
  }

  private String getConfigProperty(PerunSessionImpl sess, String property) {
    AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();

    List<Attribute> configAttribute =
        am.getEntitylessAttributes(sess, A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE);
    if (configAttribute.isEmpty() || configAttribute.get(0) == null) {
      return null;
    }

    LinkedHashMap<String, String> config = configAttribute.get(0).valueAsMap();
    return config.get(property);
  }

}
