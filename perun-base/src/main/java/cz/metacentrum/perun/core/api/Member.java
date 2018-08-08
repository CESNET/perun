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
	private boolean sponsored = false;

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

	public Integer getSourceGroupId() {
		return sourceGroupId;
	}

	public void setSourceGroupId(Integer sourceGroupId) {
		this.sourceGroupId = sourceGroupId;
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

	public boolean isSponsored() {
		return sponsored;
	}

	public void setSponsored(boolean sponsored) {
		this.sponsored = sponsored;
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
		if (!(obj instanceof Member)) {
			return false;
		}
		Member other = (Member) obj;
		return getId() == other.getId();
	}

	@Override
	public String serializeToString() {
		return this.getClass().getSimpleName() + ":[" +
				"id=<" + getId() + ">" +
				", userId=<" + getUserId() + ">" +
				", voId=<" + getVoId() + ">" +
				", status=<" + (getStatus() == null ? "\\0" : BeansUtils.createEscaping(getStatus().toString())) + ">" +
				", type=<" + (getMembershipType() == null ? "\\0" : BeansUtils.createEscaping(getMembershipType().toString())) + ">" +
				", sourceGroupId=<" + (getSourceGroupId() == null ? "\\0" : getSourceGroupId().toString()) + ">" +
				", sponsored=<" + sponsored + ">" +
				']';
	}

	@Override
	public String toString() {
		return "Member:[id='" + getId() + "', userId='" + userId + "', voId='" + voId + "', status='" + status + "', type='" + membershipType + "', sourceGroupId='" + sourceGroupId + "', sponsored='" + sponsored+ "']";
	}
}
