package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttribute;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Inner tab item for shell change
 * User in: SelfSettingsTabItem
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class ShellChangeTabItem implements TabItem{

	private PerunWebSession session = PerunWebSession.getInstance();

    private Label titleWidget = new Label("Select preferred shell");
	private SimplePanel contentWidget = new SimplePanel();
	
	private Resource resource;
	private Attribute a;
	private int userId;
	private HTML shellWrapper;

	/**
	 * Changing shell request
	 *
     * @param resource
     * @param userId
     * @param a Attribute to be changed
     * @param shellWrapper Shell wrapper
     */
	public ShellChangeTabItem(Resource resource, int userId, Attribute a, HTML shellWrapper){
		this.resource = resource;
		this.userId = userId;
		this.a = a;
		this.shellWrapper = shellWrapper;
	}
	

	public boolean isPrepared(){
		return (userId != 0 && resource != null);		
	}
	
	public Widget draw() {

        VerticalPanel vp = new VerticalPanel();
		
		final FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");
        ft.setHTML(0, 0, "Available shells:");
        ft.getFlexCellFormatter().setStyleName(0, 0, "itemName");
		final ListBox shells = new ListBox();
		ft.setWidget(0, 1, shells);

        vp.add(ft);

		// callback for available shells
		// TODO - retrieve only 1 attribute ?
		GetAttributes attrs = new GetAttributes(new JsonCallbackEvents(){
			public void onError(PerunError error) {
				shells.clear();
                shells.addItem("Error while loading");
			}
			public void onFinished(JavaScriptObject jso) {
				shells.clear();
				ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
				if (list.isEmpty() || list == null) {
                    shells.addItem("No shells available");
					return;
				}
				// fill shells
				for (Attribute a : list) {
					if (a.getFriendlyName().equalsIgnoreCase("shells")) {
						for (int i=0; i<a.getValueAsJsArray().length(); i++) {
							// fill shell values
							shells.addItem(a.getValueAsJsArray().get(i));
						}
						break;
					}
				}
				// set selected
				for (int i=0; i<shells.getItemCount() ; i++) {
					if (shells.getValue(i).equalsIgnoreCase(a.getValue())) {
						shells.setSelectedIndex(i);
						break;
					}
				}
			}
            public void onLoadingStart() {
                shells.clear();
                shells.addItem("Loading...");
            }
		});
		

		final TabItem tab = this;

        TabMenu menu = new TabMenu();
        vp.add(menu);
        vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

        final CustomButton selectShellButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save preferred shell");
        selectShellButton.setEnabled(false);
        selectShellButton.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				// OK click button
				// set new value
				a.setValue(shells.getValue(shells.getSelectedIndex()));
				// send request
				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);
				ids.put("facility", resource.getFacilityId());
				SetAttribute request = new SetAttribute(JsonCallbackEvents.disableButtonEvents(selectShellButton, new JsonCallbackEvents(){
					public void onFinished(JavaScriptObject jso) {
						shellWrapper.setHTML(a.getValue());
						session.getTabManager().closeTab(tab);
					}
				}));
				request.setAttribute(ids, a);
			}
		});
		attrs.getResourceAttributes(resource.getId());
		attrs.retrieveData();


        shells.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                if (shells.getValue(shells.getSelectedIndex()).equalsIgnoreCase(a.getValue())) {
                    selectShellButton.setEnabled(false);
                } else {
                    selectShellButton.setEnabled(true);
                }
            }
        });

        menu.addWidget(selectShellButton);
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

		contentWidget.setWidget(vp);
		return getWidget();
		
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.settingToolsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 404;
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
		
		ShellChangeTabItem oth = (ShellChangeTabItem) obj;
		
		if (oth.userId != this.userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		
	}
	
	public boolean isAuthorized() {
		return session.isSelf(userId);
	}
	
}