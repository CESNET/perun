package cz.metacentrum.perun.webgui.tabs.attributestabs;

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
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.DeleteAttribute;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesDefinition;
import cz.metacentrum.perun.webgui.json.attributesManager.UpdateAttribute;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;
import cz.metacentrum.perun.webgui.tabs.AttributesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.entitylessattributestabs.EntitylessAttributesDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Tab for managing attribute definitions in Perun.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AttributeDefinitionsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Attributes");

	ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

	/**
	 * Creates a tab instance
	 */
	public AttributeDefinitionsTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// create main panel for content
		VerticalPanel mainPage = new VerticalPanel();
		mainPage.setWidth("100%");

		// create new instance for jsonCall
		final GetAttributesDefinition attrDef = new GetAttributesDefinition();

		final ExtendedSuggestBox box = new ExtendedSuggestBox(attrDef.getOracle());

		// custom events for reloading when created or deleted
		final JsonCallbackEvents refreshTabEvents = JsonCallbackEvents.mergeEvents(JsonCallbackEvents.refreshTableEvents(attrDef), new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				if (box.getSuggestBox().getValue() != null)
				attrDef.filterTable(box.getSuggestBox().getValue());
			}
		});

		// TAB MENU
		TabMenu tabMenu = new TabMenu();
		tabMenu.addWidget(UiElements.getRefreshButton(this));

		// create buttons
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, buttonTranslation.createAttributeDefinition(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateAttributeDefinitionTabItem());
			}
		}));

		// remove button
		final CustomButton deleteButton = TabMenu.getPredefinedButton(ButtonType.DELETE, buttonTranslation.deleteAttributeDefinition());
		deleteButton.setEnabled(false);
		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<AttributeDefinition> attrDefToBeDeleted = attrDef.getTableSelectedList();
				String text = "Following attribute definitions will be deleted.</p><p style=\"color: red;\">All stored values of such attributes will be deleted too!";
				UiElements.showDeleteConfirm(attrDefToBeDeleted, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for (int i = 0; i < attrDefToBeDeleted.size(); i++) {
							final int x = i;
							DeleteAttribute request = new DeleteAttribute(JsonCallbackEvents.disableButtonEvents(deleteButton, new JsonCallbackEvents(){
								@Override
								public void onFinished(JavaScriptObject jso) {
									// remove deleted attrs from table
									attrDef.removeFromBackupTable(attrDefToBeDeleted.get(x));
								}
							}));
							request.deleteAttributeDefinition(attrDefToBeDeleted.get(i).getId());
						}
					}
				});
			}
		});
		tabMenu.addWidget(deleteButton);

		// filter box
		tabMenu.addFilterWidget(box, new PerunSearchEvent() {
			public void searchFor(String text) {
				attrDef.filterTable(text);
			}
		}, buttonTranslation.filterAttributeDefinition());

		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				final ArrayList<AttributeDefinition> list = attrDef.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
					for (int i = 0; i < list.size(); i++) {
						final int x = i;
						UpdateAttribute request = new UpdateAttribute(JsonCallbackEvents.disableButtonEvents(saveButton, new JsonCallbackEvents(){
							@Override
							public void onFinished(JavaScriptObject jso) {
								attrDef.getSelectionModel().setSelected(list.get(x), false);
							}
						}));
						request.updateAttribute(list.get(i));
					}
				}
			}
		});
		tabMenu.addWidget(saveButton);

		//if checkbox is checked only entityless attributes will be shown
		CheckBox showEntitylessBox = new CheckBox("Show only Entityless Attributes");
		showEntitylessBox.setValue(false);
		showEntitylessBox.addValueChangeHandler(valueChangeEvent -> {
			if (showEntitylessBox.getValue()) {
				attrDef.setEntity("entityless");
			} else {
				attrDef.setEntities(new HashSet<>());
			}
			attrDef.retrieveData();
		});
		tabMenu.addWidget(showEntitylessBox);

		// add menu to page
		mainPage.add(tabMenu);
		mainPage.setCellHeight(tabMenu, "30px");

		CellTable<AttributeDefinition> attrDefTable = attrDef.getTable(new FieldUpdater<AttributeDefinition, String>() {
			@Override
			public void update(int index, AttributeDefinition object, String value) {
				if (object.getEntity().equals("entityless")) {
					session.getTabManager().addTabToCurrentTab(new EntitylessAttributesDetailTabItem(object), true);
				} else {
					session.getTabManager().addTabToCurrentTab(new AttributeDefinitionDetailTabItem(object), true);
				}
			}
		});
		attrDefTable.setStyleName("perun-table");
		ScrollPanel scrollTable = new ScrollPanel(attrDefTable);
		scrollTable.addStyleName("perun-tableScrollPanel");

		JsonUtils.addTableManagedButton(attrDef, attrDefTable, deleteButton);

		// put page into scroll panel
		mainPage.add(scrollTable);

		session.getUiElements().resizePerunTable(scrollTable, 350, this);

		this.contentWidget.setWidget(mainPage);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.attributesDisplayIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 563;
		int result = 1;
		result = prime * result * 13;
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

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Attributes", getUrlWithParameters());
	}

	public boolean isAuthorized() {
		return session.isPerunAdmin();
	}

	public final static String URL = "attr-def";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return AttributesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public AttributeDefinitionsTabItem load(Map<String, String> parameters) {
		return new AttributeDefinitionsTabItem();
	}

}
