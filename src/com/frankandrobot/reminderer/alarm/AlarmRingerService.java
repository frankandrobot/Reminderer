package com.frankandrobot.reminderer.alarm;

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

import com.frankandrobot.reminderer.helpers.Logger;

import java.util.Timer;
import java.util.TimerTask;

//TODO test these scenarios:
//DONE phone is locked and alarm comes in
//DONE respects silent mode
//DONE use alarm volume
//DONE on phone/listening to music and alarm comes in
//DONE alarm comes in then get phone call/start music
//DONE headphones plugged in (and listening to music or on call)
//DONE return audio focus when done
//DONE handle what happens when two alarms are due back to back?
//DONE handle when user kills alarm
//DONE handle when alarm expires

public class AlarmRingerService extends Service
{
    private static final String TAG = "R:AlarmRinger";

    private static final long[] sVibratePattern = new long[]{500, 500};
    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;
    private static final long ALARM_TIMEOUT = 1000 * 30;

    private AudioManager audioManager;

    private MediaPlayer mMediaPlayer;
    private PhoneVibrator phoneVibrator = null;
    private MediaPlayerSetup mediaPlayerSetup = null;
    private float systemAlarmVolume;
    private AudioFocusMonitor focusMonitor = new AudioFocusMonitor();
    private StopAlarmTimer stopAlarmTimer;

    @Override
    public void onCreate()
    {
        AlarmAlertWakeLock.getInstance().acquireCpuWakeLock(this, 0);
    }

