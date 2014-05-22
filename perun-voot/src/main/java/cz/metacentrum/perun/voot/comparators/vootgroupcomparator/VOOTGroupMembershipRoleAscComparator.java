package cz.metacentrum.perun.voot.comparators.vootgroupcomparator;

import cz.metacentrum.perun.voot.VOOTGroup;
import java.util.Comparator;

/**
 * Comparator for groups, which compare ascending by membership role.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTGroupMembershipRoleAscComparator implements Comparator<VOOTGroup>{

	@Override
	public int compare(VOOTGroup vootGroup1, VOOTGroup vootGroup2) {
		return vootGroup1.getVoot_membership_role().compareTo(vootGroup2.getVoot_membership_role());
	}
}
