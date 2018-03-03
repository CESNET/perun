package cz.metacentrum.perun.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**This class represents simple publish-subscribe pattern to distribute audit messages
 *
 * @author Richard Husár 445238@mail.muni.cz
 */
public class PubsubMechanizm implements Runnable
{
    private final static Logger log = LoggerFactory.getLogger(PubsubMechanizm.class);

    /**
     * Definition of operation which will be executed
     */
    public class Operation
    {
        public Operation(Object eventType, Object object)
        {
            this.type = eventType;
            this.payload = object;
        }

        public final Object type;

        public final Object payload;
    }

    /**
     * Interface for listener to implement
     */
    public interface Listener
    {
        void onEventReceived(Object event, Object object);
    }

    private int NUMBER_OF_THREADS = 1;

    ExecutorService ex;

    private final BlockingQueue<Operation> mQueue;

    private Map<Object, Set<Listener>> listeners;
    private Map<Pair<Object,Listener>,List<String>> listOfParams;

    private static PubsubMechanizm _instance;

    public static PubsubMechanizm getInstance()
    {
        if (_instance == null)
        {
            synchronized (PubsubMechanizm.class)
            {
                if (_instance == null)
                    _instance = new PubsubMechanizm();
            }
        }
        return _instance;
    }

    private PubsubMechanizm()
    {
        listeners = new ConcurrentHashMap<Object, Set<Listener>>();
        listOfParams = new ConcurrentHashMap<Pair<Object,Listener>,List<String>>();
        mQueue = new LinkedBlockingQueue<Operation>();
        ex = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        ex.submit(this);
    }

    /**
     * Registers certain event type for listener
     * @param eventType type for which listener will recieve messages
     * @param listener
     */
    public void addListener(Object eventType, Listener listener)
    {
        add(eventType, listener);
        listOfParams.put(new Pair<Object,Listener>(eventType,listener),new ArrayList<String>());
    }

    /**
     *
     * register multiple event types for listener
     * @param listener
     * @param eventTypes types for which listener will recieve messages
     */
    public void addListeners(Listener listener, Object... eventTypes)
    {
        for (Object eventType : eventTypes)
        {
            add(eventType, listener);
            listOfParams.put(new Pair<Object,Listener>(eventType,listener),new ArrayList<String>());
        }
    }

    /**
     * Registers certain event type for listener with specific parameters
     * @param eventType
     * @param listener
     * @param params list of parameters under which messages will be filtered (example: "user.id=43")
     */
    public void addListener(Object eventType, Listener listener, List<String> params){
        add(eventType,listener);
        listOfParams.put(new Pair<Object,Listener>(eventType,listener), params);
    }

    private void add(Object eventType, Listener listener)
    {
        Set<Listener> list;
        list = listeners.get(eventType);
        if (list == null)
        {
            synchronized (this) // take a smaller lock
            {
                if ((list = listeners.get(eventType)) == null)
                {
                    list = new CopyOnWriteArraySet<Listener>();
                    listeners.put(eventType, list);
                }
            }
        }
        list.add(listener);
    }

    /**
     * Removes subscribtion to event type for listnener
     * @param eventType
     * @param listener
     */
    public void removeListener(Object eventType, Listener listener)
    {
        remove(eventType, listener);
        listOfParams.remove(new Pair<Object,Listener>(eventType,listener));
    }

    /**
     * Removes only parameters from this subscribtion to certain event type for listnener
     * @param eventType
     * @param listener
     * @param params parameters to be removed
     */
    public void removeListener(Object eventType, Listener listener, List<String> params){
        List<String> paramsOfListener = listOfParams.get(new Pair<Object,Listener>(eventType,listener));
        if(paramsOfListener != null || !paramsOfListener.isEmpty()){
            for (String param:
                 paramsOfListener) {
                for (String paramToRemove:
                     params) {
                    if(param.equals(paramToRemove)){
                        paramsOfListener.remove(param);
                    }
                }
            }
        }
    }

    /**
     * Removes subscribtion to more events for listener
     * @param listener
     * @param eventTypes
     */
    public void removeListeners(Listener listener, Object... eventTypes)
    {
        for (Object eventType : eventTypes)
        {
            remove(eventType, listener);
            listOfParams.remove(new Pair<Object,Listener>(eventType,listener));
        }
    }

    private void remove(Object eventType, Listener listener)
    {
        Set<Listener> l = null;
        l = listeners.get(eventType);
        if (l != null)
        {
            l.remove(listener);
        }
    }

