package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.core.api.NamespaceRules;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LoginNamespacesRulesConfigLoader {

  private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
  private static final Logger log = LoggerFactory.getLogger(LoginNamespacesRulesConfigLoader.class);

  private Resource configurationPath;

  public void setConfigurationPath(Resource configurationPath) {
    this.configurationPath = configurationPath;
  }

  public Map<String, NamespaceRules> loadNamespacesRulesConfig() {
    Map<String, NamespaceRules> namespacesRules = new HashMap<>();

    try {
      JsonNode rootNode = loadConfigurationFile(configurationPath);
      loadNamespacesRulesFromJsonNode(rootNode)
          .forEach(namespace -> namespacesRules.put(namespace.getNamespaceName(), namespace));

    } catch (RuntimeException e) {
      throw new InternalErrorException("Configuration file has invalid syntax. Configuration file: " +
          configurationPath.getFilename(), e);
    }

    return namespacesRules;
  }

  private Set<NamespaceRules> loadNamespacesRulesFromJsonNode(JsonNode rootNode) {
    Set<NamespaceRules> rules = new HashSet<>();
    //Fetch all namespaces from the configuration file
    JsonNode namespacesNodes = rootNode.get("namespaces");

    // For each namespace node construct NamespaceRules and add it to the set
    Iterator<String> namespacesNames = namespacesNodes.fieldNames();
    while (namespacesNames.hasNext()) {
      String namespaceName = namespacesNames.next();
      JsonNode namespaceNode = namespacesNodes.get(namespaceName);
      JsonNode defaultEmail = namespaceNode.get("default_email");
      JsonNode csvGenHeader = namespaceNode.get("csv_gen_header");
      JsonNode csvGenPlaceholder = namespaceNode.get("csv_gen_placeholder");
      JsonNode csvGenHeaderDescription = namespaceNode.get("csv_gen_header_description");
      JsonNode requiredAttributesNode = namespaceNode.get("required_attributes");
      JsonNode optionalAttributesNode = namespaceNode.get("optional_attributes");
      Set<String> requiredAttributes = objectMapper.convertValue(requiredAttributesNode, new TypeReference<>() {
      });
      Set<String> optionalAttributes = objectMapper.convertValue(optionalAttributesNode, new TypeReference<>() {
      });

      NamespaceRules namespaceRules = new NamespaceRules();
      namespaceRules.setNamespaceName(namespaceName);
      namespaceRules.setDefaultEmail(defaultEmail.asText());
      namespaceRules.setRequiredAttributes(requiredAttributes);
      namespaceRules.setOptionalAttributes(optionalAttributes);
      if (csvGenHeader != null && !csvGenHeader.isNull()) {
        namespaceRules.setCsvGenHeader(csvGenHeader.asText());
      }
      if (csvGenPlaceholder != null && !csvGenPlaceholder.isNull()) {
        namespaceRules.setCsvGenPlaceholder(csvGenPlaceholder.asText());
      }
      if (csvGenHeaderDescription != null && !csvGenHeaderDescription.isNull()) {
        namespaceRules.setCsvGenHeaderDescription(csvGenHeaderDescription.asText());
      }

      rules.add(namespaceRules);
    }

    return rules;
  }

  private JsonNode loadConfigurationFile(Resource resource) {

    JsonNode rootNode;
    try (InputStream is = resource.getInputStream()) {
      rootNode = objectMapper.readTree(is);
    } catch (FileNotFoundException e) {
      throw new InternalErrorException(
          "Configuration file not found for namespaces rules. It should be in: " + resource, e);
    } catch (IOException e) {
      throw new InternalErrorException("IO exception was thrown during the processing of the file: " + resource, e);
    }

    return rootNode;
  }
}
