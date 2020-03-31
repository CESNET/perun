package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ModuleConfiguration;
import cz.metacentrum.perun.core.api.ModulesConfigContainer;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The purpose of the ModulesConfigLoader is to load modules configurations from the all defined module's configuration files.
 * Only files which ends with ".properties" are loaded.
 *
 * Production configuration files are located in /etc/perun/modules/
 * Configuration files which are used during the build are located in perun-base/src/test/resources/modules/
 */
public class ModulesConfigLoader {

	private Resource[] configurationPath;

	/**
	 *  Load configuration for modules into the ModulesConfigContainer as a map
	 *  where a key is represented by the file name without the ".properties extension"
	 *  and a value is ModuleConfiguration which holds all module's properties.
	 */
	public void loadConfiguration() {
		ModulesConfigContainer container = ModulesConfigContainer.getInstance();
		Map<String, ModuleConfiguration> modulesProperties = new HashMap<>();
		for (Resource resource: configurationPath) {
			ModuleConfiguration moduleConfig = loadModuleProperties(resource);
			modulesProperties.put(moduleConfig.getModuleName(), moduleConfig);
		}
		container.setModulesProperties(modulesProperties);
	}

	public void setConfigurationPath(Resource[] configurationPath) {
		this.configurationPath = configurationPath;
	}

	/**
	 * Load module's properties and return them as a ModuleConfiguration class
	 *
	 * @param configuration resource with module's configuration
	 * @return ModuleConfiguration class which contains all module's properties
	 */
	private ModuleConfiguration loadModuleProperties(Resource configuration) {
		Map<String, String> moduleProperties = new HashMap<>();
		Properties properties = new Properties();
		try (InputStream is = configuration.getInputStream()) {
			properties.load(is);
			properties.forEach((k, v) -> moduleProperties.put((String) k, (String) v));
		} catch (IOException e) {
			throw new InternalErrorException("IO exception was thrown during the processing of the file: " + configuration, e);
		}
		String moduleName = configuration.getFilename().replace(".properties", "");
		return new ModuleConfiguration(moduleName, moduleProperties);
	}
}
