package cz.metacentrum.perun.webgui.tabs;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;

/**
 * Tab for Page not found case
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PageNotFoundTabItem implements TabItem, TabItemWithUrl {

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Page not found");

	/**
	 * Creates a tab instance
	 */
	public PageNotFoundTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// text
		HTML text = new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>Requested page was not found.</h2>");
		firstTabPanel.add(text);
		// format
		firstTabPanel.setCellHorizontalAlignment(text, HasHorizontalAlignment.ALIGN_CENTER);
		firstTabPanel.setCellVerticalAlignment(text, HasVerticalAlignment.ALIGN_MIDDLE);

		this.contentWidget.setWidget(firstTabPanel);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.errorIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 547;
		int result = 1;
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

	}

	public boolean isAuthorized() {
		// always authorized
		return true;
	}

	static public final String URL = "404";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return OtherTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}



}
