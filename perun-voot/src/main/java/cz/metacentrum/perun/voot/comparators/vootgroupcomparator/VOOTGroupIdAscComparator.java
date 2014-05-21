/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.voot.comparators.vootgroupcomparator;

import cz.metacentrum.perun.voot.VOOTGroup;
import java.util.Comparator;

/**
 * Comparator for groups, which compare ascending by id.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 * @version $Id: $
 */
public class VOOTGroupIdAscComparator implements Comparator<VOOTGroup>{

	@Override
		public int compare(VOOTGroup vootGroup1, VOOTGroup vootGroup2) {
			return vootGroup1.getId().compareTo(vootGroup2.getId());
		}
}
