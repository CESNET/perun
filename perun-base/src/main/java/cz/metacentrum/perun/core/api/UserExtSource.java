package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * User external authentication.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class UserExtSource extends Auditable {

	private ExtSource extSource;
	private String login;
	private int userId = -1;
	private int loa;

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

	public UserExtSource(int id, ExtSource source, String login, int userId, int loa,
			String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.extSource = source;
		this.login = login;
		this.loa = loa;
		this.userId = userId;
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

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", login=<").append(getLogin() == null ? "\\0" : BeansUtils.createEscaping(getLogin())).append(">").append(
			", source=<").append(extSource == null ? "\\0" : getExtSource().serializeToString()).append(">").append(
			", loa=<").append(getLoa()).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[").append(
			"id='").append(getId()).append(
			"', login='").append(login).append('\'').append(
			", source='").append(extSource).append('\'').append(
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
}
