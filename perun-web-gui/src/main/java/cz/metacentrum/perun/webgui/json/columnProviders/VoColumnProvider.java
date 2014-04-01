package cz.metacentrum.perun.webgui.json.columnProviders;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.Column;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCellWithAuthz;

import java.util.Comparator;

/**
 * Provide columns definitions for VO object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VoColumnProvider {

	private PerunTable<VirtualOrganization> table;
	private FieldUpdater<VirtualOrganization, VirtualOrganization> fieldUpdater;

	/**
	 * New instance of VoColumnProvider
	 *
	 * @param table table to add columns to
	 * @param fieldUpdater field updater used when cell is "clicked"
	 */
	public VoColumnProvider(PerunTable<VirtualOrganization> table, FieldUpdater<VirtualOrganization, VirtualOrganization> fieldUpdater) {
		this.table = table;
		this.fieldUpdater = fieldUpdater;
	}

	public void addIdColumn(IsClickableCell authz) {
		addIdColumn(authz, 0);
	}

	public void addIdColumn(IsClickableCell authz, int width) {

		// create column
		Column<VirtualOrganization, VirtualOrganization> idColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<VirtualOrganization>(authz, "id"), new JsonUtils.GetValue<VirtualOrganization, VirtualOrganization>() {
			@Override
			public VirtualOrganization getValue(VirtualOrganization object) {
				return object;
			}
		}, fieldUpdater);

		// add column only if extended info is visible
		if (JsonUtils.isExtendedInfoVisible()) {

			table.addColumn(idColumn, "VO Id");

			if (width != 0) {
				table.setColumnWidth(idColumn, width, Style.Unit.PX);
			}

			// sort column
			idColumn.setSortable(true);
			table.getColumnSortHandler().setComparator(idColumn, new GeneralComparator<VirtualOrganization>(GeneralComparator.Column.ID));

		}

	}


	public void addNameColumn(IsClickableCell authz) {
		addNameColumn(authz, 0);
	}

	public void addNameColumn(IsClickableCell authz, int width) {

		// create column
		Column<VirtualOrganization, VirtualOrganization> nameColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<VirtualOrganization>(authz, "name"), new JsonUtils.GetValue<VirtualOrganization, VirtualOrganization>() {
			@Override
			public VirtualOrganization getValue(VirtualOrganization object) {
				return object;
			}
		}, fieldUpdater);

		// add column
		table.addColumn(nameColumn, "Name");
		if (width != 0) {
			table.setColumnWidth(nameColumn, width, Style.Unit.PX);
		}

		// sort column
		nameColumn.setSortable(true);
		table.getColumnSortHandler().setComparator(nameColumn, new GeneralComparator<VirtualOrganization>(GeneralComparator.Column.NAME));

	}

	public void addShortNameColumn(IsClickableCell authz) {
		addShortNameColumn(authz, 0);
	}

	public void addShortNameColumn(IsClickableCell authz, int width) {

		// create column
		Column<VirtualOrganization, VirtualOrganization> shortNameColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<VirtualOrganization>(authz, "shortName"), new JsonUtils.GetValue<VirtualOrganization, VirtualOrganization>() {
			@Override
			public VirtualOrganization getValue(VirtualOrganization object) {
				return object;
			}
		}, fieldUpdater);

		// add column
		table.addColumn(shortNameColumn, "Short name");
		if (width != 0) {
			table.setColumnWidth(shortNameColumn, width, Style.Unit.PX);
		}

		// sort column
		shortNameColumn.setSortable(true);
		table.getColumnSortHandler().setComparator(shortNameColumn, new Comparator<VirtualOrganization>() {
			public int compare(VirtualOrganization o1, VirtualOrganization o2) {
				return o1.getShortName().compareToIgnoreCase(o2.getShortName());
			}
		});

	}

	public static IsClickableCell<GeneralObject> getDefaultClickableAuthz() {

		return new IsClickableCell<GeneralObject>() {

			@Override
			public boolean isClickable(GeneralObject object) {
				VirtualOrganization vo = object.cast();
				return (PerunWebSession.getInstance().isVoAdmin(vo.getId()) || PerunWebSession.getInstance().isVoObserver(vo.getId()));
			}

			@Override
			public String linkUrl(GeneralObject object) {
				VirtualOrganization vo = object.cast();
				return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + VoDetailTabItem.URL + "?id=" + vo.getId()+"&active=1";
			}

		};

	}

}
