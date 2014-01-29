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

        String tags = "\\0";
        if (getResourceTags() != null && !getResourceTags().isEmpty()) {
            ArrayList<String> list = new ArrayList<String>();
            for (ResourceTag t : getResourceTags()) {
                list.add(t.serializeToString());
            }
            tags = list.toString();
        }

        return this.getClass().getSimpleName() +":[" +
                "id=<" + getId() + ">" +
                ", voId=<" + getVoId() + ">" +
                ", facilityId=<" + getFacilityId() + ">" +
                ", name=<" + (super.getName() == null ? "\\0" : BeansUtils.createEscaping(super.getName())) + ">" +
                ", description=<" + (super.getDescription() == null ? "\\0" : BeansUtils.createEscaping(super.getDescription())) + ">" +
                ", facility=<" + (getFacility() == null ? "\\0" : getFacility().serializeToString()) + ">" +
                ", vo=<" + (getVo() == null ? "\\0" : getVo().serializeToString()) + ">" +
                ", resourceTags=<" + tags + ">" +
                ']';
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":["
                + "id='" + getId()
                + "', voId='" + super.getVoId()
                + "', facilityId='" + super.getFacilityId()
                + "', name='" + super.getName()
                + "', description='" + super.getDescription()
                + "', facility='" + getFacility()
                + "', vo='" + getVo()
                + "', resourceTags='" + getResourceTags()
                + "']";
    }

}