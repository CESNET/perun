package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.propagationStatsReader.GetFacilityState;
import cz.metacentrum.perun.webgui.model.FacilityState;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.PerunAdminTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.DestinationResultsTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Page with Tasks and their propagation status
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz> 
 * @version $Id$
 */

public class PropagationsTabItem implements TabItem, TabItemWithUrl{

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
    private Label titleWidget = new Label("Propagations");

    /**
     * Creates a tab instance
     */
    public PropagationsTabItem(){ }

    private int mainrow = 0;
    private int okCounter = 0;
    private int errorCounter = 0;
    private int notDeterminedCounter = 0;
    private int procesingCounter = 0;

    public boolean isPrepared(){
        return true;
    }

    public Widget draw() {

        mainrow = 0;
        okCounter = 0;
        errorCounter = 0;
        notDeterminedCounter = 0;
        procesingCounter = 0;
        VerticalPanel mainTab = new VerticalPanel();
        mainTab.setWidth("100%");

        final TabItem tab = this;

        // MAIN PANEL
        final ScrollPanel firstTabPanel = new ScrollPanel();
        firstTabPanel.setSize("100%", "100%");
        firstTabPanel.setStyleName("perun-tableScrollPanel");

        final FlexTable help = new FlexTable();
        help.setCellPadding(4);
        help.setWidth("100%");
        help.setHTML(0, 0, "<strong>Color&nbsp;notation:</strong>");
        help.getFlexCellFormatter().setWidth(0, 0, "100px");
        help.setHTML(0, 1, "<strong>OK</strong>");
        help.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
        help.getFlexCellFormatter().setWidth(0, 1, "50px");
        help.getFlexCellFormatter().setStyleName(0, 1, "green");
        help.setHTML(0, 2, "<strong>Error</strong>");
        help.getFlexCellFormatter().setWidth(0, 2, "50px");
        help.getFlexCellFormatter().setStyleName(0, 2, "red");
        help.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
        help.setHTML(0, 3, "<strong>Not&nbsp;determined</strong>");
        help.getFlexCellFormatter().setWidth(0, 3, "50px");
        help.getFlexCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
        help.getFlexCellFormatter().setStyleName(0, 3, "notdetermined");
        /*
        help.setHTML(0, 4, "<strong>Processing</strong>");
        help.getFlexCellFormatter().setWidth(0, 4, "50px");
        help.getFlexCellFormatter().setStyleName(0, 4, "yellow");
        help.getFlexCellFormatter().setHorizontalAlignment(0, 4, HasHorizontalAlignment.ALIGN_CENTER);
        */

        final CustomButton cb = new CustomButton(ButtonTranslation.INSTANCE.refreshButton(), ButtonTranslation.INSTANCE.refreshPropagationResults(),SmallIcons.INSTANCE.updateIcon(), new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().reloadTab(tab);
            }
        });

        help.setWidget(0, 5, cb);
        help.getFlexCellFormatter().setWidth(0, 5, "200px");

        help.setHTML(0, 6, "&nbsp;");
        help.getFlexCellFormatter().setWidth(0, 6, "50%");

        mainTab.add(help);
        mainTab.add(new HTML("<hr size=\"2\" />"));
        mainTab.add(firstTabPanel);

        final FlexTable content = new FlexTable();
        content.setWidth("100%");
        content.setBorderWidth(0);
        firstTabPanel.add(content);
        content.setStyleName("propagationTable", true);
        final AjaxLoaderImage im = new AjaxLoaderImage();
        content.setWidget(0, 0, im);
        content.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

        final GetFacilityState callback = new GetFacilityState(0, 0, new JsonCallbackEvents(){
            public void onLoadingStart(){
                im.loadingStart();
                cb.setProcessing(true);
            }
            public void onError(PerunError error){
                im.loadingError(error);
                cb.setProcessing(false);
            }
            public void onFinished(JavaScriptObject jso) {
                im.loadingFinished();
                cb.setProcessing(false);
                content.clear();
                content.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
                ArrayList<FacilityState> list = JsonUtils.jsoAsList(jso);
                if (list != null && !list.isEmpty()){

                    list = new TableSorter<FacilityState>().sortByFacilityName(list);

                    ArrayList<FacilityState> cluster = new ArrayList<FacilityState>();
                    ArrayList<FacilityState> host = new ArrayList<FacilityState>();
                    ArrayList<FacilityState> storage = new ArrayList<FacilityState>();
                    ArrayList<FacilityState> general = new ArrayList<FacilityState>();


                    for (FacilityState st : list) {
                        if (st.getFacility().getType().equalsIgnoreCase("cluster") || st.getFacility().getType().equalsIgnoreCase("vcluster")) {
                            cluster.add(st);
                        } else if (st.getFacility().getType().equalsIgnoreCase("host") || st.getFacility().getType().equalsIgnoreCase("vhost")) {
                            host.add(st);
                        } else if (st.getFacility().getType().equalsIgnoreCase("storage")) {
                            storage.add(st);
                        } else {
                            general.add(st);
                        }
                    }

                    // PROCESS CLUSTERS AND VCLUSTERS

                    for (final FacilityState state : cluster) {

                        content.setHTML(mainrow, 0, "<strong>" + state.getFacility().getName() + "&nbsp;(" + state.getFacility().getType() + ")</strong>");

                        final FlowPanel inner = new FlowPanel();
                        content.setWidget(mainrow+1, 0, inner);
                        content.getFlexCellFormatter().setStyleName(mainrow + 1, 0, "propagationTablePadding");

                        Set<String> destinations = state.getDestinations().keySet();
                        ArrayList<String> destList = new ArrayList<String>();
                        int width = 0;
                        for (String dest : destinations) {
                            destList.add(dest);
                            if (dest.indexOf(".")*8 > width) {
                                width = dest.indexOf(".")*8;
                            }
                        }

                        Collections.sort(destList);

                        for (final String dest : destList) {

                            String show = dest.substring(0, dest.indexOf("."));
                            Anchor hyp = new Anchor();
                            hyp.setHTML("<span style=\"display: inline-block; width: "+width+"px; text-align: center;\">"+show+"</span>");
                            hyp.addClickHandler(new ClickHandler() {
                                public void onClick(ClickEvent clickEvent) {
                                    session.getTabManager().addTab(new DestinationResultsTabItem(state.getFacility(), null, dest, true));
                                }
                            });
                            inner.add(hyp);

                            // style
                            if (state.getDestinations().get(dest).equals(new JSONString("ERROR"))) {
                                hyp.addStyleName("red");
                                errorCounter++;
                            } else if (state.getDestinations().get(dest).equals(new JSONString("OK"))) {
                                hyp.addStyleName("green");
                                okCounter++;
                            } else {
                                hyp.addStyleName("notdetermined");
                                notDeterminedCounter++;
                            }

                        }

                        if (destList.isEmpty()) {
                            notDeterminedCounter++;
                        }

                        mainrow++;
                        mainrow++;

                    }

                    // PROCESS HOSTS and VHOSTS

                    int width = 0;
                    for (FacilityState state : host) {
                        if (state.getFacility().getName().length()*8 > width) {
                            width = state.getFacility().getName().length()*8;
                        }
                    }

                    FlowPanel inner = new FlowPanel();
                    for (final FacilityState state : host) {

                        Set<String> destinations = state.getDestinations().keySet();
                        ArrayList<String> destList = new ArrayList<String>();
                        for (String dest : destinations) {
                            destList.add(dest);
                        }

                        Collections.sort(destList);

                        for (final String dest : destList) {

                            Anchor hyp = new Anchor();
                            hyp.setHTML("<span style=\"display: inline-block; width: "+width+"px; text-align: center;\">"+dest+"</span>");
                            inner.add(hyp);
                            hyp.addClickHandler(new ClickHandler() {
                                public void onClick(ClickEvent clickEvent) {
                                    session.getTabManager().addTab(new DestinationResultsTabItem(state.getFacility(), null, dest, true));
                                }
                            });

                            // style
                            if (state.getDestinations().get(dest).equals(new JSONString("ERROR"))) {
                                hyp.addStyleName("red");
                                errorCounter++;
                            } else if (state.getDestinations().get(dest).equals(new JSONString("OK"))) {
                                hyp.addStyleName("green");
                                okCounter++;
                            }  else {
                                hyp.addStyleName("notdetermined");
                                notDeterminedCounter++;
                            }
                        }

                        if (destList.isEmpty()) {
                            Anchor hyp = new Anchor();
                            hyp.setHTML("<span style=\"display: inline-block; width: "+width+"px; text-align: center;\">"+state.getFacility().getName()+"</span>");
                            inner.add(hyp);
                            hyp.addStyleName("notdetermined");
                            notDeterminedCounter++;
                        }

                    }

                    if (!host.isEmpty()) {
                        content.setHTML(mainrow, 0, "<strong>Hosts and vHosts</strong>");
                        mainrow++;
                    }
                    content.setWidget(mainrow, 0, inner);
                    content.getFlexCellFormatter().setStyleName(mainrow, 0, "propagationTablePadding");
                    mainrow++;

                    // PROCESS REST

                    ArrayList<FacilityState> rest = new ArrayList<FacilityState>();
                    rest.addAll(storage);
                    rest.addAll(general);

                    for (final FacilityState state : rest) {

                        content.setHTML(mainrow, 0, "<strong>" + state.getFacility().getName() + "&nbsp;(" + state.getFacility().getType() + ")</strong>");

                        final FlowPanel inner2 = new FlowPanel();
                        content.setWidget(mainrow+1, 0, inner2);
                        content.getFlexCellFormatter().setStyleName(mainrow+1, 0, "propagationTablePadding");

                        Set<String> destinations = state.getDestinations().keySet();
                        ArrayList<String> destList = new ArrayList<String>();
                        int width2 = 0;
                        for (String dest : destinations) {
                            destList.add(dest);
                            if (dest.indexOf(".")*8 > width2) {
                                width2 = dest.indexOf(".")*8;
                            }
                        }

                        Collections.sort(destList);

                        for (final String dest : destList) {

                            String show = dest.substring(0, dest.indexOf("."));
                            Anchor hyp = new Anchor();
                            hyp.setHTML("<span style=\"display: inline-block; width: "+width2+"px; text-align: center;\">"+show+"</span>");
                            hyp.addClickHandler(new ClickHandler() {
                                public void onClick(ClickEvent clickEvent) {
                                    session.getTabManager().addTab(new DestinationResultsTabItem(state.getFacility(), null, dest, true));
                                }
                            });
                            inner2.add(hyp);

                            // style
                            if (state.getDestinations().get(dest).equals(new JSONString("ERROR"))) {
                                hyp.addStyleName("red");
                                errorCounter++;
                            } else if (state.getDestinations().get(dest).equals(new JSONString("OK"))) {
                                hyp.addStyleName("green");
                                okCounter++;
                            } else {
                                hyp.addStyleName("notdetermined");
                                notDeterminedCounter++;
                            }

                        }

                        if (destList.isEmpty()) {
                            notDeterminedCounter++;
                        }

                        mainrow++;
                        mainrow++;

                    }

                    // set counters
                    help.setHTML(0, 1, "<strong>Ok&nbsp;("+okCounter+")</strong>");
                    help.setHTML(0, 2, "<strong>Error&nbsp;("+errorCounter+")</strong>");
                    help.setHTML(0, 3, "<strong>Not&nbsp;determined&nbsp;("+notDeterminedCounter+")</strong>");
                    //help.setHTML(0, 4, "<strong>Processing&nbsp;(" + procesingCounter + ")</strong>");


                }
            }
        }); // get for all facilities for VO
        callback.retrieveData();

        // resize perun table to correct size on screen
        session.getUiElements().resizePerunTable(firstTabPanel, 400, this);

        this.contentWidget.setWidget(mainTab);
        return getWidget();
    }

    public Widget getWidget() {
        return this.contentWidget;
    }

    public Widget getTitle() {
        return this.titleWidget;
    }

    public ImageResource getIcon() {
        return SmallIcons.INSTANCE.arrowRightIcon();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 122341;
        return result;
    }

    /**
     * @param obj
     */
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
        session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Propagations", getUrlWithParameters());
    }

    public boolean isAuthorized() {

        if (session.isPerunAdmin()) {
            return true;
        } else {
            return false;
        }

    }

    public final static String URL = "propags";

    public String getUrl()
    {
        return URL;
    }

    public String getUrlWithParameters()
    {
        return PerunAdminTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
    }

    static public PropagationsTabItem load(Map<String, String> parameters)
    {
        return new PropagationsTabItem();
    }
}