package cz.metacentrum.perun.webgui.tabs.testtabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.rtMessagesManager.SendMessageToRt;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.TestTabs;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;

import java.util.Map;

/**
 * Testing tab for JSON parser.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class TestJSONParserTabItem implements TabItem, TabItemWithUrl {


	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	private TextBox returnedValue = new TextBox();
	private Label titleWidget = new Label("Testing JSON parser ");

	/**
	 * Changing shell request
	 */
	public TestJSONParserTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		Button sendMessageButton = new Button("Parse response");

		final FlexTable ft = new FlexTable();
		ft.setCellSpacing(15);

		int row = 0;

		ft.setText(row, 0, "Server response:");
		ft.setWidget(row, 1, returnedValue);
		row++;

		ft.setText(row, 0, "Callback name:");
		ft.setHTML(row, 1, "callbackPost");
		row++;

		ft.setWidget(row, 0, sendMessageButton);

		row++;


		sendMessageButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				String resp = returnedValue.getText();

				// trims the whitespace
				resp = resp.trim();

				// short comparing
				if(("callbackPost(null);").equalsIgnoreCase(resp)){
					UiElements.generateInfo("Parser result", "Parsed value is: NULL");
				}

				// if starts with callbackName( and ends with ) or ); - wrapped, must be unwrapped
				RegExp re = RegExp.compile("^" + "callbackPost" + "\\((.*)\\)|\\);$");
				MatchResult result = re.exec(resp);
				if(result != null){
					resp = result.getGroup(1);
				}

				UiElements.generateInfo("Unwrapped value", "Non-null unwrapped value is: "+resp);

				// if response = null - return null
				if(resp.equals("null")){
					UiElements.generateInfo("Parser result", "Parsed value is: NULL");
				}

				// normal object
				JavaScriptObject jso = JsonUtils.parseJson(resp);
				BasicOverlayType basic = jso.cast();
				UiElements.generateInfo("Parser result", "Parsed result is: "+basic.getString());

			}
		});

		this.contentWidget.setWidget(ft);

		return getWidget();

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.settingToolsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1153;
		int result = 4304;
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

	public void open(){ }

	public boolean isAuthorized() {
		return session.isPerunAdmin();
	}

	public final static String URL = "json";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return TestTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public TestJSONParserTabItem load(Map<String, String> parameters) {
		return new TestJSONParserTabItem();
	}

}
