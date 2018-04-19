package cz.metacentrum.perun.core.impl;

import ch.qos.logback.classic.gaffer.PropertyUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityCreated;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static sun.invoke.util.ValueConversions.cast;

public class AuditerSubscriber {
    private final static Logger log = LoggerFactory.getLogger(AuditerSubscriber.class);

    private JdbcPerunTemplate jdbc;
    private int lastProcessedId = 0; //id of last processed message
    private String subscriberName;
    private static Pubsub pubsub = Pubsub.getInstance();

    private Map<Object,List<String>> classFilter = new HashMap<>();

    private static final RowMapper<String> AUDITER_LOG_MAPPER = new RowMapper<String>() {
        public String mapRow(ResultSet rs, int i) throws SQLException {
            AuditMessage auditMessage = Auditer.AUDITMESSAGE_MAPPER.mapRow(rs, i);
            return auditMessage.getMsg();
        }
    };

    public AuditerSubscriber(String subscriberName, DataSource perunPool) throws InternalErrorException {

        this.jdbc = new JdbcPerunTemplate(perunPool);
        this.subscriberName = subscriberName;
        try {
            this.lastProcessedId = jdbc.queryForInt("select last_processed_id from auditer_subscribers where name=?", subscriberName);
        } catch(EmptyResultDataAccessException ex) {
            //listenerName doesn't have record in auditer_subscribers
            try {
                // New listener, set the lastProcessedId to the latest one
                lastProcessedId = jdbc.queryForInt("select max(id) from auditer_log_json");

                int subscriberId = Utils.getNewId(jdbc, "auditer_subscribers_id_seq");
                jdbc.update("insert into auditer_subscribers (id, name, last_processed_id, modified_at) values (?,?,?," + Compatibility.getSysdate() + ")", subscriberId, subscriberName, lastProcessedId);
                log.debug("New subscriber [name: '{}', lastProcessedId: '{}'] created.", subscriberName, lastProcessedId);
            } catch(Exception e) {
                throw new InternalErrorException(e);
            }
        } catch(Exception ex) {
            throw new InternalErrorException(ex);
        }
    }

    public List<String> getMessages() throws InternalErrorException {
        try {
            int maxId = jdbc.queryForInt("select max(id) from auditer_log_json");
            if(maxId > lastProcessedId) {
                List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log_json where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER, this.lastProcessedId, maxId);
                this.lastProcessedId = maxId;
                jdbc.update("update auditer_subscribers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", this.lastProcessedId, this.subscriberName);
                return messages;
            }
            return new ArrayList<String>();
        } catch(Exception ex) {
            throw new InternalErrorException(ex);
        }
    }

    public boolean addFilter(Object obj){

        if(!classFilter.containsKey(obj)){
            classFilter.put(obj,new ArrayList<String>());
            return true;
        }
        return false;
    }

    public boolean removeFilter(Object obj){
        if(classFilter.containsKey(obj)){
            classFilter.remove(obj);
            return true;
        }
        return false;
    }

    public boolean addFilter(Object obj, List<String> listOfParam){

        if(!classFilter.containsKey(obj)){
            classFilter.put(obj,new ArrayList<String>()); //new filter
        }
        //add params
        for(String param : listOfParam){
            classFilter.get(obj).add(param);
        }

        return true;
    }

    public Map<Object,List<String>> getFilters(){
        return Collections.unmodifiableMap(classFilter);
    }

    public void removeAllFilters(){
        classFilter.clear();
    }

    public List<String> getMessagesFiltered() throws InternalErrorException {
        List<String> messages = getMessages();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Object event = null;
        List<String> filtered = new ArrayList<String>();
        //apply filter
        for(int i = 0; i < messages.size(); i++){
            String m = messages.get(i);
            try{
                Class clazz = Class.forName(getNameOfClassAttribute(m));
                event = mapper.readValue(m,clazz);
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

            pubsub.publish(event.getClass(),m);
//            if(event != null){
//                if(classFilter.containsKey(event.getClass())){
//                    if(!classFilter.get(event.getClass()).isEmpty()){
//                        boolean containsAll = true;
//                        for (String param:
//                             classFilter.get(event.getClass())) {
//                            if(!m.contains(param)){
//                               containsAll = false;
//                            }
//                        }
//                        if(containsAll){
//                            filtered.add(m);
//                        }
//                    }else{
//                        filtered.add(m);
//                    }
//
//                }
//            }


        }
        return filtered;

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
