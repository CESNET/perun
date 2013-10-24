package cz.metacentrum.perun.webgui.tabs.vostabs;

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
import cz.metacentrum.perun.webgui.json.registrarManager.*;
import cz.metacentrum.perun.webgui.model.ApplicationFormItem;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.registrartabs.MailsTabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.CopyFormTabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.CreateFormItemTabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.PreviewFormTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * VO Applications
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class VoApplicationFormSettingsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading VO application form");

	// data
	private VirtualOrganization vo;
	//data
	private int voId;
	
	// source list with items
	protected ArrayList<ApplicationFormItem> sourceList;

	
	/**
	 * Creates a tab instance
	 *
     * @param vo
     */
	public VoApplicationFormSettingsTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param voId
     */
	public VoApplicationFormSettingsTabItem(int voId){
		this.voId = voId;
        JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                vo = jso.cast();
            }
        };
        new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}
	
	public boolean isPrepared(){
		return !(vo == null);
	}

	public Widget draw() {

		// MAIN PANEL
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

        this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": "+"application form");

        // HORIZONTAL MENU
        final TabMenu menu = new TabMenu();
        vp.add(menu);
        vp.setCellHeight(menu, "30px");
		
		// request
		final GetFormItems itemsRequest = new GetFormItems(PerunEntity.VIRTUAL_ORGANIZATION, vo.getId(), true, new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				if (error.getName().equalsIgnoreCase("FormNotExistsException")) {
					// no form, add create button
					final CustomButton create = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createEmptyApplicationForm());
					create.addClickHandler(new ClickHandler(){
						public void onClick(ClickEvent event) {
							// disable button event with refresh page on finished
							CreateApplicationForm request = new CreateApplicationForm(PerunEntity.VIRTUAL_ORGANIZATION, voId, JsonCallbackEvents.disableButtonEvents(create, new JsonCallbackEvents(){
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
		
		// refresh table events
		final JsonCallbackEvents refreshEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				itemsRequest.prepareSettings(sourceList);
			}
		};

		// save button
		final CustomButton save = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveApplicationFormSettings());
		menu.addWidget(save);
        save.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				UpdateFormItems request = new UpdateFormItems(PerunEntity.VIRTUAL_ORGANIZATION, voId, new JsonCallbackEvents(){
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
		
		// add button
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addNewAppFormItem(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                session.getTabManager().addTabToCurrentTab(new CreateFormItemTabItem(sourceList, refreshEvents));
            }
        }));

		menu.addWidget(new CustomButton(ButtonTranslation.INSTANCE.copyFromVoButton(), ButtonTranslation.INSTANCE.copyFromVo(), SmallIcons.INSTANCE.copyIcon(), new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CopyFormTabItem(vo.getId(), 0));
			}
		}));
		
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.PREVIEW, ButtonTranslation.INSTANCE.previewAppForm(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                GeneralObject go = vo.cast();
                session.getTabManager().addTab(new PreviewFormTabItem(go, sourceList), true);
            }
        }));
		
		// AUTO APROVAL + NOTIFICATIONS
		
		// autoaproval widget already defined
		GetApplicationForm form = new GetApplicationForm(PerunEntity.VIRTUAL_ORGANIZATION, voId);
		form.setHidden(true);
		form.retrieveData();
		menu.addWidget(form.getApprovalWidget());
		
		menu.addWidget(new CustomButton(ButtonTranslation.INSTANCE.emailNotificationsButton(), ButtonTranslation.INSTANCE.emailNotifications(), SmallIcons.INSTANCE.emailIcon(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTab(new MailsTabItem(voId, 0));
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
		VoApplicationFormSettingsTabItem other = (VoApplicationFormSettingsTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}


	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
        session.getUiElements().getBreadcrumbs().setLocation(vo, "Application form", getUrlWithParameters());
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
	
	public final static String URL = "appl-form";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}
	
	static public VoApplicationFormSettingsTabItem load(Map<String, String> parameters)
	{
		int voId = Integer.parseInt(parameters.get("id"));
		return new VoApplicationFormSettingsTabItem(voId);
	}

}