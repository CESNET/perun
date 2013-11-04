package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.registrarManager.*;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Types of emails for application
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlámal <256627@mail.muni.cz>
 * @version $Id$
 */
public class MailsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Application notifications");

	// data
	private VirtualOrganization vo;
	private int voId;
	private int groupId;
	private Group group;

	private ApplicationForm form;
	
	private PerunEntity entity = PerunEntity.VIRTUAL_ORGANIZATION;
	int entityId = 0;
	
	/**
	 * Creates a tab instance
	 *
     * @param vo
     * @param group (null = if you want only VO form)
     * @param form
     */
	public MailsTabItem(VirtualOrganization vo, Group group, ApplicationForm form) {
		this.vo = vo;
		this.voId = vo.getId();
		this.form = form;
		this.groupId = 0;
		if (group != null) {
			this.group = group;
			this.groupId = group.getId();
		}
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param voId
     * @param groupId (0 = if you want only VO form)
     */
	public MailsTabItem(int voId, int groupId) {
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
        new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
		
		JsonCallbackEvents events2 = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				form = jso.cast();
			}
		};
		this.groupId = groupId;
		if (groupId != 0) {
			new GetApplicationForm(PerunEntity.GROUP, groupId, events2).retrieveData();
			JsonCallbackEvents events3 = new JsonCallbackEvents(){
				public void onFinished(JavaScriptObject jso) {
					group = jso.cast();
				}
			};
            new GetEntityById(PerunEntity.GROUP, groupId, events3).retrieveData();
		} else {
			new GetApplicationForm(PerunEntity.VIRTUAL_ORGANIZATION, voId, events2).retrieveData();
		}
		
	}
	
	public boolean isPrepared(){
		
		if (groupId == 0) {
			return (vo != null && form != null);			
		} else {
			return (vo != null && form != null && group != null);
		}
		
	}

	public Widget draw() {
		
		final GetApplicationMails mailsRequest;
		
		String title = "";
		if (group != null) {
			title = group.getName();
			entity = PerunEntity.GROUP;
			entityId = group.getId(); 
			mailsRequest = new GetApplicationMails(entity, group.getId());
		} else {
			title = vo.getName();
			entityId = vo.getId();
			mailsRequest = new GetApplicationMails(entity, vo.getId());
		}
		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(title)+": "+"application notifications");
		
		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();
		firstTabPanel.add(menu);
		firstTabPanel.setCellHeight(menu, "30px");

		// add button
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addMail(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                session.getTabManager().addTabToCurrentTab(new CreateMailTabItem(vo, group, form));
            }
        }));
		
		// remove button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeMail());
		menu.addWidget(removeButton);
        removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<ApplicationMail> list = mailsRequest.getTableSelectedList();
                String text = "Following mail definitions will be removed and users won't receive them anymore.";
                UiElements.showDeleteConfirm(list, text, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        for (int i=0; i<list.size(); i++) {
                            if (i != list.size()-1) {
                                DeleteApplicationMail request = new DeleteApplicationMail(entity, JsonCallbackEvents.disableButtonEvents(removeButton));
                                request.deleteMail(list.get(i).getId(), entityId);
                            } else {
                                // refresh table on last call
                                DeleteApplicationMail request = new DeleteApplicationMail(entity, JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(mailsRequest)));
                                request.deleteMail(list.get(i).getId(), entityId);
                            }
                        }
                    }
                });
			}
		});
		
		// enable button
        CustomButton enableButton = TabMenu.getPredefinedButton(ButtonType.ENABLE, ButtonTranslation.INSTANCE.enableMail(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                ArrayList<ApplicationMail> list = mailsRequest.getTableSelectedList();
                if (UiElements.cantSaveEmptyListDialogBox(list)) {
                    SetSendingEnabled request = new SetSendingEnabled(JsonCallbackEvents.refreshTableEvents(mailsRequest));
                    request.setEnabled(list, true);
                }
            }
        });
		menu.addWidget(enableButton);
		
		// disable button
        CustomButton disableButton = TabMenu.getPredefinedButton(ButtonType.DISABLE, ButtonTranslation.INSTANCE.disableMail(), new ClickHandler() {

            public void onClick(ClickEvent event) {
                ArrayList<ApplicationMail> list = mailsRequest.getTableSelectedList();
                if (UiElements.cantSaveEmptyListDialogBox(list)) {
                    SetSendingEnabled request = new SetSendingEnabled(JsonCallbackEvents.refreshTableEvents(mailsRequest));
                    request.setEnabled(list, false);
                }
            }
        });
        menu.addWidget(disableButton);
		
		// for VO only
		if (group == null) {
			menu.addWidget(new CustomButton(ButtonTranslation.INSTANCE.mailFooterButton(), ButtonTranslation.INSTANCE.editMailFooter(), SmallIcons.INSTANCE.emailIcon(), new ClickHandler(){
				public void onClick(ClickEvent event) {
                    session.getTabManager().addTabToCurrentTab(new EditMailFooterTabItem(vo));
				}
			}));
		}

        CustomButton copy;

        if (group == null) {
           copy = new CustomButton(ButtonTranslation.INSTANCE.copyFromVoButton(), ButtonTranslation.INSTANCE.copyMailsFromVo(), SmallIcons.INSTANCE.copyIcon(), new ClickHandler(){
                public void onClick(ClickEvent event) {
                    session.getTabManager().addTabToCurrentTab(new CopyMailsTabItem(voId, 0));
                }
            });
        } else {
            copy = new CustomButton(ButtonTranslation.INSTANCE.copyFromGroupButton(), ButtonTranslation.INSTANCE.copyMailsFromGroup(), SmallIcons.INSTANCE.copyIcon(), new ClickHandler(){
                public void onClick(ClickEvent event) {
                    session.getTabManager().addTabToCurrentTab(new CopyMailsTabItem(group.getVoId(), groupId));
                }
            });
        }

        menu.addWidget(copy);

		// TABLE
		CellTable<ApplicationMail> table = mailsRequest.getTable(new FieldUpdater<ApplicationMail, String>() {
			public void update(int index, ApplicationMail appMail, String value) {
				session.getTabManager().addTabToCurrentTab(new EditMailTabItem(appMail));
			}
		});
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

        removeButton.setEnabled(false);
        enableButton.setEnabled(false);
        disableButton.setEnabled(false);
        JsonUtils.addTableManagedButton(mailsRequest, table, removeButton);
        JsonUtils.addTableManagedButton(mailsRequest, table, enableButton);
        JsonUtils.addTableManagedButton(mailsRequest, table, disableButton);

        session.getUiElements().resizePerunTable(sp, 100);
		firstTabPanel.add(sp);
		

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
		return SmallIcons.INSTANCE.emailIcon(); 
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
		MailsTabItem other = (MailsTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}


	public void open()
	{
		
		if (groupId == 0) {
			session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
            session.getUiElements().getBreadcrumbs().setLocation(vo, "Application notifications", getUrlWithParameters());
		} else {
			session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
            session.getUiElements().getBreadcrumbs().setLocation(group, "Application notifications", getUrlWithParameters());
		}
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
		
	}


	public boolean isAuthorized() {
		
		if (session.isVoAdmin(voId) || session.isGroupAdmin(groupId)) {
			return true; 
		} else {
			return false;
		}

	}
	
	public final static String URL = "app-mails";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return RegistrarTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?vo=" + voId + "&group=" + groupId;
	}
	
	static public MailsTabItem load(Map<String, String> parameters)
	{
		int voId = Integer.parseInt(parameters.get("vo"));
		int groupId = Integer.parseInt(parameters.get("group"));
		return new MailsTabItem(voId, groupId);
	}

}