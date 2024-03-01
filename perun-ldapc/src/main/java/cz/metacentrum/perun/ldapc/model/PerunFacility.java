package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;

public interface PerunFacility extends PerunEntry<Facility> {

  public void addFacility(Facility facility);

  public void deleteFacility(Facility facility);

  public void synchronizeFacility(Facility facility, Iterable<Attribute> attrs);

  public void updateFacility(Facility facility);

}

