package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Object which represents RichResource
 *
 * @author  Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class RichResource extends Resource {

    private Vo vo;
    private Facility facility;

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

    @Override
    public String serializeToString() {
        return this.getClass().getSimpleName() +":[" +
                "id=<" + getId() + ">" +
                ", voId=<" + getVoId() + ">" +
                ", facilityId=<" + getFacilityId() + ">" +
                ", name=<" + (super.getName() == null ? "\\0" : BeansUtils.createEscaping(super.getName())) + ">" +
                ", description=<" + (super.getDescription() == null ? "\\0" : BeansUtils.createEscaping(super.getDescription())) + ">" +
                ", facility=<" + (getFacility() == null ? "\\0" : getFacility().serializeToString()) + ">" +
                ", vo=<" + (getVo() == null ? "\\0" : getVo().serializeToString()) + ">" +
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
                + "']";
    }

}