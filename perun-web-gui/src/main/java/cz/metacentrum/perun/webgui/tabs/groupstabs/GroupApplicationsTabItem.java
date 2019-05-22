package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.ObjectTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationsForGroup;
import cz.metacentrum.perun.webgui.json.registrarManager.HandleApplication;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.registrartabs.ApplicationDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Group Applications
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GroupApplicationsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading Group applications");

	// data
	private Group group;
	//data
	private int groupId;
	private int selectedIndex = 3;

	/**
	 * Creates a tab instance
	 *
	 * @param group
	 */
	public GroupApplicationsTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param groupId
	 */
	public GroupApplicationsTabItem(int groupId){
		this.groupId = groupId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
	}

	public boolean isPrepared(){
		return !(group == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// request
		final GetApplicationsForGroup applicationsRequest = new GetApplicationsForGroup(group.getId());
		final JsonCallbackEvents refreshEvent = JsonCallbackEvents.refreshTableEvents(applicationsRequest);

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName())+": "+"applications");

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));
		firstTabPanel.add(menu);
		firstTabPanel.setCellHeight(menu, "30px");

		/*
		// verify button
		final CustomButton verify = TabMenu.getPredefinedButton(ButtonType.VERIFY, ButtonTranslation.INSTANCE.verifyApplication());
		verify.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Application> list = applicationsRequest.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					for (int i=0; i<list.size(); i++) {
						if (i != list.size()-1) {
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(verify));
							request.verifyApplication(list.get(i).getId());
						} else {
							// refresh table on last call
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(verify, refreshEvent));
							request.verifyApplication(list.get(i).getId());
						}
					}
				}
			}
		});

		// accept button
		final CustomButton approve = TabMenu.getPredefinedButton(ButtonType.APPROVE, ButtonTranslation.INSTANCE.approveApplication());
		approve.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Application> list = applicationsRequest.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					for (int i=0; i<list.size(); i++) {
						if (i != list.size()-1) {
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(approve));
							request.approveApplication(list.get(i));
						} else {
							// refresh table on last call
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(approve, refreshEvent));
							request.approveApplication(list.get(i));
						}
					}
				}
			}
		});

		//reject button
		final CustomButton reject = TabMenu.getPredefinedButton(ButtonType.REJECT, ButtonTranslation.INSTANCE.rejectApplication());
		reject.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<Application> list = applicationsRequest.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					// confirm content
					FlexTable content = new FlexTable();
					content.setCellSpacing(10);
					content.setHTML(0, 0, "Please specify reason of rejection to let user know why was application rejected.");
					content.getFlexCellFormatter().setColSpan(0, 0, 2);
					final TextArea reason = new TextArea();
					reason.setSize("300px", "150px");
					content.setHTML(1, 0, "<strong>Reason: </strong>");
					content.setWidget(1, 1, reason);

					Confirm c = new Confirm("Specify reason", content, new ClickHandler(){
						public void onClick(ClickEvent event) {

							for (int i=0; i<list.size(); i++) {
								if (i != list.size()-1) {
									HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(reject));
									request.rejectApplication(list.get(i).getId(), reason.getText());
								} else {
									// refresh table on last call
									HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(reject, refreshEvent));
									request.rejectApplication(list.get(i).getId(), reason.getText());
								}
							}

						}
					}, true);
					c.show();
				}
			}
		});

		// delete button
		final CustomButton delete = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteApplication());
		delete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Application> list = applicationsRequest.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					for (int i=0; i<list.size(); i++) {
						if (i != list.size()-1) {
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(delete));
							request.deleteApplication(list.get(i).getId());
						} else {
							// refresh table on last call
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(delete, refreshEvent));
							request.deleteApplication(list.get(i).getId());
						}
					}
				}
			}
		});

		menu.addWidget(verify);
		menu.addWidget(approve);
		menu.addWidget(reject);
		menu.addWidget(delete);
		*/

		// FILTER
		menu.addWidget(new HTML("<strong>State: </strong>"));

		// state
		final ListBox stateListBox = new ListBox();
		stateListBox.addItem(WidgetTranslation.INSTANCE.listboxAll(), "");
		stateListBox.addItem(ObjectTranslation.INSTANCE.applicationStateNew(), "NEW");
		stateListBox.addItem(ObjectTranslation.INSTANCE.applicationStateVerified(), "VERIFIED");
		stateListBox.addItem("Pending", "NEW,VERIFIED");
		stateListBox.addItem(ObjectTranslation.INSTANCE.applicationStateApproved(), "APPROVED");
		stateListBox.addItem(ObjectTranslation.INSTANCE.applicationStateRejected(), "REJECTED");
		stateListBox.setSelectedIndex(selectedIndex);
		menu.addWidget(stateListBox);

		stateListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				selectedIndex = stateListBox.getSelectedIndex();
				applicationsRequest.setState(stateListBox.getValue(stateListBox.getSelectedIndex()));
				applicationsRequest.clearTable();
				applicationsRequest.retrieveData();
			}
		});

		// FILTER 2
		menu.addWidget(new HTML("<strong>Submitted&nbsp;by: </strong>"));
		menu.addFilterWidget(new ExtendedSuggestBox(applicationsRequest.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				applicationsRequest.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterApplications());

		// TABLE
		applicationsRequest.setState(stateListBox.getValue(stateListBox.getSelectedIndex()));
		CellTable<Application> table = applicationsRequest.getTable(new FieldUpdater<Application, String>() {
			public void update(int index, Application object, String value) {
				session.getTabManager().addTabToCurrentTab(new ApplicationDetailTabItem(object), true);
			}
		});
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");


		session.getUiElements().resizePerunTable(sp, 100);
		firstTabPanel.add(sp);

		/*
		verify.setEnabled(false);
		approve.setEnabled(false);
		reject.setEnabled(false);
		delete.setEnabled(false);

		if (session.isGroupAdmin(groupId) || session.isVoAdmin(group.getVoId()))  {

			JsonUtils.addTableManagedButton(applicationsRequest, table, approve);
			JsonUtils.addTableManagedButton(applicationsRequest, table, reject);
			JsonUtils.addTableManagedButton(applicationsRequest, table, delete);

			if (session.isPerunAdmin()) {

				JsonUtils.addTableManagedButton(applicationsRequest, table, verify);

			}

		} else {



		}

		*/
		applicationsRequest.setCheckable(false);

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
		return SmallIcons.INSTANCE.applicationFromStorageIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1409;
		int result = 1;
		result = prime * result + groupId;
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
		GroupApplicationsTabItem other = (GroupApplicationsTabItem) obj;
		if (groupId != other.groupId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(group, "Applications", getUrlWithParameters());
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "appls";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public GroupApplicationsTabItem load(Map<String, String> parameters) {
		int groupId = Integer.parseInt(parameters.get("id"));
		return new GroupApplicationsTabItem(groupId);
	}

}
