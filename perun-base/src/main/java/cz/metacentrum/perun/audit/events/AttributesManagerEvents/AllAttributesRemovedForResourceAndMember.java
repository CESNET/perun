package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesRemovedForResourceAndMember {

    private Resource resource;
    private Member member;
    private String name = this.getClass().getName();
    private String message;

    public AllAttributesRemovedForResourceAndMember() {
    }



    public AllAttributesRemovedForResourceAndMember(Resource resource, Member member) {
        this.resource = resource;
        this.member = member;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s and %s.",resource, member);
    }
}
