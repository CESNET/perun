package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.applicationresources.FormValidator;
import cz.metacentrum.perun.webgui.client.applicationresources.RegistrarFormItemGenerator;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Display preview of VOs/Groups application form based on current
 * config state in GUI. Allows to set type and language on page and reload.
 *
 * USE AS INNER TAB ONLY or DON'T add tab url between vos tabs
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PreviewFormTabItem implements TabItem, TabItemWithUrl {

	private PerunWebSession session = PerunWebSession.getInstance();
	private ArrayList<ApplicationFormItem> formItems;
	private Label titleWidget = new Label("form preview");
	private SimplePanel contentWidget = new SimplePanel();

	private String appType = "INITIAL";
	private String locale = "en";
	private ArrayList<RegistrarFormItemGenerator> applFormGenerators = new ArrayList<RegistrarFormItemGenerator>();
	private Button sendButton;

	private ApplicationForm form;
	private int formId;

	/**
	 * Create new instance of this tab
	 *
	 * @param form Form to get authz from
	 * @param formItems application form items to display
	 */
	public PreviewFormTabItem(ApplicationForm form, ArrayList<ApplicationFormItem> formItems) {
		this.form = form;
		this.formId = form.getId();
		this.formItems = formItems;
	}

	public boolean isPrepared() {
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

		if (form.getGroup() != null) {
			titleWidget.setText(Utils.getStrippedStringWithEllipsis(form.getGroup().getShortName())+": form preview");
		} else {
			titleWidget.setText(Utils.getStrippedStringWithEllipsis(form.getVo().getName())+": form preview");
		}

		final TabItem tab = this;

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		final ScrollPanel sp = new ScrollPanel();

		final CustomButton switchType = new CustomButton(ButtonTranslation.INSTANCE.switchToExtensionButton(), ButtonTranslation.INSTANCE.switchBetweenInitialAndExtension(), SmallIcons.INSTANCE.applicationFormIcon());
		switchType.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// switch type
				if (appType.equalsIgnoreCase("EXTENSION")) {
					appType = "INITIAL";
					switchType.setText(ButtonTranslation.INSTANCE.switchToExtensionButton());
				} else {
					appType = "EXTENSION";
					switchType.setText(ButtonTranslation.INSTANCE.switchToInitialButton());
				}
				// prepare new
				prepareApplicationForm(sp);
			}
		});
		menu.addWidget(switchType);

		if (!Utils.getNativeLanguage().isEmpty()) {
			final CustomButton switchLocale = new CustomButton(ButtonTranslation.INSTANCE.switchToCzechButton(Utils.getNativeLanguage().get(1)), ButtonTranslation.INSTANCE.switchBetweenCzechAndEnglish(), SmallIcons.INSTANCE.locateIcon());
			menu.addWidget(switchLocale);
			switchLocale.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					// switch type
					if (locale.equalsIgnoreCase("en")) {
						locale = Utils.getNativeLanguage().get(0);
						switchLocale.setText(ButtonTranslation.INSTANCE.switchToEnglishButton());
					} else {
						locale = "en";
						switchLocale.setText(ButtonTranslation.INSTANCE.switchToCzechButton(Utils.getNativeLanguage().get(1)));
					}
					// prepare new
					prepareApplicationForm(sp);
				}
			});
		}

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		vp.add(sp);
		vp.setCellHeight(sp, "100%");

		prepareApplicationForm(sp);

		session.getUiElements().resizeSmallTabPanel(sp, 350, tab);
		contentWidget.setWidget(vp);
		return getWidget();

	}

	/**
	 * Prepares the widgets from the items as A DISPLAY FOR THE USER
	 *
	 * @param sp scroll panel
	 */
	public void prepareApplicationForm(ScrollPanel sp){

		FlexTable ft = new FlexTable();
		ft.setSize("100%", "100%");
		ft.setCellPadding(10);
		FlexCellFormatter fcf = ft.getFlexCellFormatter();

		int i = 0;
		for(final ApplicationFormItem item : formItems) {

			// skip items not from correct app type
			ArrayList<String> itemApplicationTypes = JsonUtils.listFromJsArrayString(item.getApplicationTypes());
			if (!itemApplicationTypes.contains(appType)) continue;

			// generate correct items
			RegistrarFormItemGenerator gen = new RegistrarFormItemGenerator(item, locale);
			this.applFormGenerators.add(gen);
			gen.addValidationTrigger(new FormValidator() {

				public void triggerValidation() {
					validateFormValues(false);
				}
			});


			// if button, add onclick
			if(item.getType().equals("SUBMIT_BUTTON")){

				this.sendButton = (Button) gen.getWidget();
				sendButton.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {

						// revalidate again, with force validation
						if(!validateFormValues(true)){
							return;
						}

						// sending is disabled
						Confirm c = new Confirm("Sending disabled", new Label("Sending form is disabled in preview mode, but form items value validation works."), true);
						c.show();

					}
				});
			}

			// get localized texts
			ItemTexts itemTexts = item.getItemTexts(locale);

			if(item.getHidden().equals("ALWAYS")) {
				continue;
			}

			// WITH LABEL (input box ...)
			if(gen.isLabelShown()){

				// 0 = label
				if (item.isRequired() == true) {
					// required
					ft.setHTML(i, 0, "<strong>" + SafeHtmlUtils.fromString(gen.getLabelOrShortname()).asString() + "*</strong>");
				} else {
					// optional
					ft.setHTML(i, 0, "<strong>" + SafeHtmlUtils.fromString(gen.getLabelOrShortname()).asString() + "</strong>");
				}

				// 1 = widget with icons
				HorizontalPanel hp = new HorizontalPanel();
				hp.setSpacing(10);

				Widget w = gen.getWidget();
				w.setTitle(itemTexts.getHelp());
				if (item.getDisabled().equals("ALWAYS")) {
					w.getElement().setAttribute("disabled","disabled");
				}
				hp.add(w);

				// lock icon 
				if(item.getDisabled() != "NEVER") {
					Image lockIcon =  new Image(SmallIcons.INSTANCE.lockIcon());
					lockIcon.setStyleName("pointer");

					String dependency = item.getDisabledDependencyItemId() == 0 ? "it" : formItems.stream()
						.filter(it -> it.getId() == item.getDisabledDependencyItemId())
						.collect(Collectors.toList()).get(0).getShortname();
					switch (item.getDisabled()) {
						case "ALWAYS":
							lockIcon.setTitle("Always disabled");
							break;
						case "IF_EMPTY":
							lockIcon.setTitle("Disabled when " + dependency + " is empty");
							break;
						case "IF_PREFILLED":
							lockIcon.setTitle("Disabled when " + dependency + " is prefilled");
							break;
					}

					hp.add(lockIcon);
				}

				// eye icon
				if(item.getHidden() != "NEVER") {
					Image eyeIcon =  new Image(SmallIcons.INSTANCE.hiddenIcon());
					eyeIcon.setStyleName("pointer");

					String dependency = item.getHiddenDependencyItemId() == 0 ? "it" : formItems.stream()
						.filter(it -> it.getId() == item.getHiddenDependencyItemId())
						.collect(Collectors.toList()).get(0).getShortname();
					switch (item.getHidden()) {
						case "ALWAYS":
							eyeIcon.setTitle("Always hidden");
							break;
						case "IF_EMPTY":
							eyeIcon.setTitle("Hidden when " + dependency + " is empty");
							break;
						case "IF_PREFILLED":
							eyeIcon.setTitle("Hidden when " + dependency + " is prefilled");
							break;
					}

					hp.add(eyeIcon);
				}

				ft.setWidget(i, 1, hp);

				// 2 = status
				ft.setWidget(i, 2, gen.getStatusWidget());

				// 3 = HELP
				if(itemTexts.getHelp() != null && itemTexts.getHelp().length() > 0) {
					Label help = new Label(itemTexts.getHelp());
					ft.setWidget(i, 3, help);
				}

				// format
				fcf.setStyleName(i, 0, "applicationFormLabel");
				fcf.setStyleName(i, 1, "applicationFormWidget");
				fcf.setStyleName(i, 2, "applicationFormCheck");
				fcf.setStyleName(i, 3, "applicationFormHelp");
				ft.setWidth("100%");

				// ELSE HTML COMMENT

			} else {
				ft.setWidget(i, 0, gen.getWidget());
				// colspan = 2
				fcf.setColSpan(i, 0, 4);
				fcf.setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_LEFT);
				fcf.setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_MIDDLE);
			}
			i++;
		}

		sp.setWidget(ft);

	}

	/**
	 * Validates the form values
	 */
	protected boolean validateFormValues(boolean forcedValidation) {

		if(sendButton == null) return false;

		boolean valid = true;

		sendButton.setEnabled(true);
		for(RegistrarFormItemGenerator gen : applFormGenerators){
			if(gen.getInputChecker().isValidating() || !gen.getInputChecker().isValid(forcedValidation)){
				sendButton.setEnabled(false);
				valid = false;

				if(!forcedValidation){
					return false;
				}
			}
		}
		return valid;

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.applicationFormMagnifyIcon();
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(form.getVo().getId()) || session.isGroupAdmin(form.getGroup().getId())) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "preview";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + formId;
	}

	@Override
	public int hashCode() {
		final int prime = 1493;
		int result = 1;
		result = prime * result + formId;
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
		PreviewFormTabItem other = (PreviewFormTabItem) obj;
		if (formId != other.formId)
			return false;
		return true;
	}

}
