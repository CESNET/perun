package cz.metacentrum.perun.ldapc.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.Name;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunEntry;
import cz.metacentrum.perun.ldapc.model.impl.AbstractPerunEntry;
import cz.metacentrum.perun.ldapc.service.LdapcManager;

public abstract class AbstractSynchronizer {

	@Autowired
	protected LdapcManager ldapcManager;

	protected List<String> fillPerunAttributeNames(List<String> attrNames) throws InternalErrorException  {
		PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
		List<String> result = new ArrayList<String>();
		for(String name : attrNames) {
			if(name.endsWith(":")) {
				result.addAll(perun.getAttributesManagerBl().getAllSimilarAttributeNames(ldapcManager.getPerunSession(), name));
			} else {
				result.add(name);
			}
		}
		return result;
	}

	protected void removeOldEntries(PerunEntry<?> perunEntry, Set<Name> presentEntries, Logger log) throws InternalErrorException {
		List<Name> ldapEntries = perunEntry.listEntries();
		log.debug("Checking for old entries: {} present, {} active", ldapEntries.size(), presentEntries.size());
		for (Name name : ldapEntries) {
			if(!presentEntries.contains(name)) {
				log.debug("Removing entry {} which is not present anymore", name);
				perunEntry.deleteEntry(name);
			}
		}
	}
}
