package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.TextBox;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesDefinitionV2;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Logout button with image
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class PerunSearchParametersWidget extends Composite {

	
	private FlexTable ft = new FlexTable();
	private CustomButton addParameterButton = new CustomButton("Add parameter", SmallIcons.INSTANCE.addIcon());
	private CustomButton searchButton = new CustomButton("Search", SmallIcons.INSTANCE.magnifierIcon());

	private Map<ListBoxWithObjects<AttributeDefinition>, TextBox> inputs = new HashMap<ListBoxWithObjects<AttributeDefinition>, TextBox>();
	
	private ArrayList<AttributeDefinition> availableAttrDefs = new ArrayList<AttributeDefinition>();
	
	private SearchEvent event;
    private PerunWebSession session = PerunWebSession.getInstance();
	
	/**
	 * Search inteface
	 */
	public interface SearchEvent{
		/**
		 * Search
		 * @param map Key = name, Value = value to search
		 */
		void search(Map<String, String> map);
	}
	
	
	
	/**
	 * Creates a new button
	 */
	public PerunSearchParametersWidget(final PerunEntity entity, SearchEvent event) {
		
		
		this.initWidget(ft);
		
		this.event = event;
		
		final FlexCellFormatter ftf = ft.getFlexCellFormatter();
		
		// add param button
		addParameterButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addParameter();
			}
		});
		
		// search button
		searchButton.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				doSearch();
				
			}
		});
		
		//ft.setHTML(0, 0, "<h3>" + "Search by parameters:" + "</h3>");
		//ftf.setColSpan(0, 0, 4);
		
		// prepare list of attr defs
		GetAttributesDefinitionV2 req = new GetAttributesDefinitionV2(new JsonCallbackEvents(){
			
			public void onLoadingStart(){
				
				// loading
				ft.setText(1, 0, "Loading ...");					
			}
			
			public void onFinished(JavaScriptObject jso){
				
				// parse name we need
				String ent = "";
				switch(entity){
					case USER:
						ent = "user";
						break;
						
					default:
						return;
				}
				
				// take only defs we need
				ArrayList<AttributeDefinition> allDefs = JsonUtils.jsoAsList(jso);
                for(AttributeDefinition def : allDefs)
				{
                    if (def.getEntity().equals(ent)) {
                        availableAttrDefs.add(def);
                    }
				}
                availableAttrDefs = new TableSorter<AttributeDefinition>().sortByAttrDefNameTranslation(availableAttrDefs);
				
				// add parameter button
				addParameter();
                rebuild();

			}
			
		});
		
		req.retrieveData();
		
		
	}
	
	/**
	 * Updates ListBoxes
	 */
	protected void updateListBoxes()
	{
		for(Map.Entry<ListBoxWithObjects<AttributeDefinition>, TextBox> entry : inputs.entrySet())
		{
			ListBoxWithObjects<AttributeDefinition> lb = entry.getKey();
			int selectedItem = lb.getSelectedIndex();
			lb.clear();
			lb.addAllItems(availableAttrDefs);
			lb.setSelectedIndex(selectedItem);
		}
		
	}
	
	/**
	 * Adds a parameter
	 */
	protected void addParameter()
	{

		TextBox tb = new TextBox();
		final ListBoxWithObjects<AttributeDefinition> lb = new ListBoxWithObjects<AttributeDefinition>();
		inputs.put(lb, tb);
		lb.addAllItems(availableAttrDefs);

        rebuild();

	}
	
	protected void doSearch()
	{
		Map<String, String> attrsToSearchBy = new HashMap<String, String>();
		
		for(Map.Entry<ListBoxWithObjects<AttributeDefinition>, TextBox> entry : inputs.entrySet())
		{
			ListBoxWithObjects<AttributeDefinition> lb = entry.getKey();
			
			// value
			String value = entry.getValue().getText();
			
			// attribute name
			String name = lb.getSelectedObject().getName();
			
			attrsToSearchBy.put(name, value);
		}
		
		event.search(attrsToSearchBy);
	}

    /**
     * Method which rebuild whole searcher widget when number of params changes
     */
    protected void rebuild() {

        ft.clear();

        //ft.setHTML(0, 0, "<h3>" + "Search by parameters:" + "</h3>");
        //ft.getFlexCellFormatter().setColSpan(0, 0, 4);

        int row = 1;
        for(Map.Entry<ListBoxWithObjects<AttributeDefinition>, TextBox> entry : inputs.entrySet())
        {

            final ListBoxWithObjects<AttributeDefinition> lb = entry.getKey();
            int selectedItem = lb.getSelectedIndex();
            lb.clear();
            lb.addAllItems(availableAttrDefs);
            lb.setSelectedIndex(selectedItem);

            CustomButton rb = new CustomButton("", SmallIcons.INSTANCE.deleteIcon());

            ft.setWidget(row, 0, lb);
            ft.setText(row, 1, "=");
            ft.setWidget(row, 2, entry.getValue());
            ft.setWidget(row, 3, rb);

            if (inputs.entrySet().size() <= 1) {
                // allow remove if more than 1
                rb.setEnabled(false);
                rb.setTitle("Enabled only when more than 1 parameter is used.");
            }

            rb.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent clickEvent) {
                    inputs.keySet().remove(lb);
                    rebuild();
                }
            });

            row++;

        }

        ft.setWidget(row, 0, addParameterButton);
        ft.setHTML(row, 1, "");
        ft.setWidget(row, 2, searchButton);



    }


}