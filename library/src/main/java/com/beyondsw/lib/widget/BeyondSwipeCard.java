package com.beyondsw.lib.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;


/**
 * Created by wensefu on 2017/2/10.
 */

public class BeyondSwipeCard extends ViewGroup {

    private static final String TAG = "BeyondSwipeCard";

    private Adapter mAdapter;

    /**
     * 静止时最多可以看到的卡片數
     */
    private int mMaxVisibleCnt = 3;

    /**
     * 同时最多add到控件的子view数
     */
    private int mLayerCnt = 4;

    /**
     * 层叠效果高度
     */
    private int mLayerEdgeHeight = 24;

    private SwipeDataObserver mDataObserver;
    private boolean mHasRegisteredObserver;

    private static final float SCALE_FACTOR = 0.8f;
    private float mScaleFactor = SCALE_FACTOR;

    private static final float ALPHA_FACTOR = 0.6f;
    private float mAlphaFactor = ALPHA_FACTOR;

    private boolean mSwipeAllowed = true;
    private ISwipeTouchHelper mTouchHelper;

    public BeyondSwipeCard(Context context) {
        this(context, null);
    }

    public BeyondSwipeCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    public void addView(View child) {
        throw new UnsupportedOperationException("addView(View) is not supported");
    }

    @Override
    public void addView(View child, int index) {
        throw new UnsupportedOperationException("addView(View, int) is not supported");
    }

    @Override
    public void addView(View child, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, LayoutParams) "
                + "is not supported");
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, int, LayoutParams) "
                + "is not supported");
    }

    @Override
    public void removeView(View child) {
        throw new UnsupportedOperationException("removeView(View) is not supported");
    }

    @Override
    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported");
    }

    @Override
    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported");
    }

    public void setSwipeAllowed(boolean allowed) {
        if (mSwipeAllowed != allowed) {
            mSwipeAllowed = allowed;
        }
    }

    public boolean isSwipeAllowed() {
        return mSwipeAllowed;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout");
        final int cnt = getChildCount();
        if (cnt == 0) {
            return;
        }
        final int centerX = (r - l - getPaddingLeft() - getPaddingRight()) / 2;
        final int centerY = (b - t - getPaddingBottom() - getPaddingTop()) / 2;
        int layerIndex = 0;
        for (int i = 0; i < cnt; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int half_childWidth = child.getMeasuredWidth() / 2;
                int half_childHeight = child.getMeasuredHeight() / 2;
                child.layout(centerX - half_childWidth, centerY - half_childHeight, centerX + half_childWidth, centerY + half_childHeight);

                if (layerIndex > 0) {
                    float scale = 1 - layerIndex * (1 - mScaleFactor);
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                    float alpha = 1 - layerIndex * (1 - mAlphaFactor);
                    child.setAlpha(alpha);
                    child.offsetTopAndBottom(Math.round(half_childHeight * (1 - scale) + mLayerEdgeHeight * layerIndex));
                }
                if (layerIndex < mMaxVisibleCnt - 1) {
                    layerIndex++;
                }
            }
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return childCount - 1 - i;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.measure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        safeRegisterObserver();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        safeUnRegisterObserver();
    }

    private void safeUnRegisterObserver() {
        if (mAdapter != null && mDataObserver != null && mHasRegisteredObserver) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
            mHasRegisteredObserver = false;
        }
    }

    private void safeRegisterObserver() {
        safeUnRegisterObserver();
        if (mDataObserver == null) {
            mDataObserver = new SwipeDataObserver();
        }
        mAdapter.registerDataSetObserver(mDataObserver);
        mHasRegisteredObserver = true;
    }

    private LayoutParams getDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    private void initChildren() {
        int cnt = mAdapter == null ? 0 : mAdapter.getCount();
        if (cnt == 0) {
            removeAllViews();
        } else {
            cnt = Math.min(cnt, mMaxVisibleCnt + 1);
            for (int i = 0; i < cnt; i++) {
                addViewInLayout(mAdapter.getView(i, null, this), -1, getDefaultLayoutParams(), false);
            }
            requestLayout();
        }
    }

    public void setAdapter(Adapter adapter) {
        safeUnRegisterObserver();
        mAdapter = adapter;
        safeRegisterObserver();
        initChildren();
    }

    private class SwipeDataObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            super.onChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        Log.d("SwipeTouchHelper", "dispatchTouchEvent: action=" + action);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mTouchHelper == null) {
            mTouchHelper = new SwipeTouchHelper(this);
        }
        return mTouchHelper.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mTouchHelper.onTouchEvent(ev);
    }
}
