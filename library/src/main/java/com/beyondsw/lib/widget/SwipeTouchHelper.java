package com.beyondsw.lib.widget;

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

    //1,速度大于一定值时卡片滑出消失
//    2，滑动距离超过一定值后卡片消失，滑动过程中改变alpha值
//    4，卡片消失后数据刷新
//    5，滑动方向控制
//    6，view缓存

    private static final String TAG = "SwipeTouchHelper";

    private BeyondSwipeCard mSwipeView;
    private float mLastX;
    private float mLastY;
    private int mTouchSlop;
    private boolean mIsBeingDragged;
    private View mTouchChild;
    private float mChildInitX;
    private float mChildInitY;
    private float mAnimStartX;
    private float mAnimStartY;

    private boolean mShouldDisappear;

    private SpringSystem mSpringSystem;
    private Spring mSpring;

    public SwipeTouchHelper(BeyondSwipeCard view) {
        mSwipeView = view;
        final ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        Log.d(TAG, "mTouchSlop=" + mTouchSlop);
        updateFirstChild();

        mSpringSystem = SpringSystem.create();
    }

    private SpringListener mSpringListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            Log.d(TAG, "onSpringUpdate: value=" + value);
            mTouchChild.setX(mAnimStartX - (mAnimStartX - mChildInitX) * value);
            mTouchChild.setY(mAnimStartY - (mAnimStartY - mChildInitY) * value);
            onCoverScrolled();
        }
    };

    private void updateFirstChild(){
        if (mSwipeView.getChildCount() > 0) {
            mTouchChild = mSwipeView.getChildAt(0);
            mChildInitX = mTouchChild.getX();
            mChildInitY = mTouchChild.getY();
        } else {
            mTouchChild = null;
        }
    }

    @Override
    public void onChildAddOrRemove() {
        updateFirstChild();
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

    private boolean canDrag(float dx,float dy){
        //// TODO: 17-2-13
        return true;
    }

    private void performDrag(float dx, float dy) {
        View cover = mSwipeView.getChildAt(0);
        cover.setX(cover.getX() + dx);
        cover.setY(cover.getY() + dy);
        onCoverScrolled();
        Log.d(TAG, "performDrag: dx=" + dx + "dy=" + dy + "left=" + cover.getLeft());
    }

    private void animateToInitPos() {
        if (mTouchChild != null) {
            if (mSpring != null) {
                mSpring.removeAllListeners();
            }
            mAnimStartX = mTouchChild.getX();
            mAnimStartY = mTouchChild.getY();
            mSpring = mSpringSystem.createSpring();
            mSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(40,5));
            mSpring.addListener(mSpringListener);
            mSpring.setEndValue(1);
        }
    }

    private void animateToDisappear(){

    }

    private void onCoverScrolled() {
        if (mTouchChild == null) {
            return;
        }
        float dx = mTouchChild.getX() - mChildInitX;
        float dy = mTouchChild.getY() - mChildInitY;
        if (dx == 0) {
            mTouchChild.setRotation(0);
        } else {
            float maxRotation = mSwipeView.getMaxRotation();
            float rotation = maxRotation * (2 * dx / mTouchChild.getWidth());
            if (rotation > maxRotation) {
                rotation = maxRotation;
            } else if (rotation < -maxRotation) {
                rotation = -maxRotation;
            }
            mTouchChild.setRotation(rotation);
        }
        double distance = Math.sqrt(dx * dx + dy * dy);
        int dismiss_distance = mSwipeView.getDismissDistance();
        if (distance >= dismiss_distance) {
            mSwipeView.onCoverScrolled(1);
            mShouldDisappear = true;
        } else {
            mSwipeView.onCoverScrolled((float) distance / dismiss_distance);
            mShouldDisappear = false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mSwipeView.isSwipeAllowed()) {
            return false;
        }
        if (mTouchChild == null) {
            return false;
        }
        float x = ev.getX();
        float y = ev.getY();
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_DOWN,x=" + ev.getX());
                if (!isTouchOnFirstChild(x, y)) {
                    Log.d(TAG, "onInterceptTouchEvent: !isTouchOnFirstChild");
                    mIsBeingDragged = false;
                    return false;
                }
                requestParentDisallowInterceptTouchEvent(true);
                if (mSpring != null && !mSpring.isAtRest()) {
                    mSpring.removeAllListeners();
                    //mSpring.setAtRest();
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_MOVE");
                float dx = x - mLastX;
                float dy = y - mLastY;
                Log.d(TAG, "onInterceptTouchEvent: move,dx=" + dx + ",dy=" + dy);
                if (canDrag(dx, dy) && Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
                    //touchSlop这段距离忽略移动,防止第一次移动时抖动
                    mLastX = x;
                    mLastY = y;
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_UP");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_CANCEL");
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
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: ACTION_MOVE");
                if (mIsBeingDragged) {
                    float dx = x - mLastX;
                    float dy = y - mLastY;
                    mLastX = x;
                    mLastY = y;
                    performDrag(dx, dy);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: ACTION_UP");
//                if (mShouldDisappear) {
//
//                } else {
//                    animateToInitPos();
//                }
                animateToInitPos();
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "onTouchEvent: ACTION_CANCEL");
                mIsBeingDragged = false;
                break;
        }
        return true;
    }
}
