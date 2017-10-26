package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type for Cabinet API: Publication
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class Publication extends JavaScriptObject {

	protected Publication() {}

	/**
	 * Returns object ID
	 * @return ID
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final native void setId(int id) /*-{
		this.id = id;
	}-*/;

	/**
	 * Returns external ID
	 * @return ID
	 */
	public final native int getExternalId() /*-{
		return this.externalId;
	}-*/;

	public final native void setExternalId(int id) /*-{
		this.externalId = id;
	}-*/;

	/**
	 * Returns publication system ID
	 * @return ID
	 */
	public final native int getPublicationSystemId() /*-{
		return this.publicationSystemId;
	}-*/;

	public final native void setPublicationSystemId(int id) /*-{
		this.publicationSystemId = id;
	}-*/;

	/**
	 * Returns publication system name
	 * @return publication system name
	 */
	public final native String getPublicationSystemName() /*-{
		return this.pubSystemName;
	}-*/;

	/**
	 * Returns list with authors
	 * @return list
	 */
	public final native JsArray<Author> getAuthors() /*-{
		return this.authors;
	}-*/;

	/**
	 * Returns string with authors formatted by the standard
	 * @return list
	 */
	public final native String getAuthorsFormatted() /*-{
		var authorsString = "";
		if (this.authors == null) { return authorsString; }
		for(var i in this.authors)
		{
			if (this.authors[i] != null) {
				if (this.authors[i].lastName != null) {
					authorsString += this.authors[i].lastName.toUpperCase()
				}
				if (this.authors[i].firstName != null) {
					authorsString += " " +  this.authors[i].firstName + ", ";
				} else {
					authorsString += ", ";
				}
			} else {
				continue;
			}
			// crop long list of authors to three...
			if (i == 2) {
				if (authorsString.length >= 2) {
					authorsString = authorsString.substring(0, authorsString.length-2);
				}
				if (this.authors.length > 3) {
					authorsString += " et al."
				}
				return authorsString;
			}
		}
		if (authorsString.length >= 2) {
			authorsString = authorsString.substring(0, authorsString.length-2);
		}
		return authorsString;
	}-*/;


	/**
	 * Returns the title
	 * @return title
	 */
	public final native String getTitle() /*-{
		if (this.title == null) {
			return "";
		}
		return this.title;
	}-*/;

	/**
	 * Set publication title
	 * @param title
	 */
	public final native void setTitle(String title) /*-{
		this.title = title;
	}-*/;

	/**
	 * Returns the year
	 * @return year
	 */
	public final native int getYear() /*-{
		return this.year;
	}-*/;

	/**
	 * Set year of publishing
	 * @param year
	 */
	public final native void setYear(int year) /*-{
		this.year = year;
	}-*/;

	/**
	 * Returns the main
	 * @return main
	 */
	public final native String getMain() /*-{
		return this.main;
	}-*/;

	/**
	 * Set main entry (cite)
	 * @param main
	 */
	public final native void setMain(String main) /*-{
		this.main = main;
	}-*/;

	/**
	 * Returns the ISBN
	 * @return ISBN
	 */
	public final native String getIsbn() /*-{
		return this.isbn;
	}-*/;

	/**
	 * Set ISBN
	 * @param isbn
	 */
	public final native void setIsbn(String isbn) /*-{
		this.isbn = isbn;
	}-*/;

	/**
	 * Returns the categoryId
	 * @return categoryId
	 */
	public final native int getCategoryId() /*-{
		if (!this.categoryId) { return 0; }
		return this.categoryId;
	}-*/;

	/**
	 * Returns category name
	 * @return category name
	 */
	public final native String getCategoryName() /*-{
		return this.categoryName;
	}-*/;

	/**
	 * Sets category name
	 * @param catName name
	 */
	public final native String setCategoryName(String catName) /*-{
		this.categoryName = catName;
	}-*/;

	/**
	 * Sets category ID
	 * @param categoryId
	 */
	public final native void setCategoryId(int categoryId) /*-{
		this.categoryId = categoryId;
	}-*/;

	/**
	 * Returns thanks
	 * @return thanks
	 */
	public final native JsArray<Thanks> getThanks() /*-{
		if (!this.thanks) { return ""; }
		return this.thanks;
	}-*/;

	/**
	 * Sets thanks to publication
	 * @param thks
	 */
	public final native void setThanks(JsArray<Thanks> thks) /*-{
		this.thanks = thks;
	}-*/;

	/**
	 * Returns who created the publication
	 * @return actor
	 */
	public final native String getCreatedBy() /*-{
		return this.createdBy;
	}-*/;

	/**
	 * Returns who created the publication
	 * @return ID of user, who created publication
	 */
	public final native int getCreatedByUid() /*-{
		if (!this.createdByUid) return 0;
		return this.createdByUid;
	}-*/;

	public final native void setCreatedByUid(int uid) /*-{
		this.createdByUid = uid;
	}-*/;

	/**
	 * Sets user who created publication
	 * @param actor
	 */
	public final native void setCreatedBy(String actor) /*-{
		this.createdBy = actor;
	}-*/;

	/**
	 * Returns the date of publication creation
	 * @return date of publication creation
	 */
	public final native double getCreatedDate() /*-{
		return this.createdDate;
	}-*/;

	/**
	 * Sets date when publication was created
	 * @param date
	 */
	public final native void setCreatedDate(double date) /*-{
		this.createdDate = date;
	}-*/;


	/**
	 * Returns rank of publication
	 * @return rank of publication
	 */
	public final native double getRank() /*-{
		return this.rank;
	}-*/;

	/**
	 * Sets rank for publication (default should be 0)
	 * @param rank
	 */
	public final native void setRank(double rank) /*-{
		this.rank = rank;
	}-*/;

	/**
	 * Returns DOI
	 * @return doi
	 */
	public final native String getDoi() /*-{
		if (!this.doi) { return ""; }
		return this.doi;
	}-*/;

	/**
	 * Set DOI
	 * @param newDoi
	 */
	public final native void setDoi(String newDoi) /*-{
		return this.doi = newDoi;
	}-*/;

	/**
	 * If publication is locked
	 * @return true if pub locked / false otherwise
	 */
	public final native boolean getLocked() /*-{
		if (!this.locked) { return false; }
		return this.locked;
	}-*/;

	/**
	 * Set publication locked
	 * @param locked true = locked / false = unlocked
	 */
	public final native void setLocked(boolean locked) /*-{
		this.locked = locked;
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
	public final boolean equals(Publication o)
	{
		return o.getId() == this.getId();
	}

}
