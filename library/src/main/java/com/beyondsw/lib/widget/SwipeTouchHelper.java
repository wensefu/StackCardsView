package com.beyondsw.lib.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import com.beyondsw.lib.widget.rebound.SimpleSpringListener;
import com.beyondsw.lib.widget.rebound.Spring;
import com.beyondsw.lib.widget.rebound.SpringConfig;
import com.beyondsw.lib.widget.rebound.SpringListener;
import com.beyondsw.lib.widget.rebound.SpringSystem;


/**
 * Created by wensefu on 17-2-12.
 */
public class SwipeTouchHelper implements ISwipeTouchHelper, Handler.Callback {

    //// TODO: 2017/2/14
//    2，消失过程中改变alpha值
//    6，view缓存
    // 7,多点触控处理

    private static final String TAG = "StackCardsView-touch";

    private static final float SLOPE = 1.732f;
    private StackCardsView mSwipeView;
    private float mLastX;
    private float mLastY;
    private float mInitDownX;
    private float mInitDownY;
    private boolean mOnTouchableChild;
    private int mDragSlop;
    private int mMaxFlingVelocity;
    private float mMinFlingVelocity;
    private VelocityTracker mVelocityTracker;
    private boolean mIsBeingDragged;
    private boolean mIsDisappearing;
    private View mTouchChild;
    private float mChildInitX;
    private float mChildInitY;
    private float mChildInitRotation;
    private float mAnimStartX;
    private float mAnimStartY;
    private float mAnimStartRotation;
    private SpringSystem mSpringSystem;
    private Spring mSpring;
    private OverScroller mScroller;

    private static final int MIN_FLING_VELOCITY = 400;

    private Handler mHandler;
    private static final int MSG_DO_DISAPPEAR_SCROLL = 1;

    public SwipeTouchHelper(StackCardsView view) {
        mSwipeView = view;
        final Context context = view.getContext();
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mDragSlop = (int) (configuration.getScaledTouchSlop() / mSwipeView.getDragSensitivity());
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        float density = context.getResources().getDisplayMetrics().density;
        mMinFlingVelocity = (int) (MIN_FLING_VELOCITY * density);
        if (mSwipeView.getChildCount() > 0) {
            updateCoverInfo(mSwipeView.getChildAt(0));
        }
        mSpringSystem = SpringSystem.create();
        mScroller = new OverScroller(context, new DecelerateInterpolator());
        mHandler = new Handler(this);
    }


