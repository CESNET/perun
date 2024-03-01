package cz.metacentrum.perun.dispatcher.jms;

import java.util.concurrent.BlockingDeque;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.springframework.stereotype.Service;

@Service
public class EngineMessageProducerFactory {

  private EngineMessageProducer producer;

  public void createProducer(String queueName, Session session, BlockingDeque<TextMessage> outputQueue) {
    producer = new EngineMessageProducer(queueName, session, outputQueue);
  }

  public EngineMessageProducer getProducer() {
    return this.producer;
  }

  public void removeProducer() {
    this.producer.shutdown();
    producer = null;
  }

  public void setProducer(EngineMessageProducer producer) {
    this.producer = producer;
  }


}
