package cz.metacentrum.perun.dispatcher.hornetq;

import org.hornetq.spi.core.logging.LogDelegate;
import org.hornetq.spi.core.logging.LogDelegateFactory;

/**
 * Factory for logging delegation of HornetQ server.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class LogbackLogDelegateFactory implements LogDelegateFactory {

  @Override
  public LogDelegate createDelegate(Class clazz) {
    return new LogbackLogDelegate(clazz);
  }

}
