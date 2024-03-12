package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class SponsorshipDoesNotExistException extends PerunException {
  public SponsorshipDoesNotExistException(Member sponsoredMember, User sponsor) {
    super(sponsoredMember + " is not sponsored by: " + sponsor);
  }

  public SponsorshipDoesNotExistException(String s) {
    super(s);
  }

  public SponsorshipDoesNotExistException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public SponsorshipDoesNotExistException(Throwable throwable) {
    super(throwable);
  }
}
