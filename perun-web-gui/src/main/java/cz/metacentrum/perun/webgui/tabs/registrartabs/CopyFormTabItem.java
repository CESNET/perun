package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.registrarManager.CopyForm;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Tab which allow you to copy application form from VO to VO
 * !!! USE AS INNER TAB ONLY !!!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CopyFormTabItem implements TabItem {

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
	private Label titleWidget = new Label("Copy form items");

	/**
	 * Entity ID to set
	 */
	private int voId = 0;
	private int groupId = 0;

	/**
	 * Creates a tab instance
	 *
	 * @param voId
	 * @param groupId
	 */
	public CopyFormTabItem(int voId, int groupId){
		this.voId = voId;
		this.groupId = groupId;
	}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		final FlexTable content = new FlexTable();
		content.setStyleName("inputFormFlexTable");

		// boxes
		final ListBoxWithObjects<VirtualOrganization> vosBox = new ListBoxWithObjects<VirtualOrganization>();
		final ListBoxWithObjects<Group> groupsBox = new ListBoxWithObjects<Group>();

		final CustomButton save;

		final TabItem tab = this;

		VerticalPanel vp = new VerticalPanel();
		TabMenu menu = new TabMenu();

		titleWidget.setText("Copy form items from VO / group");

		save = TabMenu.getPredefinedButton(ButtonType.OK, ButtonTranslation.INSTANCE.copyFromVo());

		// get them
		final GetVos vos = new GetVos(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				vosBox.clear();
				ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
				vos = new TableSorter<VirtualOrganization>().sortByName(vos);
				vosBox.addAllItems(vos);

				// get them
				GetAllGroups getGroups = new GetAllGroups(voId, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						groupsBox.clear();
						ArrayList<Group> groups = JsonUtils.jsoAsList(jso);
						groups = new TableSorter<Group>().sortByName(groups);
						groupsBox.addNotSelectedOption();
						groupsBox.addAllItems(groups);
						save.setEnabled(true);
					}
					@Override
					public void onError(PerunError error) {
						groupsBox.removeNotSelectedOption();
						groupsBox.clear();
						groupsBox.addItem("Error while loading");
						save.setEnabled(false);
					}
					@Override
					public void onLoadingStart(){
						groupsBox.removeNotSelectedOption();
						groupsBox.clear();
						groupsBox.addItem("Loading...");
						save.setEnabled(false);
					}
				});
				getGroups.retrieveData();

			}
			@Override
			public void onError(PerunError error) {
				vosBox.addItem("Error while loading");
				save.setEnabled(false);
			}
			@Override
			public void onLoadingStart(){
				vosBox.addItem("Loading...");
				save.setEnabled(false);
			}
		});
		vos.retrieveData();

		vosBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {

				if (vosBox.getSelectedObject() != null) {

					// get them
					GetAllGroups getGroups = new GetAllGroups(vosBox.getSelectedObject().getId(), new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso) {
							groupsBox.clear();
							ArrayList<Group> groups = JsonUtils.jsoAsList(jso);
							groups = new TableSorter<Group>().sortByName(groups);
							groupsBox.addNotSelectedOption();
							groupsBox.addAllItems(groups);
							save.setEnabled(true);
						}
						@Override
						public void onError(PerunError error) {
							groupsBox.removeNotSelectedOption();
							groupsBox.clear();
							groupsBox.addItem("Error while loading");
							save.setEnabled(false);
						}
						@Override
						public void onLoadingStart(){
							groupsBox.removeNotSelectedOption();
							groupsBox.clear();
							groupsBox.addItem("Loading...");
							save.setEnabled(false);
						}
					});
					getGroups.retrieveData();

				}

			}
		});

		content.setHTML(0, 0, "Source VO:");
		content.getFlexCellFormatter().setStyleName(0, 0, "itemName");
		content.setWidget(0, 1, vosBox);
		content.setHTML(1, 0, "Source group:");
		content.getFlexCellFormatter().setStyleName(1, 0, "itemName");
		content.setWidget(1, 1, groupsBox);

		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {

				CopyForm request = null;
				if (groupsBox.getSelectedIndex() != 0 && voId != 0 && groupId == 0) {

					// from group to VO
					request = new CopyForm(PerunEntity.GROUP, groupsBox.getSelectedObject().getId(), PerunEntity.VIRTUAL_ORGANIZATION, voId, JsonCallbackEvents.closeTabDisableButtonEvents(save, tab));

				} else if (groupsBox.getSelectedIndex() != 0 && voId != 0 && groupId != 0) {

					// from group to group
					request = new CopyForm(PerunEntity.GROUP, groupsBox.getSelectedObject().getId(), PerunEntity.GROUP, groupId, JsonCallbackEvents.closeTabDisableButtonEvents(save, tab));

				} else if (groupsBox.getSelectedIndex() == 0 && voId != 0 && groupId == 0) {

					// from VO to VO
					request = new CopyForm(PerunEntity.VIRTUAL_ORGANIZATION, vosBox.getSelectedObject().getId(), PerunEntity.VIRTUAL_ORGANIZATION, voId, JsonCallbackEvents.closeTabDisableButtonEvents(save, tab));

				} else if (groupsBox.getSelectedIndex() == 0 && voId != 0 && groupId != 0) {

					// from VO to group
					request = new CopyForm(PerunEntity.VIRTUAL_ORGANIZATION, vosBox.getSelectedObject().getId(), PerunEntity.GROUP, groupId, JsonCallbackEvents.closeTabDisableButtonEvents(save, tab));

				}

				request.copyForm();

			}
		});

		content.setHTML(2, 0, "All form items will be added to yours.");
		content.getFlexCellFormatter().setStyleName(2, 0, "inputFormInlineComment");
		content.getFlexCellFormatter().setColSpan(2, 0, 2);

		menu.addWidget(save);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		}));

		vp.add(content);
		vp.add(menu);
		vp.setCellHeight(menu, "30px");
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
		final int prime = 971;
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
		CopyFormTabItem create = (CopyFormTabItem) obj;
		if (voId != create.voId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		// no open for inner tab
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}
	}

}