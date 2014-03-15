package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.User;

/**
 *  JavaDoc - TODO
 * 
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VootPerson {
    
    private String id;
    private String displayName;

    //private Email[] emails;
    public VootPerson(User user){
        this.id = Integer.toString(user.getId());
        this.displayName = user.getDisplayName();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    
}
