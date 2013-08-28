package com.frankandrobot.reminderer.alarm;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.frankandrobot.reminderer.helpers.Logger;

import java.util.HashMap;

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

    HashMap<Integer,PowerManager.WakeLock> hmWakeLocks = new HashMap<Integer, PowerManager.WakeLock>();

    static AlarmAlertWakeLock getInstance() { return instance; }

    /**
     * Creates a new wake lock for the given wake lock id
     * If a wake lock for the given id already exists, this method does nothing
     *
     * @param context da context
     * @param wakeLockId wake lock id
     */
    void acquireCpuWakeLock(Context context, int wakeLockId)
    {
        if (Logger.LOGV) Log.v(TAG, "Acquiring cpu wake lock");
        if (hmWakeLocks.get(wakeLockId) == null)
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            PowerManager.WakeLock sCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                                                                | PowerManager.ACQUIRE_CAUSES_WAKEUP //force screen to turn on
                                                                | PowerManager.ON_AFTER_RELEASE, //makes screen stay on longer
                                                                TAG);
            sCpuWakeLock.acquire();

            hmWakeLocks.put(wakeLockId, sCpuWakeLock);
        }
    }

    void releaseCpuLock(int wakeLockId)
    {
        if (Logger.LOGV) Log.v(TAG, "Releasing cpu wake lock");
        if (hmWakeLocks.get(wakeLockId) != null)
        {
            hmWakeLocks.get(wakeLockId).release();
            hmWakeLocks.remove(wakeLockId);
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        for(Integer wakeLockId:hmWakeLocks.keySet())
            hmWakeLocks.get(wakeLockId).release();

        super.finalize();
    }
}
