/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.voot.comparators.vootgroupcomparator;

import cz.metacentrum.perun.voot.VOOTGroup;
import java.util.Comparator;

/**
 * Comparator for groups, which compare descending by description.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 * @version $Id: $
 */
public class VOOTGroupDescriptionDescComparator implements Comparator<VOOTGroup>{

	@Override
		public int compare(VOOTGroup vootGroup1, VOOTGroup vootGroup2) {
			return vootGroup2.getDescription().compareTo(vootGroup1.getDescription());
		}
}
