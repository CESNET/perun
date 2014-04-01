package cz.metacentrum.perun.webgui.client.applicationresources;

/**
 * Used for checking inputs
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 *
 */
public interface FormInputChecker {

	/**
	 * Whether is the input valid
	 * @param forceNewValidation Whether to force new validation or use old result, improves speed
	 * @return
	 */
	public boolean isValid(boolean forceNewValidation);

	/**
	 * Whether is input async & validating
	 * @return
	 */
	public boolean isValidating();


	/**
	 * Whether to use "OK" when validated OK
	 * @return
	 */
	public boolean useDefaultOkMessage();
}
