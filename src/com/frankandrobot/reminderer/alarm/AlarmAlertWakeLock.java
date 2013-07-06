package com.frankandrobot.reminderer.alarm;

import com.frankandrobot.reminderer.helpers.Logger;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

/**
 * Convenience class for acquiring CPU wake lock - keep the phone from going
 * back to sleep when alarm goes off
 * 
 * @author uri
 * 
 */
class AlarmAlertWakeLock {
    private static String TAG = "R:WakeLock";
    private static PowerManager.WakeLock sCpuWakeLock;

    static void acquireCpuWakeLock(Context context) {
	if (Logger.LOGV)
	    Log.v(TAG, "Acquiring cpu wake lock");
	if (sCpuWakeLock != null) {
	    return;
	}

	PowerManager pm = (PowerManager) context
		.getSystemService(Context.POWER_SERVICE);

	sCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
		| PowerManager.ACQUIRE_CAUSES_WAKEUP
		| PowerManager.ON_AFTER_RELEASE, TAG);
	sCpuWakeLock.acquire();
    }

    static void releaseCpuLock() {
	if (Logger.LOGV)
	    Log.v(TAG, "Releasing cpu wake lock");
	if (sCpuWakeLock != null) {
	    sCpuWakeLock.release();
	    sCpuWakeLock = null;
	}
    }
}
