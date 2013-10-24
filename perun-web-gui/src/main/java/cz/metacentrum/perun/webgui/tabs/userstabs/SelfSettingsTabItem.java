package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.GetRequiredAttributes;
import cz.metacentrum.perun.webgui.json.membersManager.GetMemberByUser;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAllowedResources;
import cz.metacentrum.perun.webgui.json.usersManager.GetVosWhereUserIsMember;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.userstabs.RequestQuotaChangeTabItem.QuotaType;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab with user's settings for User
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class SelfSettingsTabItem implements TabItem, TabItemWithUrl, TabItemWithHelp{

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
	private Label titleWidget = new Label("Loading user");

	/**
	 * Search string
	 */
	private User user;
	private int userId;

	// virtual organization which display settings for
	private VirtualOrganization vo;

	private int voId = 0;
	
	/**
	 * Creates a tab instance
     */
	public SelfSettingsTabItem(){
		this.user = session.getActiveUser();
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance
     * @param user
     */
	public SelfSettingsTabItem(User user){
		this.user = user;
		this.userId = user.getId();
	}
	
	/**
	 * Creates a tab instance
     */
	public SelfSettingsTabItem(int userId){
		this.userId = userId;
        new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                user = jso.cast();
            }
        }).retrieveData();
	}
	
	/**
	 * Creates a tab instance
     * @param user
     * @param vo
     */
	public SelfSettingsTabItem(User user, VirtualOrganization vo){
		this(user);
		this.vo = vo;
		this.voId = vo.getId();
	}
	
	/**
	 * Creates a tab instance
     * @param userId
     * @param voId
     */
	public SelfSettingsTabItem(int userId, int voId){
		this(userId);
		this.voId = voId;
        new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                vo = jso.cast();
            }
        }).retrieveData();
	}

	public boolean isPrepared(){
		if(user == null){
			return false;
		}
		
		if(voId == 0){
			return true;
		}
		
		if(vo == null){
			return false;
		}
		
		return true;
	}
	
	
	/** 
	 * Loads all VOs for the user.
	 * Beneath all VOs, the list of resources is shown
	 * 
	 * @return
	 */
	private Widget loadVosForUser()
	{
		final VerticalPanel vp = new VerticalPanel();
		
		final ScrollPanel scroll = new ScrollPanel();
		scroll.setWidget(vp);
		scroll.setStyleName("perun-tableScrollPanel");
		session.getUiElements().resizeSmallTabPanel(scroll, 350, this);
		scroll.setWidth("100%");
		
		
		vp.add(new HTML("Loading virtual organizations"));
		vp.add(new AjaxLoaderImage());
		
		
		
		// RETRIEVES ALL VOS WHERE USER IS A MEMBER
		GetVosWhereUserIsMember vosRequest = new GetVosWhereUserIsMember(userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){

				vp.clear();
				
				// ITERATION VIA VOS
				ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
				
				// if vos length = 1, load immediately all resources
				final boolean loadImmediately = (vos.size() == 1); 
				
				// FOR EACH VO, FIND THE MEMBER
				for(final VirtualOrganization vo : vos)
				{
					GetMemberByUser memberRequest = new GetMemberByUser(vo.getId(), userId, new JsonCallbackEvents(){
						
						public void onFinished(JavaScriptObject jso){
							
							Member member = jso.cast();
							
							// add the disclosure panel with VOs
							vp.add( userSettingsForVoDisclosurePanel(vo, member, loadImmediately));
						}
					});
					memberRequest.retrieveData();
				}
			}
		});
		
		vosRequest.retrieveData();
		
		return scroll;
	}
	
	
	/**
	 * Prepares a VO settings for the user
	 * 
	 * @param vo
	 * @return
	 */
	private Widget prepareVoForUser(final VirtualOrganization vo)
	{
		final ScrollPanel scroll = new ScrollPanel();
		scroll.setStyleName("perun-tableScrollPanel");
		session.getUiElements().resizeSmallTabPanel(scroll, 350, this);
		scroll.setWidth("100%");
		
		
		GetMemberByUser memberRequest = new GetMemberByUser(vo.getId(), userId, new JsonCallbackEvents(){
			
			public void onFinished(JavaScriptObject jso){
				
				Member member = jso.cast();
				
				// add the disclosure panel with VOs
				scroll.setWidget((userSettingsForVo(vo, member)));
			}
		});
		memberRequest.retrieveData();
		
		return scroll;
	}
	
	
	/**
	 * Disclosure panel with VO & its resources
	 * 
	 * @param vo
	 * @param member
	 * @param loadImmediately
	 * @return
	 */
	protected Widget userSettingsForVoDisclosurePanel(final VirtualOrganization vo, final Member member, final boolean loadImmediately)
	{
		// header
		final FlexTable header = new FlexTable();
		header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.buildingIcon()));
		header.setHTML(0, 1, "<h2 class=\"disclosurePanelHeader\">" + vo.getName() + "</h2>");					
		header.setTitle("Click to show resources for "+ vo.getName());
		
		// disclosure panel
		final DisclosurePanel settings = new DisclosurePanel();
		settings.setWidth("100%");
		settings.setHeader(header);		
		
		// load content on open
		settings.addOpenHandler(new OpenHandler<DisclosurePanel>(){
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				if (settings.getContent() == null) {
					settings.setContent(userSettingsForVo(vo, member)); // set content
				}
			}
		});
		
		
		settings.setOpen(loadImmediately);

		
		return settings;
	}
	
	
	/**
	 * User settings for the VO
	 * @param vo
	 * @param member
	 * @return
	 */
	protected Widget userSettingsForVo(VirtualOrganization vo, Member member)
	{
		final VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");
		
		GetAllowedResources resourcesCallback = new GetAllowedResources(member.getId(), new JsonCallbackEvents(){
		
			public void onLoadingStart()
			{
				vp.clear();
				vp.add(new AjaxLoaderImage());
				
			}
			
			public void onFinished(JavaScriptObject jso) {
				vp.clear();
				
				// convert & sort
				ArrayList<Resource> resources = JsonUtils.jsoAsList(jso);
				resources = new TableSorter<Resource>().sortByDescription(resources);
				// if empty
				if (resources.isEmpty() || resources == null) {
					vp.add(new HTML("<p>No resources under this VO available.</p>"));
					return;
				}
				// process
				for (Resource r : resources) {
					DisclosurePanel settings = new DisclosurePanel();
					settings.setWidth("100%");

					final FlexTable header = new FlexTable();
					header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.databaseServerIcon()));
					header.setHTML(0, 1, "<h3>" + r.getDescription() + " (" +  r.getName() + ")</h3>");					
					header.setTitle("Click to show setting for resource "+r.getName());
					settings.setHeader(header);
					vp.add(loadSettings(settings, r)); // load content for each resource
				}
			}
		});
		
		resourcesCallback.retrieveData();
		
		return vp;
	}
	
	
	
	public Widget draw() {

		
		
		if(vo == null)
		{
			this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()) + ": settings");
			this.contentWidget.setWidget(loadVosForUser());
		}else{
			this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()) + ": settings for " + vo.getName());
			this.contentWidget.setWidget(prepareVoForUser(vo));
			
		}
		
		
		return getWidget();
		
	}
	

	/**
	 * Help widget
	 * @return
	 */
    public Widget getHelpWidget()
    {
    	String help = "";
    	
    	help += "<p>" + "The resources are grouped by virtual organizations - click on a VO to see the resources." + "</p>";
    	help += "<p><strong>" + "Click on a resource to change its settings:" + "</strong></p>";
    	help += "<ul>";
    	help += "   <li>Shell</li>";
    	help += "</ul>";
    	help += "<p><strong>" + "Or you can request quota change:" + "</strong></p>";
    	help += "<ul>";
    	help += "   <li>Data quota</li>";
    	help += "   <li>Files quota</li>";
    	help += "</ul>";
    	
    	
    	/*
    	FlexTable ft = new FlexTable();
    	ft.setWidget(0, 0, new HTML());*/
    	
    	return new HTML(help);
    }
	
	
	
	/**
	 * Load settings for each resource (loaded only once onClick on header)
	 * 
	 * @return settings content for each resource
	 */
	private Widget loadSettings(final DisclosurePanel settings, final Resource resource) {
		
		// create content table
		final FlexTable layoutx = new FlexTable();
		layoutx.setCellSpacing(7);
		
		// layout ROWS
		// 0 - shell
		// 1 - data quota
		// 2 - files quota
		
		// layout COLUMNS
		// 0 - title
		// 1 - value
		// 2 - change button
		
		
		// get member
		final GetMemberByUser callMember = new GetMemberByUser(resource.getVoId(), userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				// member
				final Member mem = jso.cast();
				// set ids
				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("member", mem.getId());
				ids.put("resource", resource.getId());
				ids.put("resourceToGetServicesFrom", resource.getId()); // to filter empty values
				ids.put("workWithUserAttributes", 1);
				
				// get req. attrs - to filter eg. if quota is used or not
				final GetRequiredAttributes attributes = new GetRequiredAttributes(ids, new JsonCallbackEvents(){
					public void onLoadingStart() {
						//AjaxLoaderImage loader = new AjaxLoaderImage(true);
						
						//layoutx.setWidget(0, 0, loader.loadingStart());
					}
					public void onError(PerunError error) {
						AjaxLoaderImage loader = new AjaxLoaderImage(true);
						layoutx.setWidget(0, 0, loader.loadingError(error));
					}
					public void onFinished(JavaScriptObject jso) {
						// fill layout
						ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
						boolean empty = true; // check if any attributes displayed
						layoutx.getWidget(0, 0).removeFromParent(); // remove loading image
					
						for (final Attribute a : attrs) {
							if (a.getFriendlyName().equalsIgnoreCase("shell")) {
								layoutx.setHTML(0, 0, "<strong>Shell: </strong>");
					
								final HTML shellWrapper = new HTML(a.getValue());
								layoutx.setWidget(0, 1, shellWrapper);
								empty = false;
								// change button
								CustomButton cb = new CustomButton("Change",SmallIcons.INSTANCE.cogIcon());
								layoutx.setWidget(0, 2, cb);
								// click handler
								cb.addClickHandler(new ClickHandler() {
									public void onClick(ClickEvent event) {



										session.getTabManager().addTabToCurrentTab(new ShellChangeTabItem(resource, userId, a, shellWrapper));
										
									}
								});
							} else if (a.getFriendlyName().equalsIgnoreCase("dataLimit")) {
								
								final CustomButton quotaChangeButton = new CustomButton("Request change", SmallIcons.INSTANCE.databaseIcon());

								// display value
								layoutx.setHTML(1, 0, "<strong>Data quota: </strong>");	
				
								// get default
								Map<String, Integer> ids = new HashMap<String, Integer>();
								ids.put("resource", resource.getId());
								GetAttributes defaultAttr = new GetAttributes(new JsonCallbackEvents(){
									public void onError(PerunError error) {
										if (a.getValue().equalsIgnoreCase("null")) {
											layoutx.setHTML(1, 1, "Using default (default: error while loading)");
										} else {
											layoutx.setHTML(1, 1, String.valueOf(a.getValue())+" (default: error while loading)");
										}
                                        quotaChangeButton.addClickHandler(new ClickHandler() {
                                            public void onClick(ClickEvent event) {
                                                session.getTabManager().addTabToCurrentTab(new RequestQuotaChangeTabItem(resource, user, QuotaType.DATA, a.getValue(), "error while loading"));
                                            }
                                        });
									}
									public void onFinished(JavaScriptObject jso) {
										ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
										boolean empty = true;
										for (final Attribute resAttr : attrs) { 
											if (resAttr.getFriendlyName().equalsIgnoreCase("defaultDataLimit")) {
												if (a.getValue().equalsIgnoreCase("null")) {
													// null private + default
													layoutx.setHTML(1, 1, "Using default (default: "+resAttr.getValue()+")");
												} else {
													// private - default
													layoutx.setHTML(1, 1, String.valueOf(a.getValue())+" (default: "+resAttr.getValue()+")");
												}
												empty = false;
                                                quotaChangeButton.addClickHandler(new ClickHandler() {
                                                    public void onClick(ClickEvent event) {
                                                        session.getTabManager().addTabToCurrentTab(new RequestQuotaChangeTabItem(resource, user, QuotaType.DATA, a.getValue(), resAttr.getValue()));
                                                    }
                                                });
											}
										}
										// if no default found, write down at least private
										if (empty) {
											if (a.getValue().equalsIgnoreCase("null")) {
												layoutx.setHTML(1, 1, "Using default (default: Not set)");
											} else {
												layoutx.setHTML(1, 1, String.valueOf(a.getValue())+" (default: Not set)");												
											}
                                            quotaChangeButton.addClickHandler(new ClickHandler() {
                                                public void onClick(ClickEvent event) {
                                                    session.getTabManager().addTabToCurrentTab(new RequestQuotaChangeTabItem(resource, user, QuotaType.DATA, a.getValue(), "Not set"));
                                                }
                                            });
										}
									}
								});
								defaultAttr.setIds(ids);
								defaultAttr.retrieveData();
								empty = false;

								layoutx.setWidget(1, 2, quotaChangeButton);
								
								
							} else if (a.getFriendlyName().equalsIgnoreCase("filesLimit")) {
								layoutx.setHTML(2, 0, "<strong>Files quota: </strong>");
				;
								// get default
								Map<String, Integer> ids = new HashMap<String, Integer>();
								ids.put("resource", resource.getId());

                                final CustomButton quotaChangeButton = new CustomButton("Request change", SmallIcons.INSTANCE.databaseIcon());

                                GetAttributes defaultAttr = new GetAttributes(new JsonCallbackEvents(){
									public void onError(PerunError error) {
										if (a.getValue().equalsIgnoreCase("null")) {
											layoutx.setHTML(2, 1, "Using default (default: error while loading)");
										} else {
											layoutx.setHTML(2, 1, String.valueOf(a.getValue())+" (default: error while loading)");
										}
                                        quotaChangeButton.addClickHandler(new ClickHandler() {
                                            public void onClick(ClickEvent event) {
                                                session.getTabManager().addTabToCurrentTab(new RequestQuotaChangeTabItem(resource, user, QuotaType.FILES, a.getValue(), "Error while loading"));
                                            }
                                        });
									}
									public void onFinished(JavaScriptObject jso) {
										ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
										boolean empty = true;
										for (final Attribute resAttr : attrs) { 
											if (resAttr.getFriendlyName().equalsIgnoreCase("defaultFilesLimit")) {
												if (a.getValue().equalsIgnoreCase("null")) {
													// null private + default
													layoutx.setHTML(2, 1, "Using default (default: "+resAttr.getValue()+")");
												} else {
													// private + default
													layoutx.setHTML(2, 1, String.valueOf(a.getValue())+" (default: "+resAttr.getValue()+")");
												}
												empty = false;
                                                quotaChangeButton.addClickHandler(new ClickHandler() {
                                                    public void onClick(ClickEvent event) {
                                                        session.getTabManager().addTabToCurrentTab(new RequestQuotaChangeTabItem(resource, user, QuotaType.FILES, a.getValue(), resAttr.getValue()));
                                                    }
                                                });
											}
										}
										// if no default found, write down at least private
										if (empty) {
											if (a.getValue().equalsIgnoreCase("null")) {
												layoutx.setHTML(2, 1, "Using default (default: Not set)");
											} else {
												layoutx.setHTML(2, 1, String.valueOf(a.getValue())+" (default: Not set)");												
											}
                                            quotaChangeButton.addClickHandler(new ClickHandler() {
                                                public void onClick(ClickEvent event) {
                                                    session.getTabManager().addTabToCurrentTab(new RequestQuotaChangeTabItem(resource, user, QuotaType.FILES, a.getValue(), "Not set"));
                                                }
                                            });
										}

										layoutx.setWidget(2, 2, quotaChangeButton);
									}
								});
								defaultAttr.setIds(ids);
								defaultAttr.retrieveData();
								empty = false;
							}
						}
						if (empty) {
							layoutx.setHTML(0, 0, "<strong>No shell settings available.</strong>");
							layoutx.getFlexCellFormatter().setColSpan(0, 0, 3);
						}
					}
				});
				attributes.retrieveData();
			}
			public void onLoadingStart() {
				AjaxLoaderImage loader = new AjaxLoaderImage(true);
				layoutx.setWidget(0, 0, loader.loadingStart());
			}
			public void onError(PerunError error) {
				AjaxLoaderImage loader = new AjaxLoaderImage(true);
				layoutx.setWidget(0, 0, loader.loadingError(error));
			}
		});
		
		
		// load content on open
		settings.addOpenHandler(new OpenHandler<DisclosurePanel>(){
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				if (settings.getContent() == null) {
					callMember.retrieveData();
					settings.setContent(layoutx); // set content
				}
			}
		});
		
		return settings;
		
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.cogIcon(); 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 432;
		result = prime * result;
		return result;
	}

	/**
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (this.userId != ((SelfSettingsTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
        session.setActiveUser(user);
        session.getUiElements().getMenu().openMenu(MainMenu.USER);
	}
	
	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "settings";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		String str = UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
		if(voId != 0){
			str += "&vo=" + voId;
		}
		return str;
	}
	
	static public SelfSettingsTabItem load(Map<String, String> parameters) {
		if (parameters.containsKey("id")) {
			int uid = Integer.parseInt(parameters.get("id"));
			if (uid != 0) {
				if (parameters.containsKey("vo")) {
					int voId = Integer.parseInt(parameters.get("vo"));
					if(voId != 0){
						return new SelfSettingsTabItem(uid, voId);
					}
				}else{
					return new SelfSettingsTabItem(uid);
				}
			}
		}
		return new SelfSettingsTabItem();
	}

}