package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.registrar.model.ApplicationForm;

public class FormItemsUpdated {

    private ApplicationForm form;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FormItemsUpdated(ApplicationForm form) {
        this.form = form;
    }

    public FormItemsUpdated() {
    }

    @Override
    public String toString() {
        return "Application form ID=" + form.getId() + " voID=" + form.getVo().getId() + ((form.getGroup() != null) ? (" groupID=" + form.getGroup().getId()) : "") + " has had its items updated.";
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
}
