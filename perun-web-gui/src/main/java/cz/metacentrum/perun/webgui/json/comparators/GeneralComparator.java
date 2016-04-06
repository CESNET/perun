package cz.metacentrum.perun.webgui.json.comparators;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.resources.Collator;
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
		String name = (o1.getName() != null) ? o1.getName() : "";
		String name2 = (o2.getName() != null) ? o2.getName() : "";
		return Collator.getInstance().compareIgnoreCase(name, name2);
	}

	/**
	 * Compares GeneralObjects by the description
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByDescription(GeneralObject o1, GeneralObject o2)
	{
		String desc = (o1.getDescription() != null) ? o1.getDescription() : "";
		String desc2 = (o2.getDescription() != null) ? o2.getDescription() : "";
		return Collator.getInstance().compareIgnoreCase(desc, desc2);
	}

	/**
	 * Compares GeneralObjects by the status
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByStatus(GeneralObject o1, GeneralObject o2)
	{
		String stat = (o1.getStatus() != null) ? o1.getStatus() : "";
		String stat2 = (o2.getStatus() != null) ? o2.getStatus() : "";
		return Collator.getInstance().compareIgnoreCase(stat, stat2);
	}
}
