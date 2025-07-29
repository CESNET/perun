package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunEntry;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.naming.Name;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSynchronizer {

  @Autowired
  protected LdapcManager ldapcManager;

  /**
   * Retrieves similar attribute names for attribute names ending with `:` (e.g., all namespace attributes for
   * login-namespace), or simply returns the attribute name if it's complete.
   * @param attrNames attribute names to potentially retrieve similar names for
   * @return the names with similar names included
   */
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

  /**
   * Removes old entries from the LDAP directory that are no longer present in the specified set
   * of active entries.
   *
   * @param perunEntry the PerunEntry object providing access to LDAP entries
   * @param presentEntries the set of currently active entries to be retained
   * @param log the logger used to log debug information during the operation
   */
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
