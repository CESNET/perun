package cz.metacentrum.perun.core.api;


/**
 * Candidate with member and rich user of a Virtual Organization.
 *
 * @author Vojtech Sassmann &lt;vojtech.sassmann@gmail.com&gt;
 */
public class MemberCandidate {

	private Candidate candidate;

	private Member member;

	private RichUser richUser;

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public RichUser getRichUser() {
		return richUser;
	}

	public void setRichUser(RichUser richUser) {
		this.richUser = richUser;
	}

	/**
	 *  Returns bean name like VO, Member, Resource,...
	 */
	public String getBeanName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MemberCandidate that = (MemberCandidate) o;

		if (candidate != null ? !candidate.equals(that.candidate) : that.candidate != null) return false;
		if (member != null ? !member.equals(that.member) : that.member != null) return false;
		return richUser != null ? richUser.equals(that.richUser) : that.richUser == null;
	}

	@Override
	public int hashCode() {
		int result = candidate != null ? candidate.hashCode() : 0;
		result = 31 * result + (member != null ? member.hashCode() : 0);
		result = 31 * result + (richUser != null ? richUser.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+ ":[" +
				"candidate='" + candidate +
				"', member='" + member +
				"', richUser='" + richUser +
				"']";
	}
}
