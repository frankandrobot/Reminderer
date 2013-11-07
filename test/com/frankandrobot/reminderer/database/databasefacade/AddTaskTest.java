package com.frankandrobot.reminderer.database.databasefacade;

import android.content.Context;

import com.frankandrobot.reminderer.database.databasefacade.CursorNonQueryLoaders.AddTask;
import com.frankandrobot.reminderer.datastructures.Task;


public class AddTaskTest
{
    static public void addTask(Context context, Task task)
    {
        CursorNonQueryLoaders.AddTask addTask = new AddTask(context, task);
        addTask.loadInBackground();
    }
}
