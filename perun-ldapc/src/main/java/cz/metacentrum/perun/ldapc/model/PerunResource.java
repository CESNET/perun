package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.util.List;

public interface PerunResource extends PerunEntry<Resource> {

  /**
   * Add resource to LDAP
   *
   * @param resource resource from Perun
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void addResource(Resource resource);

  public void assignGroup(Resource resource, Group group);

  /**
   * Remove resource from LDAP
   *
   * @param resource resource from Perun
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void deleteResource(Resource resource);

  public void removeGroup(Resource resource, Group group);

  public void synchronizeGroups(Resource resource, List<Group> assignedGroups);

  public void synchronizeResource(Resource resource, Iterable<Attribute> attrs, List<Group> assignedGroups);

  public void updateResource(Resource resource);
}
