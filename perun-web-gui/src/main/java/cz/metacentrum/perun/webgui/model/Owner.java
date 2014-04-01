package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.localization.ObjectTranslation;

/**
 * Overlay type for Owner object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class Owner extends JavaScriptObject {

	protected Owner() {}

	/**
	 * Get ID of owner
	 *
	 * @return ID of owner
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

		/**
		 * Get name of owner
		 *
		 * @return name of owner
		 */
		public final native String getName() /*-{
			return this.name;
		}-*/;

		/**
		 * Get contact info of owner
		 *
		 * @return contact of owner
		 */
		public final native String getContact() /*-{
			return this.contact;
		}-*/;

		/**
		 * Get type of contact
		 *
		 * @return type of contact (ADMINISTRATIVE,TECHNICAL)
		 */
		public final native String getType() /*-{
			return this.type;
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
		public final boolean equals(Owner o)
		{
			return o.getId() == this.getId();
		}

	/**
	 * Enumeration of all possible types of owners
	 */
	public enum OwnerType{
		ADMINISTRATIVE,
			TECHNICAL
	}

	/**
	 * Provide translation for all possible owners types
	 *
	 * @param type type to translate
	 * @return translated type
	 */
	public static final String getTranslatedType(String type) {

		if ("technical".equalsIgnoreCase(type)){
			return ObjectTranslation.INSTANCE.ownerTypeTechnical();
		} else if ("administrative".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.ownerTypeAdministrative();
		} else {
			return type;
		}

	};

}