    @Override
    public void onDestroy()
    {
        stop();

        if (stopAlarmTimer != null ) stopAlarmTimer.cancel();
        stopAlarmTimer = null;

        AlarmAlertWakeLock.getInstance().releaseCpuLock(0);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    /**
     * Called with each request to the start the service.
     *
     * The only tricky part is handling back-to-back alarms.
     *
     * @param intent the intent
     * @param flags the flags
     * @param startId the startId
     * @return sticky flag
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (Logger.LOGV) Log.v(TAG, "onStartCommand()");

        if (intent != null)
        {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            int ringerMode = audioManager.getRingerMode();

            synchronized(this)
            {
                //enable alarm sound
                if (ringerMode == AudioManager.RINGER_MODE_NORMAL)
                {
                    //if the media player is already setup don't set it up again
                    if (mediaPlayerSetup == null)
                    {
                        mediaPlayerSetup = new MediaPlayerSetup().setup();
                    }
                    mediaPlayerSetup.start();
                }
                //enable vibrator
                if (ringerMode == AudioManager.RINGER_MODE_NORMAL
                        || ringerMode == AudioManager.RINGER_MODE_SILENT)
                {
                    if (phoneVibrator == null)
                    {
                        phoneVibrator = new PhoneVibrator().setup();
                    }
                    phoneVibrator.start();
                }
                //setup alarm killer
                if (stopAlarmTimer != null)
                {
                    stopAlarmTimer.cancel();
                }
                stopAlarmTimer = new StopAlarmTimer(true);
                stopAlarmTimer.start();
            }
            return START_STICKY;
        }

        // No intent, tell the system not to restart us.
        stopSelf();
        return START_NOT_STICKY;
    }

/*        final Task task = intent.getParcelableExtra(AlarmConstants.TASK_INTENT_EXTRA);

        if (task == null)
        {
            Log.v(TAG, "Failed to parse the task from the intent");
            stopSelf();
            return START_NOT_STICKY;
        }

        // TODO what does this do? stop alarm if receive two alarms in a row?
        if (mCurrentTask != null)
        {
            sendKillBroadcast(mCurrentTask);
        }*/

        // TODO add foreground notification
        //mCurrentTask = task;

    /**
     * Initializes the MediaPlayer.
     *
     * Sets up the MediaPlayer via #prepareAsync.
     * Logic continues in #onPrepared and other listener classes.
     *
     * As per the docs, since this is a Service we should use #prepareAsync.
     *
     * NOTE: Instead of using the TelephonyManager to figure out if we're on a call,
     * we get an audio focus.
     */
    class MediaPlayerSetup implements OnPreparedListener
    {
        private boolean isPrepared = false;

        public MediaPlayerSetup() {}

        public MediaPlayerSetup setup()
        {
            if (Logger.LOGV) Log.v(TAG, "setupMediaPlayer()");

            //pre-setup before calling onPrepare
            systemAlarmVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                                  / (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayerErrorListener());
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try
            {
                mMediaPlayer.setDataSource(AlarmRingerService.this,
                                           Settings.System.DEFAULT_ALARM_ALERT_URI);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Couldn't load ringer: " + e.toString());
                mMediaPlayer.release();
                mMediaPlayer = null;
                return null;
            }
            mMediaPlayer.setOnPreparedListener(this);
            // logic continues in onPrepared
            return this;
        }

        /**
         * Called when MediaPlayer is ready
         *
         * @param arg0 the media player
         */
        @Override
        public void onPrepared(MediaPlayer arg0)
        {
            if (Logger.LOGV) Log.v(TAG, "onPrepared() media player");
            isPrepared = true;
            startPlayback();
        }

        public void start()
        {
            if (Logger.LOGV) Log.v(TAG, "start() media player");
            if (!isPrepared) mMediaPlayer.prepareAsync();
            else startPlayback();
        }

        private void startPlayback()
        {
            if (Logger.LOGV) Log.v(TAG, "start()");

            int result = audioManager.requestAudioFocus(focusMonitor,
                                                        AudioManager.STREAM_MUSIC,
                                                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            {
                Log.e(TAG, "could not get audio focus. User on phone?");
                new MediaPlayerErrorListener().onError(mMediaPlayer, 0, 0);
            }
            else
            {
                if (Logger.LOGV) Log.v(TAG, "playing ringtone");
                mMediaPlayer.setVolume(systemAlarmVolume, systemAlarmVolume);
                if (mMediaPlayer.isPlaying())
                {
                    mMediaPlayer.seekTo(0);
                }
                mMediaPlayer.start();
            }
            // logic continues in onAudioFocusChange
        }
    }

    /**
     * This is how we handle phone calls and other cases when the audio is in
     * use
     */
    class AudioFocusMonitor implements OnAudioFocusChangeListener
    {
        @Override
        public void onAudioFocusChange(int focusChange)
        {
            // test for null mediaplayer and exit if null
            if (mMediaPlayer != null)
            {
                switch (focusChange)
                {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // resume playback
                        if (!mMediaPlayer.isPlaying())
                        {
                            if (Logger.LOGV) Log.v(TAG, "playing alarm at system alarm volume");
                            mMediaPlayer.setVolume(systemAlarmVolume, systemAlarmVolume);
                            mMediaPlayer.start();
                        }
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS:
                        // Lost focus for an unbounded amount of time: stop playback and
                        // release media player
                        if (Logger.LOGV) Log.v(TAG, "mediaplayer lost focus");
                        if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // Lost focus for a short time, but we have to stop
                        // playback. We don't release the media player because playback
                        // is likely to resume
                        if (Logger.LOGV)
                            Log.v(TAG, "mediaplayer temporarily lost focus");
                        if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // Lost focus for a short time, but it's ok to keep playing
                        // at an attenuated level
                        if (mMediaPlayer.isPlaying())
                        {
                            if (Logger.LOGV) Log.v(TAG, "playing alarm at min volume");
                            mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                        }
                        break;
                }
            }
        }
    }

    class MediaPlayerErrorListener implements OnErrorListener
    {
        @Override
        public boolean onError(MediaPlayer mp, int arg1, int arg2)
        {
            Log.e(TAG, "Error occurred while playing audio.");
            mp.stop();
            mp.release();
            mMediaPlayer = null;
            return true;
        }
    }

    /*private void sendKillBroadcast(Task task)
    {
        long millis = System.currentTimeMillis() - mStartTime;
        int minutes = (int) Math.round(millis / 60000.0);
        Intent alarmKilled = new Intent(AlarmConstants.TASK_ALARM_KILLED);
        // alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, task);
        // alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
        // tell alarm receiver to stop notification & update AlarmAlertActivity
        sendBroadcast(alarmKilled);
    }*/

    /**
     * Stops alarm audio and disables alarm if it not snoozed and not repeating
     */
    synchronized public void stop()
    {
        if (Logger.LOGV) Log.v(TAG, "stop()");
        if (mMediaPlayer != null)
        {
            if (mMediaPlayer.isPlaying())
            {
                mMediaPlayer.stop();
                ((AudioManager)getSystemService(Context.AUDIO_SERVICE))
                        .abandonAudioFocus(focusMonitor);
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (phoneVibrator != null) phoneVibrator.stop();
    }

    /**
     * Start the vibrator
     */
    class PhoneVibrator
    {
        private Vibrator mVibrator;

        public PhoneVibrator setup()
        {
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            return this;
        }

        public PhoneVibrator start()
        {
            mVibrator.vibrate(sVibratePattern, 0);
            return this;
        }

        public PhoneVibrator stop()
        {
            mVibrator.cancel();
            return this;
        }
    }

    class StopAlarmTimer extends Timer
    {
        private TimerTask mAlarmKillerTask = new TimerTask()
        {

            @Override
            public void run()
            {
                if (Logger.LOGV)
                {
                    Log.v(TAG, "*********** alarm killer triggered ***********");
                }
                //sendKillBroadcast(mCurrentTask);
                stopSelf();
            }
        };

        public StopAlarmTimer(boolean b) { super(b); }

        public void start()
        {
            this.schedule(mAlarmKillerTask, ALARM_TIMEOUT);
        }
    }
}
