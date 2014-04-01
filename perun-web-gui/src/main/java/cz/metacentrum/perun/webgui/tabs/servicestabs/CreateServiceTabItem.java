package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.ownersManager.GetOwners;
import cz.metacentrum.perun.webgui.json.servicesManager.CreateService;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Tab with create service form
 *
 * ! USE AS INNER TAB ONLY !
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateServiceTabItem implements TabItem {

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
	private Label titleWidget = new Label("Create service");

	/**
	 * Tab with create service form
     */
	public CreateServiceTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");

        final ExtendedTextBox serviceName = new ExtendedTextBox();
        final ListBoxWithObjects<Owner> ownersDropDown = new ListBoxWithObjects<Owner>();

        final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (serviceName.getTextBox().getText().trim().isEmpty()) {
                    serviceName.setError("Name can't be empty");
                    return false;
                } else {
                    serviceName.setOk();
                    return true;
                }
            }
        };
        serviceName.setValidator(validator);

        // prepares layout
        FlexTable layout = new FlexTable();
        layout.setStyleName("inputFormFlexTable");
        FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

        // close tab events
        final TabItem tab = this;

        TabMenu menu = new TabMenu();

        // fill form
        layout.setHTML(0, 0, "Name:");
        layout.setWidget(0, 1, serviceName);
        layout.setHTML(1, 0, "Owner:");
        layout.setWidget(1, 1, ownersDropDown);

        for (int i=0; i<layout.getRowCount(); i++) {
            cellFormatter.addStyleName(i, 0, "itemName");
        }

        // create button
        final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createService());
        createButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (validator.validateTextBox()) {
                    CreateService request = new CreateService(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
                    request.createService(serviceName.getTextBox().getText().trim(), ownersDropDown.getSelectedObject().getId());
                }
            }
        });

        // cancel button
        final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        });

        menu.addWidget(createButton);
        menu.addWidget(cancelButton);

		// get owners
		final GetOwners owners = new GetOwners(new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				ownersDropDown.clear();
                // convert
                ArrayList<Owner> own = JsonUtils.jsoAsList(jso);
				own = new TableSorter<Owner>().sortByName(own);
				// check
				if (own.isEmpty() || own == null) {
					ownersDropDown.addItem("No owners available");
					return;
				}
                createButton.setEnabled(true);
				// process
				for (int i=0; i<own.size(); i++){
					ownersDropDown.addItem(own.get(i));
				}
			}
            public void onLoadingStart() {
                createButton.setEnabled(false);
                ownersDropDown.clear();
                ownersDropDown.addItem("Loading...");
            }
            public void onError(PerunError error) {
                createButton.setEnabled(false);
            }
        });
		owners.retrieveData();

        vp.add(layout);
        vp.add(menu);
        vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

        // add tabs to the main panel
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
		return SmallIcons.INSTANCE.trafficLightsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 31;
		result = prime * result;
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

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

}