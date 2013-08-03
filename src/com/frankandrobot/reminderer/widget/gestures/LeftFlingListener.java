package com.frankandrobot.reminderer.widget.gestures;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.frankandrobot.reminderer.widget.MainTaskListFragment;

/**
 * Implements a left fling on a {@link android.view.View}.
 *
 * A swipe is a "left fling" if the user moved his finger at more than
 * SCREEN_WIDTH / 2 pixels per second.
 *
 */
public class LeftFlingListener implements OnTouchListener
{
    private MainTaskListFragment mainTaskListFragment;
    private VelocityTracker mVelocityTracker = null;

    private int cursorPosition;
    private FlingThreshold flingThreshold;

    static public class FlingThreshold
    {
        private int flingThreshold;
        private int fullWidth;

        public FlingThreshold(Context context)
        {
            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(dm);
            fullWidth = (int) (Math.ceil(dm.widthPixels * (dm.densityDpi / 160.0)));
            flingThreshold = (int) (Math.ceil(dm.widthPixels * (dm.densityDpi / 160.0)) * 0.5f);
        }

        public int value() { return flingThreshold; }

        public int fullWidth() { return fullWidth; }
    }

    public LeftFlingListener(FlingThreshold flingThreshold)
    {
        this.flingThreshold = flingThreshold;
    }


    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                //get velocity in pixels per second
                mVelocityTracker.computeCurrentVelocity(1000);
                // Best practice to use VelocityTrackerCompat where possible.
                if (VelocityTrackerCompat.getXVelocity(mVelocityTracker,
                                                       pointerId) < -flingThreshold.value() )
                {
                Log.d("",
                      "Fling!: " + VelocityTrackerCompat.getXVelocity(mVelocityTracker,
                                                                      pointerId));
                //Log.d("", "Y velocity: " + String.valueOf(viewPosition));
                    view.clearAnimation();
                    TranslateAnimation translateAnim = new TranslateAnimation(0,
                                                                              -flingThreshold.fullWidth(),
                                                                              0,
                                                                              0);
                    translateAnim.setDuration(250);
                    translateAnim.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            //yourListViewAdapter.yourListItems.remove(position);
                            //yourListViewAdapter.notifyDataSetChanged();
                        }
                    });
                    view.startAnimation(translateAnim);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }

    public void setCursorPosition(int pos) { this.cursorPosition = pos; }
}
