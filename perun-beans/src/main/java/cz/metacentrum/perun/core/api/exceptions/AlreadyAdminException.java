package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.rt.AlreadyAdminRuntimeException;

/**
 * Checked version of AlreadyAdminException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.AlreadyAdminRuntimeException
 * @author Martin Kuba
 */
public class AlreadyAdminException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;
	private User user;
	private Facility facility;
	private Vo vo;
	private Group group;
	private Group authorizedGroup;

	public AlreadyAdminException(AlreadyAdminRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public AlreadyAdminException(String message) {
		super(message);
	}

	public AlreadyAdminException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyAdminException(Throwable cause) {
		super(cause);
	}

	public AlreadyAdminException(Member member) {
		super(member.toString());
		this.member = member;
	}

	// when user is already admin
	public AlreadyAdminException(String message, Throwable cause, User user, Vo vo) {
		super(message, cause);
		this.user = user;
		this.vo = vo;
	}

	public AlreadyAdminException(String message, Throwable cause, User user, Facility facility) {
		super(message, cause);
		this.user = user;
		this.facility = facility;
	}

	public AlreadyAdminException(String message, Throwable cause, User user, Group group) {
		super(message, cause);
		this.user = user;
		this.group = group;
	}

	public AlreadyAdminException(User user) {
		super(user.toString());
		this.user = user;
	}

	public AlreadyAdminException(User user, Vo vo) {
		super(user.toString());
		this.user = user;
		this.vo = vo;
	}

	// when group is already admin
	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, Vo vo) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
		this.vo = vo;
	}

	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, Facility facility) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
		this.facility = facility;
	}

	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, Group group) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
		this.group = group;
	}

	public AlreadyAdminException(Group authorizedGroup) {
		super(authorizedGroup.toString());
		this.authorizedGroup = authorizedGroup;
	}

	public AlreadyAdminException(Group authorizedGroup, Vo vo) {
		super(authorizedGroup.toString());
		this.authorizedGroup = authorizedGroup;
		this.vo = vo;
	}

	// getters

	public Member getMember() {
		return member;
	}

	public User getUser() {
		return user;
	}

	public Vo getVo() {
		return vo;
	}

	public Facility getFacility() {
		return facility;
	}

	public Group getGroup() {
		return group;
	}

	public Group getAuthorizedGroup() {
		return authorizedGroup;
	}

}
