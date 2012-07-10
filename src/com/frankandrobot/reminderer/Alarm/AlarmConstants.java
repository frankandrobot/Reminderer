package com.frankandrobot.reminderer.Alarm;

public class AlarmConstants {

    /**
     * If the alarm is older than STALE_WINDOW seconds, ignore. It is probably
     * the result of a time or timezone change
     */
    final static int STALE_WINDOW = 60 * 30;

    /* This string is used when passing an Task object through an intent. */
    public static final String TASK_INTENT_EXTRA = "intent.extra.task";

    // This action triggers the AlarmReceiver as well as the AlarmRinger. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    public static final String TASK_ALARM = "com.frankandrobot.reminderer.TASK_ALARM";

    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String TASK_RAW_DATA = "intent.extra.task_raw";
}
