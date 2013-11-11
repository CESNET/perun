package cz.metacentrum.perun.webgui.json.comparators;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.resources.Collator;
import cz.metacentrum.perun.webgui.model.Attribute;

import java.util.Comparator;

/**
 * Comparator for Attribute and AttributeDefinition object
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class AttributeComparator<T extends JavaScriptObject> implements Comparator<T>{

	static public enum Column {
		TRANSLATED_NAME, TRANSLATED_DESCRIPTION
	}
	
	private Column attr;
	
	/**
	 * Creates a new Comparator with specified attribute to sort by
	 * @param attr
	 */
	public AttributeComparator(Column attr){
		this.attr = attr;
	}
	
	
	/**
	 * Compares the two objects
	 * 
	 * @param obj1 First object
	 * @param obj2 Second object
	 */
	public int compare(T obj1, T obj2) {
		
		Attribute o1 = obj1.cast();
		Attribute o2 = obj2.cast();
		
		switch(this.attr)
		{
			case TRANSLATED_NAME:
				return this.compareByName(o1, o2);
			case TRANSLATED_DESCRIPTION:
				return this.compareByDescription(o1, o2);
		}
		return 0;
	}
	
	/**
	 * Compares Attributes by the name
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByName(Attribute o1, Attribute o2) {
		Collator customCollator = Collator.getInstance();
		return customCollator.compare(o1.getDisplayName(),o2.getDisplayName());
	}
	
	/**
	 * Compares Attributes by the description
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByDescription(Attribute o1, Attribute o2) {
		Collator customCollator = Collator.getInstance();
		return customCollator.compare(o1.getDescription(),o2.getDescription());
	}
	
}