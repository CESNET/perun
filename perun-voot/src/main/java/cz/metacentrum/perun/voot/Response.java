/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.voot;

import java.util.List;

/**
 * JavaDoc - TODO
 * 
 * @author Martin Malik <374128@mail.muni.cz>
 */
public abstract class Response {
 
    private Integer startIndex = 0;
    private Integer totalResults = 0;
    private Integer itemsPerPage = 0;
  
    public void setPage(int totalResults, int itemsPerPage){
        this.totalResults = totalResults;
        this.itemsPerPage = itemsPerPage;
    }
    
    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }
    
}
