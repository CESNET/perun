package cz.metacentrum.perun.webgui.tabs.testtabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.rtMessagesManager.SendMessageToRt;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.TestTabs;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;

import java.util.Map;

/**
 * Inner tab item for shell change
 * User in: SelfResourcesSettingsTabItem
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class TestRtReportingTabItem implements TabItem, TabItemWithUrl {


	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	private TextBox subjectTextBox = new TextBox();

	private TextBox messageTextBox = new TextBox();

	private TextBox voIdTextBox = new TextBox();

	private TextBox rtQueueTextBox = new TextBox();

	private Label titleWidget = new Label("Testing RPC request");

	/**
	 * Changing shell request
	 */
	public TestRtReportingTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		Button sendMessageButton = new Button("Send to RT");

		final FlexTable ft = new FlexTable();
		ft.setCellSpacing(15);

		int row = 0;

		ft.setText(row, 0, "Subject");
		ft.setWidget(row, 1, subjectTextBox);
		row++;

		ft.setText(row, 0, "Text");
		ft.setWidget(row, 1, messageTextBox);
		row++;

		ft.setText(row, 0, "VO ID (doesn't have to be specified)");
		ft.setWidget(row, 1, voIdTextBox);
		row++;

		ft.setText(row, 0, "RT queue (doesn't have to be specified)");
		ft.setWidget(row, 1, rtQueueTextBox);
		row++;


		ft.setText(row, 0, "Send");
		ft.setWidget(row, 1, sendMessageButton);
		row++;


		sendMessageButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				sendToRt();
			}
		});

		this.contentWidget.setWidget(ft);

		return getWidget();

	}

	protected void sendToRt()
	{
		String message = messageTextBox.getText();
		String subject = subjectTextBox.getText();
		String voIdString = voIdTextBox.getText();
		String rtQueue = rtQueueTextBox.getText();
		int voId = -1;

		if(!voIdString.isEmpty())
		{
			try{
				voId = Integer.parseInt(voIdString);
			}catch(NumberFormatException e){
				Window.alert("Incorrect VO id format");
				return;
			}
		}


		SendMessageToRt query = new SendMessageToRt();

		if(voId == -1){
			query.sendMessage(rtQueue, subject, message);
		}else{
			query.sendMessage(rtQueue, subject, message, voId);
		}


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
		final int prime = 1163;
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

	public void open()
	{

	}

	public boolean isAuthorized() {

		return session.isPerunAdmin();

	}

	public final static String URL = "rt";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return TestTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public TestRtReportingTabItem load(Map<String, String> parameters)
	{
		return new TestRtReportingTabItem();
	}

}
