package com.frankandrobot.reminderer.database;

import android.net.Uri;

public class DbColumns {

    public final static String AUTHORITY_NAME = "com.frankandrobot.reminderer.dbprovider";

    // the columns of the tables should go here

    public final static String TASK_TABLE = "task";
    public final static String TASK_ID = "_id";
    public final static String TASK = "task";
    public final static String TASK_DUE_DATE = "duedate";

    // URIs

    public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY_NAME
	    + "/" + TASK_TABLE);
    public final static Uri DUEDATE_URI = Uri.parse("content://" + AUTHORITY_NAME
	    + "/" + TASK_TABLE + "/" + TASK_DUE_DATE);

    // projection used to display in alert listview
    //
    // you use two pairs. One used inside the contentprovider and another
    // outside of the content provider
    public final static String[] TASK_ALERT_LISTVIEW_CP = { TASK_ID, TASK, TASK_DUE_DATE };
    public final static String[] TASK_ALERT_LISTVIEW_NO_CP = { TASK, TASK_DUE_DATE };

    public final static String DEFAULT_SORT = TASK_DUE_DATE + " ASC";

    public final static String LTE = "<=";
    public final static String EQ = "=";
    public final static String GTE = ">=";
}
