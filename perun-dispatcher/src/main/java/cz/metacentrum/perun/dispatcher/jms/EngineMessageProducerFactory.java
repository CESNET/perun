package cz.metacentrum.perun.dispatcher.jms;

import javax.jms.Session;

import org.springframework.stereotype.Service;

@Service
public class EngineMessageProducerFactory {

	private EngineMessageProducer producer;
	
	public EngineMessageProducer getProducer() {
		return this.producer;
	}

	public void setProducer(EngineMessageProducer producer) {
		this.producer = producer;
	}

	public void createProducer(String queueName, Session session) {
		producer = new EngineMessageProducer(queueName, session);
	}

	public void removeProducer() {
		this.producer.shutdown();
		producer = null;
	}


}
