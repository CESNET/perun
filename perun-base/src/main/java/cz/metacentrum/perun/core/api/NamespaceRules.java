package cz.metacentrum.perun.core.api;

import java.util.Objects;
import java.util.Set;

public class NamespaceRules {

	private String namespaceName;
	private String defaultEmail;
	private Set<String> requiredAttributes;
	private Set<String> optionalAttributes;

	public NamespaceRules(String namespaceName, String defaultEmail, Set<String> requiredAttributes, Set<String> optionalAttributes) {
		this.namespaceName = namespaceName;
		this.defaultEmail = defaultEmail;
		this.requiredAttributes = requiredAttributes;
		this.optionalAttributes = optionalAttributes;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public Set<String> getRequiredAttributes() {
		return requiredAttributes;
	}

	public void setRequiredAttributes(Set<String> requiredAttributes) {
		this.requiredAttributes = requiredAttributes;
	}

	public Set<String> getOptionalAttributes() {
		return optionalAttributes;
	}

	public void setOptionalAttributes(Set<String> optionalAttributes) {
		this.optionalAttributes = optionalAttributes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NamespaceRules that = (NamespaceRules) o;
		return Objects.equals(getNamespaceName(), that.getNamespaceName()) && Objects.equals(getRequiredAttributes(), that.getRequiredAttributes()) && Objects.equals(getOptionalAttributes(), that.getOptionalAttributes());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getNamespaceName(), getRequiredAttributes(), getOptionalAttributes());
	}

	@Override
	public String toString() {
		return "NamespaceRules{" +
			"namespaceName='" + namespaceName + '\'' +
			", requiredAttributes=" + requiredAttributes +
			", optionalAttributes=" + optionalAttributes +
			'}';
	}

	public String getDefaultEmail() {
		return defaultEmail;
	}

	public void setDefaultEmail(String defaultEmail) {
		this.defaultEmail = defaultEmail;
	}
}
