package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.*;

import java.util.Map;

/**
 * Pages, which are in facility admin part of menu
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 3e399fce015f85ec33b7c1fe7e57eb0e0567b171 $
 */
public class FacilitiesTabs {

	private PerunWebSession session = PerunWebSession.getInstance();
	
	static public final String URL = "fac";
	
	/**
	 * Creates a new instance of pages
     */
	public FacilitiesTabs(){}

	/**
	 * Loads the page
	 * 
	 * @return true on success / false otherwise
	 */
	public boolean loadTab(final String tab, final Map<String, String> parameters) {
		
		if(tab == null){
			return false;				
		}
		
		// if active
		final boolean open = ("1".equals(parameters.get("active")));
					
		if (tab.equals(FacilitiesSelectTabItem.URL)) {
			session.getTabManager().addTab(new FacilitiesSelectTabItem(), open);
			return true;
		}
		
		if (tab.equals(FacilityDetailTabItem.URL)) {
			session.getTabManager().addTab(FacilityDetailTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(FacilityResourcesTabItem.URL)) {
			session.getTabManager().addTab(FacilityResourcesTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(FacilityHostsTabItem.URL)) {
			session.getTabManager().addTab(FacilityHostsTabItem.load(parameters), open);
			return true;
		}

        if (tab.equals(FacilityHostsSettingsTabItem.URL)) {
            session.getTabManager().addTab(FacilityHostsSettingsTabItem.load(parameters), open);
            return true;
        }
		
		if (tab.equals(FacilityStatusTabItem.URL)) {
			session.getTabManager().addTab(FacilityStatusTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(FacilityDestinationsTabItem.URL)) {
			session.getTabManager().addTab(FacilityDestinationsTabItem.load(parameters), open);
			return true;
		}
		if(tab.equals(FacilityPropagationTabItem.URL)){
			session.getTabManager().addTab(FacilityPropagationTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(FacilitySettingsTabItem.URL)) {
			session.getTabManager().addTab(FacilitySettingsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(FacilityAllowedGroupsTabItem.URL)) {
			session.getTabManager().addTab(FacilityAllowedGroupsTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(TaskResultsTabItem.URL)) {
			session.getTabManager().addTab(TaskResultsTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(FacilityOwnersTabItem.URL)) {
			session.getTabManager().addTab(FacilityOwnersTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(FacilityManagersTabItem.URL)) {
			session.getTabManager().addTab(FacilityManagersTabItem.load(parameters), open);
			return true;
		}

        if (tab.equals(FacilitiesPropagationsTabItem.URL)) {
            session.getTabManager().addTab(FacilitiesPropagationsTabItem.load(parameters), open);
            return true;
        }

        if (tab.equals(DestinationResultsTabItem.URL)) {
            session.getTabManager().addTab(DestinationResultsTabItem.load(parameters), open);
            return true;
        }

        if (tab.equals(CreateFacilityTabItem.URL)) {
            session.getTabManager().addTab(CreateFacilityTabItem.load(parameters), open);
            return true;
        }
		
		return false;
		
	}
	
}