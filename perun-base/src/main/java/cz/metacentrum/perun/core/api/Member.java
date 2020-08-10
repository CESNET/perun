package cz.metacentrum.perun.core.api;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
	private Map<Integer, MemberGroupStatus> groupsStatuses = new HashMap<>();

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

	/**
	 * Adds member's status for given group. If member already had a VALID status
	 * for given group, nothing is changed.
	 *
	 * @param groupId group ID
	 * @param status member's status for given group
	 */
	public void putGroupStatus(int groupId, MemberGroupStatus status) {
		MemberGroupStatus currentValue = this.groupsStatuses.get(groupId);
		if (currentValue == MemberGroupStatus.VALID) {
			return;
		}

		groupsStatuses.put(groupId, status);
	}

	public Map<Integer, MemberGroupStatus> getGroupStatuses() {
		return Collections.unmodifiableMap(groupsStatuses);
	}

	/**
	 * Returns group status of member for given context.
	 *
	 * This value is used to calculate member's group status for groups
	 * that are relevant to given context. E.g.: If this member is returned from call
	 * ResourceManager.getAllowedMembers(), this status returns member's total group status
	 * calculated from groups that can access this resource and contains this member.
	 * @return memberGroup status for context relevant groups.
	 */
	public MemberGroupStatus getGroupStatus() {
		if (groupsStatuses.containsValue(MemberGroupStatus.EXPIRED) && !groupsStatuses.containsValue(MemberGroupStatus.VALID)) {
			return MemberGroupStatus.EXPIRED;
		}

		return MemberGroupStatus.VALID;
	}

	protected void setGroupsStatuses(Map<Integer, MemberGroupStatus> groupsStatuses) {
		if (groupsStatuses == null) {
			throw new IllegalArgumentException("Group statuses cannot be null.");
		}
		this.groupsStatuses = new HashMap<>(groupsStatuses);
	}

	/**
	 * Adds member's statuses for given group. If member already had a VALID status
	 * for any of given groups, then nothing is changed for the group.
	 *
	 * @param groupStatuses map containing group's IDs and member statuses
	 */
	public void putGroupStatuses(Map<Integer, MemberGroupStatus> groupStatuses) {
		if (groupStatuses == null) {
			throw new IllegalArgumentException("GroupStatuses cannot be null.");
		}
		for (Integer integer : groupStatuses.keySet()) {
			putGroupStatus(integer, groupStatuses.get(integer));
		}
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
		return "Member:[id='" + getId() +
			"', userId='" + userId +
			"', voId='" + voId +
			"', status='" + status +
			"', type='" + membershipType +
			"', sourceGroupId='" + sourceGroupId +
			"', sponsored='" + sponsored +
			"']";
	}
}
