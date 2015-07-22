package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;

/**
 * Represents an application by a user to a VO. Application is a request for becoming a member of the VO.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Application {

	public static enum AppState { NEW, VERIFIED, APPROVED, REJECTED }

	public static enum AppType { INITIAL, EXTENSION }

	private int id;
	private Vo vo;
	private Group group;
	private AppType type;
	private String fedInfo;
	private AppState state = AppState.NEW;
	private String extSourceName;
	private String extSourceType;
	private int extSourceLoa = 0; // 0 - by default
	private User user;

	private String createdBy;
	private String createdAt;
	private String modifiedBy;
	private String modifiedAt;

	public Application() {
	}

	public Application(int id, Vo vo, Group group, AppType type, String fedInfo, AppState state, String extSourceName, String extSourceType, User user) {
		this.id = id;
		this.vo = vo;
		this.group = group;
		this.type = type;
		this.fedInfo = fedInfo;
		this.state = state;
		this.extSourceName = extSourceName;
		this.extSourceType = extSourceType;
		this.user = user;
	}

	public Application(int id, Vo vo, Group group, AppType type, String fedInfo, AppState state, String extSourceName, String extSourceType, int extSourceLoa, User user) {
		this(id, vo, group, type, fedInfo, state, extSourceName, extSourceType, user);
		this.extSourceLoa = extSourceLoa;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Vo getVo() {
		return vo;
	}

	public void setVo(Vo vo) {
		this.vo = vo;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public AppType getType() {
		return type;
	}

	public void setType(AppType type) {
		this.type = type;
	}

	public String getFedInfo() {
		return fedInfo;
	}

	public void setFedInfo(String fedInfo) {
		this.fedInfo = fedInfo;
	}

	public AppState getState() {
		return state;
	}

	public void setState(AppState state) {
		this.state = state;
	}

	public String getExtSourceName() {
		return extSourceName;
	}

	public void setExtSourceName(String extSourceName) {
		this.extSourceName = extSourceName;
	}

	public void setExtSourceType(String extSourceType) {
		this.extSourceType = extSourceType;
	}

	public String getExtSourceType() {
		return extSourceType;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getExtSourceLoa() {
		return extSourceLoa;
	}

	public void setExtSourceLoa(int extSourceLoa) {
		this.extSourceLoa = extSourceLoa;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	/**
	 * Return bean name as PerunBean does.
	 *
	 * @return Class simple name (beanName)
	 */
	public String getBeanName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+":[" +
			"id='" + getId() + '\'' +
			", vo='" + getVo() + '\'' +
			", group='" + getGroup() + '\'' +
			", fedInfo='" + getFedInfo() + '\'' +
			", type='" + getType().toString() + '\'' +
			", state='" + getState().toString() + '\'' +
			", extSourceName='" + getExtSourceName() + '\'' +
			", extSourceType='" + getExtSourceType() + '\'' +
			", extSourceLoa='" + getExtSourceLoa() + '\'' +
			", user='" + getUser() + '\'' +
			", created_at='" + getCreatedAt() + '\'' +
			", created_by='" + getCreatedBy() + '\'' +
			", modified_at='" + getModifiedAt() + '\'' +
			", modified_by='" + getModifiedBy() + '\'' +
			']';
	}

}
