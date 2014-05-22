package cz.metacentrum.perun.voot.comparators.vootgroupcomparator;

import cz.metacentrum.perun.voot.VOOTGroup;
import java.util.Comparator;

/**
 * Comparator for groups, which compare descending by id.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTGroupIdDescComparator implements Comparator<VOOTGroup>{

	@Override
	public int compare(VOOTGroup vootGroup1, VOOTGroup vootGroup2) {
		return vootGroup2.getId().compareTo(vootGroup1.getId());
	}
}
