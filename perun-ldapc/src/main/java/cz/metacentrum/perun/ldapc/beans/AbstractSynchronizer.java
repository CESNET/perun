package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunEntry;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractSynchronizer {

	@Autowired
	protected LdapcManager ldapcManager;

	protected List<String> fillPerunAttributeNames(List<String> attrNames) {
		PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
		List<String> result = new ArrayList<String>();
		for (String name : attrNames) {
			if (name.endsWith(":")) {
				result.addAll(perun.getAttributesManagerBl().getAllSimilarAttributeNames(ldapcManager.getPerunSession(), name));
			} else {
				result.add(name);
			}
		}
		return result;
	}

	protected void removeOldEntries(PerunEntry<?> perunEntry, Set<Name> presentEntries, Logger log) {
		List<Name> ldapEntries = perunEntry.listEntries();
		log.debug("Checking for old entries: {} present, {} active", ldapEntries.size(), presentEntries.size());
		for (Name name : ldapEntries) {
			if (!presentEntries.contains(name)) {
				log.debug("Removing entry {} which is not present anymore", name);
				perunEntry.deleteEntry(name);
			}
		}
	}
}
