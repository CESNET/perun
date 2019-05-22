package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.membersManager.DeleteMember;
import cz.metacentrum.perun.webgui.json.membersManager.FindCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.membersManager.GetCompleteRichMembers;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToVoTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.CreateServiceMemberInVoTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * VO Members page
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VoMembersTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading VO members");

	// data
	private VirtualOrganization vo;

	// when searching
	private String searchString = "";
	private boolean search = true;
	private boolean wasDisabled = false;
	private int voId;

	/**
	 * Creates a tab instance
	 *
	 * @param vo
	 */
	public VoMembersTabItem(VirtualOrganization vo) {
		this.vo = vo;
		this.voId = vo.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param voId
	 */
	public VoMembersTabItem(int voId) {
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}

	public boolean isPrepared() {
		return !(vo == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// SET TAB NAME
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName()) + ": members");

		// MAIN PANEL
		final VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// MENU
		TabMenu tabMenu = new TabMenu();
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		// DISABLED CHECKBOX
		final CheckBox disabled = new CheckBox(WidgetTranslation.INSTANCE.showDisabledMembers());
		disabled.setTitle(WidgetTranslation.INSTANCE.showDisabledMembersTitle());
		disabled.setValue(wasDisabled);
		disabled.setVisible(false);

		// CALLBACKS
		final GetCompleteRichMembers members = new GetCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, voId, null);
		final FindCompleteRichMembers findMembers = new FindCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, voId, "", null);
		members.excludeDisabled(!wasDisabled);

		final CustomButton searchButton = TabMenu.getPredefinedButton(ButtonType.SEARCH, ButtonTranslation.INSTANCE.searchMemberInVo());
		final CustomButton listAllButton = TabMenu.getPredefinedButton(ButtonType.LIST_ALL_MEMBERS, ButtonTranslation.INSTANCE.listAllMembersInVo());
		if (!session.isVoAdmin(voId)) findMembers.setCheckable(false);

		final CellTable<RichMember> table = findMembers.getEmptyTable(new FieldUpdater<RichMember, RichMember>() {
			// when user click on a row -> open new tab
			public void update(int index, RichMember object, RichMember value) {
				session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), 0));
			}
		});

		// refresh
		tabMenu.addWidget(UiElements.getRefreshButton(this));

		// ADD
		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addMemberToVo(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddMemberToVoTabItem(voId), true);
			}
		});
		if (!session.isVoAdmin(voId)) addButton.setEnabled(false);
		tabMenu.addWidget(addButton);

		// REMOVE
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeMemberFromVo());
		if (!session.isVoAdmin(voId)) removeButton.setEnabled(false);
		tabMenu.addWidget(removeButton);

		/*
		final CustomButton addServiceButton = new CustomButton(ButtonTranslation.INSTANCE.createServiceMemberButton(), ButtonTranslation.INSTANCE.createServiceMember(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new CreateServiceMemberInVoTabItem(vo));
			}
		});
		if (!session.isVoAdmin(voId)) addServiceButton.setEnabled(false);
		tabMenu.addWidget(addServiceButton);
		*/

		// refreshMembers
		final JsonCallbackEvents refreshEvent = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				if (search) {
					findMembers.searchFor(searchString);
				} else {
					findMembers.clearTable();
					members.retrieveData();
				}
			}
		};

		// add click handler for remove button
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// state specific events
				final ArrayList<RichMember> membersForRemoving = findMembers.getTableSelectedList();
				String text = "Following members will be removed from VO and their settings will be lost.<p>You can consider changing their status to \"DISABLED\", which will prevent them from accessing VO resources.";
				UiElements.showDeleteConfirm(membersForRemoving, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i = 0; i < membersForRemoving.size(); i++) {
							DeleteMember request;
							if (i == membersForRemoving.size() - 1) {
								request = new DeleteMember(JsonCallbackEvents.disableButtonEvents(removeButton, refreshEvent));
							} else {
								request = new DeleteMember(JsonCallbackEvents.disableButtonEvents(removeButton));
							}
							request.deleteMember(membersForRemoving.get(i).getId());
						}
					}
				});
			}
		});

		final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			public void searchFor(String text) {
				searchString = text;
				search = true;
				findMembers.searchFor(text);
			}
		}, searchButton);
		searchBox.getTextBox().setText(searchString);

		// checkbox click handler
		disabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				wasDisabled = disabled.getValue();
				if (search) {
					// case when update but not triggered by button
					searchString = searchBox.getTextBox().getText();
					members.excludeDisabled(!disabled.getValue());
				} else {
					members.excludeDisabled(!disabled.getValue());
					members.retrieveData();
				}
			}
		});

		findMembers.setEvents(JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(searchButton, JsonCallbackEvents.disableCheckboxEvents(disabled)),
				new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						searchBox.getTextBox().setEnabled(true);
						listAllButton.setEnabled(true);
					}

					@Override
					public void onError(PerunError error) {
						searchBox.getTextBox().setEnabled(true);
						listAllButton.setEnabled(true);
					}

					@Override
					public void onLoadingStart() {
						searchBox.getTextBox().setEnabled(false);
						listAllButton.setEnabled(false);
						disabled.setVisible(false);
					}
				}
		));

		members.setEvents(JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(listAllButton, JsonCallbackEvents.disableCheckboxEvents(disabled)),
				new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						// pass data to table handling callback
						findMembers.onFinished(jso);
						((AjaxLoaderImage) table.getEmptyTableWidget()).setEmptyResultMessage("VO has no members.");
						searchBox.getTextBox().setEnabled(true);
						searchButton.setEnabled(true);
					}

					@Override
					public void onError(PerunError error) {
						// pass data to table handling callback
						findMembers.onError(error);
						searchBox.getTextBox().setEnabled(true);
						searchButton.setEnabled(true);
					}

					@Override
					public void onLoadingStart() {
						searchBox.getTextBox().setEnabled(false);
						searchButton.setEnabled(false);
						disabled.setVisible(true);
						// to show progress when reloading
						((AjaxLoaderImage)table.getEmptyTableWidget()).loadingStart();
					}
				}
		));

		// LIST ALL BUTTON
		listAllButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				search = false;
				searchString = "";
				searchBox.getTextBox().setText("");
				findMembers.clearTable();
				members.retrieveData();
			}
		});
		tabMenu.addWidget(listAllButton);

		tabMenu.addWidget(disabled);

		/* WHEN TAB RELOADS, CHECK THE STATE */
		if (search) {
			findMembers.searchFor(searchString);
		} else {
			members.excludeDisabled(!disabled.getValue());
			members.retrieveData();
		}

		ScrollPanel tableWrapper = new ScrollPanel();
		table.addStyleName("perun-table");
		tableWrapper.setWidget(table);
		tableWrapper.addStyleName("perun-tableScrollPanel");

		session.getUiElements().resizePerunTable(tableWrapper, 350, this);

		// add menu and the table to the main panel
		firstTabPanel.add(tableWrapper);

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(findMembers, table, removeButton);

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
		return SmallIcons.INSTANCE.userGreenIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1609;
		int result = 1;
		result = prime * result + voId;
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
		VoMembersTabItem other = (VoMembersTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(vo, "Members", getUrlWithParameters());
		if (vo != null) {
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isVoObserver(voId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "members";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}

	static public VoMembersTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("id"));
		return new VoMembersTabItem(voId);
	}

	static public VoMembersTabItem load(VirtualOrganization vo) {
		return new VoMembersTabItem(vo);
	}

}
