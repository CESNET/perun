package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AuditerPublisher {
    private final static Logger log = LoggerFactory.getLogger(AuditerPublisher.class);
    private AuditerConsumer consumer;
    private static Pubsub pubsub = Pubsub.getInstance();


    public AuditerPublisher(AuditerConsumer auditerConsumer) throws InternalErrorException {
        this.consumer = auditerConsumer;
    }

    public List<String> getMessages(){
        List<String> messages = new ArrayList<>();

        try {
            messages = consumer.getMessagesInJson();
        } catch (InternalErrorException e) {
            log.error("Could not get messages to publisher: " + e.getMessage());
        }

        return messages;
    }


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
                log.error("Bad parse: " + e.getMessage());
                continue;
            } catch (JsonMappingException e) {
                log.error("Bad map: " + e.getMessage());
                continue;
            } catch (IOException e) {
                log.error("exception: " + e.getMessage());
                continue;
            } catch (ClassNotFoundException e) {
                log.error("ClassNotFoundException: " + e.getMessage() + " for message: " + m);
                continue;
            }
            //forward message to pubsub channel with given topic (event class)
            pubsub.publish(event.getClass(), event);
        }
    }

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
