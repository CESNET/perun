package cz.metacentrum.perun.core.impl.modules;

import cz.metacentrum.perun.core.api.exceptions.rt.ModulePropertyNotFoundException;
import java.util.List;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface ModulesConfigLoader {

  /**
   * For module with the given name, find an Integer configuration property with given name.
   *
   * @param moduleName name of a module
   * @param property   name of a property
   * @return found Integer property for given module
   * @throws ModulePropertyNotFoundException when the specified property value is null or is not found or the specified
   *                                         module configuration is not found
   */
  Integer loadInteger(String moduleName, String property);

  /**
   * For module with the given name, find a list of Integers configuration property with given name.
   *
   * @param moduleName name of a module
   * @param property   name of a property
   * @return found list of Integers property for given module
   * @throws ModulePropertyNotFoundException when the specified property value is null or is not found or the specified
   *                                         module configuration is not found
   */
  List<Integer> loadIntegerList(String moduleName, String property);

  /**
   * For module with the given name, find a list of Integers configuration property with given name. If the module
   * configuration does not contain the specified property, return the default value.
   *
   * @param moduleName   name of a module
   * @param property     name of a property
   * @param defaultValue default value
   * @return found list of Integers property for given module, or default value if not found
   * @throws ModulePropertyNotFoundException when the specified property value is null or is not found or the specified
   *                                         module configuration is not found
   */
  List<Integer> loadIntegerListOrDefault(String moduleName, String property, List<Integer> defaultValue);

  /**
   * For module with the given name, find an Integer configuration property with given name. If the module configuration
   * does not contain the specified property, return the default value.
   *
   * @param moduleName   name of a module
   * @param property     name of a property
   * @param defaultValue default value
   * @return found Integer property for given module, or default value if not found
   * @throws ModulePropertyNotFoundException when the specified module configuration is not found
   */
  Integer loadIntegerOrDefault(String moduleName, String property, Integer defaultValue);

  /**
   * For module with the given name, find a String configuration property with given name.
   *
   * @param moduleName name of a module
   * @param property   name of a property
   * @return found String property for given module
   * @throws ModulePropertyNotFoundException when the specified property value is null or is not found or the specified
   *                                         module configuration is not found
   */
  String loadString(String moduleName, String property);

  /**
   * For module with the given name, find a list of Strings configuration property with given name.
   *
   * @param moduleName name of a module
   * @param property   name of a property
   * @return found list of Strings property for given module
   * @throws ModulePropertyNotFoundException when the specified property value is null or is not found or the specified
   *                                         module configuration is not found
   */
  List<String> loadStringList(String moduleName, String property);

  /**
   * For module with the given name, find a list of Strings configuration property with given name. If the module
   * configuration does not contain the specified property, return the default value.
   *
   * @param moduleName   name of a module
   * @param property     name of a property
   * @param defaultValue default value
   * @return found list of Strings property for given module, or default value if not found
   * @throws ModulePropertyNotFoundException when the specified module configuration is not found
   */
  List<String> loadStringListOrDefault(String moduleName, String property, List<String> defaultValue);

  /**
   * For module with the given name, find a String configuration property with given name. If the module configuration
   * does not contain the specified property, return the default value.
   *
   * @param moduleName   name of a module
   * @param property     name of a property
   * @param defaultValue default value
   * @return found String property for given module, or default value if not found
   * @throws ModulePropertyNotFoundException when the specified module configuration is not found
   */
  String loadStringOrDefault(String moduleName, String property, String defaultValue);

  /**
   * Checks if configuration file for given module exists.
   *
   * @param moduleName name of a module
   * @return true if configuration file exists, false otherwise
   */
  boolean moduleFileExists(String moduleName);
}
