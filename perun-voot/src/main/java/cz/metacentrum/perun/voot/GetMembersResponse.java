/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.voot;

/**
 * JavaDoc - TODO
 * 
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class GetMembersResponse extends Response{
    
    private VootPerson[] entry;

    public void setPage(int itemsPerPage) {
        if(entry==null) return;
        super.setPage(entry.length, itemsPerPage);    
    }

    public VootPerson[] getEntry() {
        return entry;
    }

    public void setEntry(VootPerson[] entry) {
        this.entry = entry;
    }
    
}
