package cz.metacentrum.perun.webgui.json.comparators;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.model.GeneralObject;

import java.util.Comparator;

/**
 * Comparator for any Perun object - it makes a GeneralObject from them.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GeneralComparator<T extends JavaScriptObject> implements Comparator<T>{

	static public enum Column {
		ID, NAME, STATUS, DESCRIPTION;
	}

	private Column attr;

	/**
	 * Creates a new Comparator with specified attribute to sort by
	 * @param attr
	 */
	public GeneralComparator(Column attr){
		this.attr = attr;
	}


	/**
	 * Compares the two objects
	 *
	 * @param obj1 First object
	 * @param obj2 Second object
	 */
	public int compare(T obj1, T obj2) {

		GeneralObject o1 = obj1.cast();
		GeneralObject o2 = obj2.cast();

		switch(this.attr)
		{
			case ID:
				return this.compareById(o1, o2);
			case NAME:
				return this.compareByName(o1, o2);
			case DESCRIPTION:
				return this.compareByDescription(o1, o2);
			case STATUS:
				return this.compareByStatus(o1, o2);
		}
		return 0;
	}

	/**
	 * Compares GeneralObjects by ID.
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareById(GeneralObject o1, GeneralObject o2)
	{
		return o1.getId() - o2.getId();
	}


	/**
	 * Compares GeneralObjects by the name
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByName(GeneralObject o1, GeneralObject o2)
	{
		return o1.getName().compareToIgnoreCase(o2.getName());
	}

	/**
	 * Compares GeneralObjects by the description
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByDescription(GeneralObject o1, GeneralObject o2)
	{
		return o1.getDescription().compareToIgnoreCase(o2.getDescription());
	}

	/**
	 * Compares GeneralObjects by the status
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByStatus(GeneralObject o1, GeneralObject o2)
	{
		return o1.getStatus().compareToIgnoreCase(o2.getStatus());
	}
}
