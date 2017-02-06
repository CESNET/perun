package cz.metacentrum.perun.cabinet.model;

import cz.metacentrum.perun.core.api.PerunBean;
/**
 * Class represents a publication system, i.e. prezentator.
 * Holds information necessary to work with it (connection url etc).
 *
 * Property type is supposed to hold full qualified class name of appropriate
 * business strategy class (i.e. prezentator strategy).
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PublicationSystem extends PerunBean {

	private String friendlyName;
	private String url;
	private String username;
	private String password;
	private String loginNamespace;
	private String type;

	/**
	 * This method returns the value of the database column PUBLICATION_SYSTEM.friendlyName
	 *
	 * @return the value of PUBLICATION_SYSTEM.friendlyName
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * This method sets the value of the database column PUBLICATION_SYSTEM.friendlyName
	 *
	 * @param friendlyName the value for PUBLICATION_SYSTEM.friendlyName
	 */
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName == null ? null : friendlyName.trim();
	}

	/**
	 * This method returns the value of the database column PUBLICATION_SYSTEM.url
	 *
	 * @return the value of PUBLICATION_SYSTEM.url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * This method sets the value of the database column PUBLICATION_SYSTEM.url
	 *
	 * @param url the value for PUBLICATION_SYSTEM.url
	 */
	public void setUrl(String url) {
		this.url = url == null ? null : url.trim();
	}

	/**
	 * This method returns the value of the database column PUBLICATION_SYSTEM.username
	 *
	 * @return the value of PUBLICATION_SYSTEM.username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * This method sets the value of the database column PUBLICATION_SYSTEM.username
	 *
	 * @param username the value for PUBLICATION_SYSTEM.username
	 */
	public void setUsername(String username) {
		this.username = username == null ? null : username.trim();
	}

	/**
	 * This method returns the value of the database column PUBLICATION_SYSTEM.password
	 *
	 * @return the value of PUBLICATION_SYSTEM.password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * This method sets the value of the database column PUBLICATION_SYSTEM.password
	 *
	 * @param password the value for PUBLICATION_SYSTEM.password
	 */
	public void setPassword(String password) {
		this.password = password == null ? null : password.trim();
	}

	/**
	 * This method returns the value of the database column PUBLICATION_SYSTEM.loginNamespace
	 *
	 * @return the value of PUBLICATION_SYSTEM.loginNamespace
	 */
	public String getLoginNamespace() {
		return loginNamespace;
	}

	/**
	 * This method sets the value of the database column PUBLICATION_SYSTEM.loginNamespace
	 *
	 * @param loginNamespace the value for PUBLICATION_SYSTEM.loginNamespace
	 */
	public void setLoginNamespace(String loginNamespace) {
		this.loginNamespace = loginNamespace == null ? null : loginNamespace.trim();
	}

	/**
	 * This method returns the value of the database column PUBLICATION_SYSTEM.type
	 *
	 * @return the value of PUBLICATION_SYSTEM.type
	 */
	public String getType() {
		return type;
	}

	/**
	 * This method sets the value of the database column PUBLICATION_SYSTEM.type
	 *
	 * @param type the value for PUBLICATION_SYSTEM.type
	 */
	public void setType(String type) {
		this.type = type == null ? null : type.trim();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(getClass().getSimpleName()).append(":[id=").append(this.getId()).append(", friendlyName=").append(this.getFriendlyName()).append(", loginNamespace=").append(this.getLoginNamespace()).append(", username=").append(this.getUsername()).append(", password=").append(this.getPassword()).append(", type=").append(this.getType()).append(", url=").append(this.getUrl()).append("]").toString();
	}

}
