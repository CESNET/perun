package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents wrapper for AuditerConsumer and publishes messages recieved
 * from AuditerConsumer to PubSub mechanizm
 * @author Richard Husár 445238@mail.muni.cz
 */
public class AuditerPublisher {
    private final static Logger log = LoggerFactory.getLogger(AuditerPublisher.class);
    private AuditerConsumer consumer;
    private static PubsubMechanizm pubsubMechanizm = PubsubMechanizm.getInstance();


    public AuditerPublisher(AuditerConsumer auditerConsumer) throws InternalErrorException {
        this.consumer = auditerConsumer;
    }

    /**
     * Get json messages from auditer through auditerConsumer
     * @return audit messages in JSON
     */
    public List<String> getMessages(){
        List<String> messages = new ArrayList<>();

        try {
            messages = consumer.getMessagesInJson();
        } catch (InternalErrorException e) {
            log.error("Could not get messages to publisher: " + e.getMessage());
        }

        return messages;
    }

    /**
     * Deserialize messages and pulishes them into pubsubMechanizm channel
     * @param messages messages to be parsed
     */
    public void publishMessages(List<String> messages){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Object event = null;

        //get event type (topic for subscribtion)
        for(int i = 0; i < messages.size(); i++) {
            String m = messages.get(i);
            try {
                Class clazz = Class.forName(getNameOfClassAttribute(m));
                event = mapper.readValue(m, clazz);
            } catch (JsonParseException e) {
                log.error("Could not parse message: " + e.getMessage());
                continue;
            } catch (JsonMappingException e) {
                log.error("Could not map message for class. " + e.getMessage());
                continue;
            } catch (IOException e) {
                log.error("IOException at message: " + e.getMessage());
                continue;
            } catch (ClassNotFoundException e) {
                log.error("ClassNotFoundException: " + e.getMessage() + " for message: " + m);
                continue;
            }
            //forward message to pubsubMechanizm channel with given topic (event class)
            pubsubMechanizm.publish(event.getClass(), event);
        }
    }

    /**
     * Get name of class from json string
     * @param jsonString
     * @return name of class included in json string
     */
    public String getNameOfClassAttribute(String jsonString){
        try {
            //get everything from in between of next quotes
            String message;
            int index = jsonString.indexOf("\"name\":\"cz");
            if(index != -1){
                message = jsonString.substring(index+8);
                message = message.substring(0,message.indexOf("\""));
                return message;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get name from json string: \"{}\".",jsonString);
        }
        return "";
    }

}
