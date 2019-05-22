package cz.metacentrum.perun.webgui.tabs.testtabs;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesV2;
import cz.metacentrum.perun.webgui.json.comparators.AttributeComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.TestTabs;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeDescriptionCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeNameCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeValueCell;

import java.util.ArrayList;
import java.util.Map;

/**
 * Inner tab item for shell change
 *
 * @author Pavel Zlamal <256627@mail-muni.cz>
 */

public class TestDataGridAttributesTabItem implements TabItem, TabItemWithUrl {


	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimpleLayoutPanel contentWidget = new SimpleLayoutPanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Datagrid test - attributes");

	/**
	 * Changing shell request
	 *
	 */
	public TestDataGridAttributesTabItem(){}

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

		//contentWidget.setSize("100%", "100%");

		DockLayoutPanel ft = new DockLayoutPanel(Style.Unit.PX);
		contentWidget.setWidget(ft);

		final DataGrid gridTable = new DataGrid();
		gridTable.setSize("100%", "100%");

		final ArrayList<Attribute> vosList = new ArrayList<Attribute>();
		final GetAttributesV2 getVos = new GetAttributesV2(new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				vosList.addAll(new TableSorter<Attribute>().sortByAttrNameTranslation(JsonUtils.<Attribute>jsoAsList(jso)));
				gridTable.setRowData(vosList);
				gridTable.redraw();
			}

		});
		getVos.getUserAttributes(3411);
		getVos.retrieveData();

		gridTable.setSelectionModel(new MultiSelectionModel<Attribute>(new GeneralKeyProvider<Attribute>()));
		final SelectionModel<Attribute> selectionModel = gridTable.getSelectionModel();
		gridTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);

		Column<Attribute, Boolean> checkBoxColumn = new Column<Attribute, Boolean>(
				new CheckboxCell(true, true)) {
			@Override
			public Boolean getValue(Attribute object) {
				// Get the value from the selection model.
				return selectionModel.isSelected(object);
			}
		};
		checkBoxColumn.setFieldUpdater(new FieldUpdater<Attribute, Boolean>() {
			@Override
			public void update(int i, Attribute Attribute, Boolean aBoolean) {
				selectionModel.setSelected(Attribute, aBoolean);
			}
		});

		// Checkbox column header
		CheckboxCell cb = new CheckboxCell();
		Header<Boolean> checkBoxHeader = new Header<Boolean>(cb) {
			public Boolean getValue() {
				return false;//return true to see a checked checkbox.
			}
		};
		checkBoxHeader.setUpdater(new ValueUpdater<Boolean>() {
			public void update(Boolean value) {
				// sets selected to all, if value = true, unselect otherwise
				for(Attribute obj : vosList){
					selectionModel.setSelected(obj, value);
				}
			}
		});

		gridTable.addColumn(checkBoxColumn, checkBoxHeader, checkBoxHeader);
		gridTable.setColumnWidth(checkBoxColumn, 40.0, Style.Unit.PX);

		TextColumn<Attribute> idColumn = new TextColumn<Attribute>() {
			@Override
			public String getValue(Attribute object) {
				return String.valueOf(object.getId());
			}
		};

		if (JsonUtils.isExtendedInfoVisible()) {
			gridTable.addColumn(idColumn, "Id", "Id");
			gridTable.setColumnWidth(idColumn, "90px");
		}

		// Name column
		Column<Attribute, Attribute> nameColumn = JsonUtils.addColumn(new PerunAttributeNameCell());

		// Description column
		Column<Attribute, Attribute> descriptionColumn = JsonUtils.addColumn(new PerunAttributeDescriptionCell());

		// Value column
		Column<Attribute, Attribute> valueColumn = JsonUtils.addColumn(new PerunAttributeValueCell(), new FieldUpdater<Attribute, Attribute>() {
			public void update(int index, Attribute object, Attribute value) {
				object = value;
				selectionModel.setSelected(object, object.isAttributeValid());
			}
		});

		ColumnSortEvent.ListHandler<Attribute> columnSortHandler = new ColumnSortEvent.ListHandler<Attribute>(vosList);
		gridTable.addColumnSortHandler(columnSortHandler);

		// Sorting name column
		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new AttributeComparator<Attribute>(AttributeComparator.Column.TRANSLATED_NAME));

		// Sorting description column
		descriptionColumn.setSortable(true);
		columnSortHandler.setComparator(descriptionColumn, new AttributeComparator<Attribute>(AttributeComparator.Column.TRANSLATED_DESCRIPTION));

		// Add sorting
		gridTable.addColumnSortHandler(columnSortHandler);

		// updates the columns size
		gridTable.setColumnWidth(nameColumn, 200.0, Style.Unit.PX);
		gridTable.setColumnWidth(valueColumn, 420.0, Style.Unit.PX);


		gridTable.addColumn(nameColumn, "Name");
		gridTable.addColumn(valueColumn, "Value");
		gridTable.addColumn(descriptionColumn, "Description");

		TabMenu tabMenu = new TabMenu();

		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new TestDataGridTabItem(), true);
			}
		}));

		ft.addNorth(tabMenu, 50);
		ft.add(gridTable);

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
		final int prime = 1129;
		int result = 1304;
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

		return session.isPerunAdmin();


	}


	public final static String URL = "datagrid2";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return TestTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}


	static public TestDataGridAttributesTabItem load(Map<String, String> parameters)
	{
		return new TestDataGridAttributesTabItem();
	}


}
