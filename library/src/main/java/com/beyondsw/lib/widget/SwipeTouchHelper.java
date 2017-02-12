package com.beyondsw.lib.widget;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

/**
 * Created by wensefu on 17-2-12.
 */
public class SwipeTouchHelper implements ISwipeTouchHelper {

    private static final String TAG = "SwipeTouchHelper";

    private BeyondSwipeCard mSwipeView;
    private float mLastX;
    private float mLastY;
    private int mTouchSlop;
    private boolean mIsBeingDragged;
    private View mTouchableChild;
    private float mCoverInitX;
    private float mCoverInitY;

    public SwipeTouchHelper(BeyondSwipeCard view) {
        mSwipeView = view;
        final ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
        mTouchSlop = configuration.getScaledPagingTouchSlop();
        Log.d(TAG, "mTouchSlop=" + mTouchSlop);
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = mSwipeView.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean isTouchOnFirstChild(float x, float y) {
        View first = mSwipeView.getChildAt(0);
        return x >= first.getLeft() && x <= first.getRight() && y >= first.getTop() && y <= first.getBottom();
    }

    private boolean canDrag(float dx,float dy){
        //// TODO: 17-2-13
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mSwipeView.isSwipeAllowed()) {
            return false;
        }
        if (mSwipeView.getChildCount() == 0) {
            return false;
        }
        float x = ev.getX();
        float y = ev.getY();
        if (!isTouchOnFirstChild(x, y)) {
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_DOWN,x=" + ev.getX());
                requestParentDisallowInterceptTouchEvent(true);
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_MOVE");
                float dx = x - mLastX;
                float dy = y - mLastY;
                Log.d(TAG, "onInterceptTouchEvent: move,dx=" + dx + ",dy=" + dy);
                if (canDrag(dx, dy) && Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
                    performDrag(dx,dy);
                    mIsBeingDragged = true;
                }
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

    private void performDrag(float dx, float dy) {
        View cover = mSwipeView.getChildAt(0);
        cover.setX(cover.getX() + dx);
        cover.setY(cover.getY() + dy);
        Log.d(TAG, "performDrag: dx=" + dx + "dy=" + dy + "left=" + cover.getLeft());
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
                float dx = x - mLastX;
                float dy = y - mLastY;
                mLastX = x;
                mLastY = y;
                performDrag(dx,dy);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: ACTION_UP");
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
