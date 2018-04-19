package cz.metacentrum.perun.core.impl;

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

public class Pubsub implements Runnable
{
    public class Operation
    {
        public Operation(Object eventType, Object o)
        {
            this.type = eventType;
            this.payload = o;
        }

        public final Object type;

        public final Object payload;
    }

    public interface Listener
    {
        void onEventReceived(Object event, Object object);
    }

    private int NUMBER_OF_THREADS = 1;

    ExecutorService ex;

    private final BlockingQueue<Operation> mQueue;

    private Map<Object, Set<Listener>> listeners;
    private Map<Pair<Object,Listener>,List<String>> listOfParams;

    private static Pubsub _instance;

    public static Pubsub getInstance()
    {
        if (_instance == null)
        {
            synchronized (Pubsub.class)
            {
                if (_instance == null)
                    _instance = new Pubsub();
            }
        }
        return _instance;
    }

    private Pubsub()
    {
        listeners = new ConcurrentHashMap<Object, Set<Listener>>();
        listOfParams = new ConcurrentHashMap<Pair<Object,Listener>,List<String>>();
        mQueue = new LinkedBlockingQueue<Operation>();
        ex = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        ex.submit(this);
    }

    public void addListener(Object eventType, Listener listener)
    {
        add(eventType, listener);

        listOfParams.put(new Pair<Object,Listener>(eventType,listener),new ArrayList<String>());
    }

    public void addListeners(Listener listener, Object... eventTypes)
    {
        for (Object eventType : eventTypes)
        {
            add(eventType, listener);
            listOfParams.put(new Pair<Object,Listener>(eventType,listener),new ArrayList<String>());
        }
    }

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

    public void removeListener(Object eventType, Listener listener)
    {
        remove(eventType, listener);
        listOfParams.remove(new Pair<Object,Listener>(eventType,listener));
    }

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

    public boolean publish(Object eventType, Object o)
    {
        Set<Listener> l = listeners.get(eventType);
        if (l != null && l.size() >= 0)
        {
            mQueue.add(new Operation(eventType, o));
            return true;
        }
        return false;
    }


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
                    if(!listOfParams.get(new Pair<>(eventType,l)).isEmpty()){ //check if eventype does not have specific params to filter
                        boolean satisfiesParams = true;
                        for (String param:
                                listOfParams.get(new Pair<>(eventType,l))) {
                            if (!satisfiesParams) {
                                continue;
                            }
                            satisfiesParams = checkParams(param, o);

                            if (satisfiesParams) {
                                l.onEventReceived(eventType, o);
                            }
                        }
                    }else{
                        l.onEventReceived(eventType, o);
                    }

                }
            }
        }
    }

    private boolean checkParams(String param,Object object) {
        Pair<String,String> parsedParam = parseParams(param);
        if(parsedParam.left != ""){
            String[] parts = parsedParam.left.split("\\.");
                Object bean = object;
                int i = 0;
                while(bean != null && i < parts.length ) {
                    bean = getProperty(bean, parts[i]);
                    i++;
                }
                String result = bean.toString();
                if(result.equals(parsedParam.right)) {
                    return true;
                }
        }
        return false;
    }

    private Pair<String,String> parseParams(String param){
        if(param.contains("=")){
            return new Pair<String,String>(param.substring(0,param.indexOf("=")),param.substring(param.indexOf("=")+1));
        }
        return new Pair<String,String>("","");
    }

    public class Pair<L,R> extends org.apache.commons.lang3.tuple.Pair{
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

    public Object getProperty(Object bean,String propertyName) {
        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(bean.getClass(), Object.class);

            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : props) {
                String name = pd.getName();
                Method getter = pd.getReadMethod();
                Class<?> type = pd.getPropertyType();

                Object value = null;
                value = getter.invoke(bean);
                if(propertyName.equals(name)){
                    return value;
                }
            }
        }catch (IntrospectionException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


}
