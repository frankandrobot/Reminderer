package com.frankandrobot.reminderer.alarm;

public class AlarmConstants
{
    /* This string is used when passing an Task object through an intent. */
    public static final String TASK_INTENT_EXTRA = "intent.extra.task";
    // This action triggers the AlarmReceiver as well as the AlarmRinger. It
    // is a public action used in the manifest for receiving alarm broadcasts
    // from the alarm manager.
    public static final String TASK_ALARM_ALERT = "com.frankandrobot.reminderer.TASK_ALARM_ALERT";
    public static final String TASK_ALARM_KILLED = "com.frankandrobot.reminderer.TASK_ALARM_KILLED";
    /** This extra is the raw alarm object data. It is used in the
      * AlarmManagerService to avoid a ClassNotFoundException when filling in
      * the Intent extras.
     * */
    public static final String TASK_RAW_DATA = "intent.extra.task_raw";
    /**
     * The next task due time
     */
    public static final String TASK_DUETIME = "com.frankandrobot.reminderer.nexttaskduetime";
    /**
     * Play alarm up to 10 minutes before silencing
     */
    // TODO change back to 10 minute timeout
    public static final int ALARM_TIMEOUT_SECONDS = 1 * 60;
    public static final String TASK_ID_DATA = "com.frankandrobot.reminderer.task_id_data";
    /**
     * If the alarm is older than STALE_WINDOW seconds, ignore. It is probably
     * the result of a time or timezone change
     */
    final static int STALE_WINDOW = 60 * 30;
}
