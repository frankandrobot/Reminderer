package com.frankandrobot.reminderer.widget.gestures;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;

import com.frankandrobot.reminderer.helpers.Logger;

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
    private ListView listView;

    private float startX, startY;
    private int activePointerId;

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
        public void onFling(int position, View view);
    }

    public LeftFlingListener(FlingThreshold flingThreshold,
                             AnimationSet animation,
                             ListView listView,
                             IFlingListener flingListener)
    {
        this.flingThreshold = flingThreshold;
        this.animation = animation;
        this.flingListener = flingListener;
        this.listView = listView;
    }

    /**
     * Gets the default translate animation.
     *
     * It moves the view to the left (hence the {@link LeftFlingListener}).
     *
     *
     * @param distanceToTranslate distance to translate row
     * @return this
     */
    static public AnimationSet getDefaultAnimation(int distanceToTranslate)
    {
        TranslateAnimation swipeAnim = new TranslateAnimation(0, distanceToTranslate, 0, 0);
        AlphaAnimation alphaAnim = new AlphaAnimation(1, 0);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(swipeAnim);
        set.addAnimation(alphaAnim);
        set.setDuration(500);
        return set;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event)
    {
        final int index = event.getActionIndex();
        final int action = event.getActionMasked();
        final int pointerId = event.getPointerId(index);

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                isFlinging = false;

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

                //remember where we started
                startX = (float) view.getTop() + (float) view.getHeight() * 0.5f;

                //save pointer ID
                activePointerId = MotionEventCompat.getPointerId(event,
                                                                 MotionEventCompat.getActionIndex(event));

                break;
            case MotionEvent.ACTION_MOVE:
                //if we move outside the view, the cancel the fling
                final int pointerIndex =
                        MotionEventCompat.findPointerIndex(event, activePointerId);
                final float y = MotionEventCompat.getY(event, pointerIndex);

                if (Math.abs(y - startY) > view.getHeight())
                {
                    isFlinging = false;
                }

                mVelocityTracker.addMovement(event);
                //get velocity in pixels per second
                mVelocityTracker.computeCurrentVelocity(1000);
                final float velocity = VelocityTrackerCompat.getXVelocity(mVelocityTracker,
                                                                          pointerId);
                if (!isFlinging && velocity < -flingThreshold.value() )
                {
                    if (Logger.LOGD) Log.d("", "Fling!: " + velocity);
                    isFlinging = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isFlinging)
                {
                    isFlinging = false;
                    listView.setEnabled(false);
                    view.clearAnimation();
                    view.startAnimation(animation);
                    view.getAnimation().setAnimationListener(new AnimationListener()
                    {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation)
                        {
                            //view.setVisibility(View.INVISIBLE);
                            flingListener.onFling(cursorPosition,
                                                  view);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                }
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }

    public void setCursorPosition(int pos) { this.cursorPosition = pos; }
}
