package cz.metacentrum.perun.core.impl.modules;

import static cz.metacentrum.perun.core.impl.Utils.notNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import cz.metacentrum.perun.core.api.exceptions.rt.ModulePropertyNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */

public class ModulesYamlConfigLoader implements ModulesConfigLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ModulesYamlConfigLoader.class);

  private String modulesDirPath = "/etc/perun/modules/";

  public ModulesYamlConfigLoader() {
  }

  public ModulesYamlConfigLoader(String modulesDirPath) {
    this.modulesDirPath = modulesDirPath;
  }

  /**
   * Loads a root JsonNode from the given path.
   *
   * @param path path
   * @return loaded root JsonNode
   */
  private static JsonNode loadModulesYamlFile(String path) {
    try {
      YAMLMapper mapper = new YAMLMapper();
      return mapper.readTree(new File(path));
    } catch (IOException e) {
      LOG.error("Failed to load a module's yaml property file at: {}", path);
      throw new ModulePropertyNotFoundException("Failed to load a module's yaml property file.", e);
    }
  }

  /**
   * Parse a corresponding JsonNode from the given root node, with the specified property name. The syntax supports a
   * dot notation to identify children nodes. Eg: oidc.client.id.
   *
   * @param root         root node
   * @param propertyName name of the desired property
   * @return JsonNode corresponding to the specified property name
   */
  private static JsonNode parsePropertyNode(JsonNode root, String propertyName) {
    JsonNode currentNode = root;
    while (propertyName.contains(".")) {
      String[] split = propertyName.split("\\.", 2);
      currentNode = currentNode.get(split[0]);
      propertyName = split[1];
    }
    return currentNode.get(propertyName);
  }

  @Override
  public Integer loadInteger(String moduleName, String property) {
    JsonNode propertyNode = loadPropertyNode(moduleName, property);
    if (propertyNode == null || propertyNode.isNull()) {
      throw new ModulePropertyNotFoundException(moduleName, property);
    }
    return propertyNode.asInt();
  }

  @Override
  public List<Integer> loadIntegerList(String moduleName, String property) {
    JsonNode propertyNode = loadPropertyNode(moduleName, property);
    if (propertyNode == null || propertyNode.isNull()) {
      throw new ModulePropertyNotFoundException(moduleName, property);
    }
    List<Integer> values = new ArrayList<>();
    propertyNode.iterator().forEachRemaining(node -> values.add(node.isNull() ? null : node.asInt()));
    return values;
  }

  @Override
  public List<Integer> loadIntegerListOrDefault(String moduleName, String property, List<Integer> defaultValue) {
    JsonNode propertyNode = loadPropertyNode(moduleName, property);
    if (propertyNode == null || propertyNode.isNull()) {
      return defaultValue;
    }
    List<Integer> values = new ArrayList<>();
    propertyNode.iterator().forEachRemaining(node -> values.add(node.isNull() ? null : node.asInt()));
    return values;
  }

  @Override
  public Integer loadIntegerOrDefault(String moduleName, String property, Integer defaultValue) {
    JsonNode propertyNode = loadPropertyNode(moduleName, property);
    if (propertyNode == null || propertyNode.isNull()) {
      return defaultValue;
    }
    return propertyNode.asInt();
  }

  /**
   * Loads a JsonNode corresponding to the given module and property name. This method expects a {moduleName}.yaml file
   * at the modulesDirPath.
   *
   * @param moduleName   name of the module
   * @param propertyName property name
   * @return JsonNode corresponding to the desired property.
   */
  private JsonNode loadPropertyNode(String moduleName, String propertyName) {
    notNull(moduleName, "configFile");
    notNull(propertyName, "propertyName");

    String path = modulesDirPath + moduleName + ".yaml";

    JsonNode root = loadModulesYamlFile(path);

    return parsePropertyNode(root, propertyName);
  }

  @Override
  public String loadString(String moduleName, String property) {
    JsonNode propertyNode = loadPropertyNode(moduleName, property);
    if (propertyNode == null || propertyNode.isNull()) {
      throw new ModulePropertyNotFoundException(moduleName, property);
    }
    return propertyNode.asText();
  }

  @Override
  public List<String> loadStringList(String moduleName, String property) {
    JsonNode propertyNode = loadPropertyNode(moduleName, property);
    if (propertyNode == null || propertyNode.isNull()) {
      throw new ModulePropertyNotFoundException(moduleName, property);
    }
    List<String> values = new ArrayList<>();
    propertyNode.iterator().forEachRemaining(node -> values.add(node.asText()));
    return values;
  }

  @Override
  public List<String> loadStringListOrDefault(String moduleName, String property, List<String> defaultValue) {
    JsonNode propertyNode = loadPropertyNode(moduleName, property);
    if (propertyNode == null || propertyNode.isNull()) {
      return defaultValue;
    }
    List<String> values = new ArrayList<>();
    propertyNode.iterator().forEachRemaining(node -> values.add(node.asText()));
    return values;
  }

  @Override
  public String loadStringOrDefault(String moduleName, String property, String defaultValue) {
    JsonNode propertyNode = loadPropertyNode(moduleName, property);
    if (propertyNode == null || propertyNode.isNull()) {
      return defaultValue;
    }
    return propertyNode.asText();
  }

  @Override
  public boolean moduleFileExists(String moduleName) {
    notNull(moduleName, "configFile");
    String path = modulesDirPath + moduleName + ".yaml";

    File f = new File(path);
    return f.exists() && !f.isDirectory();
  }
}
