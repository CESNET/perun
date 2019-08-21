package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface PerunFacility extends PerunEntry<Facility> {
	
	public void addFacility(Facility facility)  throws InternalErrorException;
	
	public void deleteFacility(Facility facility) throws InternalErrorException;
	
	public void updateFacility(Facility facility) throws InternalErrorException; 

	public void synchronizeFacility(Facility facility, Iterable<Attribute> attrs) throws InternalErrorException; 

}
	
