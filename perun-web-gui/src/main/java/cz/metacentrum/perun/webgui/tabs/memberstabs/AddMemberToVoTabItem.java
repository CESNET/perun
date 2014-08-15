package cz.metacentrum.perun.webgui.tabs.memberstabs;

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
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.membersManager.CreateMember;
import cz.metacentrum.perun.webgui.json.registrarManager.SendInvitation;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.json.vosManager.FindCandidates;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.MembersTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Add member to VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AddMemberToVoTabItem implements TabItem, TabItemWithUrl {

	/**
	 * vo id
	 */
	private int voId;
	private VirtualOrganization vo;

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
	private Label titleWidget = new Label("Loading VO");

	private boolean searchCandidates = false;
	private String searchString = "";
	private CustomButton addCandidatesButton;
	private CustomButton addUsersButton;
	private ArrayList<GeneralObject> alreadyAddedList = new ArrayList<GeneralObject>();
	private SimplePanel alreadyAdded = new SimplePanel();

	/**
	 * Constructor
	 *
	 * @param voId ID of VO into which member should be added
	 */
	public AddMemberToVoTabItem(int voId){
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso)
			{
				vo = jso.cast();
			}
		};
		GetEntityById callback = new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events);
		callback.retrieveData();
	}

	public boolean isPrepared(){
		return !(vo == null);
	}

	/**
	 * Constructor
	 *
	 * @param vo VO into which member should be added
	 */
	public AddMemberToVoTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
	}

	public Widget draw() {

		titleWidget.setText("Add member(s)");

		// draw the main tab
		final VerticalPanel mainTab = new VerticalPanel();
		mainTab.setSize("100%", "100%");

		final TabMenu tabMenu = new TabMenu();
		mainTab.add(tabMenu);
		mainTab.setCellHeight(tabMenu, "30px");

		addCandidatesButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedCandidateToVo());
		addUsersButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedCandidateToVo());

		final CheckBox searchExternal = new CheckBox("Search in external sources");
		searchExternal.setValue(searchCandidates);

		// jsonCallback to get candidates
		final FindCandidates candidates = new FindCandidates(voId, "");
		final FindCompleteRichUsers users = new FindCompleteRichUsers("", null);
		users.findWithoutVo(true, voId);

		final CustomButton searchButton = new CustomButton("Search", SmallIcons.INSTANCE.findIcon());

		final CellTable<Candidate> candidatesTable = candidates.getEmptyTable();
		final CellTable<User> usersTable = users.getEmptyTable();
		final ScrollPanel scrollPanel = new ScrollPanel();

		final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				if (searchExternal.getValue()) {
					scrollPanel.setWidget(candidatesTable);
					candidates.searchFor(text);
				} else {
					scrollPanel.setWidget(usersTable);
					users.searchFor(text);
				}
			}
		}, searchButton);
		searchBox.getTextBox().setText(searchString);

		searchExternal.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
				searchCandidates = searchExternal.getValue();
				searchString = searchBox.getTextBox().getText().trim();
				if (searchExternal.getValue()) {
					scrollPanel.setWidget(candidatesTable);
					candidates.searchFor(searchString);
				} else {
					scrollPanel.setWidget(usersTable);
					users.searchFor(searchString);
				}
			}
		});

		// search candidate - select if found one
		JsonCallbackEvents selectOneEvent = JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(searchButton, JsonCallbackEvents.disableCheckboxEvents(searchExternal)),                new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				searchBox.getTextBox().setEnabled(true);
				if (searchCandidates) {
					// check in candidates table
					ArrayList<Candidate> array = JsonUtils.jsoAsList(jso);
					if (array != null && array.size() == 1) {
						candidates.setSelected(array.get(0));
					}
					tabMenu.addWidget(2, addCandidatesButton);
				} else {
					// check in users table
					ArrayList<User> array = JsonUtils.jsoAsList(jso);
					if (array != null && array.size() == 1) {
						users.setSelected(array.get(0));
					}
					tabMenu.addWidget(2, addUsersButton);
				}
			}
		@Override
		public void onLoadingStart() {
			searchBox.getTextBox().setEnabled(false);
		}
		@Override
		public void onError(PerunError error) {
			searchBox.getTextBox().setEnabled(true);
		}
		});
		// set event for search
		candidates.setEvents(selectOneEvent);
		users.setEvents(selectOneEvent);

		if (searchCandidates) {
			tabMenu.addWidget(2, addCandidatesButton);
		} else {
			tabMenu.addWidget(2, addUsersButton);
		}

		CustomButton invite = new CustomButton("Invite user(s)", SmallIcons.INSTANCE.emailIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (searchCandidates) {
					for (Candidate candid : candidates.getTableSelectedList()) {
						SendInvitation invite = new SendInvitation(voId, 0);
						invite.inviteUser(candid);
					}
				} else {
					for (User usrs : users.getTableSelectedList()) {
						SendInvitation invite = new SendInvitation(voId, 0);
						invite.inviteUser(usrs);
					}
				}
			}
		});
		tabMenu.addWidget(invite);

		final TabItem tab = this;
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, !alreadyAddedList.isEmpty());
			}
		}));

		tabMenu.addWidget(searchExternal);

		// add candidate button
		addCandidatesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Candidate candidateToBeAdded = candidates.getSelected();
				if (candidateToBeAdded == null) {
					UiElements.cantSaveEmptyListDialogBox(null);
				} else {
					CreateMember request = new CreateMember(JsonCallbackEvents.disableButtonEvents(addCandidatesButton, new JsonCallbackEvents(){
						private Candidate saveSelected;
						@Override
						public void onFinished(JavaScriptObject jso) {
							// put names to already added
							if (saveSelected != null) {
								GeneralObject go = saveSelected.cast();
								alreadyAddedList.add(go);
							}
							candidates.clearTableSelectedSet();
							rebuildAlreadyAddedWidget();
							// clear search
							searchBox.getTextBox().setText("");
						}
					@Override
					public void onLoadingStart(){
						saveSelected = candidates.getSelected();
					}
					}));
					request.createMember(voId, candidateToBeAdded);
				}
			}});

		addUsersButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<User> selected = users.getTableSelectedList();
				if(UiElements.cantSaveEmptyListDialogBox(selected)){
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
					for (int i=0; i<selected.size(); i++) {
						final int n = i;
						CreateMember request = new CreateMember(JsonCallbackEvents.disableButtonEvents(addUsersButton, new JsonCallbackEvents(){
							private User saveSelected;
							@Override
							public void onFinished(JavaScriptObject jso) {
								// put names to already added
								if (saveSelected != null) {
									GeneralObject go = saveSelected.cast();
									alreadyAddedList.add(go);
									users.getSelectionModel().setSelected(saveSelected, false);
									rebuildAlreadyAddedWidget();
									// clear search
									searchBox.getTextBox().setText("");
								}
							}
						@Override
						public void onLoadingStart(){
							saveSelected = selected.get(n);
						}
						}));
						request.createMember(voId, selected.get(i));
					}
				}
			}
		});

		addUsersButton.setEnabled(false);
		JsonUtils.addTableManagedButton(users, usersTable, addUsersButton);

		// tables
		candidatesTable.addStyleName("perun-table");
		usersTable.addStyleName("perun-table");
		if (searchCandidates) {
			scrollPanel.add(candidatesTable);
		} else {
			scrollPanel.add(usersTable);
		}
		scrollPanel.addStyleName("perun-tableScrollPanel");

		// load if stored search string is not empty
		if (searchCandidates) {
			candidates.searchFor(searchString);
		} else {
			users.searchFor(searchString);
		}

		rebuildAlreadyAddedWidget();
		mainTab.add(alreadyAdded);

		// style
		// do not use resizePerunTable() when tab is in overlay - wrong width is calculated
		session.getUiElements().resizeSmallTabPanel(scrollPanel, 350, this);
		mainTab.add(scrollPanel); // add table to page

		this.contentWidget.setWidget(mainTab);

		return getWidget();
	}

	/**
	 * Rebuild already added widget based on already added members
	 */
	private void rebuildAlreadyAddedWidget() {

		alreadyAdded.setStyleName("alreadyAdded");
		alreadyAdded.setVisible(!alreadyAddedList.isEmpty());
		alreadyAdded.setWidget(new HTML("<strong>Already added: </strong>"));
		for (int i=0; i<alreadyAddedList.size(); i++) {

			if (alreadyAddedList.get(i).getObjectType().equals("Candidate")) {
				Candidate c = alreadyAddedList.get(i).cast();
				alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML()+ ((i!=0) ? ", " : "") + c.getFullName());
			} else {
				User u = alreadyAddedList.get(i).cast();
				alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") + u.getFullName());
			}
		}

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
		final int prime = 31;
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
		AddMemberToVoTabItem other = (AddMemberToVoTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "add-to-vo";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?vo=" + voId;
	}

	static public AddMemberToVoTabItem load(Map<String, String> parameters) {
		int gid = Integer.parseInt(parameters.get("vo"));
		return new AddMemberToVoTabItem(gid);
	}

}
