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
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

/**
 * Implements a left fling on a {@link android.view.View}.
 *
 * A swipe is a "left fling" if the user moved his finger at more than
 * SCREEN_WIDTH / 2 pixels per second.
 *
 */
public class LeftFlingListener implements OnTouchListener
{
    private VelocityTracker mVelocityTracker = null;

    private int cursorPosition;
    private FlingThreshold flingThreshold;
    private IFlingListener flingListener;
    private Animation animation;
    private boolean isFlinging;

    /**
     * Instantiate this class to get a fling threshold.
     *
     * When using in a ListView, it doesn't make sense to instantiate
     * this class for each row. Instantiate it once then use it for each row.
     */
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

    /**
     * #onFling is called when the fling animation finishes
     */
    public interface IFlingListener
    {
        public void onFling(int position, View view, float velocity);
    }

    public LeftFlingListener(FlingThreshold flingThreshold,
                             Animation animation,
                             IFlingListener flingListener)
    {
        this.flingThreshold = flingThreshold;
        this.animation = animation;
        this.flingListener = flingListener;
    }

    static public Animation getDefaultAnimation(int distanceToTranslate)
    {
        TranslateAnimation translateAnim = new TranslateAnimation(0,
                                                                  distanceToTranslate,
                                                                  0,
                                                                  0);
        translateAnim.setDuration(500);
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
        return translateAnim;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event)
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
                isFlinging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                //get velocity in pixels per second
                mVelocityTracker.computeCurrentVelocity(1000);
                final float velocity = VelocityTrackerCompat.getXVelocity(mVelocityTracker,
                                                                  pointerId);
                if (!isFlinging && velocity < -flingThreshold.value() )
                {
                    isFlinging = true;
                    Log.d("",
                          "Fling!: " + velocity);
                        view.clearAnimation();
                    animation.setFillAfter(true);
                    animation.setFillEnabled(true);
                    animation.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation)
                        {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation)
                        {
                            flingListener.onFling(cursorPosition, view, velocity);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation)
                        {

                        }
                    });
                    view.startAnimation(animation);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isFlinging = false;
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }

    public void setCursorPosition(int pos) { this.cursorPosition = pos; }
}
