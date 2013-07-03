package com.frankandrobot.reminderer.Helpers;

import com.frankandrobot.reminderer.datastructures.DataStructure;

import org.junit.Test;

public class DataStructureTest
{
    @Test
    public void testSet() throws Exception
    {
        Sample sample = new Sample();
        sample.set(Sample.Field1.class, "Hasta la vista");
        sample.set(Sample.Field2.greeting, "hi");
        sample.set(Sample.Field2.goodbye, "bye");
        System.out.println(sample);
    }

    @Test
    public void testGet() throws Exception
    {
        Sample sample = new Sample();
        /*sample.set(Sample.Field1.date, new Date());
        System.out.println(sample.get(Sample.Field1.date));*/
    }

    @Test
    public void testToString() throws Exception
    {

    }
}

class Sample extends DataStructure
{
    public class Field1 implements Field<String> {}

    enum Field2 implements Field<String>
    {
        greeting
        ,goodbye
    }
}