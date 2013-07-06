package com.frankandrobot.reminderer.datastructures;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

/**
 * A datastructure with auto-generated getters and setters.
 *
 * It is possible to write functions that take variable __names__ as arguments
 * without using reflection.
 *
 * It is also possible to write javascript-like methods, such as apply(), that
 * copy all an objects fields into another object.
 *
 * The {@link DataStructure} class is basically a giant hack. It started with
 * me using a {@link HashMap} to store variable fields indexed by {@link Enum}s
 * and then eventually morphing into this datastructure class.
 *
 * Usage is similar to the {@link Calendar} object.
 *
 * <pre>
 * <code>
 * class Test extends DataStructure
 * {
 *     public enum StringField implements Field<String>
 *     {
 *         description
 *         ,name
 *     }
 *
 *     public enum IntField implements Field<Integer>
 *     {
 *         counter
 *     }
 * }
 *
 * Test test = new Test();
 * test.set(StringField.description, "Hello world");
 * test.set(IntField.counter, 500);
 * System.out.println("test.get(StringField.description");
 * //dumping the whole datastruct also works:
 * System.out.println(test);
 *
 * </code>
 * </pre>
 */
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
            tmp += String.format("%s:%s,%n",
                                 key,
                                 hmFieldValues.get(key));
        }
        for(Class<?> key: hmClassValues.keySet())
        {
            tmp += String.format("%s:%s,%n",
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
