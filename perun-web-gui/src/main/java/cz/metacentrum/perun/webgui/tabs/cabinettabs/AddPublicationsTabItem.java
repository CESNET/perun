package cz.metacentrum.perun.webgui.tabs.cabinettabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.cabinetManager.*;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.userstabs.IdentitySelectorTabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab for importing publications from external systems into Perun
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class AddPublicationsTabItem implements TabItem, TabItemWithUrl, TabItemWithHelp {

	// TAB NEEDED
    private PerunWebSession session = PerunWebSession.getInstance();
	private User user;
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Add publications");
	private int userId;
	private SimplePanel helpWidget = new SimplePanel();

	// STATE
	private enum State { START, CREATE, IMPORT, REVIEW };
	private State state = State.START;  // START by default
	private State previousState; // to determine if was CREATE or IMPORT in REVIEW

	// WORKFLOW NEEDED
	private ArrayList<Publication> importedPublications = new ArrayList<Publication>();
	private boolean hadError = false; // if error during import
	private int counter = 0; // counter of import requests to synchronize them
	private TabItem tab; // tab to be closed or reloaded
	private ArrayList<Category> categories = new ArrayList<Category>(); // available categories
	private int defaultCategoryId; // default category ID

	
	/**
	 * Creates a tab instance
	 *
     */
	public AddPublicationsTabItem() {
		this.user = session.getUser();
		this.userId = session.getUser().getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param user
     */
	public AddPublicationsTabItem(User user){
		this.user = user;
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance
     * @param userId
     */
	public AddPublicationsTabItem(int userId){
		this.userId = userId;
        new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                user = jso.cast();
            }
        }).retrieveData();
	}

	public boolean isPrepared(){
		return !(user == null);
	}

	public Widget draw() {

		tab = this; // save this tab for reloading
		this.contentWidget.clear(); // clear content if previously used
		helpWidget.clear(); // clear help
		
		if (state == State.START) {
			
			previousState = State.START; // to help determine what was
			
			loadStartScreen();
			
			titleWidget.setText("Add publications");
			
			helpWidget.add(new HTML("<ol style=\"line-height:1.5;\"><li><strong>Please select either \"Create\" or \"Import\" for new publication.</strong>" +
					"<p> - To add custom publication or report publication for somebody else choose \"Create\".</p>" +
					"<p> - To add publication you already reported in IS MU / OBD ZČU please choose \"Import\".</p></li><ol>"));			
			
		} else if (state == State.CREATE) {
			
			previousState = State.CREATE; // to help determine what was
			
			loadCreateScreen();
			
			titleWidget.setText("Create publication");
			
			helpWidget.add(new HTML("<ol style=\"line-height:1.5;\"><li><strong>Enter publication title and full citation.</strong></li>" +
					"<li>If known, <strong>enter publication's ISBN / ISSN / DOI</strong>" +
					"<li>If you don't want to be automatically added as author of publications, uncheck \"Add me as author\" checkbox.</li>" +
					"<li>Please use <strong>\"Check\" button</strong> to see, if same publication already exists in Perun. If so, you don't have to create new, just add yourself as author of publication.</li>" +
					"<li>When satisfied, <strong>click on \"Create\" button</strong> to submit your publication.</li>" +
					"<li>You will be then able to review publication details.</li><ol>"));			
	
		} else if (state == State.IMPORT) {

			previousState = State.IMPORT; // to help determine what was
			
			loadImportScreen();
			
			titleWidget.setText("Import publications");
			
			helpWidget.add(new HTML("<ol style=\"line-height:1.5;\"><li><strong>Select ext. publication system for searching</strong>. Currently we can import publications reported in IS MU and OBD ZČU.</li>" +
					"<li><strong>Select year range</strong> to search and click on \"Search in\" button." +
					"<p> - Search in IS MU is based on your's UČO." +
					"<br /> - Search in OBD ZČU is based on your last name.</p></li>" +
					"<li>If you don't want to be automatically added as author of publications, uncheck \"Add me as author\" checkbox.</li>"+
					"<li><strong>Select publications to import</strong> and <strong>click on \"Import\" button.</strong></li>" +
					"<li>You will be then able to review publication details." +
					"<p> - If some of your colleagues already reported same publication, some properties may already be set.</p></li><ol>"));			

			
		} else if (state == State.REVIEW){

			loadReviewScreen();
			
			// help widget
			helpWidget.add(new HTML("<ol style=\"line-height:1.5;\"><li><strong>Click on publication title</strong> to edit it's details." +
					"<p> - If some of your colleagues already reported same publication, some properties may already be set.</p>" +
					"<li><strong>Update full citation</strong> if needed and click on \"Save changes\" button.</li>" +
					"<li><strong>Select category</strong> where publication belongs and click on \"Save changes\" button. If not sure, keep it as it is (\"Ke kontrole\").</li>" +
					"<li><strong>Add acknowledgment</strong> mentioned in your publication. Typically \"MetaCentrum\" or \"CERIT-SC\".</li>" +
					"<li><strong>Add authors</strong> of your publication. Authors can be only users of PERUN. If not added, they can't have benefits from reporting publication.</li>" +
					"<li>When you are done, <strong>click on \"Finish\"</strong> button left of publication title. When clicked, publication is removed from the view and can't be edited anymore.</li></ol>"));			

		}

		return getWidget();

	}
	
	/**
	 * Draw tab content for START state - selection of create / import
	 */
	private void loadStartScreen() {

		HorizontalPanel vp = new HorizontalPanel();
		vp.setHeight("500px");
		vp.setWidth("1000px");
		
		// IMPORT LAYOUT
		DecoratorPanel dp = new DecoratorPanel();
		FlexTable importLayout = new FlexTable();
        importLayout.setCellSpacing(10);
		dp.add(importLayout);
		// button
		CustomButton importButton = new CustomButton("Import publication", SmallIcons.INSTANCE.addIcon());
		// layout
		importLayout.setHTML(0, 0, "<h3>Import publication</h3>");
		importLayout.setHTML(1, 0, "<strong>Are you from MU, ZCU ? You can now import publications </br> you already reported in university publication systems.</strong>");
		importLayout.setWidget(2, 0, importButton);
		
		// CREATE LAYOUT
		DecoratorPanel dp2 = new DecoratorPanel();
		FlexTable createLayout = new FlexTable();
        createLayout.setCellSpacing(10);
		dp2.add(createLayout);
		// button
		CustomButton createButton = new CustomButton("Create publication", SmallIcons.INSTANCE.addIcon());
		// layout
		createLayout.setHTML(0, 0, "<h3>Create publication</h3>");
		createLayout.setHTML(1, 0, "<strong>When you want to add custom publication or report publication for somebody else.</strong>");
		createLayout.setWidget(2, 0, createButton);
		
		// ADD CONTENT
		vp.add(dp);
		dp.setWidth("400px");
		vp.add(dp2);
		dp2.setWidth("400px");
		vp.setCellVerticalAlignment(dp, HasVerticalAlignment.ALIGN_MIDDLE);
		vp.setCellVerticalAlignment(dp2, HasVerticalAlignment.ALIGN_MIDDLE);
		vp.setCellHorizontalAlignment(dp, HasHorizontalAlignment.ALIGN_CENTER);
		vp.setCellHorizontalAlignment(dp2, HasHorizontalAlignment.ALIGN_CENTER);
		
		// CLICK HANDLERS
		
		importButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				state = State.IMPORT;
				session.getTabManager().reloadTab(tab);
			}
		});
		
		createButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				state = State.CREATE;
				session.getTabManager().reloadTab(tab);
			}
		});
		
		this.contentWidget.setWidget(vp);
		
	}
	
	/**
	 * Draw tab content for CREATE state.
	 */
	private void loadCreateScreen() {
		
		// MAIN PANEL
		final VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setSize("100%", "100%");
		
		// MAIN MENU
		TabMenu menu = new TabMenu();
		mainPanel.add(menu);
        mainPanel.setCellHeight(menu, "30px");
		// splitter
		HTML splitter = new HTML("<hr size=\"2px\" width=\"100%\" />");
		mainPanel.add(splitter);
		
		// Widgets
		CustomButton backButton = TabMenu.getPredefinedButton(ButtonType.BACK, "Go back to start page - !! ALL UNSAVED CHANGES WILL BE LOST !!", new ClickHandler(){
			public void onClick(ClickEvent event) {
				importedPublications.clear();
				hadError = false;
				state = State.START; // go back to original state
				session.getTabManager().reloadTab(tab);
			}
		});
		menu.addWidget(backButton);
		
		final TextBox title = new TextBox();
		title.setMaxLength(1024);
		final TextBox isbn = new TextBox();
		isbn.setMaxLength(32);
		final TextBox doi = new TextBox();
		doi.setMaxLength(256);
		final TextArea cite = new TextArea();
		cite.setSize("300px", "50px");
		cite.getElement().setAttribute("maxlength", "4000");
		final ListBox year = new ListBox();
		final CheckBox addAsAuthor = new CheckBox("Add me as author");
		addAsAuthor.setValue(true);
		addAsAuthor.setTitle("When checked, you will be automatically added as author of created publication");
		
		final CustomButton finishButton = TabMenu.getPredefinedButton(ButtonType.CREATE, "Create publication in Perun");
        menu.addWidget(finishButton);
        finishButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				// check
				if (title.getText().equalsIgnoreCase("") || cite.getText().equalsIgnoreCase("")) {
					Confirm c = new Confirm("Please fill publication details first", new HTML("<p>\"Title\" and \"Full cite\" can't be empty."), true);
					c.setNonScrollable(true);
                    c.show();
					return;
				}
				// create
				CreatePublication request = new CreatePublication(JsonCallbackEvents.disableButtonEvents(finishButton, new JsonCallbackEvents(){
					public void onFinished(JavaScriptObject jso) {
						Publication pub = jso.cast();
						importedPublications.add(pub);
						counter--;
						if (addAsAuthor.getValue()) {
							CreateAuthorship request = new CreateAuthorship();
							request.createAuthorship(pub.getId(), userId);
							previousState = State.CREATE;
							state = State.REVIEW;
							session.getTabManager().reloadTab(tab);
						}
					}
					@Override
					public void onError(PerunError error) {
						hadError = true;
						counter--;
					}
				}));
				request.createPublication(title.getText(), defaultCategoryId, Integer.parseInt(year.getValue(year.getSelectedIndex())), isbn.getText(), doi.getText(), cite.getText());				
				counter++;
			}
		});
		
		// checking
		final Map<String, Object> ids = new HashMap<String, Object>();
		ids.put("userId", session.getUser().getId());
		ids.put("authors", 1);
		final FindSimilarPublications filterCall = new FindSimilarPublications(ids);
		filterCall.setCheckable(false);
		CellTable<Publication> table = filterCall.getEmptyTable(new FieldUpdater<Publication, String>() {
			public void update(int index, Publication object, String value) {
				session.getTabManager().addTab(new PublicationDetailTabItem(object, true));
			}
		});
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel();
		sp.add(table);
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);
		table.setWidth("100%");
		table.removeColumn(0);
		table.removeColumn(5);
		table.removeColumn(4);
		table.removeColumn(3);
		
		// check layout
		final FlexTable checkLayout = new FlexTable();
		checkLayout.setVisible(false);
		
		CustomButton checkButton = new CustomButton("Check", "Check if same publication exist in Perun", SmallIcons.INSTANCE.booksIcon());
        filterCall.setEvents(JsonCallbackEvents.disableButtonEvents(checkButton));
        menu.addWidget(checkButton);
        checkButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				checkLayout.setVisible(true);
				
				ids.clear();
				ids.put("authors", 1);
				if (!title.getText().equals("")) {
					ids.put("title", title.getText());
				}
				if (!isbn.getText().equals("")) {
					ids.put("isbn", isbn.getText());
				}
				if (!doi.getText().equals("")) {
					ids.put("doi", doi.getText());
				}
				filterCall.clearTable();
				filterCall.retrieveData();
			}
		});
		
		menu.addWidget(new Image(LargeIcons.INSTANCE.errorIcon()));
		menu.addWidget(new HTML("<span style=\"font-size: 12pt;\">Please check for existence of same publication before submission.</span>"));

        // TODO - visible error notice !!
		FindAllCategories request = new FindAllCategories(new JsonCallbackEvents(){
			@Override
            public void onFinished(JavaScriptObject jso){
				categories = JsonUtils.<Category>jsoAsList(jso);
				if (!categories.isEmpty()) {
					for (Category c : categories) {
						if (c.getName().equalsIgnoreCase("Ke kontrole")) {
							finishButton.setEnabled(true); // enable create if not empty and default found
							defaultCategoryId = c.getId(); // set default
						}
					}
				}

			}
            @Override
            public void onError(PerunError error){
                finishButton.setEnabled(false);
			}
            @Override
            public void onLoadingStart(){
                finishButton.setEnabled(false);
            }
		});
		request.retrieveData();

		// set year
		for (int i=2004; i<= JsonUtils.getCurrentYear(); i++) {
			year.addItem(String.valueOf(i));
		}
		year.setSelectedIndex(year.getItemCount()-1);
		
		// layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		layout.setSize("100%", "200px");
		layout.setWidget(0, 0, new Image(LargeIcons.INSTANCE.bookEditIcon()));
		layout.setHTML(0, 1, "<h3>Create new publication</h3>");
		layout.getFlexCellFormatter().setColSpan(0, 1, 2);
		// form
		layout.setHTML(1, 1, "Title:");
		layout.setWidget(1, 2, title);
		layout.setHTML(2, 1, "Year:");
		layout.setWidget(2, 2, year);
		layout.setHTML(3, 1, "ISBN&nbsp;/&nbsp;ISSN:");
		layout.setWidget(3, 2, isbn);
		layout.setHTML(4, 1, "DOI:");
		layout.setWidget(4, 2, doi);
		layout.setHTML(5, 1, "Full&nbsp;cite:");
		layout.setWidget(5, 2, cite);
		layout.setWidget(6, 2, addAsAuthor);

        for (int i=1; i<layout.getRowCount(); i++) {
            layout.getFlexCellFormatter().setStyleName(i, 0, "itemName");
        }
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSize("100%","100%");
		hp.add(layout);
		hp.setCellWidth(layout, "200px");
		
		checkLayout.setWidget(0, 0, new Image(LargeIcons.INSTANCE.helpIcon()));
		checkLayout.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
		checkLayout.setHTML(0, 1, "If desired publication already exists (i.e. was reported by one of your colleagues), please check if you are among it's authors ('reported by' column). If so, you don't have to report publication again. You can add yourself as author on publication detail page displayed by clicking on the row. <strong>If publication is locked, notify administrator (meta@cesnet.cz) about your request.</strong>");
		checkLayout.setWidget(1, 0, sp);
		checkLayout.getFlexCellFormatter().setColSpan(1, 0, 2);
		hp.add(checkLayout);
		
		
		mainPanel.add(hp);
		
		this.contentWidget.setWidget(mainPanel);
		
	}
	
	/**
	 * Draw tab content for IMPORT state.
	 */
	private void loadImportScreen() {

		// MAIN PANEL
		final VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setSize("100%", "100%");

		// MAIN MENU
		TabMenu menu = new TabMenu();
		mainPanel.add(menu);
        mainPanel.setCellHeight(menu, "30px");

        CustomButton searchButton = new CustomButton("Search in", SmallIcons.INSTANCE.booksIcon());
        searchButton.setTitle("Search selected external source for your's publications");

        // CALLBACK
		final FindExternalPublications find = new FindExternalPublications(userId, JsonCallbackEvents.disableButtonEvents(searchButton));

		// WIDGETS
		final CustomButton importButton = new CustomButton("Import", SmallIcons.INSTANCE.addIcon());
		importButton.setTitle("Loading...");

		final ListBox yearSince = new ListBox(false);
		final ListBox yearTill = new ListBox(false);

		final CheckBox addAsAuthor = new CheckBox("Add me as author");
		addAsAuthor.setValue(true);
		addAsAuthor.setTitle("When checked, you will be automatically added as author of imported publications");

		final ListBox namespace = new ListBox(false);
		namespace.addItem("Prezentator (MU)", "mu");
		namespace.addItem("OBD 3.0 (ZČU)", "zcu");
		namespace.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event) {
				// set namespace on change
				find.setNamespace(namespace.getValue(namespace.getSelectedIndex()));
			}
		});
		namespace.setTitle("Select publications external source to search in.");
		find.setNamespace("mu"); // mu by default

		// save for import
		importButton.setEnabled(false);
		FindAllCategories request = new FindAllCategories(new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				categories = JsonUtils.jsoAsList(jso);
				if (!categories.isEmpty()) {
					for (Category c : categories) {
						if (c.getName().equalsIgnoreCase("Ke kontrole")) {
							importButton.setTitle("Import selected publications into Perun");
							defaultCategoryId = c.getId(); // set default
						}
					}
				}

			}
			public void onError(PerunError error){
				importButton.setTitle("Unable to retrieve default category. Please refresh tab.");
                importButton.setEnabled(false);
			}
		});
		request.retrieveData();

		importButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {

				for (final Publication pub : find.getTableSelectedList()) {

					pub.setCategoryId(defaultCategoryId); // set default category for import

					CreatePublication request = new CreatePublication(new JsonCallbackEvents(){
						public void onFinished(JavaScriptObject jso) {
							Publication pub = jso.cast();
							importedPublications.add(pub);
							counter--;
							if (addAsAuthor.getValue() && !pub.getLocked()) {
								// create authorship only if wanted and not locked
								CreateAuthorship request = new CreateAuthorship();
								request.createAuthorship(pub.getId(), userId);								
							}
						}
						@Override
						public void onError(PerunError error) {
							// FIXME - for local testing
							/*
							pub.setId(624);
							pub.setLocked(false);
							pub.setCategoryName("Ke kontrole");
							importedPublications.add(pub);
							*/
							counter--;
							hadError = true;
						}
					});
					request.createPublication(pub);
					counter++;
				}

				Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
					public boolean execute() {
						if (counter == 0) {  
							state = State.REVIEW;
							session.getTabManager().reloadTab(tab); // reload this tab with imported publications
							return false;
						} else  {
							return true;
						}

					}
				}, 500); // run every 500ms

			}
		});

		searchButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if ( userId == 0 ){ 
					Window.alert("User ID = 0 is not valid. Please select User tu search for his/hers publications."); 
					return; 
				}		
				find.setUser(userId);
				find.setYearSince(Integer.parseInt(yearSince.getValue(yearSince.getSelectedIndex())));
				find.setYearTill(Integer.parseInt(yearTill.getValue(yearTill.getSelectedIndex())));
				find.clearTable();
				find.retrieveData();
			}
		});

		// FILL LISTBOXS
		for (int i=2004; i<=JsonUtils.getCurrentYear(); i++) {
			yearSince.addItem(String.valueOf(i));
			yearTill.addItem(String.valueOf(i));
		}
		yearSince.setSelectedIndex(yearSince.getItemCount()-2);
		yearTill.setSelectedIndex(yearTill.getItemCount()-1);

		// PUT WIDGETS IN MENU
		
		CustomButton backButton = TabMenu.getPredefinedButton(ButtonType.BACK, "Go back to start page - !! ALL UNSAVED CHANGES WILL BE LOST !!", new ClickHandler(){
			public void onClick(ClickEvent event) {
				importedPublications.clear();
				hadError = false;
				state = State.START; // go back to original state
				session.getTabManager().reloadTab(tab);
			}
		});

        menu.addWidget(backButton);
		menu.addWidget(importButton);
		menu.addWidget(addAsAuthor);

		menu.addWidget(searchButton);
		menu.addWidget(namespace);
		menu.addWidget(new HTML("<strong>Year since: </strong>"));
		menu.addWidget(yearSince);
		menu.addWidget(new HTML("<strong>Year till: </strong>"));
		menu.addWidget(yearTill);

		// GET TABLE
		CellTable<Publication> table = find.getEmptyTable();

        importButton.setEnabled(false);
        JsonUtils.addTableManagedButton(find, table, importButton);

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel();
		sp.add(table);
		sp.addStyleName("perun-tableScrollPanel");
		mainPanel.add(sp);

		// resize perun table to correct size on screen
		session.getUiElements().resizePerunTable(sp, 350, this);
		
		this.contentWidget.setWidget(mainPanel);
		
	}
	
	/**
	 * Drwa tab content for REVIEW state
	 */
	private void loadReviewScreen() {
		
		// REVIEW STATE TAB
			
		// MAIN PANEL
		VerticalPanel configMainPanel = new VerticalPanel();
		configMainPanel.setSize("100%", "100%");

		// MENU
		TabMenu menu = new TabMenu();
		// SCROLLABLE CONTENT
		final VerticalPanel scrollContent = new VerticalPanel();
		scrollContent.addStyleName("perun-table");
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.add(scrollContent);
		scrollPanel.addStyleName("perun-tableScrollPanel");

		// WIDGETS
		CustomButton backButton = TabMenu.getPredefinedButton(ButtonType.BACK, "Go back to import page - !! ALL UNSAVED CHANGES WILL BE LOST !!", new ClickHandler(){
			public void onClick(ClickEvent event) {
				importedPublications.clear();
				hadError = false;
				categories.clear();
				state = previousState; // go back to original state IMPORT or CREATE
				session.getTabManager().reloadTab(tab);
			}
		});
		menu.addWidget(backButton);
		
		// finish button
		final CustomButton finishButton = TabMenu.getPredefinedButton(ButtonType.FINISH, "Finish adding and close tab - !! ALL UNSAVED CHANGES WILL BE LOST !!", new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (importedPublications.isEmpty()) {
					session.getTabManager().closeTab(tab);
				}
			}
		});
        menu.addWidget(finishButton);
		finishButton.setTitle("");
		finishButton.setVisible(false);

		final Image errorIcon = new Image(LargeIcons.INSTANCE.errorIcon());
		final HTML errorText = new HTML("<h4 style=\"font-size: 12pt; color:red\">Some publications were not imported! Please review imported publication(s) first. Click on publication title to see it's details.</h4>");
		final HTML successText = new HTML("<h4 style=\"font-size: 12pt; color:red\">Please review created publication(s). Click on publication title to see it's details.</h4>");
		
		if (importedPublications.isEmpty()) {
			scrollContent.add(new HTML("<p style=\"font-size: 12pt; font-weight:bold; text-align:center; color:red\">No publication(s) created! Please go back and retry!</p>"));
		} else {
			if (hadError) {
				menu.addWidget(errorIcon);
				menu.addWidget(errorText);
			} else {
				menu.addWidget(errorIcon);
				menu.addWidget(successText);
			}
		}

		for (final Publication pub : importedPublications) {

			// PUT PUB DETAIL TABLE IN CONTENT
			
			final FlexTable pubTable = new FlexTable();
			pubTable.setSize("100%", "100%");

			final HTML splitter = new HTML("<hr size=\"2px\" width=\"100%\" />");

			// PUBLICATION HEADER
			
			final TabMenu pubHeader = new TabMenu();
			
			// main
			final TextArea cite = new TextArea();
			cite.setSize("500px", "50px");
			cite.setText(pub.getMain());
			// set max length
			cite.getElement().setAttribute("maxlength", "4000");
			// category
			final ListBoxWithObjects<Category> categoryBox = new ListBoxWithObjects<Category>();
			
			// finish button
			final CustomButton finishPublicationButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "", new ClickHandler(){
				public void onClick(ClickEvent event) {
					// DONT CALL UPDATE FOR LOCKED PUBS
					if (pub.getLocked() == true) {
						// remove finished publication
						for (int i=0; i<importedPublications.size(); i++) {
							if (importedPublications.get(i).getId()==pub.getId()) {
								importedPublications.remove(i);
								break;
							}
						}
						pubHeader.removeFromParent();
						splitter.removeFromParent();
						pubTable.removeFromParent();
						if (importedPublications.isEmpty()) {
							errorIcon.setVisible(false);
							errorText.setVisible(false);
							successText.setVisible(false);
							finishButton.setVisible(true); // enable finish if all pubs removed
							scrollContent.add(new HTML("<p style=\"font-size: 12pt; font-weight:bold; text-align:center; color:darkgreen\">You are now done. <br/><br/>Please click on Finish or Back button (close tab / repeat create or import publication).</p>"));
						}
					} else {
						// CALL UPDATE ON UNLOCKED PUBS
						pub.setCategoryId(categoryBox.getSelectedObject().getId());
						pub.setMain(cite.getText());
						UpdatePublication request = new UpdatePublication(new JsonCallbackEvents(){
							@Override
							public void onFinished(JavaScriptObject jso) {
								// remove finished publication
								for (int i=0; i<importedPublications.size(); i++) {
									if (importedPublications.get(i).getId()==pub.getId()) {
										importedPublications.remove(i);
										break;
									}
								}
								pubHeader.removeFromParent();
								splitter.removeFromParent();
								pubTable.removeFromParent();
								if (importedPublications.isEmpty()) {
									errorIcon.setVisible(false);
									errorText.setVisible(false);
									successText.setVisible(false);
									finishButton.setVisible(true); // enable finish if all pubs removed
									scrollContent.add(new HTML("<p style=\"font-size: 12pt; font-weight:bold; text-align:center; color:darkgreen\">You are now done. <br/><br/>Please click on Finish or Back button (close tab / repeat create or import publication).</p>"));
								}	
							}
							// FIXME - testing
							/*
							@Override
							public void onError(PerunError error) {
								onFinished(null);
							}
							*/
						});
						request.updatePublication(pub);		

					}
				}
			});
            pubHeader.addWidget(finishPublicationButton);
			finishPublicationButton.setTitle("Finish editing publication (remove from view)");
			finishPublicationButton.setVisible(false);
			
			if (pub.getLocked()==true) {
				finishPublicationButton.setText("Remove from view");
			}
			
			// put into header
			pubHeader.addWidget(finishPublicationButton);
			pubHeader.addWidget(new Image(LargeIcons.INSTANCE.bookIcon()));
			Anchor title = new Anchor("<h3>"+pub.getTitle()+"</h3>", true);
			title.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					if (pubTable.isVisible()) {
						pubTable.setVisible(false);
					} else {
						finishPublicationButton.setVisible(true); // enable finishing
						pubTable.setVisible(true);
					}
				}
			});
			pubHeader.addWidget(title);

			// HIDE / SHOW widgets based on previous state
			if (previousState == State.CREATE) {
				pubTable.setVisible(true);
				finishPublicationButton.setVisible(true);
			} else {
				// hide when was imported more publication
				pubTable.setVisible(false); 
			}
			
			// PUT HEADER AND TABLE TO SCROLLABLE CONTENT
			scrollContent.add(pubHeader);
			scrollContent.add(pubTable);
			scrollContent.add(splitter);
			
			// CITE

			TabMenu citePanel = new TabMenu();
			citePanel.addWidget(new Image(LargeIcons.INSTANCE.bookEditIcon()));
			citePanel.addWidget(new HTML("<h4>Full cite</h4>"));

			citePanel.addWidget(cite);
			
			if (pub.getLocked()==true) {
				// disable cite modification if locked
				cite.setEnabled(false);
			}

			// CATEGORY

			TabMenu categoryPanel = new TabMenu();
			categoryPanel.addWidget(new Image(LargeIcons.INSTANCE.bookshelfIcon()));
			categoryPanel.addWidget(new HTML("<h4>Category</h4>"));

			if (pub.getLocked()==true) {
				// if locked display category name
				categoryPanel.addWidget(new HTML(pub.getCategoryName()));
			} else {
				// not locked display selection box
				categories = new TableSorter<Category>().sortByName(categories);
				for (Category c : categories) {
					categoryBox.addItem(c);
					if (pub.getCategoryName().equalsIgnoreCase(c.getName())) {
						categoryBox.setSelected(c, true);  // select category from publication
					}
				}
				categoryPanel.addWidget(categoryBox);
			}
			
			// THANKS 

			
			VerticalPanel thanksPanel = new VerticalPanel();
			thanksPanel.setSize("100%", "100%");
			final TabMenu thanksMenu = new TabMenu();
			thanksMenu.addWidget(new Image(LargeIcons.INSTANCE.smallBusinessIcon()));
			thanksMenu.addWidget(new HTML("<h4>Acknowledgment</h4>"));
			thanksPanel.add(thanksMenu);

			// callback
			final FindThanksByPublicationId thanksCall = new FindThanksByPublicationId(pub.getId());
			if (pub.getLocked()==true) {
				// disable modifications if locked
				thanksCall.setCheckable(false);
				thanksMenu.setVisible(false);
			}
			CellTable<Thanks> thanksTable = thanksCall.getTable();

			// button
            thanksMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, "Add acknowledgement to publication", new ClickHandler(){
				public void onClick(ClickEvent event) {
					JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(thanksCall);
					session.getTabManager().addTabToCurrentTab(new CreateThanksTabItem(pub, events), true);
				}
			}));

            final CustomButton removeThanksButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove acknowledgement from publication");
            removeThanksButton.addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    final ArrayList<Thanks> list = thanksCall.getTableSelectedList();
                    String text = "Following acknowledgements will be removed from publication. Publication authors may loose benefits granted by them.";
                    UiElements.showDeleteConfirm(list, text, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
                            for(int i=0; i<list.size(); i++){
                                if (i == list.size()-1) {
                                    DeleteThanks request = new DeleteThanks(JsonCallbackEvents.disableButtonEvents(removeThanksButton, JsonCallbackEvents.refreshTableEvents(thanksCall)));
                                    request.deleteThanks(list.get(i).getId());
                                } else {
                                    DeleteThanks request = new DeleteThanks(JsonCallbackEvents.disableButtonEvents(removeThanksButton));
                                    request.deleteThanks(list.get(i).getId());
                                }
                            }
                        }
                    });
                }});

            removeThanksButton.setEnabled(false);
            JsonUtils.addTableManagedButton(thanksCall, thanksTable, removeThanksButton);

            /*
			// refresh table button
			thanksMenu.addButton("Refresh table", SmallIcons.INSTANCE.updateIcon(), new ClickHandler() {
                public void onClick(ClickEvent event) {
                    thanksCall.clearTable();
                    thanksCall.retrieveData();
                }
            });
			*/
			thanksTable.addStyleName("perun-table");
			ScrollPanel sp = new ScrollPanel();
			sp.add(thanksTable);
			sp.addStyleName("perun-tableScrollPanel");
			thanksPanel.add(sp);

			
			// AUTHORS


			VerticalPanel authorsPanel = new VerticalPanel();
			authorsPanel.setSize("100%", "100%");
			TabMenu authorMenu = new TabMenu();
			authorsPanel.add(authorMenu);
			authorMenu.addWidget(new Image(LargeIcons.INSTANCE.userGreenIcon()));
			authorMenu.addWidget(new HTML("<h4>Authors / Reported by</h4>"));

			// callback
			final FindAuthorsByPublicationId authorCall = new FindAuthorsByPublicationId(pub.getId());
			if (pub.getLocked()==true) {
				authorCall.setCheckable(false);
				authorMenu.setVisible(false);
			}

            final CustomButton addMyselfButton = new CustomButton("Add myself", "Add you as author", SmallIcons.INSTANCE.addIcon());
            addMyselfButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (session.isPerunAdmin() && session.getUser() == null) {
						// for perunadmin to load session
						session.getTabManager().addTab(new IdentitySelectorTabItem());
					} else {
						JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(addMyselfButton, JsonCallbackEvents.refreshTableEvents(authorCall));
						CreateAuthorship request = new CreateAuthorship(events);
						request.createAuthorship(pub.getId(), session.getUser().getId());
					}
				}
			});
            authorMenu.addWidget(addMyselfButton);

			CustomButton addOthersButton = new CustomButton("Add others", "Add more authors", SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
				public void onClick(ClickEvent event) {
					JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(authorCall);
					session.getTabManager().addTabToCurrentTab(new AddAuthorTabItem(pub, events), true);
				}
			});
            authorMenu.addWidget(addOthersButton);

            final CustomButton removeAuthorButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove selected author(s) from publication");
			removeAuthorButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {

                    final ArrayList<Author> list = authorCall.getTableSelectedList();
                    final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(authorCall);
                    String text = "Following authors will be removed from publication. They will lose all benefits granted by reporting this publication.";
                    UiElements.showDeleteConfirm(list, text, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
                            for (int i = 0; i < list.size(); i++) {
                                if (i == list.size() - 1) {
                                    DeleteAuthorship request = new DeleteAuthorship(JsonCallbackEvents.disableButtonEvents(removeAuthorButton, events));
                                    request.deleteAuthorship(pub.getId(), list.get(i).getId());
                                } else {
                                    DeleteAuthorship request = new DeleteAuthorship(JsonCallbackEvents.disableButtonEvents(removeAuthorButton));
                                    request.deleteAuthorship(pub.getId(), list.get(i).getId());
                                }
                            }
                        }
                    });
                }
            });

            /*
			// refresh table button
			authorMenu.addButton("Refresh table", SmallIcons.INSTANCE.updateIcon(), new ClickHandler(){
				public void onClick(ClickEvent event) {
					authorCall.clearTable();
					authorCall.retrieveData();
				}
			});
            */

			// fill table
			CellTable<Author> authTable = authorCall.getEmptyTable();

            removeAuthorButton.setEnabled(false);
            JsonUtils.addTableManagedButton(authorCall, authTable, removeAuthorButton);

			authTable.addStyleName("perun-table");
			authorCall.retrieveData();
			ScrollPanel authSp = new ScrollPanel();
			authSp.add(authTable);
			authSp.addStyleName("perun-tableScrollPanel");
			authorsPanel.add(authSp);
			
			// PUT ALL SUBENTRIES INTO TABLE
			
			if (pub.getLocked() == true) {
				pubTable.setHTML(0, 0, "<p style=\"font-size: 10pt; font-weight:bold; text-align:center; color:red\">Same publication is already in Perun and it's locked. To perform changes notify administrator (meta@cesnet.cz).");
			} else {
				pubTable.setHTML(0, 0, "<ol style=\"color:red\"><li>Please check and correct citation if needed.</li><li>Select publication's category (if not sure keep as it is).</li><li>Add other authors and acknowledgements. (If same publication was already reported, it can have some authors and acknowledgements set.)</li><li>When done with changes, click on \"Save changes\" button. Saved publication will be removed from review list and considered done.</li></ol>");
			}
			pubTable.setWidget(1, 0, citePanel);
			pubTable.setWidget(2, 0, categoryPanel);
			pubTable.setWidget(3, 0, thanksPanel);
			pubTable.setWidget(4, 0, authorsPanel);

		}

		// PUT CONTENT TO TAB		
		configMainPanel.add(menu);
		configMainPanel.add(new HTML("<hr size=\"2px\" width=\"100%\" />"));
		configMainPanel.add(scrollPanel);

		// resize perun table to correct size on screen
		session.getUiElements().resizePerunTable(scrollPanel, 350, this);
		
		this.contentWidget.setWidget(configMainPanel);
		
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
		result = prime * result * 22 * this.userId;
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
		AddPublicationsTabItem other = (AddPublicationsTabItem) obj;
		if (!(userId == (other.userId)))
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "My publications", CabinetTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"userpubs?user="+userId, "Add publication", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true; 
		} else {
			return false;
		}

	}

	public final static String URL = "add";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?user=" + userId;
	}

	static public AddPublicationsTabItem load(Map<String, String> parameters) {
		
		if (parameters.containsKey("user")) {
			int userId = Integer.parseInt(parameters.get("user"));
			if (userId != 0) {
				return new AddPublicationsTabItem(userId);
			}
		}
		return new AddPublicationsTabItem();

	}

	public Widget getHelpWidget() {
		return this.helpWidget;
	}

}