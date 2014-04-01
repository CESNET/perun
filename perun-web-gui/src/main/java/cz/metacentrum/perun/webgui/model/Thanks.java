package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for Cabinet API: Thanks
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class Thanks extends JavaScriptObject {

	protected Thanks() {}

	/**
	 * Returns object ID
	 * @return ID
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

		/**
		 * Returns authorship ID
		 * @return ID
		 */
		public final native int getPublicationId() /*-{
			return this.publicationId;
		}-*/;

		/**
		 * Returns owner ID
		 * @return ID
		 */
		public final native int getOwnerId() /*-{
			return this.ownerId;
		}-*/;

		/**
		 * Returns owner
		 * @return owner
		 */
		public final native String getOwnerName() /*-{
			return this.ownerName;
		}-*/;

		/**
		 * Returns CreatedBy
		 * @return actor
		 */
		public final native String getCreatedBy() /*-{
			return this.createdBy;
		}-*/;

		/**
		 * Returns who created the thanks
		 * @return ID of user, who created thanks
		 */
		public final native int getCreatedByUid() /*-{
			if (!this.createdByUid) return 0;
			return this.createdByUid;
		}-*/;

		public final native void setCreatedByUid(int uid) /*-{
			this.createdByUid = uid;
		}-*/;

		/**
		 * Returns created date
		 * @return DATE
		 */
		public final native double getCreatedDate() /*-{
			return this.createdDate;
		}-*/;

		/**
		 * Returns Perun specific type of object
		 *
		 * @return type of object
		 */
		public final native String getObjectType() /*-{
			if (!this.objecttype) {
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
		public final boolean equals(Thanks o)
		{
			return o.getId() == this.getId();
		}

}
