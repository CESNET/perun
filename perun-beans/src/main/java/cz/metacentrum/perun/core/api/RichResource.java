package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.BeansUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Object which represents RichResource
 *
 * @author  Pavel Zlamal <256627@mail.muni.cz>
 */

public class RichResource extends Resource {

	private Vo vo;
	private Facility facility;
	private List<ResourceTag> resourceTags;

	/**
	 * Constructs a new instance.
	 */
	public RichResource() {}

	/**
	 * Constructs a new instance.
	 */
	public RichResource(Resource resource) {
		super(resource.getId(), resource.getName(), resource.getDescription(), resource.getFacilityId(), resource.getVoId(), resource.getCreatedAt(),
				resource.getCreatedBy(), resource.getModifiedAt(), resource.getModifiedBy(), resource.getCreatedByUid(), resource.getModifiedByUid());
	}

	/**
	 * Sets VO associated with this resource
	 *
	 * @param vo VO associated with resource
	 */
	public void setVo(Vo vo) {
		this.vo = vo;
	}

	/**
	 * Sets Facility associated with this resource
	 *
	 * @param facility Facility associated with resource
	 */
	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	/**
	 * Returns VO associated with this resource
	 *
	 * @return VO associated with resource
	 */
	public Vo getVo() {
		return this.vo;
	}

	/**
	 * Returns Facility associated with this resource
	 *
	 * @return Facility associated with resource
	 */
	public Facility getFacility() {
		return this.facility;
	}

	/**
	 * Returns list of associated ResourceTags with this resource
	 *
	 * @return List<ResourcesTag> associated with resource
	 */
	public List<ResourceTag> getResourceTags() {
		return resourceTags;
	}

	/**
	 * Set list of associated ResourceTags with this resource
	 *
	 * @param resourceTags ResourceTags associated with resource
	 */
	public void setResourceTags(List<ResourceTag> resourceTags) {
		this.resourceTags = resourceTags;
	}

	/**
	 * Add ResourceTag to Resource (used to fill in from SQL)
	 *
	 * @param tag ResourceTag to add
	 */
	public void addResourceTag(ResourceTag tag) {
		if (resourceTags == null) {
			this.resourceTags = new ArrayList<ResourceTag>();
		}
		if (tag != null && !resourceTags.contains(tag)) {
			this.resourceTags.add(tag);
		}
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		String tags = "\\0";
		if (getResourceTags() != null && !getResourceTags().isEmpty()) {
			ArrayList<String> list = new ArrayList<String>();
			for (ResourceTag t : getResourceTags()) {
				list.add(t.serializeToString());
			}
			tags = list.toString();
		}

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", voId=<").append(getVoId()).append(">").append(
			", facilityId=<").append(getFacilityId()).append(">").append(
			", name=<").append(super.getName() == null ? "\\0" : BeansUtils.createEscaping(super.getName())).append(">").append(
			", description=<").append(super.getDescription() == null ? "\\0" : BeansUtils.createEscaping(super.getDescription())).append(">").append(
			", facility=<").append(getFacility() == null ? "\\0" : getFacility().serializeToString()).append(">").append(
			", vo=<").append(getVo() == null ? "\\0" : getVo().serializeToString()).append(">").append(
			", resourceTags=<").append(tags).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":["
			).append("id='").append(getId()
			).append("', voId='").append(super.getVoId()
			).append("', facilityId='").append(super.getFacilityId()
			).append("', name='").append(super.getName()
			).append("', description='").append(super.getDescription()
			).append("', facility='").append(getFacility()
			).append("', vo='").append(getVo()
			).append("', resourceTags='").append(getResourceTags()
			).append("']").toString();
	}

}
