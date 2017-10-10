package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * User external authentication.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class UserExtSource extends Auditable implements Comparable<PerunBean> {

	private ExtSource extSource;
	private String login;
	private int userId = -1;
	private int loa;
	/* Persistent flag of this UserExtSource.
	 * false = UserExtSource can be removed. It is truly external.
	 * true = UserExtSource can NOT be removed. It is somehow important and needed in the system. */
	private boolean persistent;

	public UserExtSource(){
		super();
	}

	public UserExtSource(int id, ExtSource source, String login, int userId, int loa) {
		this(id, source, login, userId);
		this.loa = loa;
	}

	public UserExtSource(int id, ExtSource source, String login, int userId) {
		this(id, source, login);
		this.userId = userId;
	}

	public UserExtSource(int id, ExtSource source, String login) {
		super(id);
		this.login = login;
		this.extSource = source;
	}

	public UserExtSource(ExtSource source, int loa, String login) {
		this.loa = loa;
		this.login = login;
		this.extSource = source;
	}

	public UserExtSource(ExtSource source, String login) {
		this.login = login;
		this.extSource = source;
	}

	public UserExtSource(int id, ExtSource source, String login, int userId, int loa, boolean persistent,
	                     String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.extSource = source;
		this.login = login;
		this.loa = loa;
		this.userId = userId;
		this.persistent = persistent;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public ExtSource getExtSource() {
		return extSource;
	}

	public void setExtSource(ExtSource source) {
		this.extSource = source;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getLoa() {
		return loa;
	}

	public void setLoa(int loa) {
		this.loa = loa;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id=<").append(getId()).append(">").append(
				", login=<").append(getLogin() == null ? "\\0" : BeansUtils.createEscaping(getLogin())).append(">").append(
				", source=<").append(extSource == null ? "\\0" : getExtSource().serializeToString()).append(">").append(
				", userId=<").append(getUserId()).append(">").append(
				", loa=<").append(getLoa()).append(">").append(
				']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[").append(
				"id='").append(getId()).append('\'').append(
				", login='").append(login).append('\'').append(
				", source='").append(extSource).append('\'').append(
				", userId='").append(getUserId()).append('\'').append(
				", loa='").append(loa).append('\'').append(
				']').toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !getClass().equals(o.getClass())) return false;

		UserExtSource that = (UserExtSource) o;

		if (login != null ? !login.equals(that.login) : that.login != null) return false;
		if (extSource != null ? !extSource.equals(that.extSource) : that.extSource != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = login != null ? login.hashCode() : 0;
		result = 31 * result + (extSource != null ? extSource.hashCode() : 0);
		return result;
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof UserExtSource) {
			UserExtSource userExtSource = (UserExtSource) perunBean;
			int compare;
			//Compare on extSource
			if (this.getExtSource() == null && userExtSource.getExtSource() != null) compare = -1;
			else if (userExtSource.getExtSource() == null && this.getExtSource() != null) compare = 1;
			else if (this.getExtSource() == null && userExtSource.getExtSource() == null) compare = 0;
			else compare = this.getExtSource().compareTo(userExtSource.getExtSource());
			if(compare != 0) return compare;
			//Compare on login
			if (this.getLogin()== null && userExtSource.getLogin() != null) compare = -1;
			else if (userExtSource.getLogin() == null && this.getLogin() != null) compare = 1;
			else if (this.getLogin()== null && userExtSource.getLogin() == null) compare = 0;
			else compare = this.getLogin().compareToIgnoreCase(userExtSource.getLogin());
			if(compare != 0) return compare;
			//Compare to id if not
			return (this.getId() - perunBean.getId());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
