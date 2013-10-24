package cz.metacentrum.perun.webgui.client.applicationresources.pages;

import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * VO not found page
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class VoNotFoundPage extends Composite {

	/**
	 * Main body contents
	 */
	private VerticalPanel bodyContents = new VerticalPanel();

	/**
	 * Creates a new page with message VO not found
	 */
	public VoNotFoundPage(PerunError error) {
		
		this.initWidget(bodyContents);
		
		bodyContents.setSize("100%", "300px");
		
		bodyContents.add(new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>Virtual organization not found !</h2>"));
		bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(0), HasHorizontalAlignment.ALIGN_CENTER);
		bodyContents.setCellVerticalAlignment(bodyContents.getWidget(0), HasVerticalAlignment.ALIGN_BOTTOM);
		
		if (error != null) {
			bodyContents.add(new Label("Server responded with an error: "+error.getErrorInfo()));
			bodyContents.add(new Label("Check the URL and if the problem persists, contact your administrator."));
			bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(1), HasHorizontalAlignment.ALIGN_CENTER);
			bodyContents.setCellVerticalAlignment(bodyContents.getWidget(1), HasVerticalAlignment.ALIGN_TOP);
			bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(2), HasHorizontalAlignment.ALIGN_CENTER);
			bodyContents.setCellVerticalAlignment(bodyContents.getWidget(2), HasVerticalAlignment.ALIGN_TOP);
		} else {
			bodyContents.add(new Label("Request timeout exceeded. Refresh browser to retry."));
			bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(1), HasHorizontalAlignment.ALIGN_CENTER);
			bodyContents.setCellVerticalAlignment(bodyContents.getWidget(1), HasVerticalAlignment.ALIGN_TOP);
		}
		
	}

}