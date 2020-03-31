package cz.metacentrum.perun.core.api;

import java.util.Map;
import java.util.Objects;

/**
 * ModuleConfiguration represents properties for a particular attribute module.
 *
 * moduleName is a configuration file's name without ".properties" extension.
 * moduleProperties is a map of module's properties where each key is a name of the property
 * and value is a value of the corresponding property.
 *
 */
public class ModuleConfiguration {

	private String moduleName;
	private Map<String, String> moduleProperties;

	public ModuleConfiguration(String moduleName, Map<String, String> moduleProperties) {
		this.moduleName = moduleName;
		this.moduleProperties = moduleProperties;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public Map<String, String> getModuleProperties() {
		return moduleProperties;
	}

	public void setModuleProperties(Map<String, String> moduleProperties) {
		this.moduleProperties = moduleProperties;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ModuleConfiguration that = (ModuleConfiguration) o;
		return Objects.equals(getModuleName(), that.getModuleName()) &&
			Objects.equals(getModuleProperties(), that.getModuleProperties());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getModuleName(), getModuleProperties());
	}

	@Override
	public String toString() {
		return "ModuleConfiguration{" +
			"moduleName='" + moduleName + '\'' +
			", moduleProperties=" + moduleProperties +
			'}';
	}
}
