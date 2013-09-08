package com.frankandrobot.reminderer.ui.gestures;

import android.support.v4.app.ListFragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
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
    final static private String TAG = "R:"+LeftFlingListener.class.getSimpleName();

    private int cursorPosition;
    private IFlingListener flingListener;
    private boolean isFlinging;
    private ListFragment listFragment;

    private float currentX;
    private float currentAlpha;

    private float mDownX;
    private int mSwipeSlop = -1;
    private boolean isAnimating;

    /**
     * #onFling is called when the fling animation finishes
     */
    public interface IFlingListener
    {
        public void onFling(int position, View view);
    }

    public LeftFlingListener(ListFragment listFragment,
                             IFlingListener flingListener)
    {
        this.flingListener = flingListener;
        this.listFragment = listFragment;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        if (mSwipeSlop < 0) {
            mSwipeSlop = ViewConfiguration.get(listFragment.getActivity())
                                 .getScaledTouchSlop();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Multi-item swipes not handled
                if (isAnimating) return true;

                //mItemPressed = true;
                mDownX = event.getX();
                break;
            case MotionEvent.ACTION_CANCEL:
                setSwipePosition(view, 0);
                //mItemPressed = false;
                break;
            case MotionEvent.ACTION_MOVE:
            {
                if (isAnimating) return true;

                final float x = event.getX();

                final float deltaX = x - mDownX;
                final float deltaXAbs = Math.abs(deltaX);
                if (!isFlinging)
                {
                    if (deltaXAbs > mSwipeSlop)
                    {
                        isFlinging = true;
                        listFragment.getListView().requestDisallowInterceptTouchEvent(true);
                        //mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                    }
                }
                if (isFlinging)
                {
                   setSwipePosition(view, deltaX);
                }
            }
            break;
            case MotionEvent.ACTION_UP:
            {
                if (isAnimating) return true;

                // User let go - figure out whether to animate the view out, or back into place
                if (isFlinging) {
                    final float x = event.getX();
                    final float deltaX = x - mDownX;
                    final float deltaXAbs = Math.abs(deltaX);
                    final float fractionCovered;
                    float endX;
                    final boolean remove;
                    if (deltaXAbs > view.getWidth() / 4)
                    {
                        // Greater than a quarter of the width - animate it out
                        fractionCovered = deltaXAbs / view.getWidth();
                        endX = deltaX < 0 ? -view.getWidth() : view.getWidth();
                        remove = true;
                    } else
                    {
                        // Not far enough - animate it back
                        fractionCovered = 1 - (deltaXAbs / view.getWidth());
                        endX = 0;
                        remove = false;
                    }
                    // Animate position and alpha
                    long duration = (int) ((1 - fractionCovered) * 500);
                    animateSwipe(view, endX, duration, remove);
                } else {
                    //mItemPressed = false;
                }
            }
            break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Sets the horizontal position and translucency of the view being swiped.
     */
    private void setSwipePosition(View view, float deltaX)
    {
        final float fraction = Math.abs(deltaX) / view.getWidth();

        TranslateAnimation swipeAnim = new TranslateAnimation(deltaX, deltaX, 0, 0);
        currentX = deltaX;
        currentAlpha = (1 - fraction);
        AlphaAnimation alphaAnim = new AlphaAnimation(currentAlpha,
                                                      currentAlpha);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(swipeAnim);
        set.addAnimation(alphaAnim);
        set.setFillAfter(true);
        set.setFillEnabled(true);
            view.startAnimation(set);
    }

    /**
     * Animates a swipe of the item either back into place or out of the listview container.
     * NOTE: This is a simplified version of swipe behavior, for the purposes of this demo
     * about animation. A real version should use velocity (via the VelocityTracker class)
     * to send the item off or back at an appropriate speed.
     */
    private void animateSwipe(final View view, float endX, long duration, final boolean remove)
    {
        isAnimating = true;
        listFragment.getListView().setEnabled(false);
        TranslateAnimation swipeAnim = new TranslateAnimation(currentX, endX, 0, 0);
        AlphaAnimation alphaAnim = new AlphaAnimation(currentAlpha, remove ? 0 : 1);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(swipeAnim);
        set.addAnimation(alphaAnim);
        set.setDuration(duration);
        view.startAnimation(set);
        view.getAnimation().setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                flingListener.onFling(cursorPosition, view);
                isFlinging = false;
                isAnimating = false;
                listFragment.getListView().setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    public void setCursorPosition(int pos) { this.cursorPosition = pos; }
}
