/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.Group;

/**
 * JavaDoc - TODO
 * 
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VootGroup {
    
    private String id;
    private String name;
    private String description;

    public VootGroup(Group group){
        this.id = Integer.toString(group.getId());
        this.name = group.getName();
        this.description = group.getDescription();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
