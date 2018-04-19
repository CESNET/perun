package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailSentForApplication {

    private ApplicationMail.MailType mailType;
    private int appId;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MailSentForApplication(ApplicationMail.MailType mailType, int appId) {
        this.mailType = mailType;
        this.appId = appId;
    }

    public MailSentForApplication() {
    }

    public ApplicationMail.MailType getMailType() {
        return mailType;
    }

    public void setMailType(ApplicationMail.MailType mailType) {
        this.mailType = mailType;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Mail of Type: " + mailType + " sent for Application: " + appId;
    }
}
