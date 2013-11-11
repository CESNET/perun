package cz.metacentrum.perun.webgui.client.resources;

import cz.metacentrum.perun.webgui.json.comparators.AttributeComparator;
import cz.metacentrum.perun.webgui.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** 
 * Class used for sorting list of different types of objects in their tables
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class TableSorter<T> {

	/**
	 * Returns sorted list of objects
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their Ids
	 */
	public ArrayList<T> sortById(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				return getId(o1)-(getId(o2));
			}
		});
		return list;

	}

	/**
	 * Returns sorted list of objects
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their Names
	 * or FullNames (for Member/User/RichMember)
	 */
	public ArrayList<T> sortByName(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				Collator customCollator = Collator.getInstance();
				return customCollator.compare(getName(o1), getName(o2));
			}
		});
		return list;

	}

	/**
	 * Returns sorted list of objects
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their FriendlyName (attributes)
	 */
	public ArrayList<T> sortByFriendlyName(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				Collator customCollator = Collator.getInstance();
				return customCollator.compare(getFriendlyName(o1), getFriendlyName(o2));
			}
		});
		return list;

	}

	/**
	 * Returns sorted list of attributes
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<Attribute> sorted list of attrs by translated names
	 */
	public ArrayList<Attribute> sortByAttrNameTranslation(ArrayList<Attribute> list){
		if(list == null) return null;
		Collections.sort(list, new AttributeComparator<Attribute>(AttributeComparator.Column.TRANSLATED_NAME));
		return list;

	}

    /**
     * Returns sorted list of attribute definitions
     *
     * @param list of objects to be sorted
     * @return ArrayList<AttributeDefinition> sorted list of attrs by translated names
     */
    public ArrayList<AttributeDefinition> sortByAttrDefNameTranslation(ArrayList<AttributeDefinition> list){
        if(list == null) return null;
        Collections.sort(list, new AttributeComparator<AttributeDefinition>(AttributeComparator.Column.TRANSLATED_NAME));
        return list;

    }
	
	/**
	 * Returns sorted list of objects
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their shortName (Vos)
	 */
	public ArrayList<T> sortByShortName(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				Collator customCollator = Collator.getInstance();
				return customCollator.compare(getShortName(o1), getShortName(o2));
			}
		});
		return list;

	}
	
	/**
	 * Returns sorted list of objects
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their description (Resource)
	 */
	public ArrayList<T> sortByDescription(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				Collator customCollator = Collator.getInstance();
				return customCollator.compare(getDescription(o1), getDescription(o2));
			}
		});
		return list;

	}
	
	/**
	 * Returns sorted list of objects
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their hostname (Hosts)
	 */
	public ArrayList<T> sortByHostname(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				Collator customCollator = Collator.getInstance();
				return customCollator.compare(getHostname(o1), getHostname(o2));
			}
		});
		return list;

	}
	
	/**
	 * Returns sorted list of objects
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their service (for tasks)
	 */
	public ArrayList<T> sortByService(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				Collator customCollator = Collator.getInstance();
				return customCollator.compare(getService(o1), getService(o2));
			}
		});
		return list;

	}
	
	/**
	 * Returns sorted list of objects - FOR FACILITY STATE ONLY !!
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their facility name
	 */
	public ArrayList<T> sortByFacilityName(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				FacilityState o3 = (FacilityState)o1;
				FacilityState o4 = (FacilityState)o2;
				Collator customCollator = Collator.getInstance();
				return customCollator.compare(o3.getFacility().getName(), o4.getFacility().getName());
			}
		});
		return list;

	}	
	
	/**
	 * Returns sorted list of objects - FOR RICH TASK RESULTS ONLY !!
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their destination name
	 */
	public ArrayList<T> sortByRichTaskResultDestination(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				TaskResult o3 = (TaskResult)o1;
				TaskResult o4 = (TaskResult)o2;
				Collator customCollator = Collator.getInstance();
				return customCollator.compare(o3.getDestination().getDestination(), o4.getDestination().getDestination());
			}
		});
		return list;

	}

    /**
     * Returns sorted list of objects - FOR RICH TASK RESULTS ONLY !!
     *
     * @param list of objects to be sorted
     * @return ArrayList<T> sorted list of objects by their destination name
     */
    public ArrayList<T> sortByRichTaskResultService(ArrayList<T> list){
        if(list == null) return null;
        Collections.sort(list, new Comparator<T>(){
            public int compare(T o1, T o2) {
                TaskResult o3 = (TaskResult)o1;
                TaskResult o4 = (TaskResult)o2;
                Collator customCollator = Collator.getInstance();
                return customCollator.compare(o3.getService().getName(), o4.getService().getName());
            }
        });
        return list;

    }
	
	/**
	 * Returns !DESC! sorted list of objects - FOR APPLICATIONS ONLY !!
	 * 
	 * @param list of objects to be sorted
	 * @return ArrayList<T> sorted list of objects by their created date (recent first / older then)
	 */
	public ArrayList<T> sortByDate(ArrayList<T> list){
		if(list == null) return null;
		Collections.sort(list, new Comparator<T>(){
			public int compare(T o1, T o2) {
				Application o3 = (Application)o1;
				Application o4 = (Application)o2;
				return o4.getCreatedAt().compareToIgnoreCase(o3.getCreatedAt());
			}
		});
		return list;

	}

    /**
     * Returns sorted list of objects - FOR PUBLICATIONS ONLY !!
     *
     * @param list of objects to be sorted
     * @return ArrayList<T> sorted list of objects by their title
     */
    public ArrayList<T> sortByPublicationTitle(ArrayList<T> list){
        if(list == null) return null;
        Collections.sort(list, new Comparator<T>(){
            public int compare(T o1, T o2) {
                Publication o3 = (Publication)o1;
                Publication o4 = (Publication)o2;
                return o4.getTitle().compareToIgnoreCase(o3.getTitle());
            }
        });
        return list;

    }

    /**
	 * Returns name of object
	 * 
	 * @param value object
	 * @return name / fullName of object
	 */
	private final native String getName(T value) /*-{	

		// for members etc.
		if (value.name == null) {
		 if (value.firstName == null && value.lastName == null) {
		 	// publications
			if (value.title != null) {
				return value.title;
			}
			
			return value.user.lastName + " " + value.user.firstName;
		 } else {
		    // for candidate
		    return value.lastName + " " + value.firstName;
		 }
		}
		// for others
		return value.name;

	}-*/;

	/**
	 * Returns friendlyName of object - used for attributes
	 * 
	 * @param value object
	 * @return friendlyName of object
	 */
	private final native String getFriendlyName(T value) /*-{	

		if (value.friendlyName == null) {
		    return "";
		}
		return value.friendlyName;

	}-*/;

	/**
	 * Returns hostname of object - used for Hosts
	 * 
	 * @param value object
	 * @return hostname of object
	 */
	private final native String getHostname(T value) /*-{	

		if (value.hostname == null) {
		    return "";
		}
		return value.hostname;

	}-*/;

	/**
	 * Returns Id of object
	 * 
	 * @param value object
	 * @return id of object
	 */
	private final native int getId(T value) /*-{	
		if (value.id == null) { return 0; }
		return value.id;
	}-*/;

	/**
	 * Returns shortName of object
	 * 
	 * @param value object
	 * @return shortName of object
	 */
	private final native String getShortName(T value) /*-{	
		if (value.shortName == null || value.shortName == "") { return ""; }
		return value.shortName;
	}-*/;
	
	/**
	 * Returns description of object
	 * 
	 * @param value object
	 * @return description of object
	 */
	private final native String getDescription(T value) /*-{	
		if (value.description == null || value.description == "") { return ""; }
		return value.description;
	}-*/;
	
	/**
	 * Returns service of object (used for Tasks and RichDestinations compare)
	 * 
	 * @param value object
	 * @return service of object
	 */
	private final String getService(T value) {
		
		if (((GeneralObject)value).getObjectType().equalsIgnoreCase("RichDestination")) {
			Destination dest = (Destination)value;
			return dest.getService().getName();
		} else if (((GeneralObject)value).getObjectType().equalsIgnoreCase("ExecService")) {
            ExecService exec = (ExecService)value;
            return exec.getService().getName();
        } else {
			Task task = (Task)value;
			return task.getExecService().getService().getName() + task.getExecService().getType();	
		}
		
	}
}