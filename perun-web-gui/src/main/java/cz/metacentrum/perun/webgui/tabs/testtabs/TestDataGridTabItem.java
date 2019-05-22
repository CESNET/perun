package cz.metacentrum.perun.webgui.tabs.testtabs;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.json.vosManager.DeleteVo;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.TestTabs;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.vostabs.CreateVoTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Inner tab item for shell change
 *
 * @author Pavel Zlamal <256627@mail-muni.cz>
 */

public class TestDataGridTabItem implements TabItem, TabItemWithUrl {


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
	private Label titleWidget = new Label("Datagrid test");

	/**
	 * Changing shell request
	 */
	public TestDataGridTabItem(){ }

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

		final ArrayList<VirtualOrganization> vosList = new ArrayList<VirtualOrganization>();
		final GetVos getVos = new GetVos(new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				vosList.addAll(new TableSorter<VirtualOrganization>().sortByName(JsonUtils.<VirtualOrganization>jsoAsList(jso)));
				gridTable.setRowData(vosList);
				gridTable.redraw();
			}

		});
		getVos.retrieveData();

		gridTable.setSelectionModel(new MultiSelectionModel<VirtualOrganization>(new GeneralKeyProvider<VirtualOrganization>()));
		final SelectionModel<VirtualOrganization> selectionModel = gridTable.getSelectionModel();

		Column<VirtualOrganization, Boolean> checkBoxColumn = new Column<VirtualOrganization, Boolean>(
				new CheckboxCell(true, true)) {
			@Override
			public Boolean getValue(VirtualOrganization object) {
				// Get the value from the selection model.
				return selectionModel.isSelected(object);
			}
		};
		checkBoxColumn.setFieldUpdater(new FieldUpdater<VirtualOrganization, Boolean>() {
			@Override
			public void update(int i, VirtualOrganization virtualOrganization, Boolean aBoolean) {
				selectionModel.setSelected(virtualOrganization, aBoolean);
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
				for(VirtualOrganization obj : vosList){
					selectionModel.setSelected(obj, value);
				}
			}
		});

		gridTable.addColumn(checkBoxColumn, checkBoxHeader, checkBoxHeader);
		gridTable.setColumnWidth(checkBoxColumn, 40.0, Style.Unit.PX);

		TextColumn<VirtualOrganization> idColumn = new TextColumn<VirtualOrganization>() {
			@Override
			public String getValue(VirtualOrganization object) {
				return String.valueOf(object.getId());
			}
		};
		gridTable.addColumn(idColumn, "Id", "Id");
		gridTable.setColumnWidth(idColumn, "90px");


		Column<VirtualOrganization, String> nameColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<VirtualOrganization, String>() {
					public String getValue(VirtualOrganization object) {
						return object.getName();
					}
				}, new FieldUpdater<VirtualOrganization, String>() {
					@Override
					public void update(int i, VirtualOrganization virtualOrganization, String s) {
						session.getTabManager().addTab(new VoDetailTabItem(virtualOrganization));
					}
				});

		gridTable.addColumn(nameColumn, "Name", "Name");

		TextColumn<VirtualOrganization> shortnameColumn = new TextColumn<VirtualOrganization>() {
			@Override
			public String getValue(VirtualOrganization object) {
				return object.getShortName();
			}
		};
		gridTable.addColumn(shortnameColumn, "Short name", "Short name");

		shortnameColumn.setFieldUpdater(new FieldUpdater<VirtualOrganization, String>() {
			@Override
			public void update(int i, VirtualOrganization virtualOrganization, String s) {
				session.getTabManager().addTab(new VoDetailTabItem(virtualOrganization));
			}
		});
		idColumn.setFieldUpdater(new FieldUpdater<VirtualOrganization, String>() {
			@Override
			public void update(int i, VirtualOrganization virtualOrganization, String s) {
				session.getTabManager().addTab(new VoDetailTabItem(virtualOrganization));
			}
		});
		nameColumn.setFieldUpdater(new FieldUpdater<VirtualOrganization, String>() {
			@Override
			public void update(int i, VirtualOrganization virtualOrganization, String s) {
				session.getTabManager().addTab(new VoDetailTabItem(virtualOrganization));
			}
		});


		TabMenu tabMenu = new TabMenu();
		// CREATE & DELETE ONLY WITH PERUN ADMIN
		if(session.isPerunAdmin()) {

			tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createVo(), new ClickHandler() {
				public void onClick(ClickEvent event) {
					session.getTabManager().addTabToCurrentTab(new CreateVoTabItem());
				}
			}));

			final cz.metacentrum.perun.webgui.widgets.CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteVo());
			removeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {

					final ArrayList<VirtualOrganization> itemsToRemove = getVos.getTableSelectedList();
					if (UiElements.cantSaveEmptyListDialogBox(itemsToRemove)) {

						VerticalPanel removePanel = new VerticalPanel();
						removePanel.add(new Label("These VOs will be removed:"));
						for (int i=0; i<itemsToRemove.size(); i++ ) {
							VirtualOrganization vo = itemsToRemove.get(i);
							removePanel.add(new Label(" - " + vo.getName()));
						}

						// confirmation
						Confirm c = new Confirm("Remove VOs", removePanel, new ClickHandler() {
							public void onClick(ClickEvent event) {

								for (int i=0; i<itemsToRemove.size(); i++ ) {

									DeleteVo request;

									// if last, refresh
									if(i == itemsToRemove.size() - 1){
										request = new DeleteVo(JsonCallbackEvents.disableButtonEvents(removeButton));
									}else{
										request = new DeleteVo(JsonCallbackEvents.disableButtonEvents(removeButton));
									}

									request.deleteVo(itemsToRemove.get(i).getId(), false);
								}
								getVos.clearTableSelectedSet();
							}
						}, true);
						c.show();

					}

				}
			});
			tabMenu.addWidget(removeButton);

		}

		// filter
		tabMenu.addFilterWidget(new ExtendedSuggestBox(getVos.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				getVos.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterVo());

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
		final int prime = 1151;
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


	public final static String URL = "datagrid";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return TestTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}


	static public TestDataGridTabItem load(Map<String, String> parameters)
	{
		return new TestDataGridTabItem();
	}


}
