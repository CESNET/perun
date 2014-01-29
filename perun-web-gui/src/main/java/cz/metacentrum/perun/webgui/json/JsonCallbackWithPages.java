package cz.metacentrum.perun.webgui.json;

/**
 * A request - table - which can be pageable, must inherit this.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public interface JsonCallbackWithPages {
	
	/**
	 * Retrieves the data from the db.
	 * The table must be cleared before.
	 * Returns callback id
	 * 
	 * @param pageSize
	 * @param pageNum Starts with 0.
	 * @return callback id
	 */
	int retrieveData(int pageSize, int pageNum);
	
	/**
	 * Clears the table
	 */
	void clearTable();
	
}