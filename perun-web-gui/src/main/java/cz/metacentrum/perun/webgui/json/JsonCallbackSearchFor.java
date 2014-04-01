package cz.metacentrum.perun.webgui.json;

/**
 * Extension to JsonCallback which provides searchFor() method
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface JsonCallbackSearchFor extends JsonCallback {

	/**
	 * Provides trigger to retrieve data from RPC with new searchString parama
	 *
	 * Implementation should:
	 * 1) check validity of searchString (e.g. is not empty or null)
	 * 2) call clearTable()
	 * 3) call retrieveData()
	 *
	 * @param searchString keyword to search by
	 */
	public void searchFor(String searchString);

}
