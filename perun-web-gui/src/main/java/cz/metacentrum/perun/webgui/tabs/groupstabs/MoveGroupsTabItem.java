package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.groupsManager.MoveGroup;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Move groups under another or as top-level.
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MoveGroupsTabItem implements TabItem {

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
	private Label titleWidget = new Label("Move groups");

	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

	private VirtualOrganization vo = null;
	private Group group = null;
	private ArrayList<? extends Group> groups = null;

	/**
	 * Creates a tab instance
	 */
	public MoveGroupsTabItem(VirtualOrganization vo, ArrayList<? extends Group> groups){
		this.vo = vo;
		this.groups = new TableSorter<Group>().sortByName((ArrayList<Group>)groups);
	}

	/**
	 * Creates a tab instance
	 */
	public MoveGroupsTabItem(Group group, ArrayList<? extends Group> groups){
		this.group = group;
		this.groups = new TableSorter<Group>().sortByName((ArrayList<Group>)groups);
	}

	public boolean isPrepared(){
		return ((this.vo != null) || (this.group != null)) && (this.groups != null) && (!this.groups.isEmpty());
	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();

		int voId = (vo != null) ? vo.getId() : group.getVoId();

		List<Group> backupGroups = new ArrayList<>();
		backupGroups.addAll(groups);

		// remove any subgroups of parent groups in a list of MOVED groups
		for (Group bg : backupGroups) {
			Iterator<? extends Group> iterator = groups.iterator();
			while (iterator.hasNext()) {
				// check if is subgroup
				Group moveGroup = iterator.next();
				if (moveGroup.getName().startsWith(bg.getName()+":")) {
					iterator.remove();
				}
			}
		}

		// textboxes which set the class data when updated
		final ListBoxWithObjects<Group> vosGroups = new ListBoxWithObjects<Group>();

		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// send button
		final CustomButton moveButton = TabMenu.getPredefinedButton(ButtonType.MOVE, buttonTranslation.moveGroup());
		moveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final Group destinationGroup = vosGroups.getSelectedObject();
				final MoveGroup request = new MoveGroup();

				final JsonCallbackEvents nextEvent = JsonCallbackEvents.disableButtonEvents(moveButton, new JsonCallbackEvents() {
					int groupsCounter = 0;
					@Override
					public void onFinished(JavaScriptObject jso) {
						if (groups.size() - 1 == groupsCounter) {
							// there is another group to move
							request.setEvents(JsonCallbackEvents.closeTabDisableButtonEvents(moveButton, tab));
							request.moveGroup(groups.get(groupsCounter),destinationGroup);
						} else if (groups.size() - 1 > groupsCounter) {
							request.moveGroup(groups.get(groupsCounter), destinationGroup);
						}
						// done
					}

					@Override
					public void onError(PerunError error) {
						// close tab if failed
						moveButton.setProcessing(false);
						session.getTabManager().closeTab(tab);
					}

					@Override
					public void onLoadingStart() {
						groupsCounter++;

					}
				});
				if (groups.size() == 1) {
					// single group
					request.setEvents(JsonCallbackEvents.closeTabDisableButtonEvents(moveButton, tab));
				} else {
					// iterate over more groups
					request.setEvents(nextEvent);
				}
				request.moveGroup(groups.get(0), destinationGroup);
			}
		});

		final GetAllGroups groupsCall = new GetAllGroups(voId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				vosGroups.clear();
				ArrayList<Group> retGroups = JsonUtils.jsoAsList(jso);
				retGroups = new TableSorter<Group>().sortByName(retGroups);

				// skip groups which are moving and their sub-groups and direct parents (since they would stayed at the same place)
				Iterator<Group> iterator = retGroups.iterator();
				while (iterator.hasNext()) {
					Group retGroup = iterator.next();
					for (Group g : groups) {
						if (g.getId() == retGroup.getId() ||
								retGroup.getName().startsWith(g.getName()+":") ||
								retGroup.getName().equals(g.getName()) ||
								g.getParentGroupId() == retGroup.getId()) {
							iterator.remove();
							break;
						}
					}
				}

				for (Group g : retGroups) {
					if (!g.isCoreGroup()) {
						// SKIP CORE GROUPS !!
						vosGroups.addItem(g);
					}
				}
				if (vosGroups.getAllObjects().isEmpty()) {

					for (Group g : groups) {
						// at lease one of moving groups is a sub group, so we can offer move to top-level
						if (g.getName().contains(":")) {
							vosGroups.addNotSelectedOption();
							moveButton.setEnabled(true);
							return;
						}
					}

					// can't move group anywhere
					vosGroups.addItem("No possible destination group was found or group is already top-level.");

				} else {
					vosGroups.addNotSelectedOption();
					moveButton.setEnabled(true);
				}
			}
			public void onLoadingStart(){
				vosGroups.clear();
				vosGroups.addItem("Loading...");
				moveButton.setEnabled(false);
			}
			public void onError(PerunError error) {
				vosGroups.clear();
				vosGroups.addItem("Error while loading");
			}
		});

		groupsCall.retrieveData();

		// cancel button
		final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		});

		layout.setHTML(0, 0, "Following groups will be moved (including all their sub-groups):");
		layout.getFlexCellFormatter().setColSpan(0, 0, 2);

		String items = "<ul>";
		for (Group g : groups) {
			items = items.concat("<li>" + g.getName() + "</li>");
		}
		items = items.concat("</ul>");

		ScrollPanel sp = new ScrollPanel();
		sp.setStyleName("border");
		sp.setSize("100%", "100px");
		sp.add(new HTML(items));

		layout.setWidget(1, 0, sp);
		layout.getFlexCellFormatter().setColSpan(1, 0, 2);

		// Add some standard form options
		layout.setHTML(2, 0, "Destination&nbsp;group:");
		layout.setWidget(2, 1, vosGroups);

		for (int i=2; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		layout.setHTML(3, 0, "Group(s) will be moved (including all their sub-groups) under destination group! If no destination group is selected, group will be moved to top-level.<p>We strongly recommend to move groups one by one.");
		layout.getFlexCellFormatter().setStyleName(3, 0, "inputFormInlineComment");
		layout.getFlexCellFormatter().setColSpan(3, 0, 2);


		vp.setWidth("400px");

		menu.addWidget(moveButton);
		menu.addWidget(cancelButton);

		vp.add(layout);
		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

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
		return SmallIcons.INSTANCE.addIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 2719;
		int result = 1;
		result = prime * result + 6786786;
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

		// FIXME - temporary only for perun admin
		if (session.isPerunAdmin()){
			return true;
		}/*
		else if (session.isVoAdmin((vo != null) ? vo.getId() : group.getVoId())) {
			return true;
		}*/ else {
			return false;
		}

	}

}
