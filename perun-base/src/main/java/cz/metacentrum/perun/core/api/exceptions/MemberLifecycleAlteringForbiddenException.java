package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * Operation altering member's lifecycle is forbidden (e.g. cannot change expiration of hierarchical vo's members, if
 * they come from member vos)
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class MemberLifecycleAlteringForbiddenException extends PerunException {
  static final long serialVersionUID = 0;

  private Member member;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public MemberLifecycleAlteringForbiddenException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public MemberLifecycleAlteringForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public MemberLifecycleAlteringForbiddenException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the member
   *
   * @param member member whose lifecycle cannot be altered
   */
  public MemberLifecycleAlteringForbiddenException(Member member) {
    super(member.toString());
    this.member = member;
  }

  /**
   * Constructor with the member and message
   *
   * @param member  member whose lifecycle cannot be altered
   * @param message with details about the cause
   */
  public MemberLifecycleAlteringForbiddenException(Member member, String message) {
    super(message);
    this.member = member;
  }

  /**
   * Getter for the member
   *
   * @return member whose lifecycle cannot be altered
   */
  public Member getMember() {
    return this.member;
  }
}
