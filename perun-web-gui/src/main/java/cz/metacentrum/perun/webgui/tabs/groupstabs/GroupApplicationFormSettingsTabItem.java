package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.registrarManager.CreateApplicationForm;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationForm;
import cz.metacentrum.perun.webgui.json.registrarManager.GetFormItems;
import cz.metacentrum.perun.webgui.json.registrarManager.UpdateFormItems;
import cz.metacentrum.perun.webgui.model.ApplicationFormItem;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.registrartabs.CopyFormTabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.CreateFormItemTabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.MailsTabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.PreviewFormTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Group Application form settings
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class GroupApplicationFormSettingsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading Group application form");

	// data
	private Group group;
	//data
	private int groupId;
	
	// source list with items
	protected ArrayList<ApplicationFormItem> sourceList;

	
	/**
	 * Creates a tab instance
	 *
     * @param group
     */
	public GroupApplicationFormSettingsTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param groupId
     */
	public GroupApplicationFormSettingsTabItem(int groupId){
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

	public Widget draw() {

		// MAIN PANEL
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

        final TabMenu menu = new TabMenu();

        // request
		final GetFormItems itemsRequest = new GetFormItems(PerunEntity.GROUP, group.getId(), true, new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				if (error.getName().equalsIgnoreCase("FormNotExistsException")) {
					// no form, add create button
					final CustomButton create = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createEmptyApplicationForm());
					create.addClickHandler(new ClickHandler(){
						public void onClick(ClickEvent event) {
							// disable button event with refresh page on finished
							CreateApplicationForm request = new CreateApplicationForm(PerunEntity.GROUP, groupId, JsonCallbackEvents.disableButtonEvents(create, new JsonCallbackEvents(){
								@Override
								public void onFinished(JavaScriptObject jso) {
									draw(); // refresh page
								}
							}));
							request.createApplicationForm();
						}
					});
					
					FlexTable ft = new FlexTable();
					ft.setSize("100%", "300px");
					ft.setHTML(0, 0, "<h2>"+ WidgetTranslation.INSTANCE.formDoesntExists()+"</h2>");
					ft.setWidget(1, 0, create);
					ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
					ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
					ft.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
					ft.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_MIDDLE);
					
					vp.clear();
					vp.add(ft);
					
				}
			}
		});
		sourceList = itemsRequest.getList();

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName())+": "+"application form");

		// HORIZONTAL MENU
		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		
		// refresh table events
		final JsonCallbackEvents refreshEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				itemsRequest.prepareSettings(sourceList);
			}
		};

		// save button
		final CustomButton save = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveApplicationFormSettings());
		save.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				UpdateFormItems request = new UpdateFormItems(PerunEntity.GROUP, groupId, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						itemsRequest.retrieveData();
						save.setProcessing(false);
					}
					@Override
					public void onLoadingStart() {
                        save.setProcessing(true);
					}
					@Override
					public void onError(PerunError error) {
                        save.setProcessing(false);
					}
				});
				// reset item ordnum to correct state defined by list
				int counter = 0; // keep counter
				// process
				for (int i=0; i<itemsRequest.getList().size(); i++) {
					// if not for deletion
					if (!itemsRequest.getList().get(i).isForDelete()) {
						// set
						itemsRequest.getList().get(i).setOrdnum(counter);
						counter++;
					}
				}
				// send request
				request.updateFormItems(itemsRequest.getList());	
				
			}
		});
        menu.addWidget(save);
		
		// add button
		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addNewAppFormItem(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                session.getTabManager().addTabToCurrentTab(new CreateFormItemTabItem(sourceList, refreshEvents));
            }
        });
        menu.addWidget(addButton);

        menu.addWidget(new CustomButton(ButtonTranslation.INSTANCE.copyFromGroupButton(), ButtonTranslation.INSTANCE.copyFromGroup(), SmallIcons.INSTANCE.copyIcon(), new ClickHandler(){
            public void onClick(ClickEvent event) {
                session.getTabManager().addTabToCurrentTab(new CopyFormTabItem(group.getVoId(), groupId));
            }
        }));

        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.PREVIEW, ButtonTranslation.INSTANCE.previewAppForm(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                GeneralObject go = group.cast();
                session.getTabManager().addTab(new PreviewFormTabItem(go, sourceList), true);
            }
        }));
		
		// AUTO APROVAL + NOTIFICATIONS
		
		// autoaproval widget already defined
		GetApplicationForm form = new GetApplicationForm(PerunEntity.GROUP, groupId);
		form.setHidden(true);
		form.retrieveData();
		menu.addWidget(form.getApprovalWidget());

        menu.addWidget(new CustomButton(ButtonTranslation.INSTANCE.emailNotificationsButton(), ButtonTranslation.INSTANCE.emailNotifications(), SmallIcons.INSTANCE.emailIcon(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                session.getTabManager().addTab(new MailsTabItem(group.getVoId(), group.getId()));
            }
        }));
		
		// load elements
		itemsRequest.retrieveData();
		
		// wrap table to the scroll panel
		ScrollPanel sp = new ScrollPanel(itemsRequest.getContents());
		sp.addStyleName("perun-tableScrollPanel");		
		session.getUiElements().resizePerunTable(sp, 100, this);
		
		// add scroll table to the main panel
		vp.add(sp);
		
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
		return SmallIcons.INSTANCE.applicationFormIcon(); 
	}

	@Override
	public int hashCode() {
		final int prime = 51;
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
		GroupApplicationFormSettingsTabItem other = (GroupApplicationFormSettingsTabItem) obj;
		if (groupId != other.groupId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}


	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
        session.getUiElements().getBreadcrumbs().setLocation(group, "Application form", getUrlWithParameters());
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}

	public boolean isAuthorized() {
		
		if (session.isVoAdmin() || session.isGroupAdmin(groupId)) {
			return true; 
		} else {
			return false;
		}

	}
	
	public final static String URL = "appl-form";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?group=" + groupId;
	}
	
	static public GroupApplicationFormSettingsTabItem load(Map<String, String> parameters)
	{
		int groupId = Integer.parseInt(parameters.get("group"));
		return new GroupApplicationFormSettingsTabItem(groupId);
	}

}