package cz.metacentrum.perun.webgui.json.comparators;

import cz.metacentrum.perun.webgui.client.resources.Collator;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.User;

import java.util.Comparator;

/**
 * Special comparator for object RichMember
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class RichUserComparator implements Comparator<User>{

	static public enum Column {
		USER_ID, EMAIL, ORGANIZATION;
	}

	private Column attr;

	/**
	 * Creates a new Comparator with specified attribute to sort by
	 * @param attr
	 */
	public RichUserComparator(Column attr){
		this.attr = attr;
	}


	/**
	 * Compares the two objects
	 *
	 * @param o1 First object
	 * @param o2 Second object
	 */
	public int compare(User o1, User o2) {
		switch(this.attr)
		{
			case USER_ID:
				return this.compareByUserId(o1, o2);
			case EMAIL:
				return this.compareByEmail(o1, o2);
			case ORGANIZATION:
				return this.compareByOrganization(o1, o2);
		}

		return 0;
	}

	/**
	 * Compares RichMembers by User ID.
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByUserId(User o1, User o2)
	{
		return o1.getId() - o2.getId();
	}

	/**
	 * Compares RichMembers by emails
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByEmail(User o1, User o2)
	{
		Attribute at1 = o1.getAttribute("urn:perun:user:attribute-def:def:preferredMail");
		Attribute at2 = o2.getAttribute("urn:perun:user:attribute-def:def:preferredMail");

		String at1value = "";
		String at2value = "";

		if (at1 != null && at1.getValue() != null && !"null".equalsIgnoreCase(at1.getValue())) {
			at1value = at1.getValue();
		}
		if (at2 != null && at2.getValue() != null && !"null".equalsIgnoreCase(at2.getValue())) {
			at2value = at2.getValue();
		}

		return Collator.getInstance().compare(at1value, at2value);
	}

	/**
	 * Compares RichMembers by organizations
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareByOrganization(User o1, User o2)
	{
		Attribute at1 = o1.getAttribute("urn:perun:user:attribute-def:def:organization");
		Attribute at2 = o2.getAttribute("urn:perun:user:attribute-def:def:organization");

		String at1value = "";
		String at2value = "";

		if (at1 != null && at1.getValue() != null && !"null".equalsIgnoreCase(at1.getValue())) {
			at1value = at1.getValue();
		}
		if (at2 != null && at2.getValue() != null && !"null".equalsIgnoreCase(at2.getValue())) {
			at2value = at2.getValue();
		}

		return Collator.getInstance().compare(at1value, at2value);
	}

}
