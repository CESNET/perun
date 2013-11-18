package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.auditMessagesManager.GetAuditMessages;
import cz.metacentrum.perun.webgui.model.AuditMessage;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Audit log
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class AuditLogTabItem implements TabItem, TabItemWithUrl {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();
	
	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();
	
	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Audit Log");
	// remember last number of messages
	private int count = 20; // default 20

	/**
	 * Creates a tab instance
	 *
     */
	public AuditLogTabItem(){ }
	
	public boolean isPrepared(){
		return true;
	}
	
	public Widget draw() {

		// page main tab
		final VerticalPanel mainTab = new VerticalPanel();
		mainTab.setSize("100%", "100%");
		
		// number of messages
		final TextBox tb = new TextBox();
		tb.setText(String.valueOf(count));
		tb.setWidth("100px");

		// menu panel
		TabMenu menu = new TabMenu();
		mainTab.add(menu);
		mainTab.setCellHeight(menu, "30px");

        CustomButton refreshButton = TabMenu.getPredefinedButton(ButtonType.REFRESH, ButtonTranslation.INSTANCE.refreshAuditMessages());

        // retrieve messages
		final GetAuditMessages call = new GetAuditMessages(JsonCallbackEvents.disableButtonEvents(refreshButton));
		call.setCount(count);
		CellTable<AuditMessage> table = call.getTable();

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		mainTab.add(sp);

		// resize perun table to correct size on screen
		session.getUiElements().resizePerunTable(sp, 350, this);
		
		// refresh button action
		refreshButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (JsonUtils.checkParseInt(tb.getText())) {
                    call.clearTable();
                    count = Integer.parseInt(tb.getText());
                    call.setCount(count);
                    call.retrieveData();
                } else {
                    JsonUtils.cantParseIntConfirm("Number of messages", tb.getText());
                }
            }
        });
        menu.addWidget(refreshButton);

		// enter key = refresh on count text box
		tb.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (JsonUtils.checkParseInt(tb.getText())) {
                        call.clearTable();
                        count = Integer.parseInt(tb.getText());
                        call.setCount(count);
                        call.retrieveData();
                    } else {
                        JsonUtils.cantParseIntConfirm("Number of messages", tb.getText());
                    }
                }
            }
        });
		
		// add textbox into menu
		menu.addWidget(new HTML("<strong>Number of messages: </strong>"));
		menu.addWidget(tb);
		
		this.contentWidget.setWidget(mainTab);		
		
		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.reportEditIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result * 135;
		return result;
	}

	/**
	 * @param obj
	 */
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
        session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Audit log", getUrlWithParameters());
	}
	
	public boolean isAuthorized() {

		if (session.isPerunAdmin()) { 
			return true; 
		} else {
			return false;
		}

	}

	public final static String URL = "alog";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return PerunAdminTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}
	
	static public AuditLogTabItem load(Map<String, String> parameters)
	{
		return new AuditLogTabItem();
	}
	
}