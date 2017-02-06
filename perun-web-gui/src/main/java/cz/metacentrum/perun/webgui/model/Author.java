package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type for Cabinet API: Author
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class Author extends JavaScriptObject {

	protected Author() {}

	/**
	 * Get ID of user
	 * @return id of user
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Get first name of user
	 *
	 * @return first name of user
	 */
	public final native String getFirstName() /*-{
		if (!this.firstName) { return ""; }
		return this.firstName;
	}-*/;

	/**
	 * Get last name of user
	 *
	 * @return last name of user
	 */
	public final native String getLastName() /*-{
		if (!this.lastName) { return ""; }
		return this.lastName;
	}-*/;

	/**
	 * Get full name of user
	 *
	 * @return last+first name of user
	 */
	public final native String getFullName() /*-{
		return this.lastName + " " + this.firstName;
	}-*/;

	/**
	 * Get formatted name of user
	 *
	 * @return LAST+first name of user
	 */
	public final native String getFormattedName() /*-{
		return this.lastName.toUpperCase() + " " + this.firstName;
	}-*/;

	/**
	 * Return count of authors(users) publications
	 * based on count of his unique authorships.
	 *
	 * @return count of publications
	 */
	public final native int getPublicationsCount() /*-{
		if (!this.authorships) { return 0; }
		return this.authorships.length;
	}-*/;

	/**
	 * Get full name with titles of author
	 *
	 * @return full name with titles of author
	 */
	public final native String getDisplayName() /*-{
		var fullName = "";
		if(this.titleBefore != null){
			fullName += this.titleBefore + " ";
		}
		if(this.firstName != null){
			fullName += this.firstName + " ";
		}
		if(this.middleName != null){
			fullName += this.middleName + " ";
		}
		if(this.lastName != null){
			fullName += this.lastName;
		}
		if(this.titleAfter != null){
			fullName += ", " + this.titleAfter;
		}
		return fullName;
	}-*/;

	/**
	 * Get specified user attribute stored in Author
	 *
	 * @param urn URN of attribute to get
	 * @return user attribute or null if not present
	 */
	public final native Attribute getAttribute(String urn) /*-{
		for(var i in this.attributes){
			if(this.attributes[i].namespace + ":" + this.attributes[i].friendlyName == urn){
				return this.attributes[i];
			}
		}
		return null;
	}-*/;


	/**
	 * This property is filled only when author was found by publication ID
	 * Is used to determine "author" of the record in DB, who made him author of publication.
	 *
	 * @return authorship
	 */
	public final native JsArray<Authorship> getAuthorships() /*-{
		return this.authorships;
	}-*/;

	/**
	 * Return authorship for specific publication of author
	 *
	 * @param publicationId
	 * @return authorship
	 */
	public final Authorship getAuthorship(int publicationId) {

		JsArray<Authorship> authorships = this.getAuthorships();
		for (int i=0; i<authorships.length(); i++) {
			// return authorship if match
			if (authorships.get(i).getPublicationId() == publicationId) {
				return authorships.get(i);
			}
		}
		// null if not found
		return null;
	}

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
	public final boolean equals(Author o)
	{
		return o.getId() == this.getId();
	}
}
