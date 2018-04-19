package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;

public class FormItemUpdated {
    private ApplicationForm form;
    private ApplicationFormItem item;
    private String name = this.getClass().getName();
    private String message;


    public FormItemUpdated() {
    }

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FormItemUpdated(ApplicationForm form, ApplicationFormItem item) {
        this.form = form;
        this.item = item;
    }

    public ApplicationForm getForm() {
        return form;
    }

    public void setForm(ApplicationForm form) {
        this.form = form;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Application form ID=" + form.getId() + " voID=" + form.getVo().getId() + ((form.getGroup() != null) ? (" groupID=" + form.getGroup().getId()) : "") + " has had it itemID="+item.getId()+" updated.";
    }
}
