package cz.metacentrum.perun.webgui.json.comparators;

import com.google.gwt.core.client.JsArray;
import cz.metacentrum.perun.webgui.model.Publication;
import cz.metacentrum.perun.webgui.model.Thanks;

import java.util.Comparator;

/**
 * Special comparator for object RichMember
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class PublicationComparator implements Comparator<Publication>{

	static public enum Column {
		EXTERNAL_ID, PUBLICATION_SYSTEM_ID, AUTHORS, TITLE, YEAR, MAIN, ISBN, CREATED_DATE, CATEGORY, THANKS;
	}

	private Column attr;

	/**
	 * Creates a new Comparator with specified attribute to sort by
	 * @param attr
	 */
	public PublicationComparator(Column attr){
		this.attr = attr;
	}


	/**
	 * Compares the two objects
	 *
	 * @param o1 First object
	 * @param o2 Second object
	 */
	public int compare(Publication o1, Publication o2) {
		switch(this.attr)
		{
			case EXTERNAL_ID:
				return this.compareByExternalId(o1, o2);
			case PUBLICATION_SYSTEM_ID:
				return this.compareByPublicationSystemId(o1, o2);
			case AUTHORS:
				return this.compareByAuthors(o1, o2);
			case TITLE:
				return this.compareByTitle(o1, o2);
			case YEAR:
				return this.compareByYear(o1, o2);
			case MAIN:
				return this.compareByMain(o1, o2);
			case ISBN:
				return this.compareByIsbn(o1, o2);
			case CREATED_DATE:
				return this.compareByCreatedDate(o1, o2);
			case CATEGORY:
				return this.compareByCategory(o1, o2);
			case THANKS:
				return this.compareByThanks(o1, o2);

		}

		return 0;
	}


	private int compareByCreatedDate(Publication o1, Publication o2) {
		return (int)o1.getCreatedDate() - (int)o2.getCreatedDate();
	}


	private int compareByIsbn(Publication o1, Publication o2) {
		return o1.getIsbn().compareToIgnoreCase(o2.getIsbn());
	}


	private int compareByMain(Publication o1, Publication o2) {
		return o1.getMain().compareToIgnoreCase(o2.getMain());
	}


	private int compareByYear(Publication o1, Publication o2) {
		return o1.getYear() - o2.getYear();
	}


	private int compareByTitle(Publication o1, Publication o2) {
		return o1.getTitle().compareToIgnoreCase(o2.getTitle());
	}


	private int compareByAuthors(Publication o1, Publication o2) {
		return o1.getAuthorsFormatted().compareToIgnoreCase(o2.getAuthorsFormatted());
	}


	private int compareByPublicationSystemId(Publication o1, Publication o2) {
		return o1.getPublicationSystemId() - o2.getPublicationSystemId();
	}


	private int compareByExternalId(Publication o1, Publication o2) {
		return o1.getExternalId() - o2.getExternalId();
	}

	private int compareByCategory(Publication o1, Publication o2) {
		return o1.getCategoryName().compareToIgnoreCase(o2.getCategoryName());
	}

	private int compareByThanks(Publication o1, Publication o2) {

		String result1 = "";
		String result2 = "";
		JsArray<Thanks> thks1 = o1.getThanks();
		JsArray<Thanks> thks2 = o2.getThanks();
		for (int i=0; i<thks1.length(); i++) {
			result1 += thks1.get(i).getOwnerName();
		}
		for (int i=0; i<thks2.length(); i++) {
			result2 += thks2.get(i).getOwnerName();
		}
		return result1.compareToIgnoreCase(result2);

	}

}
