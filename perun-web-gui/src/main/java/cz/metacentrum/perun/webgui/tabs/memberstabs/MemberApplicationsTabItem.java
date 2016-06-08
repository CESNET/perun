package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationsForMember;
import cz.metacentrum.perun.webgui.json.registrarManager.HandleApplication;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.ApplicationDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Displays members applications
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MemberApplicationsTabItem implements TabItem {

	private RichMember member;
	private int memberId;
	private PerunWebSession session = PerunWebSession.getInstance();
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading member details");
	private int groupId = 0;
	private GetApplicationsForMember applicationsRequest = null;

	/**
	 * Constructor
	 *
	 * @param member RichMember object, typically from table
	 */
	public MemberApplicationsTabItem(RichMember member, int groupId){
		this.member = member;
		this.memberId = member.getId();
		this.groupId = groupId;
	}

	public boolean isPrepared(){
		return !(member == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim()) + ": applications");

		// main widget panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		// set proper request
		if (session.isVoAdmin(member.getVoId())) {
			applicationsRequest = new GetApplicationsForMember(memberId, 0);
		} else if (session.isGroupAdmin(groupId)) {
			// group admin can see only apps for his group
			applicationsRequest = new GetApplicationsForMember(memberId, groupId);
		} else if (session.isVoObserver(member.getVoId())) {
			applicationsRequest = new GetApplicationsForMember(memberId, 0);
		}

		applicationsRequest.setCheckable(false);

		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(applicationsRequest);

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
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(verify, events));
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
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(approve, events));
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
									HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(reject, events));
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
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(delete, events));
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

		menu.addFilterWidget(new ExtendedSuggestBox(applicationsRequest.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				applicationsRequest.filterTable(text);
			}
		}, "Filter by group");

		CellTable<Application> table = applicationsRequest.getTable(new FieldUpdater<Application, String>() {
			@Override
			public void update(int i, Application application, String s) {
				session.getTabManager().addTabToCurrentTab(new ApplicationDetailTabItem(application), true);
			}
		});
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp, 350, this);

		vp.add(sp);

		/*
		verify.setEnabled(false);
		approve.setEnabled(false);
		reject.setEnabled(false);
		delete.setEnabled(false);

		if (session.isVoAdmin(member.getVoId()) || session.isGroupAdmin(groupId)) {
			JsonUtils.addTableManagedButton(applicationsRequest, table, verify);
			JsonUtils.addTableManagedButton(applicationsRequest, table, approve);
			JsonUtils.addTableManagedButton(applicationsRequest, table, reject);
			JsonUtils.addTableManagedButton(applicationsRequest, table, delete);
		}
		*/

		this.contentWidget.setWidget(vp);

		return getWidget();

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.userGreenIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1447;
		int result = 1;
		result = prime * result + memberId;
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
		MemberApplicationsTabItem other = (MemberApplicationsTabItem) obj;
		if (memberId != other.memberId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(member.getVoId()) || session.isVoObserver(member.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

}
