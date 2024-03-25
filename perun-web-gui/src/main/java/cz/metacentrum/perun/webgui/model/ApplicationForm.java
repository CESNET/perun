package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for registrar: ApplicationForm
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class ApplicationForm extends JavaScriptObject {

  protected ApplicationForm() {
  }

  /**
   * Get ID
   *
   * @return id
   */
  public final native int getId() /*-{
		return this.id;
	}-*/;

  /**
   * Get {@link VirtualOrganization}
   *
   * @return VO
   */
  public final native VirtualOrganization getVo() /*-{
			return this.vo;
		}-*/;

  /**
   * Get {@link Group}
   *
   * @return VO
   */
  public final native Group getGroup() /*-{
			return this.group;
		}-*/;

  /**
   * Get automaticApproval for INITIAL app
   *
   * @return automaticApproval
   */
  public final native boolean getAutomaticApproval() /*-{
			if (!this.automaticApproval) return false;
			return this.automaticApproval;
		}-*/;

  /**
   * Set automaticApproval
   *
   * @param automatic (true=automatic / false=manual)
   */
  public final native void setAutomaticApproval(boolean automatic) /*-{
			this.automaticApproval = automatic;
		}-*/;

  public final native String getModuleClassNames() /*-{
			if (!this.moduleClassNames) return "";
			return this.moduleClassNames.join(",");
		}-*/;

  public final native void setModuleClassNames(String names) /*-{
			this.moduleClassNames = names.split(",");
		}-*/;

  /**
   * Get automaticApproval for EXTENSION
   *
   * @return automaticApproval
   */
  public final native boolean getAutomaticApprovalExtension() /*-{
			if (!this.automaticApprovalExtension) return false;
			return this.automaticApprovalExtension;
		}-*/;

  /**
   * Set automaticApprovalExtension
   *
   * @param automatic (true=automatic / false=manual)
   */
  public final native void setAutomaticApprovalExtension(boolean automatic) /*-{
			this.automaticApprovalExtension = automatic;
		}-*/;

  /**
   * Get automaticApproval for EMBEDDED
   *
   * @return automaticApproval
   */
  public final native boolean getAutomaticApprovalEmbedded() /*-{
			if (!this.automaticApprovalEmbedded) return false;
			return this.automaticApprovalEmbedded;
		}-*/;

  /**
   * Set automaticApprovalEmbedded
   *
   * @param automatic (true=automatic / false=manual)
   */
  public final native void setAutomaticApprovalEmbedded(boolean automatic) /*-{
			this.automaticApprovalEmbedded = automatic;
		}-*/;

  /**
   * Returns Perun specific type of object
   *
   * @return type of object
   */
  public final native String getObjectType() /*-{
			if (!this.beanName) {
			return "JavaScriptObject"
			}
			return this.beanName;
		}-*/;

  /**
   * Sets Perun specific type of object
   *
   * @param type type of object
   */
  public final native void setObjectType(String type) /*-{
			this.beanName = type;
		}-*/;

  /**
   * Returns the status of this item in Perun system as String
   * VALID, INVALID, EXPIRED, DISABLED
   *
   * @return string which defines item status
   */
  public final native String getStatus() /*-{
			return this.status;
		}-*/;

  /**
   * Compares to another object
   *
   * @param o Object to compare
   * @return true, if they are the same
   */
  public final boolean equals(ApplicationForm o) {
    return o.getId() == this.getId();
  }
}
