package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.ExtSource;

import java.util.List;

/**
 * Class represents user identity for Consolidator purposes.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class Identity {

	private int id;
	private String name;
	private String organization;
	private String email;
	private List<ExtSource> identities;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<ExtSource> getIdentities() {
		return identities;
	}

	public void setIdentities(List<ExtSource> identities) {
		this.identities = identities;
	}

	public String getBeanName() {
		return this.getClass().getSimpleName();
	}

}