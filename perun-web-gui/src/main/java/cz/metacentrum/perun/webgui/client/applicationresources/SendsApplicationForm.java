package cz.metacentrum.perun.webgui.client.applicationresources;

import com.google.gwt.user.client.ui.Button;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

/**
 * Sends application form
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 *
 */
public interface SendsApplicationForm {
	
	/**
	 * Sends the application form
	 */
	public void sendApplicationForm(CustomButton button);
}
