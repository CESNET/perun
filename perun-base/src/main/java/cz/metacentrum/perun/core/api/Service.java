package cz.metacentrum.perun.core.api;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class Service extends Auditable implements Comparable<PerunBean> {

	private String name;
	private String description;
	private int delay = 10;
	private int recurrence = 2;
	private boolean enabled = true;
	private String script;

	public Service(){
		super();
	}

	public Service(int id, String name, String description){
		super(id);
		this.name = name;
		this.description = description;
	}

	public Service(int id, String name, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid){
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(int recurrence) {
		this.recurrence = recurrence;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
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
		Service other = (Service) obj;
		if (getId() != other.getId())
			return false;
		return true;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id=<").append(getId()).append(">").append(
				", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
				", description=<").append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">").append(
				", delay=<").append(this.getDelay()).append(">").append(
				", recurrence=<").append(this.getRecurrence()).append(">").append(
				", enabled=<").append(this.isEnabled()).append(">").append(
				", script=<").append(getScript() == null ? "\\0" : BeansUtils.createEscaping(getScript())).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(getClass().getSimpleName()).append(":[id='").append(getId()).append("', name='").append(name)
				.append("', description='").append(getDescription()).append("', delay='").append(getDelay())
				.append("', recurrence='").append(getRecurrence()).append("', enabled='").append(isEnabled())
				.append("', script='").append(getScript()).append("']").toString();
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof Service) {
			Service service = (Service) perunBean;
			if (this.getName() == null && service.getName() != null) return -1;
			if (service.getName() == null && this.getName() != null) return 1;
			if (this.getName() == null && service.getName() == null) return 0;
			return this.getName().compareToIgnoreCase(service.getName());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
