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
public class IsMemberOfResponse extends Response{
    
    private VootGroup[] entry;
    
    public void setPage(int itemsPerPage){
        if(entry==null) return;
        super.setPage(entry.length, itemsPerPage);
    }

    public VootGroup[] getEntry() {
        return entry;
    }

    public void setEntry(VootGroup[] entry) {
        this.entry = entry;
    }
    
    
}
