package com.uiza.sdk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.uiza.sdk.utils.UZData;
import com.uiza.sdk.utils.UZViewUtils;

import java.util.List;

import timber.log.Timber;

/**
 * Player View
 */

//https://github.com/google/ExoPlayer/issues/4031
//I want to to show playback controls only when onTouch event is fired.
// How to prevent control buttons being showed up when on long pressing, dragging etc.?
public final class UZPlayerView extends PlayerView implements PlayerControlView.VisibilityListener {

    private boolean controllerVisible;
    private GestureDetectorCompat mDetector;
    private OnTouchEvent onTouchEvent;
    private OnSingleTap onSingleTap;
    private OnDoubleTap onDoubleTap;
    private OnLongPressed onLongPressed;
    private ControllerStateCallback controllerStateCallback;

    private boolean doubleTapActivated = true;
    // Variable to save current state
    private boolean isDoubleTap = false;
    /**
     * Default time window in which the double tap is active
     * Resets if another tap occurred within the time window by calling
     * {@link UZPlayerView#keepInDoubleTapMode()}
     **/
    long doubleTapDelay = 650;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = () -> {
        isDoubleTap = false;
        if (onDoubleTap != null)
            onDoubleTap.onDoubleTapFinished();
    };

    public UZPlayerView(Context context) {
        this(context, null);
    }

    public UZPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UZPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode())
            setControllerVisibilityListener(this);
        mDetector = new GestureDetectorCompat(context, new UZGestureListener());
    }

    public boolean isControllerVisible() {
        return controllerVisible;
    }

    public void setControllerStateCallback(ControllerStateCallback controllerStateCallback) {
        this.controllerStateCallback = controllerStateCallback;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        controllerVisible = visibility == View.VISIBLE;
        if (controllerStateCallback != null)
            controllerStateCallback.onVisibilityChange(controllerVisible);
    }

    public void toggleShowHideController() {
        if (controllerVisible)
            hideController();
        else
            showController();
    }

    @Override
    public void showController() {
        if (!UZData.getInstance().isSettingPlayer())
            super.showController();
    }

    @Override
    public void hideController() {
        if (!UZData.getInstance().isSettingPlayer())
            super.hideController();
    }

    public void setOnTouchEvent(OnTouchEvent onTouchEvent) {
        this.onTouchEvent = onTouchEvent;
    }

    public void setOnSingleTap(OnSingleTap onSingleTap) {
        this.onSingleTap = onSingleTap;
    }

    public void setOnDoubleTap(OnDoubleTap onDoubleTap) {
        this.onDoubleTap = onDoubleTap;
    }

    /**
     * Resets the timeout to keep in double tap mode.
     * <p>
     * Called once in {@link OnDoubleTap#onDoubleTapStarted} Needs to be called
     * from outside if the double tap is customized / overridden to detect ongoing taps
     */
    public void keepInDoubleTapMode() {
        isDoubleTap = true;
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, doubleTapDelay);
    }

    /**
     * Cancels double tap mode instantly by calling {@link OnDoubleTap#onDoubleTapFinished()}
     */
    public void cancelInDoubleTapMode() {
        mHandler.removeCallbacks(mRunnable);
        isDoubleTap = false;
        onDoubleTap.onDoubleTapFinished();
    }

    public void setOnLongPressed(OnLongPressed onLongPressed) {
        this.onLongPressed = onLongPressed;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (UZData.getInstance().isUseUZDragView())
            return false;
        else {
            return mDetector.onTouchEvent(ev);
        }
    }

    public PlayerControlView getPlayerControlView() {
        for (int i = 0; i < this.getChildCount(); i++) {
            if (this.getChildAt(i) instanceof PlayerControlView)
                return (PlayerControlView) getChildAt(i);
        }
        return null;
    }

    public View[] getAllChild() {
        PlayerControlView playerControlView = getPlayerControlView();
        if (playerControlView == null) return null;
        List<View> viewList = UZViewUtils.getAllChildren(playerControlView);
        return viewList.toArray(new View[0]);
    }

    public interface ControllerStateCallback {
        void onVisibilityChange(boolean isShow);
    }

    public interface OnSingleTap {
        void onSingleTapConfirmed(float x, float y);
    }

    public interface OnLongPressed {
        void onLongPressed(float x, float y);
    }

    public interface OnDoubleTap {
        /**
         * Called when double tapping starts, after double tap gesture
         *
         * @param posX x tap position on the root view
         * @param posY y tap position on the root view
         */
        default void onDoubleTapStarted(float posX, float posY) {
        }

        /**
         * Called for each ongoing tap (also single tap) (MotionEvent#ACTION_DOWN)
         * when double tap started and still in double tap mode defined
         * by {@link UZPlayerView#doubleTapDelay}
         *
         * @param posX x tap position on the root view
         * @param posY y tap position on the root view
         */
        default void onDoubleTapProgressDown(float posX, float posY) {
        }

        /**
         * Called for each ongoing tap (also single tap) (MotionEvent#ACTION_UP}
         * when double tap started and still in double tap mode defined
         * by {@link UZPlayerView#doubleTapDelay}
         *
         * @param posX x tap position on the root view
         * @param posY y tap position on the root view
         */
        default void onDoubleTapProgressUp(float posX, float posY) {
        }

        /**
         * Called when {@link UZPlayerView#doubleTapDelay} is over
         */
        default void onDoubleTapFinished() {
        }
    }

    public interface OnTouchEvent {

        void onSwipeRight();

        void onSwipeLeft();

        void onSwipeBottom();

        void onSwipeTop();
    }

    private class UZGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            if (isDoubleTap && onDoubleTap != null) {
                onDoubleTap.onDoubleTapProgressDown(event.getX(), event.getY());
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isDoubleTap && onDoubleTap != null) {
                onDoubleTap.onDoubleTapProgressUp(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!controllerVisible) {
                showController();
            } else if (getControllerHideOnTouch()) {
                hideController();
            }
            if (onSingleTap != null) {
                onSingleTap.onSingleTapConfirmed(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onLongPressed != null) {
                onLongPressed.onLongPressed(e.getX(), e.getY());
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!isDoubleTap && onDoubleTap != null) {
                isDoubleTap = true;
                keepInDoubleTapMode();
                onDoubleTap.onDoubleTapStarted(e.getX(), e.getY());
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // Second tap (ACTION_UP) of both taps
            Timber.e("onDoubleTapEvent: isDoubleTap = %b, onDoubleTap = %b",isDoubleTap, onDoubleTap != null);
            if (e.getActionMasked() == MotionEvent.ACTION_UP && isDoubleTap && onDoubleTap != null) {
                onDoubleTap.onDoubleTapProgressUp(e.getX(), e.getY());
                return true;
            }
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            if (onTouchEvent != null) {
                                onTouchEvent.onSwipeRight();
                            }
                        } else {
                            if (onTouchEvent != null) {
                                onTouchEvent.onSwipeLeft();
                            }
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            if (onTouchEvent != null) {
                                onTouchEvent.onSwipeBottom();
                            }
                        } else {
                            if (onTouchEvent != null) {
                                onTouchEvent.onSwipeTop();
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                Timber.e(exception);
            }
            return true;
        }
    }
}