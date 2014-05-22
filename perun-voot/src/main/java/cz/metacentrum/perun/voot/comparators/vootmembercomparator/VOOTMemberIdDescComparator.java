package cz.metacentrum.perun.voot.comparators.vootmembercomparator;

import cz.metacentrum.perun.voot.VOOTMember;
import java.util.Comparator;

/**
 * Comparator for members, which compare descending by id.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTMemberIdDescComparator implements Comparator<VOOTMember>{

	@Override
	public int compare(VOOTMember vootMember1, VOOTMember vootMember2) {
		return vootMember2.getId().compareTo(vootMember1.getId());
	}
}
