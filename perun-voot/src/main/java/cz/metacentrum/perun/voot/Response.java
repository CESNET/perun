package cz.metacentrum.perun.voot;

import java.util.Arrays;

/**
 * Structure of response with information about startIndex, totalResult, itemsPerPage and results of request.
 * Results are items, e.g. array of members or groups.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class Response {

	private Integer startIndex = 0;
	private Integer totalResults = 0;
	private Integer itemsPerPage = 0;

	private Object[] entry;

	/**
	 * Set parameters of response.
	 *
	 * @param startIndex      first index of result
	 * @param totalResults    number of results
	 * @param itemsPerPage    results per page
	 */
	public void setPage(int startIndex,int totalResults, int itemsPerPage){
		this.startIndex = startIndex;
		this.totalResults = totalResults;
		this.itemsPerPage = itemsPerPage;
	}

	/**
	 * Index of first result returned in response.
	 *
	 * @return    index of first result
	 */
	public Integer getStartIndex() {
		return startIndex;
	}

	/**
	 * Set index of first result returned in response.
	 *
	 * @param startIndex    index of first result
	 */
	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * Total number of results that would be returned.
	 *
	 * @return    total number of results
	 */
	public Integer getTotalResults() {
		return totalResults;
	}

	/**
	 * Set total number of results that would be returned.
	 *
	 * @param totalResults    total number of results
	 */
	public void setTotalResults(Integer totalResults) {
		this.totalResults = totalResults;
	}

	/**
	 * Number of results returned per page in response.
	 *
	 * @return    number of results per page
	 */
	public Integer getItemsPerPage() {
		return itemsPerPage;
	}

	/**
	 * Set number of results returned per page in response.
	 *
	 * @param itemsPerPage    number of results per page
	 */
	public void setItemsPerPage(Integer itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	/**
	 * Return entry of response. Every returned item have to be represented separate for consistency of parsing and entry is surrounded by square brackets.
	 * e.g. '[{item1},{item2}]'
	 *
	 * @return    entry of response
	 */
	public Object[] getEntry(){
		return entry;
	}

	/**
	 * Set entry of response.
	 *
	 * @param entry    entry of response
	 */
	public void setEntry(Object[] entry){
		this.entry = entry;
	}

	@Override
	public String toString(){
		return new StringBuilder().append(getClass().getSimpleName()).append(":[")
				.append("startIndex='").append(getStartIndex()).append("', ")
				.append("totalResults='").append(getTotalResults()).append("', ")
				.append("itemsPerPage='").append(getItemsPerPage()).append("', ")
				.append("entry='").append(Arrays.toString(getEntry())).append("']").toString();
	}

}
