package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.rtMessagesManager.SendMessageToRt;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Inner tab item for quota change
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class RequestQuotaChangeTabItem implements TabItem {

	static private final String RT_SUBJECT = "QUOTA: Change request";	
	
	private PerunWebSession session = PerunWebSession.getInstance();
    private SimplePanel contentWidget = new SimplePanel();

	private Label titleWidget = new Label("Loading quota change request");

	private QuotaType quotaType;
	private Resource resource;
	private User user;
	private String oldQuota;
    private String defaultQuota;
	
	public enum QuotaType {
		FILES, DATA
	}	

	/**
	 * Creates a tab instance
     */
	public RequestQuotaChangeTabItem(Resource resource, User user, QuotaType type, String oldQuota, String defaultQuota){
		this.quotaType = type;
		this.resource = resource;
		this.user = user;
		
		if (oldQuota.equalsIgnoreCase("null")) {
			this.oldQuota = "Using default";
		}  else {
            this.oldQuota = oldQuota;
        }
        if (defaultQuota.equalsIgnoreCase("null")) {
            this.defaultQuota = "Not set";
        } else {
            this.defaultQuota = defaultQuota;
        }

	}

	public boolean isPrepared(){
		return (user != null && resource != null && quotaType != null);		
	}
	
	public Widget draw() {

        VerticalPanel vp = new VerticalPanel();

		// set tab name
		this.titleWidget.setText(getQuotaTypeAsString() + " quota change request");
		
		// quota string
		//String quotaStr = getQuotaTypeAsString();
		
		// new quota input
		final TextBox newQuota = new TextBox();
        final TextBox reason = new TextBox();
		final ListBox units = new ListBox();
        units.addItem("M");
        units.addItem("G");
        units.addItem("T");
        units.setSelectedIndex(1); // default G
		
		FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");
		
		ft.setWidget(0, 0, new HTML("Resource:"));
		ft.setWidget(1, 0, new HTML("Current quota:"));
		ft.setWidget(2, 0, new HTML("Requested quota:"));
        ft.setWidget(3, 0, new HTML("Reason:"));

        for (int i=0; i<ft.getRowCount(); i++) {
            ft.getFlexCellFormatter().setStyleName(i, 0, "itemName");
        }

		ft.setText(0, 1, resource.getName());
		ft.setText(1, 1, oldQuota + " (default: "+defaultQuota +")");

		ft.setWidget(2, 1, newQuota);
        ft.setWidget(3, 1, reason);

        final CustomButton requestQuotaButton = new CustomButton("Send request", "Send quota change request", SmallIcons.INSTANCE.emailIcon());
        requestQuotaButton.setEnabled(false);

        KeyUpHandler handler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                if (!newQuota.getText().isEmpty() && !reason.getText().isEmpty()) {
                    requestQuotaButton.setEnabled(true);
                } else {
                    requestQuotaButton.setEnabled(false);
                }
            }
        };
        newQuota.addKeyUpHandler(handler);
        reason.addKeyUpHandler(handler);

        if (this.quotaType.equals(QuotaType.DATA)) {
            ft.setWidget(2, 2, units);
            ft.getFlexCellFormatter().setColSpan(4, 0, 3);
        } else {
            ft.getFlexCellFormatter().setColSpan(4, 0, 2);
        }

		requestQuotaButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
                if (quotaType.equals(QuotaType.DATA)) {
                    requestQuotaChange(newQuota.getText()+units.getItemText(units.getSelectedIndex()), reason.getText());
                } else {
                    requestQuotaChange(newQuota.getText(), reason.getText());
                }
			}
		});

        vp.add(ft);

        TabMenu menu = new TabMenu();
        menu.addWidget(requestQuotaButton);

        final TabItem tab = this;
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

        vp.add(menu);
        vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		this.contentWidget.setWidget(vp);
		return getWidget();
		
	}

	protected void requestQuotaChange(String newQuota, String reason) {
		
		SendMessageToRt rt = new SendMessageToRt(JsonCallbackEvents.closeTabEvents(session, this));
		rt.sendMessage(SendMessageToRt.DEFAULT_QUEUE, RT_SUBJECT, getRequestText(newQuota, reason), resource.getVoId());
		
	}
	
	private String getRequestText(String newQuota, String reason) {

		String text = "QUOTA CHANGE REQUEST\n\n";
		text += "User: " + user.getFullNameWithTitles() + " (user ID: " + user.getId() +")\n";
		text += "Resource: " + resource.getName() + " (resource ID: " + resource.getId() +")\n";
		text += getQuotaTypeAsString() + " quota\n";
		text += "Requested quota: " + newQuota + "\n";
        text += "Reason: " + reason + "\n";
		return text;

	}

	private String getQuotaTypeAsString() {

        switch(quotaType) {
			case FILES:
				return "Files";
			case DATA:
				return "Data";
		}
		return "Unknown";

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.databaseIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 402;
		result = prime * result;
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
		if (!this.user.equals(((RequestQuotaChangeTabItem)obj).user) && !this.resource.equals(((RequestQuotaChangeTabItem)obj).resource) && this.quotaType != ((RequestQuotaChangeTabItem)obj).quotaType)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		
	}
	
	public boolean isAuthorized() {
        return session.isSelf(user.getId());
	}
	
}