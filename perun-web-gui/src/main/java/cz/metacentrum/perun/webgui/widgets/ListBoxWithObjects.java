package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.user.client.ui.ListBox;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;

import java.util.ArrayList;

/**
 * An extension to the ListBox, which allows to store real objects in it
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class ListBoxWithObjects<T> extends ListBox {

	// the list where all data are stored
	private ArrayList<T> objects = new ArrayList<T>();

	// recognition of special options (not in list by default)
	private boolean notSelected = false;
	private boolean all = false;

	/**
	 * Returns the name of the value for each type of object
	 * 
	 * @param value object to be stored in list
	 * @return string value that represents name of object
	 */
	private native final String getValueName(T value) /*-{

	// this is a GWT hack - not quite universal, but working

	if (!value.beanName) {

	// OLD WAY
	
	// execService

	if (!value.name) {
		if (!value.user) {
			if (!value.lastName) {
				if (!value.description) {
					if (value.execServiceType) {
						return value.service.name + " " + value.execServiceType;
					}
				} else {
					return "(" + value.id + ") " + value.description;
				}
			}
	 	}	
	}

	// facility ?
	if (value.name){
		if (!value.user) { 
			if (!value.lastName) { 
				if (value.type) {
					return value.name + " (" + value.type + ")";
				}
			}
		}
	}

	// User
	if (!value.name){
		if (!value.user) { return value.lastName + " " +value.firstName; }
	}

	// RichMember
	if (!value.name) { return value.user.lastName + " " +value.user.firstName; }

	// everything else

	return value.name;

	} else {
	
	// NEW WAY
	  
	 if (value.beanName == "RichMember") {
	 	return value.user.lastName + " " +value.user.firstName;
	 } else if (value.beanName == "User") {
	 	return value.lastName + " " +value.firstName;
	 } else if (value.beanName == "RichUser") {
	 	return value.lastName + " " +value.firstName;
	 } else if (value.beanName == "Author") {
	 	return value.lastName + " " +value.firstName;
	 } else if (value.beanName == "Facility") {
	 	return value.name + " (" + value.type + ")";
	 } else if (value.beanName == "ExecService") {
	 	return value.service.name + " " + value.execServiceType;
	 } else if (value.beanName == "AttributeDefinition") {
	 	return value.displayName;
	 } else if (value.beanName == "Sld") {
	 	return "(" + value.id + ") " + value.name
	 } else if (value.beanName == "Publication") {
	 	return value.title;
	 } else if (value.beanName == "Host") {
         return value.hostname;
     } else {
	 	return value.name;
	 }
	
	}

	}-*/;

	/**
	 * Adds an item to the ListBox (at the end of the list)
	 * 
	 * @param value object to be stored
	 */
	public void addItem(T value){
		this.objects.add(value);
		this.addItem(this.getValueName(value));		
	}

	/**
	 * Clears the ListBox
	 */
	public void clear()
	{
		this.objects.clear();
		super.clear();
	}

	/**
	 * Returns object at an index
	 *
	 * @param index Index of the element in list
	 * @return object at same position in listbox
	 */	
	public T getObjectAt(int index)
	{
		return this.objects.get(index);
	}

	/**
	 * Inserts new item into the ListBox 
	 * 
	 * @param value object to be stored
	 * @param index desired index between others objects
	 */
	public void insertItem(T value, int index)
	{
		super.insertItem(this.getValueName(value), index);
		this.objects.add(index, value);
	}

	/**
	 * Remove the item at the index 
	 * 
	 * @param index index of item to be removed
	 */
	public void removeItem(int index)
	{
		super.removeItem(index);
		this.objects.remove(index);
	}

	/**
	 * Sets the value at the index
	 * 
	 * @param index position in listbox
	 * @param value new object name to be stored at same index
	 */
	public void setValue(int index, T value)
	{
		super.setValue(index,this.getValueName(value));
		this.objects.set(index, value);

	}

	/**
	 * Return true if list of objects in listBox is empty
	 * 
	 * @return boolean true - empty / false - not empty
	 */
	public boolean isEmpty() {
		return this.objects.isEmpty();
	}

	/**
	 * Adds "Not selected" option into list (always as first option)
	 */
	public void addNotSelectedOption() {
		// if not already in list
		if (notSelected == false) {
			super.insertItem(WidgetTranslation.INSTANCE.listboxNotSelected(), 0);
			notSelected = true;
		}
	}

	/**
	 * Adds "All" option into list (as second option if "not selected" is present, first otherwise)
	 * this option is allowed to be added only if there are any objects in list
	 */
	public void addAllOption() {
		// if not already in list
		if (all == false && !(isEmpty())) {
			if (notSelected == true) { super.insertItem(WidgetTranslation.INSTANCE.listboxAll(), 1); }
			else { super.insertItem(WidgetTranslation.INSTANCE.listboxAll(), 0); }
			all = true;
		}
	}

	/**
	 * Removes "Not selected" option from list (if present)
	 */
	public void removeNotSelectedOption() {
		// if present in list
		if (notSelected == true) {
			super.removeItem(0);
			notSelected = false;
		}
	}

	/**
	 * Removes "All" option from list (if present)
	 */
	public void removeAllOption() {
		// if present in list
		if (all == true) {
			if (notSelected == true) { super.removeItem(1); }
			else { super.removeItem(0); }
			all = false;
		}
	}

	/**
	 * Returns all objects in the listbox (usefull for "all" option)
	 * 
	 * @return ArrayList<T> list of object in the listbox
	 */
	public ArrayList<T> getAllObjects() {
		return this.objects;
	}

	/**
	 * Returns object at selected index. If "Not selected" or "All" options are
	 * present in listBox, corrections for index are made so right object is always returned.
	 * 
	 * If selected index corresponds with "Not selected" null is returned. 
	 * If "All" option is selected, object at zero position is returned (all objects can be 
	 * retrieved by getAllObjects()).
	 * 
	 * @return object at currently selected index
	 * 
	 */		
	public T getSelectedObject()
	{
		int index = this.getSelectedIndex();
		// lower index for special options in list
		if (notSelected == true) { index--; }
		if (all == true) { index--; }
		// return first available object if index is lower than 0
		if (index < 1 && all == true) { return this.objects.get(0); }
		if (index < 0 && notSelected == true) { return null; }
		return this.objects.get(index);
	}

	/**
	 * Removes item on selected index. If "Not selected" or "All" option is selected, no item
	 * is removed from listbox.
	 */
	public void removeSelectedItem()
	{
		int index = this.getSelectedIndex();
		// filter indexes for "not selected" and "all" options
		if (index == 0 && (all == true || notSelected == true)) { return; }
		if (index <= 1 && (all == true && notSelected == true)) { return; }
		super.removeItem(index);
		// lower index for each spec. option since correct object is selected in menu
		if (all == true) { index--; }
		if (notSelected == true) { index--; }
		this.objects.remove(index);
	}
	
	/**
	 * Sets selected object
	 * @param object
	 * @param selected
	 */
	public void setSelected(T object, boolean selected){
		if(!this.objects.contains(object)){
			return;
		}

		for(int i = 0 ; i < this.objects.size(); i++)
		{
			if(this.objects.get(i).equals(object)){
                int index = i;
                if (notSelected == true) { index++; }
                if (all == true) { index++; }
                this.setItemSelected(index, selected);
            }else{
                int index = i;
                if (notSelected == true) { index++; }
                if (all == true) { index++; }
                this.setItemSelected(index, !selected);

            }
		}
	}

	/**
	 * Adds all items to this listBox
	 * 
	 * @param objects
	 */
	public void addAllItems(Iterable<T> objects) {
		for(T obj : objects)
		{
			this.addItem(obj);
		}
	}

    /**
     * Tries to select an item in the listbox.
     *
     * @param listbox The listbox object
     * @param key The key
     * @return True if item selected.
     */
    static public boolean listBoxTrySelectItem(ListBox listbox, String key)
    {
        for(int i = 0; i< listbox.getItemCount(); i++)
        {
            String k = listbox.getValue(i);
            if(k.equals(key)){
                // select it
                listbox.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

}