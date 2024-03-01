package cz.metacentrum.perun.dispatcher.unit;

import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProcessor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EngineMessageProcessorTest extends AbstractDispatcherTest {

  @Autowired
  private EngineMessageProcessor engineMessageProcessor;

  @Test
  public void processDispatcherQueueAndMatchingRuleTest() {
    System.out.println("SystemQueueProcessor.processDispatcherQueueAndMatchingRule()");
    String testMessageTask = "";
    String testMessageRegister = "";
    String testMessageGoodbye = "";
  }

}
