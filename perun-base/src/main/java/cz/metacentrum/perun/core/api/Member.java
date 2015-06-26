package cz.metacentrum.perun.core.api;

/**
 * Member of a Virtual Organization.
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Member extends Auditable {

	private int userId;
	private int voId;
	private Status status;
	private MembershipType membershipType;
	private Integer sourceGroupId;

	public Member() {
		super();
		membershipType = MembershipType.NOT_DEFINED;
		sourceGroupId = null;
	}

	public Member(int id) {
		super(id);
		membershipType = MembershipType.NOT_DEFINED;
		sourceGroupId = null;
	}

	public Member(int id, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		membershipType = MembershipType.NOT_DEFINED;
		sourceGroupId = null;
	}

	public Member(int id, int userId) {
		this(id);
		this.userId = userId;
		membershipType = MembershipType.NOT_DEFINED;
		sourceGroupId = null;
	}

	public Member(int id, int userId, int voId, Status status) {
		this(id, userId);
		this.voId = voId;
		this.status = status;
		membershipType = MembershipType.NOT_DEFINED;
		sourceGroupId = null;
	}

	public Member(int id, int userId, int voId, Status status, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.userId = userId;
		this.voId = voId;
		this.status = status;
		membershipType = MembershipType.NOT_DEFINED;
		sourceGroupId = null;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setStatus(String status) {
		if(status == null) this.status = null;
		else this.status = Status.valueOf(status);
	}

	public int getVoId() {
		return voId;
	}

	public void setVoId(int voId) {
		this.voId = voId;
	}

	public void setMembershipType(MembershipType type) {
		this.membershipType = type;
	}

	public void setMembershipType(String type) {
		if(type == null) this.membershipType = null;
		else this.membershipType = MembershipType.valueOf(type);
	}

	public MembershipType getMembershipType() {
		return membershipType;
	}

    public Integer getSourceGroupId() {
        return sourceGroupId;
    }

    public void setSourceGroupId(Integer sourceGroupId) {
        this.sourceGroupId = sourceGroupId;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		Member other = (Member) obj;
		if (getId() != other.getId()) {
			return false;
		}
		return true;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", userId=<").append(getUserId()).append(">").append(
			", voId=<").append(getVoId()).append(">").append(
			", status=<").append(getStatus() == null ? "\\0" : BeansUtils.createEscaping(getStatus().toString())).append(">").append(
			", type=<").append(getMembershipType() == null ? "\\0" : BeansUtils.createEscaping(getMembershipType().toString())).append(">").append(
			", sourceGroupId=<").append(getSourceGroupId() == null ? "\\0" : BeansUtils.createEscaping(getSourceGroupId().toString())).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append("Member:[id='").append(getId()).append("', userId='").append(userId).append("', voId='").append(voId).append("', status='").append(status).append("', type='").append(membershipType).append("', sourceGroupId='").append(sourceGroupId).append("']").toString();
	}


}
