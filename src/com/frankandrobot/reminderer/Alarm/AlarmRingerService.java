package com.frankandrobot.reminderer.Alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import com.frankandrobot.reminderer.Helpers.Logger;
import com.frankandrobot.reminderer.parser.Task;

import java.util.Timer;
import java.util.TimerTask;

//TODO test these scenarios:
//TODO phone is locked and alarm comes in
//TODO on phone/listening to music and alarm comes in
//TODO alarm comes in then get phone call/start music
//TODO headphones plugged in (and listening to music or on call)
//TODO handle what happens when two alarms are due back to back?
//DONE handle when user kills alarm
//DONE handle when alarm expires

public class AlarmRingerService extends Service implements OnPreparedListener,
	OnErrorListener, OnAudioFocusChangeListener {
    private static final String TAG = "R:AlarmRinger";
    private static final long[] sVibratePattern = new long[] { 500, 500 };
    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    private boolean mPlaying = false;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Task mCurrentTask;
    private long mStartTime;

    // Internal messages
//    private static final int KILLER = 1000;
//    
//    private Handler mHandler = new Handler() {
//	public void handleMessage(Message msg) {
//	    switch (msg.what) {
//	    case KILLER:
//		if (Logger.LOGV) {
//		    Log.v(TAG, "*********** Alarm killer triggered ***********");
//		}
//		sendKillBroadcast((Task) msg.obj);
//		stopSelf();
//		break;
//	    }
//	}
//    };
    private Timer mAlarmKiller;
    
    private TimerTask mAlarmKillerTask = new TimerTask() {

	@Override
	public void run() {
	    if (Logger.LOGV) {
		    Log.v(TAG, "*********** Alarm killer triggered ***********");
		}
		sendKillBroadcast(mCurrentTask);
		stopSelf();
	}
	
    };

    @Override
    public void onCreate() {
	mAlarmKiller = new Timer();
	mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	AlarmAlertWakeLock.acquireCpuWakeLock(this);
    }

    @Override
    public void onDestroy() {
	mAlarmKiller.cancel();
	mAlarmKiller = null;
	stop();
	AlarmAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	if (Logger.LOGV)
	    Log.v(TAG, "onStartCommand()");

	// No intent, tell the system not to restart us.
	if (intent == null) {
	    stopSelf();
	    return START_NOT_STICKY;
	}

	final Task task = intent
		.getParcelableExtra(AlarmConstants.TASK_INTENT_EXTRA);

	if (task == null) {
	    Log.v(TAG, "Failed to parse the task from the intent");
	    stopSelf();
	    return START_NOT_STICKY;
	}

	// TODO what does this do? stop alarm if receive two alarms in a row?
	if (mCurrentTask != null) {
	    sendKillBroadcast(mCurrentTask);
	}

	// TODO add foreground notification
	mCurrentTask = task;
	initMediaPlayer();
	return START_STICKY;
    }

    /**
     * Initializes the media player
     * 
     * it uses prepareAsync() and requests an AudioFocus, so the logic continues
     * in the callback listeners.
     * 
     * NOTE: instead of the complicated init function, we could probably just
     * use one of the static MediaPlayer.create() methods
     */
    private void initMediaPlayer() {
	if (Logger.LOGV)
	    Log.v(TAG, "initMediaPlayer()");
	mMediaPlayer = new MediaPlayer();
	mMediaPlayer.setOnErrorListener(this);
	// TODO reenable looping
	// mMediaPlayer.setLooping(true);
	mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	try {
	    mMediaPlayer.setDataSource(this,
		    Settings.System.DEFAULT_RINGTONE_URI);
	} catch (Exception e) {
	    Log.e(TAG, "Couldn't load ringer: " + e.toString());
	    mMediaPlayer.release();
	    mMediaPlayer = null;
	    // can't play ringer but can run vibrator and continue other
	    // processing
	    finalProcessing();
	    return;
	}
	mMediaPlayer.setOnPreparedListener(this);
	mMediaPlayer.prepareAsync();
	// logic continues in onPrepared
    }

    /*
     * As per the docs, we're using prepareAsync() because this is a Service.
     * onPrepared() is the callback.
     * 
     * Instead of using the TelephonyManager to figure out if we're on a call,
     * we get an audio focus. The callback to requestAudioFocus() tells us if
     * other apps are using the audio.
     */
    @Override
    public void onPrepared(MediaPlayer arg0) {
	if (Logger.LOGV)
	    Log.v(TAG, "onPrepared()");
	AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	int result = audioManager.requestAudioFocus(this,
		AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
	    Log.e(TAG, "could not get audio focus");
	} else {
	    if (Logger.LOGV)
		Log.v(TAG, "playing ringtone");
	    mMediaPlayer.start();
	}
	// start vibrator and timer to kill service
	finalProcessing();
	// logic continues in onAudioFocusChange
    }

    /*
     * This is how we handle phone calls and other cases when the audio is in
     * use
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
	// test for null mediaplayer and exit if null
	if (mMediaPlayer == null)
	    return;
	switch (focusChange) {
	case AudioManager.AUDIOFOCUS_GAIN:
	    // resume playback
	    if (!mMediaPlayer.isPlaying()) {
		if (Logger.LOGV)
		    Log.v(TAG, "playing alarm at max volume");
		mMediaPlayer.start();
		mMediaPlayer.setVolume(1.0f, 1.0f);
	    }
	    break;

	case AudioManager.AUDIOFOCUS_LOSS:
	    // Lost focus for an unbounded amount of time: stop playback and
	    // release media player
	    if (Logger.LOGV)
		Log.v(TAG, "mediaplayer lost focus");
	    if (mMediaPlayer.isPlaying())
		mMediaPlayer.stop();
	    mMediaPlayer.release();
	    mMediaPlayer = null;
	    break;

	case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	    // Lost focus for a short time, but we have to stop
	    // playback. We don't release the media player because playback
	    // is likely to resume
	    if (Logger.LOGV)
		Log.v(TAG, "mediaplayer temporarily lost focus");
	    if (mMediaPlayer.isPlaying())
		mMediaPlayer.pause();
	    break;

	case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
	    // Lost focus for a short time, but it's ok to keep playing
	    // at an attenuated level
	    if (mMediaPlayer.isPlaying()) {
		if (Logger.LOGV)
		    Log.v(TAG, "playing alarm at min volume");
		mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
	    }
	    break;
	}
    }

    @Override
    public boolean onError(MediaPlayer mp, int arg1, int arg2) {
	Log.e(TAG, "Error occurred while playing audio.");
	mp.stop();
	mp.release();
	mMediaPlayer = null;
	return true;
    }

    private void sendKillBroadcast(Task task) {
	long millis = System.currentTimeMillis() - mStartTime;
	int minutes = (int) Math.round(millis / 60000.0);
	Intent alarmKilled = new Intent(AlarmConstants.TASK_ALARM_KILLED);
	// alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, task);
	// alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
	// tell alarm receiver to stop notification & update AlarmAlertActivity
	sendBroadcast(alarmKilled);
    }

    /**
     * Stops alarm audio and disables alarm if it not snoozed and not repeating
     */
    public void stop() {
	if (Logger.LOGV)
	    Log.v(TAG, "stop()");
	if (mPlaying) {
	    mPlaying = false;

	    // Stop audio playing
	    if (mMediaPlayer != null) {
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer = null;
	    }

	    // Stop vibrator
	    mVibrator.cancel();
	}
	disableKiller();
    }

    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm won't run all
     * day.
     * 
     * This just cancels the audio, but leaves the notification popped, so the
     * user will know that the alarm tripped.
     */
    private void enableKiller(Task task) {
	mAlarmKiller.schedule(mAlarmKillerTask, 1000 * AlarmConstants.ALARM_TIMEOUT_SECONDS);
//	mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, task),
//		1000 * AlarmConstants.ALARM_TIMEOUT_SECONDS);
    }

    private void disableKiller() {
	mAlarmKillerTask.cancel();
//	mHandler.removeMessages(KILLER);
    }

    /**
     * Start the vibrator, enable the timer to kill the service, record
     * startTime
     */
    private void finalProcessing() {
	/* Start the vibrator */
	mVibrator.vibrate(sVibratePattern, 0);
	// set the timer to kill the alarm
	enableKiller(mCurrentTask);
	mPlaying = true;
	mStartTime = System.currentTimeMillis();
    }
}
