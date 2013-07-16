package com.frankandrobot.reminderer.alarm;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.frankandrobot.reminderer.helpers.Logger;

/**
 * Convenience class for acquiring CPU wake lock - keep the phone from going
 * back to sleep when alarm goes off
 *
 * @author uri
 */
class AlarmAlertWakeLock
{
    final private static AlarmAlertWakeLock instance = new AlarmAlertWakeLock();
    final private static String TAG = "R:WakeLock";
    private static PowerManager.WakeLock sCpuWakeLock;

    static AlarmAlertWakeLock getInstance() { return instance; }

    void acquireCpuWakeLock(Context context)
    {
        if (Logger.LOGV) Log.v(TAG, "Acquiring cpu wake lock");
        if (sCpuWakeLock == null)
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            sCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                                                  | PowerManager.ACQUIRE_CAUSES_WAKEUP //force screen to turn on
                                                  | PowerManager.ON_AFTER_RELEASE, //makes screen stay on longer
                                          TAG);
            sCpuWakeLock.acquire();
        }
    }

    void releaseCpuLock()
    {
        if (Logger.LOGV) Log.v(TAG, "Releasing cpu wake lock");
        if (sCpuWakeLock != null)
        {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
