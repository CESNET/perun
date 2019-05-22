package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
import cz.metacentrum.perun.webgui.json.vosManager.GetCompleteCandidates;
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

	private String searchString = "";
	private CustomButton addCandidatesButton;
	private CustomButton inviteCandidatesButton;
	private ArrayList<MemberCandidate> alreadyAddedList = new ArrayList<MemberCandidate>();
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

	@Override
	public boolean isRefreshParentOnClose() {
		return !alreadyAddedList.isEmpty();
	}

	@Override
	public void onClose() {

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
		inviteCandidatesButton = new CustomButton("Invite selected", SmallIcons.INSTANCE.emailIcon());

		final GetCompleteCandidates findAll = new GetCompleteCandidates(voId, 0, "");

		final CustomButton searchButton = new CustomButton("Search", SmallIcons.INSTANCE.findIcon());

		final CellTable<MemberCandidate> candidatesTable = findAll.getEmptyTable();
		final ScrollPanel scrollPanel = new ScrollPanel();

		final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				findAll.searchFor(text);
			}
		}, searchButton);

		searchBox.getTextBox().setText(searchString);

		// search candidate - select if found one
		JsonCallbackEvents selectOneEvent = JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(searchButton), new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				searchBox.getTextBox().setEnabled(true);
				// check in candidates table
				if (findAll.getList().size() == 1) {
						if (findAll.getList().get(0).getMember() == null) {
							// select first if selectable
							findAll.setSelected(findAll.getList().get(0));
						}
				}
				tabMenu.addWidget(2, addCandidatesButton);
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
		findAll.setEvents(selectOneEvent);

		tabMenu.addWidget(2, addCandidatesButton);
		tabMenu.addWidget(3, inviteCandidatesButton);

		inviteCandidatesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// we expect, that candidate is always single
				MemberCandidate candid = findAll.getSelected();
				if (candid != null) {

					if (candid.getCandidate() != null) {
						SendInvitation invite = new SendInvitation(voId, 0);
						invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteCandidatesButton, new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								findAll.clearTableSelectedSet();
							}
						}));
						invite.inviteUser(candid.getCandidate());
					} else {
						SendInvitation invite = new SendInvitation(voId, 0);
						invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteCandidatesButton, new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								findAll.clearTableSelectedSet();
							}
						}));
						User user = candid.getRichUser();
						invite.inviteUser(user);
					}
				}
			}
		});

		final TabItem tab = this;
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// add candidate button
		addCandidatesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				MemberCandidate candidateToBeAdded = findAll.getSelected();
				if (candidateToBeAdded == null) {
					UiElements.cantSaveEmptyListDialogBox(null);
				} else {
					if (candidateToBeAdded.getCandidate() != null) {

						CreateMember request = new CreateMember(JsonCallbackEvents.disableButtonEvents(addCandidatesButton, new JsonCallbackEvents() {
							private MemberCandidate saveSelected;

							@Override
							public void onFinished(JavaScriptObject jso) {
								// put names to already added
								if (saveSelected != null) {
									alreadyAddedList.add(saveSelected);
								}
								findAll.clearTableSelectedSet();
								rebuildAlreadyAddedWidget();
								// clear search
								searchBox.getTextBox().setText("");
							}

							@Override
							public void onLoadingStart() {
								saveSelected = findAll.getSelected();
							}
						}));
						request.createMember(voId, candidateToBeAdded.getCandidate());

					} else {

						CreateMember request = new CreateMember(JsonCallbackEvents.disableButtonEvents(addCandidatesButton, new JsonCallbackEvents(){
							private MemberCandidate saveSelected;
							@Override
							public void onFinished(JavaScriptObject jso) {
								// put names to already added
								if (saveSelected != null) {
									alreadyAddedList.add(saveSelected);
									findAll.clearTableSelectedSet();
									rebuildAlreadyAddedWidget();
									// clear search
									searchBox.getTextBox().setText("");
								}
							}
							@Override
							public void onLoadingStart(){
								saveSelected = findAll.getSelected();
							}
						}));
						User user = candidateToBeAdded.getRichUser();
						request.createMember(voId, user);

					}

				}
			}});

		addCandidatesButton.setEnabled(false);
		JsonUtils.addTableManagedButton(findAll, candidatesTable, addCandidatesButton);
		inviteCandidatesButton.setEnabled(false);
		JsonUtils.addTableManagedButton(findAll, candidatesTable, inviteCandidatesButton);

		// tables
		candidatesTable.addStyleName("perun-table");
		scrollPanel.add(candidatesTable);
		scrollPanel.addStyleName("perun-tableScrollPanel");

		// load if stored search string is not empty
		findAll.searchFor(searchString);

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
			MemberCandidate c = alreadyAddedList.get(i).cast();
			if (c.getRichUser() != null) {
				alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML()+ ((i!=0) ? ", " : "") + SafeHtmlUtils.fromString(c.getRichUser().getFullName()).asString());
			} else {
				alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") + SafeHtmlUtils.fromString(c.getCandidate().getFullName()).asString());
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
		final int prime = 859;
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
