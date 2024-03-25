package cz.metacentrum.perun.dispatcher.service;

/**
 * Main class instantiating and manging various parts of Dispatcher (hornetQ server, auditer listener, task scheduling,
 * ...).
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface DispatcherManager {

  /**
   * Remove all TasksResults older than 3 days. Last one TaskResult per facility/service/destination is always kept,
   * even if older than 3 days. This method is supposed to be called by spring scheduler with defined cron.
   */
  void cleanOldTaskResults();

  /**
   * Load last persistent state of service propagation (Tasks) to in-memory scheduling pool.
   */
  void loadSchedulingPool();

  /**
   * Start listening to perun audit messages
   */
  void startAuditerListener();

  /**
   * Start HornetQ server used to pass/receive JMS messages to/from Engine
   */
  void startPerunHornetQServer();

  /**
   * Start processing read audit messages
   */
  void startProcessingEvents();

  /**
   * Start processing JMS messages sent from Engine
   */
  void startProcessingSystemMessages();

  /**
   * Start rescheduling of DONE/ERROR or stuck Tasks.
   */
  void startPropagationMaintaining();

  /**
   * Start scheduling of Tasks suitable for propagation.
   */
  void startTasksScheduling();

  /**
   * Stop listening to perun audit messages
   */
  void stopAuditerListener();

  /**
   * Stop HornetQ server used to pass/receive JMS messages to/from Engine
   */
  void stopPerunHornetQServer();

  /**
   * Stop processing read audit messages
   */
  void stopProcessingEvents();

  /**
   * Stop processing JMS messages sent from Engine
   */
  void stopProcessingSystemMessages();

  /**
   * Stop rescheduling of DONE/ERROR or stuck Tasks.
   */
  void stopPropagationMaintaining();

  /**
   * Stop scheduling of Tasks suitable for propagation.
   */
  void stopTaskScheduling();

}