    /**
     * Publishes message
     * @param eventType type of message
     * @param object actual message
     * @return
     */
    public boolean publish(Object eventType, Object object)
    {
        Set<Listener> l = listeners.get(eventType);
        if (l != null && l.size() >= 0)
        {
            mQueue.add(new Operation(eventType, object));
            return true;
        }
        return false;
    }

    /**
     * Takes recieved messages from queue,filters them based on individual listener
     * subscribtions and then distributes messages to subscribers
     */
    public void run()
    {
        Operation op;
        while (true)
        {
            try
            {
                op = mQueue.take();
            }
            catch (InterruptedException e)
            {
                continue;
            }

            Object eventType = op.type;
            Object o = op.payload;

            Set<Listener> list = listeners.get(eventType);

            if (list == null || list.isEmpty())
            {
                continue;
            }

            for (Listener l : list)
            {
                //apply filter for params
                if(listOfParams.containsKey(new Pair<>(eventType,l))){      //contains params for listener and topic
                    if(!listOfParams.get(new Pair<>(eventType,l)).isEmpty()){ //check if event type does not have specific params to filter
                        boolean satisfiesParams = true; //message must satisfy all parameters (parameter validation is conjunctive)
                        for (String param:
                                listOfParams.get(new Pair<>(eventType,l))) {
                            if (!satisfiesParams) {
                                continue;
                            }
                            satisfiesParams = checkParams(param, o);
                            // if all parametes are found in message, then send message to listener
                            if (satisfiesParams) {
                                l.onEventReceived(eventType, o);
                            }
                        }
                    }else{
                        //if listener does not have specific parameters then send all messages of this type
                        l.onEventReceived(eventType, o);
                    }

                }
            }
        }
    }

    /**
     * Checks if object representing message is containing given parameter
     * @param param parameter to be checkd
     * @param object object to be checked
     * @return true if object contains parameter, false otherwise
     */
    private boolean checkParams(String param,Object object) {
        Pair<String,String> parsedParam = parseParams(param);
        if(parsedParam.left != ""){
            String[] parts = parsedParam.left.split("\\.");
                int i = 0;
                //iterate into object properties
                while(object != null && i < parts.length ) {
                    object = getProperty(object, parts[i]);
                    i++;
                }
                String result = object.toString();
                if(result.equals(parsedParam.right)) {
                    return true;
                }
        }
        return false;
    }

    /**
     * Parse parameters to pair of key and value
     * Parsing functions based on '=' character
     * @param param parameter to be parsed
     * @return pair of key and value from parsed parameter
     */
    private Pair<String,String> parseParams(String param){
        if(param.contains("=")){
            return new Pair<String,String>(param.substring(0,param.indexOf("=")),param.substring(param.indexOf("=")+1));
        }
        return new Pair<String,String>("","");
    }

    /**
     * Class representing pair
     * @param <L> left value
     * @param <R> right value
     */
    public static class Pair<L,R> extends org.apache.commons.lang3.tuple.Pair{
        private L left;
        private R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public L getLeft() {
            return left;
        }

        @Override
        public R getRight() {
            return right;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof Pair)) return -1;
            Pair pairo = (Pair) o;
            if(this.left.equals(pairo.getLeft()) &&
                    this.right.equals(pairo.getRight())){
                return 0;
            }else{
                return 1;
            }
        }

        @Override
        public Object setValue(Object o) {
            if(o instanceof Pair){
                Pair other = (Pair) o;
                this.right = (R) other.right;
                this.left = (L) other.left;

            }
            return this;
        }
    }

    /**
     * Gets property from object, throws exception if not allowed to access property or property does not exist
     * @param bean object of which property should be returned
     * @param propertyName name of the property to be returned
     * @return property of the object with propertyName
     */
    public Object getProperty(Object bean,String propertyName) {
        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(bean.getClass(), Object.class);

            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : props) {
                String name = pd.getName();
                Method getter = pd.getReadMethod();

                Object value = null;
                value = getter.invoke(bean);
                if(propertyName.equals(name)){
                    return value;
                }
            }
        }catch (IntrospectionException e) {
            log.error("IntrospectionException when getting property {} of object: {}", propertyName, bean.getClass().getName());
        }catch (IllegalAccessException e) {
            log.error("Could not access property {} of the given object: {}.", propertyName, bean.getClass().getName());
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException when getting property {} of object: {}", propertyName, bean.getClass().getName());
        }
        return null;
    }


}
