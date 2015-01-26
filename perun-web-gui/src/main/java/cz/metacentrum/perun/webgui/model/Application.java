package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.localization.ObjectTranslation;

/**
 * Overlay type for registrar: Application
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class Application extends JavaScriptObject {

	protected Application() {}

	/**
	 * Creates the new application
	 *
	 * @param type
	 * @param fedInfo
	 * @param actor
	 * @param extSourceName
	 * @param extSourceType
	 * @return
	 */
	static public Application construct(VirtualOrganization vo, Group group, String type, String fedInfo, String actor, String extSourceName, String extSourceType, int extSourceLoa)
	{

		Application app = new JSONObject().getJavaScriptObject().cast();

		// reconstruct vo to be safe when loading it by attrs in registrar
		VirtualOrganization newVo = new JSONObject().getJavaScriptObject().cast();
		newVo.setId(vo.getId());
		newVo.setName(vo.getName());
		newVo.setShortName(vo.getShortName());
		app.setVo(newVo);
		app.setGroup(group);
		// set rest
		app.setType(type);
		app.setState("NEW");
		app.setFedInfo(fedInfo);
		app.setCreatedBy(actor);
		app.setExtSourceName(extSourceName);
		app.setExtSourceType(extSourceType);
		app.setExtSourceLoa(extSourceLoa);
		return app;
	}



	/**
	 * Get ID
	 * @return id
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Get vo
	 * @return vo
	 */
	public final native VirtualOrganization getVo() /*-{
		return this.vo;
	}-*/;

	/**
	 * Set vo
	 * @param vo
	 */
	public final native void setVo(VirtualOrganization vo) /*-{
		this.vo = vo;
	}-*/;

	/**
	 * Get Group
	 * @return group
	 */
	public final native Group getGroup() /*-{
		return this.group;
	}-*/;

	/**
	 * Set Group
	 * @param grp group
	 */
	public final native void setGroup(Group grp) /*-{
		this.group = grp;
	}-*/;

	/**
	 * Get type
	 * @return type
	 */
	public final native String getType() /*-{
		return this.type;
	}-*/;

	/**
	 * Set type
	 */
	public final native void setType(String type) /*-{
		this.type = type;
	}-*/;

	/**
	 * Get state
	 * @return state
	 */
	public final native String getState() /*-{
		return this.state;
	}-*/;

	/**
	 * Set state
	 */
	public final native void setState(String state) /*-{
		this.state = state;
	}-*/;

	/**
	 * Get created_at
	 * @return time
	 */
	public final native String getCreatedAt() /*-{
		return this.createdAt;
	}-*/;

	/**
	 * Set time
	 */
	public final native void setCreatedAt(String date) /*-{
		this.createdAt = date;
	}-*/;

	/**
	 * Get created_by
	 * @return time as string
	 */
	public final native String getCreatedBy() /*-{
		return this.createdBy;
	}-*/;

	/**
	 * Set created by
	 */
	public final native void setCreatedBy(String created) /*-{
		this.createdBy = created;
	}-*/;

	/**
	 * Get fedInfo
	 * @return fedInfo
	 */
	public final native String getFedInfo() /*-{
		return this.fedInfo;
	}-*/;

	/**
	 * Set fedInfo
	 */
	public final native void setFedInfo(String fedInfo) /*-{
		this.fedInfo = fedInfo;
	}-*/;

	/**
	 * Get extSourceName
	 * @return extSourceName
	 */
	public final native String getExtSourceName() /*-{
		return this.extSourceName;
	}-*/;

	/**
	 * Set extSourceName
	 */
	public final native void setExtSourceName(String extSourceName) /*-{
		this.extSourceName = extSourceName;
	}-*/;

	/**
	 * Get extSourceType
	 * @return extSourceType
	 */
	public final native String getExtSourceType() /*-{
		return this.extSourceType;
	}-*/;

	/**
	 * Set extSourceType
	 */
	public final native void setExtSourceType(String extSourceType) /*-{
		this.extSourceType = extSourceType;
	}-*/;

	/**
	 * Get user
	 * @return user
	 */
	public final native User getUser() /*-{
		return this.user;
	}-*/;

	/**
	 * Set user
	 * @param user
	 */
	public final native void setUser(User user) /*-{
		this.user = user;
	}-*/;

	/**
	 * Get ext source loa
	 * @return loa
	 */
	public final native int getExtSourceLoa() /*-{
		return this.extSourceLoa;
	}-*/;

	/**
	 * Set ext source loa
	 * @param loa
	 */
	public final native void setExtSourceLoa(int loa) /*-{
		this.extSourceLoa = loa;
	}-*/;

	/**
	 * Get modified_by property
	 * @return modified by (actor)
	 */
	public final native String getModifiedBy() /*-{
		return this.modifiedBy;
	}-*/;


	/**
	 * Get modified_at property
	 * @return when was modified
	 */
	public final native String getModifiedAt() /*-{
		return this.modifiedAt;
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
	 * VALID, INVALID, SUSPENDED, EXPIRED, DISABLED
	 *
	 * @return string which defines item status
	 */
	public final native String getStatus() /*-{
		return this.status;
	}-*/;

	/**
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(Application o)
	{
		return o.getId() == this.getId();
	}

	/**
	 * Define range of application types
	 */
	public enum ApplicationType {
		INITIAL,
		EXTENSION
	}

	/**
	 * Define range of application states
	 */
	public enum ApplicationState {
		NEW,
		VERIFIED,
		APPROVED,
		REJECTED
	}


	/**
	 * Return translated version of app_type or empty string
	 * @return translated app_type
	 */
	public static final String getTranslatedType(String type) {

		if ("INITIAL".equalsIgnoreCase(type)){
			return ObjectTranslation.INSTANCE.applicationTypeInitial();
		} else if ("EXTENSION".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationTypeExtension();
		} else {
			return "";
		}

	};

	/**
	 * Return translated version of current application state or empty string
	 * @return translated state
	 */
	public static final String getTranslatedState(String state) {

		if ("NEW".equalsIgnoreCase(state)) {
			return ObjectTranslation.INSTANCE.applicationStateNew();
		} else if ("VERIFIED".equalsIgnoreCase(state)) {
			return ObjectTranslation.INSTANCE.applicationStateVerified();
		} else if ("APPROVED".equalsIgnoreCase(state)) {
			return ObjectTranslation.INSTANCE.applicationStateApproved();
		} else if ("REJECTED".equalsIgnoreCase(state)) {
			return ObjectTranslation.INSTANCE.applicationStateRejected();
		} else {
			return "";
		}

	};

}
