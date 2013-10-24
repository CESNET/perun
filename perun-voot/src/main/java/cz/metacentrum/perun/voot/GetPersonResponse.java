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
public class GetPersonResponse extends Response{
    
    private VootPerson entry; 

    public void setPage(){
        if (entry==null) return;
        super.setPage(1, 1);
    }
    
    public VootPerson getEntry() {
        return entry;
    }

    public void setEntry(VootPerson entry) {
        this.entry = entry;
    }

    
}
