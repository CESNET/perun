package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Singleton class ModulesConfigContainer used to store and fetch modules configurations.
 * It is loaded by the Spring and it is accessible through the static getInstance() method.
 * Each configuration file is stored in the map modulesProperties under a separate key (file's name).
 */
public class ModulesConfigContainer {

	private static ModulesConfigContainer instance;
	private Map<String, ModuleConfiguration> modulesProperties;

	private ModulesConfigContainer() {}

	public static ModulesConfigContainer getInstance()
	{
		if (instance == null) instance = new ModulesConfigContainer();
		return instance;
	}

	/**
	 * Fetch property, stored for the module, as a String value
	 *
	 * @param moduleName name of the module for which the property is stored
	 * @param propertyName name of the property
	 * @return property as a String
	 */
	public String fetchPropertyAsString(String moduleName, String propertyName) {
		return fetchPropertyValue(moduleName, propertyName);
	}

	/**
	 * Fetch property, stored for the module, as an Integer value
	 *
	 * @param moduleName name of the module for which the property is stored
	 * @param propertyName name of the property
	 * @return property as an Integer
	 */
	public Integer fetchPropertyAsInteger(String moduleName, String propertyName) {
		String propertyValue = fetchPropertyValue(moduleName, propertyName);
		try {
			return Integer.parseInt(propertyValue);
		} catch (NumberFormatException e) {
			throw new InternalErrorException("Property " + propertyName + " in the module " + moduleName + " is in a bad format and cannot be fetched as int.");
		}
	}

	/**
	 * Fetch property, stored for the module, as a list of Strings
	 *
	 * @param moduleName name of the module for which the property is stored
	 * @param propertyName name of the property
	 * @return property as a list of Strings
	 */
	public List<String> fetchPropertyAsListOfStrings(String moduleName, String propertyName) {
		List<String> resultList = new ArrayList<>();
		String propertyValue = fetchPropertyValue(moduleName, propertyName);
		Arrays.asList(propertyValue.split(",")).forEach(elem -> resultList.add(elem.trim()));
		return resultList;
	}

	/**
	 * Fetch property, stored for the module, as a list of Integers
	 *
	 * @param moduleName name of the module for which the property is stored
	 * @param propertyName name of the property
	 * @return property as a list of Integers
	 */
	public List<Integer> fetchPropertyAsListOfIntegers(String moduleName, String propertyName) {
		String propertyValue = fetchPropertyValue(moduleName, propertyName);
		String[] stringValues = propertyValue.split(",");
		List<Integer> resultList = new ArrayList<>();
		try {
			for (String value : stringValues) {
				resultList.add(Integer.parseInt(value.trim()));
			}
		} catch (NumberFormatException e) {
			throw new InternalErrorException("Property " + propertyName + " in the module " + moduleName + " is in a bad format and cannot be fetched as list of Integers.");
		}
		return resultList;
	}

	public void setModulesProperties(Map<String, ModuleConfiguration> modulesProperties) {
		this.modulesProperties = modulesProperties;
	}

	/**
	 * Fetch property, stored for the module
	 *
	 * @param moduleName name of the module for which the property is stored
	 * @param propertyName name of the property
	 * @return property value
	 */
	private String fetchPropertyValue(String moduleName, String propertyName) {
		ModuleConfiguration moduleConfig = modulesProperties.get(moduleName);
		if (moduleConfig == null)
			throw new InternalErrorException("Configuration for module " + moduleName + " does not exists.");
		String propertyValue = moduleConfig.getModuleProperties().get(propertyName);
		if (propertyValue == null || propertyValue.isEmpty())
			throw new InternalErrorException("Property with name " + propertyName + " in module " + moduleName + " is not set.");
		return propertyValue;
	}
}
