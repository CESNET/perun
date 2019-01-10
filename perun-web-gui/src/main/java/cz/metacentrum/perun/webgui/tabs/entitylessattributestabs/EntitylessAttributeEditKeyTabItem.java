package cz.metacentrum.perun.webgui.tabs.entitylessattributestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetEntitylessAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetEntitylessAttribute;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Tab for editing entityless attriubte's keys
 *
 * @author Dano Fecko <dano9500@gmail.com>
 */
public class EntitylessAttributeEditKeyTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading vo attributes");

	//map of attributes and keys
	private HashMap<Attribute, String> map;

	private AttributeDefinition def;

	public final static String URL = "edit_keys";

	public EntitylessAttributeEditKeyTabItem(AttributeDefinition aD) {
		this.def = aD;
	}

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public String getUrlWithParameters() {
		return MainMenu.PERUN_ADMIN + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	@Override
	public Widget draw() {
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(def.getName()) + ": settings");

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();
		// refresh
		menu.addWidget(UiElements.getRefreshButton(this));

		// Get Attributes
		final GetEntitylessAttributes jsonCallback = new GetEntitylessAttributes(def);

		// get the table
		CellTable<Attribute> table = jsonCallback.getTable();

		this.map = jsonCallback.getMap();

		if (!isAuthorized()) jsonCallback.setCheckable(false);

		final CustomButton setButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
		menu.addWidget(setButton);
		if (!isAuthorized()) setButton.setEnabled(false);

		// refresh table
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(jsonCallback);

		// set button event with button disable
		final JsonCallbackEvents setButtonEvent = JsonCallbackEvents.disableButtonEvents(setButton, events);

		setButton.addClickHandler(event -> {

			ArrayList<Attribute> list = jsonCallback.getTableSelectedList();

			if (UiElements.cantSaveEmptyListDialogBox(list)) {
				Map<String, String> ids = new HashMap<>();
				SetEntitylessAttribute request = new SetEntitylessAttribute(setButtonEvent);
				for (Attribute a : list) {
					ids.put("key", map.get(a));
					request.setAttribute(ids, a);
				}
				draw();
			}
		});

		// remove attr button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeAttributes());
		menu.addWidget(removeButton);
		if (!isAuthorized()) removeButton.setEnabled(false);


		// remove button event
		removeButton.addClickHandler(event -> {
			ArrayList<Attribute> list = jsonCallback.getTableSelectedList();
			if (UiElements.cantSaveEmptyListDialogBox(list)) {
				for (Attribute a : list) {
					removeAttribute(map.get(a));
				}
			}
		});

		final TextBox keyBox = new TextBox();
		keyBox.getElement().setPropertyString("placeholder", "enter key for new attribute");

		final TextBox valueBox = new TextBox();
		valueBox.getElement().setPropertyString("placeholder", "enter value for new attribute");

		final TextBox mapKeyBox = new TextBox();
		mapKeyBox.getElement().setPropertyString("placeholder", "enter key for a map");

		final TextBox mapValueBox = new TextBox();
		mapValueBox.getElement().setPropertyString("placeholder", "enter value for a map");

		//add attr button
		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true,
			ButtonTranslation.INSTANCE.setNewAttributes(), event -> {
				map = jsonCallback.getMap();
				if (map.containsValue(keyBox.getText())) {
					UiElements.generateAlert("", "Key \"" + keyBox.getText() + "\" already exists");
					return;
				}
				if (keyBox.getText().equals("")) {
					UiElements.generateAlert("", "Enter key into key box");
					return;
				}
				if (shouldHaveValueBox() && !valueBox.getText().equals("")) {
					addAttribute(keyBox.getText(), valueBox.getText(), null);
					return;
				}
				if (shouldHaveValueBox() && valueBox.getText().equals("")) {
					UiElements.generateAlert("", "Enter value into value box");
					return;
				}
				if (def.getType().equals("java.util.LinkedHashMap")) {
					addAttribute(keyBox.getText(), mapValueBox.getText(), mapKeyBox.getText());
					return;
				}
				addAttribute(keyBox.getText(), null, null);
			});
		menu.addWidget(addButton);
		if (!isAuthorized()) addButton.setEnabled(false);

		menu.addWidget(new Label("New key:"));
		menu.addWidget(keyBox);

		if (shouldHaveValueBox()) {
			menu.addWidget(new Label("Value for new key:"));
			menu.addWidget(valueBox);
		}

		if (def.getType().equals("java.util.LinkedHashMap")) {
			menu.addWidget(new Label("First entry for new key:"));
			menu.addWidget(mapKeyBox);
			menu.addWidget(new Label("="));
			menu.addWidget(mapValueBox);
		}

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		if (isAuthorized()) JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

		// add menu and the table to the main panel
		firstTabPanel.add(menu);
		firstTabPanel.setCellHeight(menu, "30px");
		firstTabPanel.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

		this.contentWidget.setWidget(firstTabPanel);

		return getWidget();
	}

	@Override
	public Widget getWidget() {
		return this.contentWidget;
	}

	@Override
	public Widget getTitle() {
		return this.titleWidget;
	}

	@Override
	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.attributesDisplayIcon();
	}

	@Override
	public boolean multipleInstancesEnabled() {
		return false;
	}

	@Override
	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(0, "Entityless attributes setting", getUrlWithParameters());
	}

	@Override
	public boolean isAuthorized() {
		return session.isPerunAdmin();
	}

	@Override
	public boolean isPrepared() {
		return def != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EntitylessAttributeEditKeyTabItem that = (EntitylessAttributeEditKeyTabItem) o;
		return Objects.equals(def, that.def);
	}

	@Override
	public int hashCode() {
		return Objects.hash(def);
	}

	private void addAttribute(String key, String value, String mapKey) {
		if (def.getType().equals("java.lang.Integer")) {
			try {
				Integer.valueOf(value);
			} catch (NumberFormatException e) {
				UiElements.generateAlert("", "Enter NUMBER into value box");
				return;
			}
		}
		String JSON_URL = "attributesManager/setAttribute";

		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating entityless attribute " + def.getDisplayName() +
					" with key: " + key + " and value: " + value + " failed");
			}

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Creating entityless attribute " + def.getDisplayName() +
					" with key: " + key + " and value: " + value + " succeeded");
				draw();
			}

			public void onLoadingStart() {

			}

		};

		JsonPostClient jpc = new JsonPostClient(newEvents);
		jpc.sendData(JSON_URL, prepareJSONObject(key, value, mapKey));
	}

	private void removeAttribute(String key) {
		String JSON_URL = "attributesManager/removeAttribute";

		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing entityless attribute " + def.getDisplayName() +
					" with key: " + key + " failed");
			}

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Removing entityless attribute " + def.getDisplayName() +
					" with key: " + key + " succeeded");
				draw();
			}

			public void onLoadingStart() {

			}
		};

		JSONObject jo = new JSONObject();
		jo.put("key", new JSONString(key));
		jo.put("attribute", new JSONNumber(def.getId()));

		JsonPostClient jpc = new JsonPostClient(newEvents);
		jpc.sendData(JSON_URL, jo);
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject(String key, String value, String mapKey) {

		// create new Attribute jsonObject
		JSONObject newAttr = new JSONObject();
		newAttr.put("id", new JSONNumber(def.getId()));
		newAttr.put("type", new JSONString(def.getType()));
		newAttr.put("description", new JSONString(def.getDescription()));
		newAttr.put("namespace", new JSONString(def.getNamespace()));
		newAttr.put("friendlyName", new JSONString(def.getFriendlyName()));
		newAttr.put("displayName", new JSONString(def.getDisplayName()));
		newAttr.put("unique", new JSONString("false"));

		switch (def.getType()) {
			case "java.lang.String":
				newAttr.put("value", new JSONString(value));
				break;
			case "java.util.LinkedHashMap":
				JSONObject jo = new JSONObject();
				jo.put(mapKey, new JSONString(value));
				newAttr.put("value", jo);
				break;
			case "java.util.ArrayList":
				JSONArray al = new JSONArray();
				al.set(0, new JSONString("value"));
				newAttr.put("value", al);
				break;
			case "java.lang.Integer":
				newAttr.put("value", new JSONNumber(Double.parseDouble(value)));
				break;
			case "java.lang.Boolean":
				newAttr.put("value", JSONBoolean.getInstance(Boolean.parseBoolean("True")));
				break;
			case "java.lang.LargeString":
				newAttr.put("value", new JSONString(value));
				break;
			case "java.util.LargeArrayList":
				JSONArray lal = new JSONArray();
				lal.set(0, new JSONString("value"));
				newAttr.put("value", lal);
				break;
		}

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();

		jsonQuery.put("key", new JSONString(key));

		jsonQuery.put("attribute", newAttr);

		return jsonQuery;
	}

	private boolean shouldHaveValueBox() {
		return (def.getType().equals("java.lang.String") ||
			    def.getType().equals("java.lang.Integer") ||
			    def.getType().equals("java.lang.LargeString"));
	}
}
