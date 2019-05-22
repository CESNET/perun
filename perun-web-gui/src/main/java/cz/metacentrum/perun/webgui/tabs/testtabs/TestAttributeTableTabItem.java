package cz.metacentrum.perun.webgui.tabs.testtabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.TestTabs;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.PerunAttributeTableWidget;

import java.util.ArrayList;
import java.util.Map;

/**
 * Inner tab item for shell change
 * User in: SelfResourcesSettingsTabItem
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class TestAttributeTableTabItem implements TabItem, TabItemWithUrl{


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
	private Label titleWidget = new Label("Testing att table");

	/**
	 * Changing shell request
	 *
	 */
	public TestAttributeTableTabItem(){}

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

		PerunAttributeTableWidget.SaveEvent saveEvent = new PerunAttributeTableWidget.SaveEvent() {

			public void save(ArrayList<Attribute> attrs) {

				for(Attribute attr : attrs){

					session.getUiElements().setLogText("New value OK: " + attr.getValue().toString());

				}

			}
		};

		PerunAttributeTableWidget table = new PerunAttributeTableWidget(null, saveEvent, false, listOfAttributes());

		this.contentWidget.setWidget(table);

		return getWidget();

	}


	/**
	 * Prepares a list of attributes
	 * @return
	 */
	static public ArrayList<Attribute> listOfAttributes()
	{
		String jsonStr = "[{\"value\":\"Václav Mach\",\"valueCreatedAt\":null,\"valueCreatedBy\":null,\"valueModifiedAt\":null,\"valueModifiedBy\":null,\"type\":\"java.lang.String\",\"description\":\"User's display name\",\"namespace\":\"urn:perun:user:attribute-def:core\",\"friendlyNameParameter\":\"\",\"entity\":\"user\",\"friendlyName\":\"displayName\",\"baseFriendlyName\":\"displayName\",\"createdAt\":\"2011-12-07 23:02:31.0\",\"createdBy\":\"PERUNV3\",\"modifiedAt\":\"2011-12-07 23:02:31.0\",\"modifiedBy\":\"PERUNV3\",\"id\":1140,\"beanName\":\"Attribute\"},{\"value\":\"FI\",\"valueCreatedAt\":\"2012-08-17 14:32:13.0\",\"valueCreatedBy\":\"Synchronizer\",\"valueModifiedAt\":\"2012-08-17 14:32:13.0\",\"valueModifiedBy\":\"Synchronizer\",\"type\":\"java.lang.String\",\"description\":\"User's workplace\",\"namespace\":\"urn:perun:user:attribute-def:def\",\"friendlyNameParameter\":\"\",\"entity\":\"user\",\"friendlyName\":\"workplace\",\"baseFriendlyName\":\"workplace\",\"createdAt\":\"2012-08-17 12:34:21.0\",\"createdBy\":\"michalp@META\",\"modifiedAt\":\"2012-08-17 12:34:21.0\",\"modifiedBy\":\"michalp@META\",\"id\":1366,\"beanName\":\"Attribute\"},{\"value\":[\"374430@eduroam.muni.cz\"],\"valueCreatedAt\":\"2012-04-26 08:37:54.0\",\"valueCreatedBy\":\"michalp@META\",\"valueModifiedAt\":\"2012-04-26 08:37:54.0\",\"valueModifiedBy\":\"michalp@META\",\"type\":\"java.util.ArrayList\",\"description\":\"List of eduroam identities\",\"namespace\":\"urn:perun:user:attribute-def:def\",\"friendlyNameParameter\":\"\",\"entity\":\"user\",\"friendlyName\":\"eduroamIdentities\",\"baseFriendlyName\":\"eduroamIdentities\",\"createdAt\":\"2012-03-21 09:23:14.0\",\"createdBy\":\"michalp@META\",\"modifiedAt\":\"2012-03-21 09:23:14.0\",\"modifiedBy\":\"michalp@META\",\"id\":1060,\"beanName\":\"Attribute\"},{\"value\":\"+420 123 456 789\",\"valueCreatedAt\":\"2012-08-17 14:30:53.0\",\"valueCreatedBy\":\"Synchronizer\",\"valueModifiedAt\":\"2012-12-05 13:01:13.0\",\"valueModifiedBy\":\"Synchronizer\",\"type\":\"java.lang.String\",\"description\":\"User's phone\",\"namespace\":\"urn:perun:user:attribute-def:def\",\"friendlyNameParameter\":\"\",\"entity\":\"user\",\"friendlyName\":\"phone\",\"baseFriendlyName\":\"phone\",\"createdAt\":\"2012-08-17 12:32:30.0\",\"createdBy\":\"michalp@META\",\"modifiedAt\":\"2012-08-17 12:32:30.0\",\"modifiedBy\":\"michalp@META\",\"id\":1362,\"beanName\":\"Attribute\"}]";
		JavaScriptObject jso = JsonUtils.parseJson(jsonStr);
		return JsonUtils.jsoAsList(jso);
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
		final int prime = 1123;
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


	public final static String URL = "attr-table";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return TestTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}


	static public TestAttributeTableTabItem load(Map<String, String> parameters)
	{
		return new TestAttributeTableTabItem();
	}


}
