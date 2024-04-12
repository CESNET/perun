package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Config attribute for the eduPersonORCID module.
 *
 * @see urn_perun_user_attribute_def_virt_eduPersonORCID
 */
public class urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig extends
    EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

  public static final String GET_EXT_LOGIN_KEY = "get_ext_login";
  public static final String SOURCE_ATTRIBUTE_KEY = "source_attribute";
  public static final String VALUE_FILTER_KEY = "value_filter";
  public static final String ES_TYPE_KEY = "es_type";
  public static final String ES_NAME_KEY = "es_name";
  public static final String PATTERN_KEY = "pattern";
  public static final String REPLACEMENT_KEY = "replacement";

  private static final Set<String> ALLOWED_CONFIG_PROPERTIES = Set.of(SOURCE_ATTRIBUTE_KEY, GET_EXT_LOGIN_KEY,
      VALUE_FILTER_KEY, ES_TYPE_KEY, ES_NAME_KEY, PATTERN_KEY, REPLACEMENT_KEY);
  private static final Set<String> VALUE_TRANSFORMATION_KEYS = Set.of("pattern", "replacement");

  public static final Map<String, Boolean> STRING_TO_BOOLEAN_MAPPER = Map.of(
      "true", true, "1", true, "false", false, "0", false);

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, String key, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    Map<String, String> map = attribute.valueAsMap();

    Set<String> keys = map.keySet();

    if (!ALLOWED_CONFIG_PROPERTIES.containsAll(keys)) {
      keys.removeAll(ALLOWED_CONFIG_PROPERTIES);
      throw new WrongAttributeValueException(attribute, "Contains unknown key " + keys.iterator().next());
    }

    if (VALUE_TRANSFORMATION_KEYS.stream().filter(keys::contains).toList().size() == 1) {
      throw new WrongAttributeValueException(attribute,
          "Has to contain either both \"pattern\" and \"replacement\" keys or none");
    }

    if (keys.contains(GET_EXT_LOGIN_KEY) && !STRING_TO_BOOLEAN_MAPPER.containsKey(map.get(GET_EXT_LOGIN_KEY))) {
      throw new WrongAttributeValueException(attribute,
          "The value of \"get_ext_login\" property should be one of {true, false, 1, 0}");
    }

    if (keys.contains(VALUE_FILTER_KEY)) {
      try {
        Pattern ignored = Pattern.compile(map.get(key));
      } catch (PatternSyntaxException e) {
        throw new WrongAttributeValueException(attribute,
            "The \"value_filter\" property contains invalid regex");
      }
    }

    if (keys.contains(PATTERN_KEY)) {
      try {
        Pattern ignored = Pattern.compile(map.get(key));
      } catch (PatternSyntaxException e) {
        throw new WrongAttributeValueException(attribute,
            "The \"pattern\" property contains invalid regex");
      }
    }
  }


  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
    attr.setFriendlyName("eduPersonORCIDConfig");
    attr.setDisplayName("eduPersonORCID config attribute");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription(
        "A config mapping for eduPersonORCID attribute. Extends the behaviour accordingly:\n" +
            "The `source_attribute` property defines the UES attribute friendly name from" +
            " which to fetch the values in eduPersonORCID module.\n" +
            "Set the `get_ext_login` property to \"true\" or \"1\" to collect also external logins from UESs" +
            " in the eduPersonORCID module. Set to \"false\" \"0\" or leave unset to not collect the logins.\n" +
            "The `value_filter` property defines the regex according to which will be the collected values" +
            " in the eduPersonORCID module filtered.\n" +
            "The properties `type` and `name` serve as a filter for external sources in the eduPersonORCID module." +
            " Set the key `type` to filter the ESs by type and/or `name` to filter by name.\n" +
            "The properties `pattern` and `replacement` define the transformation to apply" +
            " to the collected values in the eduPersonORCID module. The key `pattern` defines the pattern to match," +
            " the key `replacement` the replacement of the matches."
    );
    return attr;
  }
}

