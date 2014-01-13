package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.generalServiceManager.InsertExecService;
import cz.metacentrum.perun.webgui.json.ownersManager.GetOwners;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Returns tab which contains addExecService form for specified service.
 *
 * !! USE AS INNER TAB ONLY !!
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz> 
 * @version $Id$
 */

public class AddExecServiceTabItem implements TabItem {

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
	private Label titleWidget = new Label("Loading service");

    private static final String DEFAULT_DELAY = "60";

	// data
	private Service service;
	private int serviceId;

	/**
	 * Creates a tab instance
     * @param service
     */
	public AddExecServiceTabItem(Service service){
		this.service = service;
		this.serviceId = service.getId();
	}
	
	public boolean isPrepared() {
		return (service != null);
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(service.getName()) + ": create exec service");
		
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

        // prepares layout
        FlexTable layout = new FlexTable();
        layout.setStyleName("inputFormFlexTable");
        layout.setWidth("350px");
        FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

        // close tab events
        final TabItem tab = this;

        TabMenu menu = new TabMenu();

		// define objects

        final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createExecService());

        final ListBoxWithObjects<Owner> owner = new ListBoxWithObjects<Owner>();
		final GetOwners owners = new GetOwners(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<Owner> own = JsonUtils.jsoAsList(jso);
                if (own != null && !own.isEmpty()) {
                    own = new TableSorter<Owner>().sortByName(own);
                    owner.clear();
                    for (int i=0; i<own.size(); i++){
                        owner.addItem(own.get(i));
                    }
                    createButton.setEnabled(true);
                } else {
                    owner.clear();
                    owner.addItem("No owners available");
                }
			}
            @Override
            public void onLoadingStart(){
                owner.addItem("Loading...");
                createButton.setEnabled(false);
            }
            @Override
            public void onError(PerunError error){
                owner.clear();
                owner.addItem("Error while loading");
                createButton.setEnabled(false);
            }
		});
		owners.retrieveData();

		Label serviceLabel = new Label();
		serviceLabel.setText(service.getName()+" ("+serviceId+")");

		final ListBox type = new ListBox(); 
		type.addItem("GENERATE", "GENERATE");
		type.addItem("SEND", "SEND");

		final CheckBox enabled = new CheckBox();
		enabled.setText("Enabled / Disabled");
		enabled.setValue(true);

		final ExtendedTextBox delay = new ExtendedTextBox();
        delay.getTextBox().setText(DEFAULT_DELAY);

        final ExtendedTextBox.TextBoxValidator delayValidator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (!JsonUtils.checkParseInt(delay.getTextBox().getText().trim())) {
                    delay.setError("Delay must be a number (time in minutes) !");
                    return false;
                } else {
                    delay.setOk();
                    return true;
                }
            }
        };
        delay.setValidator(delayValidator);

		final ExtendedTextBox scriptPath = new ExtendedTextBox();

        final ExtendedTextBox.TextBoxValidator scriptValidator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (scriptPath.getTextBox().getText().trim().isEmpty()) {
                    scriptPath.setError("Script path can't be empty !");
                    return false;
                } else {
                    scriptPath.setOk();
                    return true;
                }
            }
        };
        scriptPath.setValidator(scriptValidator);

		// layout
		layout.setHTML(0, 0, "Service:");
		layout.setHTML(1, 0, "Type:");
		layout.setHTML(2, 0, "Status:");
		layout.setHTML(3, 0, "Delay:");
		layout.setHTML(4, 0, "Script path:");
		layout.setHTML(5, 0, "Owner:");

		layout.setWidget(0, 1, serviceLabel);
		layout.setWidget(1, 1, type);
		layout.setWidget(2, 1, enabled);
		layout.setWidget(3, 1, delay);
		layout.setWidget(4, 1, scriptPath);
		layout.setWidget(5, 1, owner);

        // send button
        createButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                if (delayValidator.validateTextBox() && scriptValidator.validateTextBox()) {
                    int delayNum = Integer.parseInt(delay.getTextBox().getText().trim());
                    InsertExecService request = new InsertExecService(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
                    request.addExecService(service, owner.getSelectedObject(), type.getValue(type.getSelectedIndex()), enabled.getValue(), delayNum, scriptPath.getTextBox().getText().trim());
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

        for (int i=0; i<layout.getRowCount(); i++) {
            cellFormatter.addStyleName(i, 0, "itemName");
        }

        createButton.setEnabled(false);
        menu.addWidget(createButton);
        menu.addWidget(cancelButton);

        vp.add(layout);
        vp.add(menu);
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
		final int prime = 31;
		int result = 1;
		result = prime * result + serviceId;
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
		AddExecServiceTabItem other = (AddExecServiceTabItem) obj;
		if (serviceId != other.serviceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) { 
			return true; 
		} else {
			return false;
		}

	}

}