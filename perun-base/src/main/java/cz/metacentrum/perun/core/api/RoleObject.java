package cz.metacentrum.perun.core.api;

/**
 * Represents objects, upon which Perun roles can be set, e.g. role RESOURCEADMIN can be set upon Resource, Vo, Facility
 * or no object (None).
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public enum RoleObject {
  None, Group, Vo, Facility, Resource, User, Member, SecurityTeam
}
