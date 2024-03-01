package cz.metacentrum.perun.auditlogger.main;

import cz.metacentrum.perun.auditlogger.service.AuditLoggerManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.bl.PerunBl;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AuditLoggerStarter {
  private final static Logger log = LoggerFactory.getLogger(AuditLoggerStarter.class);

  private AuditLoggerManager auditLoggerManager;
  private AbstractApplicationContext springCtx;
  private PerunPrincipal perunPrincipal;
  private Perun perunBl;

  public AuditLoggerStarter() {
    this.perunPrincipal = new PerunPrincipal("perunAuditlogger", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL);
    springCtx = new ClassPathXmlApplicationContext("/perun-auditlogger.xml");
    this.auditLoggerManager = springCtx.getBean("auditLoggerManager", AuditLoggerManager.class);
    this.perunBl = springCtx.getBean("perun", PerunBl.class);
  }

  /**
   * Main method of auditLogger
   *
   * @param args (the only argument can be id of message to set Consumer on)
   */
  public static void main(String[] args) {
    System.out.println("Starting Perun-AuditLogger...");

    int lastProcessedIdToSet = 0;

    if (args.length == 0) {
      //This is normal behavior, do nothing special, just start auditLogger
    } else if (args.length == 1) {
      //This behavior is special, set lastProcessedId
      String argument = args[0];
      lastProcessedIdToSet = Integer.valueOf(argument);
    } else {
      System.out.println("Too much arguments, can't understand what to do, exit starting!");
      return;
    }


    try {
      AuditLoggerStarter auditLoggerStarter = new AuditLoggerStarter();

      // Just for the Spring IoC to exit gracefully...
      auditLoggerStarter.springCtx.registerShutdownHook();

      auditLoggerStarter.auditLoggerManager.setPerunPrincipal(auditLoggerStarter.perunPrincipal);
      auditLoggerStarter.auditLoggerManager.setPerunBl(auditLoggerStarter.perunBl);

      //Set lastProcessedIdToSet if bigger than 0
      if (lastProcessedIdToSet > 0) {
        auditLoggerStarter.auditLoggerManager.setLastProcessedId(lastProcessedIdToSet);
      }

      // Start processing events (run method in EventLoggerImpl)
      auditLoggerStarter.auditLoggerManager.startProcessingEvents();
    } catch (Exception e) {
      log.error(e.toString(), e);
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();
    log.info(dateFormat.format(date) + ": Done. Perun-AuditLogger has started.");
    System.out.println(dateFormat.format(date) + ": Done. Perun-AuditLogger has started.");
  }
}
