package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.ArrayList;

/**
 * Facility with list of all its owners
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class RichFacility extends Facility {

	private List<Owner> facilityOwners;

	public RichFacility(){
	}

	public RichFacility(Facility facility, List<Owner> facilityOwners) {

		super(facility.getId(), facility.getName(), facility.getDescription(), facility.getCreatedAt(), facility.getCreatedBy(),
				facility.getModifiedAt(), facility.getModifiedBy(), facility.getCreatedByUid(), facility.getModifiedByUid());
		this.facilityOwners = facilityOwners;
	}

	public List<Owner> getFacilityOwners() {
		return facilityOwners;
	}

	public void setFacilityOwners(List<Owner> facilityOwners) {
		this.facilityOwners = facilityOwners;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((facilityOwners == null) ? 0 : facilityOwners.hashCode());
		result = prime * result + getId();
		result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RichFacility other = (RichFacility) obj;
		if(!super.equals(obj)) return false;
		if (facilityOwners == null) {
			if (other.facilityOwners != null) {
				return false;
			}
		} else if (!facilityOwners.equals(other.facilityOwners)) {
			return false;
		}
		return true;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		List<Owner> facilityOwnersOld = getFacilityOwners();
		List<String> facilityOwnersNew = new ArrayList<String>();
		String sFacilityOwnersNew;

		if(getFacilityOwners() == null) sFacilityOwnersNew = "\\0";
		else {
			for(Owner o: facilityOwnersOld) {
				facilityOwnersNew.add(o.serializeToString());
			}
			sFacilityOwnersNew = facilityOwnersNew.toString();
		}

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
			", description=<").append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">").append(
			", facilityOwners=<").append(sFacilityOwnersNew).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append( ":[id='").append(getId()).append("', name='").append(this.getName()).append(
				"', description='").append(this.getDescription()).append("', facilityOwners='").append(facilityOwners).append("']").toString();
	}
}
