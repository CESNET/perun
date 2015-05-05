package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

/**
 * Application form of a VO. Use {@link cz.metacentrum.perun.registrar.RegistrarManager#getFormItems} for items.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ApplicationForm {

	private int id;
	private Vo vo;
	private Group group;
	private boolean automaticApproval;
	private boolean automaticApprovalExtension;
	private String moduleClassName;

	public ApplicationForm() {
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

	public boolean isAutomaticApproval() {
		return automaticApproval;
	}

	public void setAutomaticApproval(boolean automaticApproval) {
		this.automaticApproval = automaticApproval;
	}

	public boolean isAutomaticApprovalExtension() {
		return automaticApprovalExtension;
	}

	public void setAutomaticApprovalExtension(boolean automaticApproval) {
		this.automaticApprovalExtension = automaticApproval;
	}

	public String getModuleClassName() {
		return moduleClassName;
	}

	public void setModuleClassName(String moduleClassName) {
		this.moduleClassName = moduleClassName;
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
			", automaticApproval='" + isAutomaticApproval() + '\'' +
			", automaticApprovalExtension='" + isAutomaticApprovalExtension() + '\'' +
			", moduleClassName='" + getModuleClassName() + '\'' +
			"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplicationForm other = (ApplicationForm) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
