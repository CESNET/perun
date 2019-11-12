package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.rtMessagesManager.SendMessageToRt;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextArea;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Inner tab item for quota change
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlámal <256627@mail.muni.cz>
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
	private VirtualOrganization vo;

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

		if (oldQuota.isEmpty()) {
			this.oldQuota = "Using default";
		}  else {
			this.oldQuota = oldQuota.toString();
		}
		if (defaultQuota.isEmpty()) {
			this.defaultQuota = "Not set";
		} else {
			this.defaultQuota = defaultQuota.toString();
		}

	}

	public boolean isPrepared(){
		return (user != null && resource != null && quotaType != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, resource.getVoId(), new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		}).retrieveData();

		VerticalPanel vp = new VerticalPanel();

		// set tab name
		this.titleWidget.setText(getQuotaTypeAsString() + " quota change request");

		// quota string
		//String quotaStr = getQuotaTypeAsString();

		// new quota input
		final ExtendedTextBox newQuota = new ExtendedTextBox();
		final ExtendedTextArea reason = new ExtendedTextArea();
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
		ft.setText(1, 1, oldQuota + " (default: " + defaultQuota + ")");

		ft.setWidget(2, 1, newQuota);
		ft.getFlexCellFormatter().setColSpan(3, 1, 2);
		ft.setWidget(3, 1, reason);

		final CustomButton requestQuotaButton = new CustomButton("Send request", "Send quota change request", SmallIcons.INSTANCE.emailIcon());

		if (this.quotaType.equals(QuotaType.DATA)) {
			ft.setWidget(2, 2, units);
			ft.getFlexCellFormatter().setColSpan(4, 0, 3);
		} else {
			ft.getFlexCellFormatter().setColSpan(4, 0, 2);
		}

		final ExtendedTextBox.TextBoxValidator quotaValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (newQuota.getTextBox().getValue().trim().isEmpty()) {
					newQuota.setError("You must enter requested quota!");
				} else if (!JsonUtils.checkParseInt(newQuota.getTextBox().getValue().trim())) {
					newQuota.setError("Requested quota must be a number!");
				} else {
					newQuota.setOk();
					return true;
				}
				return false;
			}
		};
		newQuota.setValidator(quotaValidator);

		final ExtendedTextArea.TextAreaValidator reasonValidator = new ExtendedTextArea.TextAreaValidator() {
			@Override
			public boolean validateTextArea() {
				if (reason.getTextArea().getValue().trim().isEmpty()) {
					reason.setError("You must specify reason for quota change!");
					return false;
				}
				reason.setOk();
				return true;
			}
		};
		reason.setValidator(reasonValidator);
		reason.getTextArea().setSize("205px", "100px");

		requestQuotaButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				if (!quotaValidator.validateTextBox() && !reasonValidator.validateTextArea()) return;

				if (quotaType.equals(QuotaType.DATA)) {
					requestQuotaChange(newQuota.getTextBox().getValue().trim()+units.getItemText(units.getSelectedIndex()), reason.getTextArea().getText());
				} else {
					requestQuotaChange(newQuota.getTextBox().getValue().trim(), reason.getTextArea().getText());
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
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		this.contentWidget.setWidget(vp);
		return getWidget();

	}

	protected void requestQuotaChange(String newQuota, String reason) {

		SendMessageToRt rt = new SendMessageToRt(JsonCallbackEvents.closeTabEvents(session, this));
		// quota change will use per-vo (or per-resource in a future) queue
		rt.sendMessage("", RT_SUBJECT, getRequestText(newQuota, reason), resource.getVoId());

	}

	private String getRequestText(String newQuota, String reason) {

		String text = "QUOTA CHANGE REQUEST\n\n";
		text += "User: " + user.getFullNameWithTitles() + " (user ID: " + user.getId() +")\n";
		if (vo != null) {
			text += "VO: " + vo.getShortName() + " / "+ vo.getName() + " (vo ID: " + vo.getId() +")\n";
		}
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
		final int prime = 1217;
		int result = 402;
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
