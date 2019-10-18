package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.*;

/**
 * Thrown when trying to assign role to a user/group while the user/group already has the role.
 *
 */
public class AlreadyAdminException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;
	private User user;
	private Facility facility;
	private Resource resource;
	private Vo vo;
	private Group group;
	private Group authorizedGroup;
	private SecurityTeam securityTeam;
	private Role role;


	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public AlreadyAdminException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AlreadyAdminException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AlreadyAdminException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the member
	 * @param member member who is already in the role
	 */
	public AlreadyAdminException(Member member) {
		super(member.toString());
		this.member = member;
	}

	/**
	 * Constructor with a message, Throwable object, user, vo and role
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param user user who is already in the role
	 * @param vo the vo in which the user is already in the specific role
	 * @param role the role in which the user already is
	 */
	public AlreadyAdminException(String message, Throwable cause, User user, Vo vo, Role role) {
		super(message, cause);
		this.user = user;
		this.vo = vo;
		this.role = role;
	}

	/**
	 * Constructor with a message, Throwable object, user, resource and role
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param user user who is already in the role
	 * @param resource resource in which the user is already in the specific role
	 * @param role the role in which the user already is
	 */
	public AlreadyAdminException(String message, Throwable cause, User user, Resource resource, Role role) {
		super(message, cause);
		this.user = user;
		this.resource = resource;
		this.role = role;
	}

	/**
	 * Constructor with a message, Throwable object, user and sponsored user
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param user user who is already a sponsor
	 * @param sponsoredUser user who is already sponsored by that user
	 */
	public AlreadyAdminException(String message, Throwable cause, User user, User sponsoredUser) {
		super(message, cause);
		this.user = user;
	}

	/**
	 * Constructor with a message, Throwable object, user and facility
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param user user who is already in the specific role
	 * @param facility facility in which the user is already in the specific role
	 */
	public AlreadyAdminException(String message, Throwable cause, User user, Facility facility) {
		super(message, cause);
		this.user = user;
		this.facility = facility;
	}

	/**
	 * Constructor with a message, Throwable object, user and resource
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param user user who is already in the specific role
	 * @param resource resource in which the user is already in the specific role
	 */
	public AlreadyAdminException(String message, Throwable cause, User user, Resource resource) {
		super(message, cause);
		this.user = user;
		this.resource = resource;
	}

	/**
	 * Constructor with a message, Throwable object, user and a group
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param user user who is already in the specific role
	 * @param group group in which the user is already in the specific role
	 */
	public AlreadyAdminException(String message, Throwable cause, User user, Group group) {
		super(message, cause);
		this.user = user;
		this.group = group;
	}

	/**
	 * Constructor with the user
	 * @param user user who is already in the specific role
	 */
	public AlreadyAdminException(User user) {
		super(user.toString());
		this.user = user;
	}

	/**
	 * Constructor with the user and the vo
	 * @param user user who is already in the specific role
	 * @param vo vo in which the user is already in the specific role
	 */
	public AlreadyAdminException(User user, Vo vo) {
		super(user.toString());
		this.user = user;
		this.vo = vo;
	}

	/**
	 * Constructor with a message, Throwable object, authorized group, vo and role
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param authorizedGroup group which is already in the specific role
	 * @param vo vo in which the group is already in the specific role
	 * @param role the role in which the group already is
	 */
	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, Vo vo, Role role) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
		this.vo = vo;
		this.role = role;
	}

	/**
	 * Constructor with a message, Throwable object, authorized group, resource and role
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param authorizedGroup group which is already in the specific role
	 * @param resource resource in which the group is already in the specific role
	 * @param role the role in which the group already is
	 */
	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, Resource resource, Role role) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
		this.resource = resource;
		this.role = role;
	}

	/**
	 * Constructor with a message, Throwable object, authorized group and resource
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param authorizedGroup group which is already in the specific role
	 * @param facility facility in which the group is already in the specific role
	 */
	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, Facility facility) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
		this.facility = facility;
	}

	/**
	 * Constructor with a message, Throwable object, authorized group and resource
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param authorizedGroup group which is already in the specific role
	 * @param resource resource in which the group is already in the specific role
	 */
	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, Resource resource) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
		this.resource = resource;
	}

	/**
	 * Constructor with a message, Throwable object, authorized group and sponsored user
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param authorizedGroup group which is already a sponsor
	 * @param sponsoredUser user who is already sponsored by the group
	 */
	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, User sponsoredUser) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
	}

	/**
	 * Constructor with a message, Throwable object, authorized group and group
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param authorizedGroup group which is already in the specific role
	 * @param group group in which the group is already in the specific role
	 */
	public AlreadyAdminException(String message, Throwable cause, Group authorizedGroup, Group group) {
		super(message, cause);
		this.authorizedGroup = authorizedGroup;
		this.group = group;
	}

	/**
	 * Constructor with the authorized group
	 * @param authorizedGroup group which is already in the specific role
	 */
	public AlreadyAdminException(Group authorizedGroup) {
		super(authorizedGroup.toString());
		this.authorizedGroup = authorizedGroup;
	}

	/**
	 * Constructor with the authorized group and the vo
	 * @param authorizedGroup group which is already a in the specific role
	 * @param vo resource in which the group is already in the specific role
	 */
	public AlreadyAdminException(Group authorizedGroup, Vo vo) {
		super(authorizedGroup.toString());
		this.authorizedGroup = authorizedGroup;
		this.vo = vo;
	}

	/**
	 * Constructor with a message, Throwable object, user and the security team
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param user user who is already in the specific role
	 * @param securityTeam security team in which the user is already in the specific role
	 */
	public AlreadyAdminException(String message, Throwable cause, User user, SecurityTeam securityTeam) {
		super(message, cause);
		this.user = user;
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with a message, user and security team
	 * @param message message with details about the cause
	 * @param user user who is already in the specific role
	 * @param securityTeam security team in which the user is already in the specific role
	 */
	public AlreadyAdminException(String message, User user, SecurityTeam securityTeam) {
		super(message);
		this.user = user;
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with a message, Throwable object, group and a security team
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param group group which is already in the specific role
	 * @param securityTeam security team in which the group is already in the specific role
	 */
	public AlreadyAdminException(String message, Throwable cause, Group group, SecurityTeam securityTeam) {
		super(message, cause);
		this.authorizedGroup = group;
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with a message, group and a security team
	 * @param message message with details about the cause
	 * @param group group which is already in the specific role
	 * @param securityTeam security team in which the group is already in the specific role
	 */
	public AlreadyAdminException(String message, Group group, SecurityTeam securityTeam) {
		super(message);
		this.authorizedGroup = group;
		this.securityTeam = securityTeam;
	}

	// getters

	/**
	 * Getter for the member
	 * @return
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * Getter for the user
	 * @return the user who is already in the specific role
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Getter for the vo
	 * @return the vo in which the user/entity is already in the specific role
	 */
	public Vo getVo() {
		return vo;
	}

	/**
	 * Getter for the facility
	 * @return facility in which the user/entity is already in the specific role
	 */
	public Facility getFacility() {
		return facility;
	}

	/**
	 * Getter for the resource
	 * @return resource in which the user/entity is already in the specific role
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Getter for the group
	 * @return group in which the user/entity is already in the specific role
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * Getter for the authorized group
	 * @return group which is already in the specific role
	 */
	public Group getAuthorizedGroup() {
		return authorizedGroup;
	}

	/**
	 * Getter for the security team
	 * @return security team in which the user/entity is already in the specific role
	 */
	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}

	/**
	 * Getter for the role
	 * @return the specific role in which the user/group already is
	 */
	public Role getRole() {
		return role;
	}
}
