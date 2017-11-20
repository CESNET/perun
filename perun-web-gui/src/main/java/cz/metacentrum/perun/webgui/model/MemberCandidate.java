package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for MemberCandidate object.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class MemberCandidate extends JavaScriptObject {

	protected MemberCandidate() {}

	/**
	 * Returns associated RichUser
	 * @return RichUser
	 */
	public final native User getRichUser() /*-{
		return this.richUser;
	}-*/;

	/**
	 * Returns associated Candidate
	 * @return Candidate
	 */
	public final native Candidate getCandidate() /*-{
		return this.candidate;
	}-*/;

	/**
	 * Returns associated Member
	 * @return Member
	 */
	public final native Member getMember() /*-{
		return this.member;
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
		return this.objecttype;
	}-*/;

	/**
	 * Sets Perun specific type of object
	 *
	 * @param type type of object
	 */
	public final native void setObjectType(String type) /*-{
		this.objecttype = type;
	}-*/;

}
