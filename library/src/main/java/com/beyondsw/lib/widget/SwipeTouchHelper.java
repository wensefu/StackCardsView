package com.beyondsw.lib.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import com.beyondsw.lib.widget.rebound.SimpleSpringListener;
import com.beyondsw.lib.widget.rebound.Spring;
import com.beyondsw.lib.widget.rebound.SpringConfig;
import com.beyondsw.lib.widget.rebound.SpringListener;
import com.beyondsw.lib.widget.rebound.SpringSystem;


/**
 * Created by wensefu on 17-2-12.
 */
public class SwipeTouchHelper implements ISwipeTouchHelper {

    //// TODO: 2017/2/14
    //1,速度大于一定值时卡片滑出消失
//    2，滑动距离超过一定值后卡片消失，消失过程中改变alpha值
//    4，卡片消失后数据刷新
//    6，view缓存
    // 7,多点触控处理

    private static final String TAG = "StackCardsView-touch";

    private static final float SLOPE = 1.732f;
    private StackCardsView mSwipeView;
    private float mInitDownX;
    private float mInitDownY;
    private float mLastX;
    private float mLastY;
    private int mTouchSlop;
    private boolean mIsBeingDragged;
    private boolean mIsDisappearing;
    private View mTouchChild;
    private float mChildInitX;
    private float mChildInitY;
    private float mChildInitRotation;
    private float mAnimStartX;
    private float mAnimStartY;
    private float mAnimStartRotation;

    private boolean mShouldDisappear;

    private SpringSystem mSpringSystem;
    private Spring mSpring;

    public SwipeTouchHelper(StackCardsView view) {
        mSwipeView = view;
        final ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        updateCoverInfo(null);
        mSpringSystem = SpringSystem.create();
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
            Log.d(TAG, "onSpringAtRest: ");
            mSwipeView.onCoverStatusChanged(isCoverIdle());
        }
    };

    private void updateCoverInfo(View cover) {
        if (cover == null) {
            if (mSwipeView.getChildCount() > 0) {
                cover = mSwipeView.getChildAt(0);
            }
        }
        mTouchChild = cover;
        if (mTouchChild != null) {
            mChildInitX = mTouchChild.getX();
            mChildInitY = mTouchChild.getY();
            mChildInitRotation = mTouchChild.getRotation();
            Log.d(TAG, "updateCoverInfo: mChildInitX=" + mChildInitX + ",mChildInitY=" + mChildInitY);
        }
    }

    @Override
    public boolean isCoverIdle() {
        if (mTouchChild == null) {
            return true;
        }
        boolean springIdle = mSpring == null || mSpring.isAtRest();
        return springIdle && !mIsBeingDragged;
    }

    @Override
    public void onCoverChanged(View cover) {
        updateCoverInfo(cover);
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = mSwipeView.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean isTouchOnFirstChild(float x, float y) {
        if (mTouchChild == null) {
            return false;
        }
        return x >= mTouchChild.getLeft() && x <= mTouchChild.getRight() && y >= mTouchChild.getTop() && y <= mTouchChild.getBottom();
    }

    private boolean canDrag(float dx, float dy) {
        Log.d(TAG, "canDrag: dx=" + dx + ",dy=" + dy);
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
        View cover = mSwipeView.getChildAt(0);
        cover.setX(cover.getX() + dx);
        cover.setY(cover.getY() + dy);
        float rotation = dx / (mTouchSlop * 2) + cover.getRotation();
        final float maxRotation = mSwipeView.getMaxRotation();
        if (rotation > maxRotation) {
            rotation = maxRotation;
        } else if (rotation < -maxRotation) {
            rotation = -maxRotation;
        }
        cover.setRotation(rotation);
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
        Rect rect = new Rect();
        mTouchChild.getGlobalVisibleRect(rect);
        float targetX;
        if (rect.left > 0) {
            targetX = mTouchChild.getX() + rect.width();
        } else {
            targetX = mTouchChild.getX() - rect.width();
        }
        final int direction = targetX > 0 ? StackCardsView.SWIPE_RIGHT : StackCardsView.SWIPE_LEFT;
        ObjectAnimator animator = ObjectAnimator.ofFloat(mTouchChild, "x", targetX).setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSwipeView.onCardDismissed(direction);
                mSwipeView.onCoverStatusChanged(isCoverIdle());
                mIsDisappearing = false;
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
            if (Math.abs(dx) * SLOPE > Math.abs(dy)) {
                mShouldDisappear = true;
            }
        } else {
            mSwipeView.onCoverScrolled((float) distance / dismiss_distance);
            mShouldDisappear = false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mTouchChild == null) {
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            Log.d(TAG, "onInterceptTouchEvent: action=" + action + ",reset touch");
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
                Log.d(TAG, "onInterceptTouchEvent: ACTION_DOWN,x=" + x);
                if (!isTouchOnFirstChild(x, y)) {
                    Log.d(TAG, "onInterceptTouchEvent: !isTouchOnFirstChild");
                    return false;
                }
                requestParentDisallowInterceptTouchEvent(true);
                mInitDownX = x;
                mInitDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_MOVE");
                float dx = x - mInitDownX;
                float dy = y - mInitDownY;
                if (Math.sqrt(dx * dx + dy * dy) > mTouchSlop && canDrag(dx, dy)) {
                    Log.d(TAG, "onInterceptTouchEvent: mIsBeingDragged = true");
                    mIsBeingDragged = true;
                    if (mSpring != null && !mSpring.isAtRest()) {
                        mSpring.setAtRest();
                        mSpring.removeAllListeners();
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_POINTER_UP");
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        float x = ev.getX();
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                if (mSwipeView.getSwipeDirection() == 0) {
                    return false;
                }
                mIsBeingDragged = true;
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: ACTION_MOVE,mIsBeingDragged=" + mIsBeingDragged);
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (mIsBeingDragged && !mIsDisappearing) {
                    performDrag(dx, dy);
                } else {
                    Log.e(TAG, "onTouchEvent: ACTION_MOVE,mIsBeingDragged=false");
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: ACTION_UP");
                if (mShouldDisappear) {
                    animateToDisappear();
                } else {
                    animateToInitPos();
                }
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_UP");
                break;
        }
        return true;
    }
}
