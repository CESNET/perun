package cz.metacentrum.perun.core.api;


/**
 * Represents auditors message.
 *
 * @author Michal Stava
 */
public class AuditMessage {

	protected int id;
	protected String msg;
	protected String actor;
	protected String createdAt;
	protected Integer createdByUid;

	public AuditMessage() {
	}

	public AuditMessage(int id, String msg, String actor, String createdAt, Integer createdByUid) {
		this();
		this.id = id;
		this.msg = msg;
		this.actor = actor;
		this.createdAt = createdAt;
		this.createdByUid = createdByUid;
	}

	public String getFullMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(" \"").append(createdAt).append("\" \"").append(actor).append("\" ").append(msg);
		return sb.toString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public Integer getCreatedByUid() {
		return createdByUid;
	}

	public void setCreatedByUid(Integer createdByUid) {
		this.createdByUid = createdByUid;
	}

	/**
	 * Compares messages.
	 * @see Comparable#compareTo(Object)
	 * @param auditMessage AuditMessage
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
	 */
	public int compareTo(AuditMessage auditMessage) {
		int comp = compare(this.getMsg(),auditMessage.getMsg());
		if (comp!=0) {
			return comp;
		} else {
			comp = compare(this.getCreatedAt(),auditMessage.getCreatedAt());
			if (comp!=0) {
				return comp;
			} else {
				return this.getId()-auditMessage.getId();
			}
		}
	}

	/**
	 * Compares Strings and handles null values.
	 * @param s1 string or null
	 * @param s2 string or null
	 * @return compare of the two strings
	 */
	private int compare(String s1,String s2) {
		if (s1==null) s1 = "";
		if (s2==null) s2 = "";
		return s1.compareTo(s2);
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(getClass().getSimpleName());
		ret.append(":[id='");
		ret.append(id);
		ret.append("', msg='");
		ret.append(msg);
		ret.append("']");

		return ret.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + id;
		result = prime * result
			+ ((createdAt == null) ? 0 : createdAt.hashCode());
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
		AuditMessage other = (AuditMessage) obj;
		if (id != other.id)
			return false;
		if (msg == null) {
			if (other.msg != null)
				return false;
		} else if (!msg.equals(other.msg))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (createdByUid == null) {
			if (other.createdByUid != null)
				return false;
		} else if (other.createdByUid == null) {
			return false;
		} else if (createdByUid != other.createdByUid)
			return false;
		return true;
	}
}
