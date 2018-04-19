package cz.metacentrum.perun.audit.events.MailManagerEvents;

public class MailSendingEnabled {


    private int mailId;
    @Override
    public String toString() {
        return "Sending of Mail ID: " + mailId + " enabled.";
    }
}