    private SpringListener mSpringListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            mTouchChild.setX(mAnimStartX - (mAnimStartX - mChildInitX) * value);
            mTouchChild.setY(mAnimStartY - (mAnimStartY - mChildInitY) * value);
            mTouchChild.setRotation(mAnimStartRotation - (mAnimStartRotation - mChildInitRotation) * value);
            onCoverScrolled();
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            super.onSpringAtRest(spring);
            mSwipeView.onCoverStatusChanged(isCoverIdle());
        }
    };

    private void updateCoverInfo(View cover) {
        mTouchChild = cover;
        if (mTouchChild != null) {
            mChildInitX = mTouchChild.getX();
            mChildInitY = mTouchChild.getY();
            mChildInitRotation = mTouchChild.getRotation();
        }
    }

    @Override
    public boolean isCoverIdle() {
        if (mTouchChild == null) {
            return true;
        }
        boolean springIdle = mSpring == null || mSpring.isAtRest();
        return springIdle && !mIsBeingDragged && !mIsDisappearing;
    }

    @Override
    public void onCoverChanged(View cover) {
        updateCoverInfo(cover);
        if (StackCardsView.DEBUG) {
            mSwipeView.invalidate();
        }
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = mSwipeView.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean isOnTouchableChild(float x, float y) {
        if (mTouchChild == null) {
            return false;
        }
        return x >= mTouchChild.getLeft() && x <= mTouchChild.getRight() && y >= mTouchChild.getTop() && y <= mTouchChild.getBottom();
    }

    private boolean isDirectionAllowDismiss() {
        int direction = mSwipeView.getDismissDirection();
        if (direction == StackCardsView.SWIPE_ALL) {
            return true;
        } else if (direction == 0) {
            return false;
        }
        float dx = mTouchChild.getX() - mChildInitX;
        float dy = mTouchChild.getY() - mChildInitY;
        //斜率小于SLOPE时，认为是水平滑动
        if (Math.abs(dx) * SLOPE > Math.abs(dy)) {
            if (dx > 0) {
                return (direction & StackCardsView.SWIPE_RIGHT) != 0;
            } else {
                return (direction & StackCardsView.SWIPE_LEFT) != 0;
            }
        } else {
            if (dy > 0) {
                return (direction & StackCardsView.SWIPE_DOWN) != 0;
            } else {
                return (direction & StackCardsView.SWIPE_UP) != 0;
            }
        }
    }

    private boolean isDistanceAllowDismiss() {
        if (mTouchChild == null) {
            return false;
        }
        float dx = mTouchChild.getX() - mChildInitX;
        float dy = mTouchChild.getY() - mChildInitY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        int dismiss_distance = mSwipeView.getDismissDistance();
        return distance >= dismiss_distance;
    }

    private boolean isVDirectionAllowDismiss(float vx, float vy) {
        int direction = mSwipeView.getDismissDirection();
        if (direction == StackCardsView.SWIPE_ALL) {
            return true;
        } else if (direction == 0) {
            return false;
        }
        //斜率小于SLOPE时，认为是水平滑动
        if (Math.abs(vx) * SLOPE > Math.abs(vy)) {
            if (vy > 0) {
                return (direction & StackCardsView.SWIPE_RIGHT) != 0;
            } else {
                return (direction & StackCardsView.SWIPE_LEFT) != 0;
            }
        } else {
            if (vy > 0) {
                return (direction & StackCardsView.SWIPE_DOWN) != 0;
            } else {
                return (direction & StackCardsView.SWIPE_UP) != 0;
            }
        }
    }

    private boolean canDrag(float dx, float dy) {
        int direction = mSwipeView.getSwipeDirection();
        if (direction == StackCardsView.SWIPE_ALL) {
            return true;
        } else if (direction == 0) {
            return false;
        }
        //斜率小于SLOPE时，认为是水平滑动
        if (Math.abs(dx) * SLOPE > Math.abs(dy)) {
            if (dx > 0) {
                return (direction & StackCardsView.SWIPE_RIGHT) != 0;
            } else {
                return (direction & StackCardsView.SWIPE_LEFT) != 0;
            }
        } else {
            if (dy > 0) {
                return (direction & StackCardsView.SWIPE_DOWN) != 0;
            } else {
                return (direction & StackCardsView.SWIPE_UP) != 0;
            }
        }
    }

    private void performDrag(float dx, float dy) {
        if (mTouchChild == null) {
            return;
        }
        mTouchChild.setX(mTouchChild.getX() + dx);
        mTouchChild.setY(mTouchChild.getY() + dy);
        final float maxRotation = mSwipeView.getMaxRotation();
        float rotation = maxRotation * (mTouchChild.getX() - mChildInitX) / mSwipeView.getDismissDistance();
        if (rotation > maxRotation) {
            rotation = maxRotation;
        } else if (rotation < -maxRotation) {
            rotation = -maxRotation;
        }
        mSwipeView.getMaxRotation();
        mTouchChild.setRotation(rotation);
        onCoverScrolled();
    }

    private void animateToInitPos() {
        if (mTouchChild != null) {
            if (mSpring != null) {
                mSpring.removeAllListeners();
            }
            mAnimStartX = mTouchChild.getX();
            mAnimStartY = mTouchChild.getY();
            mAnimStartRotation = mTouchChild.getRotation();
            mSpring = mSpringSystem.createSpring();
            mSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(40, 5));
            mSpring.addListener(mSpringListener);
            mSpring.setEndValue(1);
            mSwipeView.onCoverStatusChanged(false);
        }
    }

    private void animateToDisappear() {
        if (mTouchChild == null) {
            return;
        }
        mIsDisappearing = true;
        mIsBeingDragged = false;
        float dx = mTouchChild.getX() - mChildInitX;
        float dy = mTouchChild.getY() - mChildInitY;
        String property;
        float target;
        int dir;
        float extraX = 0;
        float extraY = 0;
        if (mTouchChild.getRotation() > 0) {
            float[] extra = calcExtraSize();
            extraX = extra[0];
            extraY = extra[1];
        }
        if (Math.abs(dx) * SLOPE > Math.abs(dy)) {
            property = "x";
            if (dx > 0) {
                target = mSwipeView.getWidth() + extraX;
                dir = StackCardsView.SWIPE_RIGHT;
            } else {
                target = -mTouchChild.getWidth() - extraX;
                dir = StackCardsView.SWIPE_LEFT;
            }
        } else {
            property = "y";
            if (dy > 0) {
                target = mSwipeView.getHeight() + extraY;
                dir = StackCardsView.SWIPE_DOWN;
            } else {
                target = -mTouchChild.getHeight() - extraY;
                dir = StackCardsView.SWIPE_UP;
            }
        }
        final int direction = dir;
        ObjectAnimator animator = ObjectAnimator.ofFloat(mTouchChild, property, target).setDuration(4000);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSwipeView.onCardDismissed(direction);
                mIsDisappearing = false;
                mSwipeView.onCoverStatusChanged(isCoverIdle());
                mSwipeView.adjustChildren();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mSwipeView.onCoverStatusChanged(false);
            }
        });
        animator.start();
    }

    private void onCoverScrolled() {
        if (mTouchChild == null) {
            return;
        }
        float dx = mTouchChild.getX() - mChildInitX;
        float dy = mTouchChild.getY() - mChildInitY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        int dismiss_distance = mSwipeView.getDismissDistance();
        if (distance >= dismiss_distance) {
            mSwipeView.onCoverScrolled(1);
        } else {
            mSwipeView.onCoverScrolled((float) distance / dismiss_distance);
        }
    }

    private void cancelSpringIfNeeded() {
        if (mSpring != null && !mSpring.isAtRest()) {
            mSpring.setAtRest();
            mSpring.removeAllListeners();
        }
    }

    /**
     * 这里取由于角度变换导致的超过view原本矩形范围的最大size
     *
     * @return
     */
    private float[] calcExtraSize() {
        float[] result = new float[2];
        int childWidth = mTouchChild.getWidth();
        int childHeight = mTouchChild.getHeight();

        double d1 = Math.sqrt(childWidth * childWidth + childHeight * childHeight);
        result[0] = (float) (d1 - childWidth) / 2;
        result[1] = (float) (d1 - childHeight) / 2;
        return result;
    }

    private int[] calcScrollDistance(float vx, float vy, float dx, float dy) {
        int[] result = new int[2];
        float edgeDeltaX = 0;
        float edgeDeltaY = 0;
        if (vx > 0) {
            edgeDeltaX = mSwipeView.getWidth() - mTouchChild.getX();
        } else if (vx < 0) {
            edgeDeltaX = mTouchChild.getX() + mTouchChild.getWidth();
        }
        if (vy > 0) {
            edgeDeltaY = mSwipeView.getHeight() - mTouchChild.getY();
        } else if (vy < 0) {
            edgeDeltaY = mTouchChild.getHeight() + mTouchChild.getY();
        }
        float scrollDx;
        float scrollDy;
        float extraX = 0;
        float extraY = 0;
        if (mTouchChild.getRotation() != 0) {
            float[] extra = calcExtraSize();
            extraX = extra[0];
            extraY = extra[1];
        }
        if (edgeDeltaX * Math.abs(dy) >= edgeDeltaY * Math.abs(dx)) {
            scrollDy = vy > 0 ? (edgeDeltaY + extraY) : (-edgeDeltaY - extraY);
            float value = Math.abs(scrollDy * dx / dy);
            scrollDx = vx > 0 ? (value + extraX) : (-value - extraX);
        } else {
            scrollDx = vx > 0 ? (edgeDeltaX + extraX) : (-edgeDeltaX - extraX);
            float value = Math.abs(scrollDx * dy / dx);
            scrollDy = vy > 0 ? (value + extraY) : (-value - extraY);
        }
        result[0] = (int) scrollDx;
        result[1] = (int) scrollDy;
        return result;
    }

    private boolean doFling(float vx, float vy) {
        if (mTouchChild == null) {
            return false;
        }
        if (Math.abs(vx) < mMinFlingVelocity && Math.abs(vy) < mMinFlingVelocity) {
            return false;
        }
        mIsDisappearing = true;
        float dx = mTouchChild.getX() - mChildInitX;
        float dy = mTouchChild.getY() - mChildInitY;
        int[] fdxArray = calcScrollDistance(vx, vy, dx, dy);
        mScroller.startScroll((int) mTouchChild.getX(), (int) mTouchChild.getY(), fdxArray[0], fdxArray[1],2000);
        mHandler.obtainMessage(MSG_DO_DISAPPEAR_SCROLL).sendToTarget();
        return true;
    }

    private void clearVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_DO_DISAPPEAR_SCROLL) {
            if (mScroller.computeScrollOffset()) {
                mTouchChild.setX(mScroller.getCurrX());
                mTouchChild.setY(mScroller.getCurrY());
                onCoverScrolled();
                Message m = mHandler.obtainMessage(MSG_DO_DISAPPEAR_SCROLL);
                mHandler.sendMessageDelayed(m, 15);
            } else {
                mSwipeView.onCardDismissed(0); //// TODO: 17-2-23
                mIsDisappearing = false;
                mSwipeView.onCoverStatusChanged(isCoverIdle());
                mSwipeView.adjustChildren();
            }
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mTouchChild == null) {
            logw(TAG, "onInterceptTouchEvent,mTouchChild == null");
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        log(TAG, "onInterceptTouchEvent action=" + action);
        if (action == MotionEvent.ACTION_DOWN) {
            clearVelocityTracker();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            return false;
        }
        if (mIsBeingDragged && action != MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (mIsDisappearing) {
            return false;
        }
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mOnTouchableChild = isOnTouchableChild(x, y);
                if (!mOnTouchableChild) {
                    return false;
                }
                requestParentDisallowInterceptTouchEvent(true);
                mInitDownX = mLastX = x;
                mInitDownY = mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x - mInitDownX;
                float dy = y - mInitDownY;
                mLastX = x;
                mLastY = y;
                if (Math.sqrt(dx * dx + dy * dy) > mDragSlop) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        log(TAG, "onTouchEvent action=" + action + ",mOnTouchableChild=" + mOnTouchableChild);
        float x = ev.getX();
        float y = ev.getY();
        mVelocityTracker.addMovement(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mOnTouchableChild) {
                    return false;
                }
                if (mTouchChild == null) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //子view未消费down事件时，mIsBeingDragged可能为false
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (!canDrag(dx, dy)) {
                    return false;
                }
                if (!mIsBeingDragged) {
                    cancelSpringIfNeeded();
                    mIsBeingDragged = true;
                }
                performDrag(x - mLastX, y - mLastY);
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mIsBeingDragged) {
                    break;
                }
                if (isDistanceAllowDismiss()) {
                    if (isDirectionAllowDismiss()) {
                        animateToDisappear();
                    } else {
                        animateToInitPos();
                    }
                } else {
                    if (mSwipeView.isFastSwipeAllowed()) {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                        float xv = mVelocityTracker.getXVelocity();
                        float yv = mVelocityTracker.getYVelocity();
                        if (!isVDirectionAllowDismiss(xv, yv) || !doFling(xv, yv)) {
                            animateToInitPos();
                        }
                    } else {
                        animateToInitPos();
                    }
                }
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        return true;
    }

    private static void log(String tag, String msg) {
        if (StackCardsView.DEBUG) {
            Log.d(tag, msg);
        }
    }

    private static void logw(String tag, String msg) {
        if (StackCardsView.DEBUG) {
            Log.w(tag, msg);
        }
    }
}
