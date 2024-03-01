package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception throw when application can't be approved by custom VO rules. It's not meant as a "hard" error but only as a
 * notice to GUI.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CantBeApprovedException extends PerunException {

  private static final long serialVersionUID = 1L;

  private String reason = null;
  private String category = null;
  private String affiliation = null;
  private boolean isSoft = false;
  private int applicationId = 0;

  public CantBeApprovedException(String message) {
    super(message);
  }

  public CantBeApprovedException(String message, String reason, String category, String affiliation,
                                 int applicationId) {
    super(message);
    this.reason = reason;
    this.category = category;
    this.affiliation = affiliation;
    this.applicationId = applicationId;
  }

  public CantBeApprovedException(String message, String reason, String category, String affiliation, boolean isSoft,
                                 int applicationId) {
    super(message);
    this.reason = reason;
    this.category = category;
    this.affiliation = affiliation;
    this.isSoft = isSoft;
    this.applicationId = applicationId;
  }

  public CantBeApprovedException(String message, Throwable ex) {
    super(message, ex);
  }

  public CantBeApprovedException(String message, String reason, Throwable ex) {
    super(message, ex);
    this.reason = reason;
  }

  public String getAffiliation() {
    return affiliation;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public String getCategory() {
    return category;
  }

  public String getReason() {
    return reason;
  }

  public boolean isSoft() {
    return isSoft;
  }

  public void setAffiliation(String affiliation) {
    this.affiliation = affiliation;
  }

  public void setApplicationId(int applicationId) {
    this.applicationId = applicationId;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public void setSoft(boolean soft) {
    isSoft = soft;
  }

}
