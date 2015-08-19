package cz.metacentrum.perun.core.api;

/**
 * RT message bean
 *
 * @author Michal Stava <stavamichal@gmail.cz>
 */
public class RTMessage {

	private String memberPreferredEmail;
	private Integer ticketNumber;

	public RTMessage(){
	}

	public RTMessage(String memberPreferredEmail, Integer ticketNumber){
		this.memberPreferredEmail = memberPreferredEmail;
		this.ticketNumber = ticketNumber;
	}

	public String getMemberPreferredEmail() {
		return memberPreferredEmail;
	}

	public void setMemberPreferredEmail(String memberPreferredEmail) {
		this.memberPreferredEmail = memberPreferredEmail;
	}

	public Integer getTicketNumber() {
		return ticketNumber;
	}

	public void setTicketNumber(Integer ticketNumber) {
		this.ticketNumber = ticketNumber;
	}


}
