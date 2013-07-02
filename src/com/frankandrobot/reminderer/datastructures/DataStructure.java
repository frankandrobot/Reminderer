package com.frankandrobot.reminderer.datastructures;

import java.util.HashMap;
import java.util.Map;

public class DataStructure
{
    public interface Field<T> {}

    protected HashMap<Field<?>,Object> hmFieldValues = new HashMap<Field<?>,Object>();
    protected HashMap<Class<? extends Field<?>>,Object> hmClassValues  = new HashMap<Class<? extends Field<?>>,Object>();

    public <U> DataStructure set(Field<U> key, U value)
    {
        hmFieldValues.put(key, value);
        return this;
    }

    public <U,T extends Field<U>> DataStructure set(Class<T> key, U value)
    {
        hmClassValues.put(key, value);
        return this;
    }

    public <U,T extends Field<U>> U get(Class<T> key)
    {
        return (U) hmClassValues.get(key);
    }

    public <U> U get(Field<U> key)
    {
        return (U) hmFieldValues.get(key);
    }

    public String toString()
    {
        String tmp = "[";
        for(Object key: hmFieldValues.keySet())
        {
            tmp += String.format("%s:%s,",
                                 key,
                                 hmFieldValues.get(key));
        }
        for(Class<?> key: hmClassValues.keySet())
        {
            tmp += String.format("%s:%s,",
                                 key.getSimpleName(),
                                 hmClassValues.get(key));
        }

        return tmp + "]";
    }

    public <T extends DataStructure> T combine(T ds)
    {
        if (ds != null)
        {
            for(Map.Entry<Field<?>,?> entry:ds.hmFieldValues.entrySet())
            {
                hmFieldValues.put(entry.getKey(), entry.getValue());
            }
            for(Map.Entry<Class<? extends Field<?>>, Object> entry:ds.hmClassValues.entrySet())
            {
                hmClassValues.put(entry.getKey(), entry.getValue());
            }
        }
        return (T) this;
    }
}
