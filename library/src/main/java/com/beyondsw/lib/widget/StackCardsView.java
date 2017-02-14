package com.beyondsw.lib.widget;

import android.content.Context;
import android.database.Observable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wensefu on 2017/2/10.
 */

public class StackCardsView extends ViewGroup {

    private static final String TAG = "StackCardsView";

    /**
     * 禁止滑动
     */
    public static final int SWIPE_NONE = 0;

    /**
     * 支持左滑
     */
    public static final int SWIPE_LEFT = 1;

    /**
     * 支持右滑
     */
    public static final int SWIPE_RIGHT = 1 << 1;

    /**
     * 支持上滑
     */
    public static final int SWIPE_TOP = 1 << 2;

    /**
     * 支持下滑
     */
    public static final int SWIPE_BOTTOM = 1 << 3;

    /**
     * 允许任意方向滑动
     */
    public static final int SWIPE_ALL = SWIPE_LEFT | SWIPE_RIGHT | SWIPE_TOP | SWIPE_BOTTOM;

    private Adapter mAdapter;

    /**
     * 静止时最多可以看到的卡片数
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

    private InnerDataObserver mDataObserver;
    private boolean mHasRegisteredObserver;

    private static final float SCALE_FACTOR = 0.8f;
    private float mScaleFactor = SCALE_FACTOR;

    private static final float ALPHA_FACTOR = 0.6f;
    private float mAlphaFactor = ALPHA_FACTOR;

    private static final int SWIPE_TO_DISMISS_DISTINCE = 500;
    private int mDismissDistance = SWIPE_TO_DISMISS_DISTINCE;

    //卡片消失时的透明度
    private static final float DISMISS_ALPHA = 0.3f;
    private float mDismissAlpha = DISMISS_ALPHA;

    //滑动时的最大旋转角度
    private float mMaxRotation = 6;

    private float[] mScaleArray;
    private float[] mAlphaArray;
    private float[] mTranslationYArray;

    private boolean mSwipeAllowed = true;
    private ISwipeTouchHelper mTouchHelper;

    private List<OnCardSwipedListener> mCardSwipedListenrs;

    public StackCardsView(Context context) {
        this(context, null);
    }

    public StackCardsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);
    }

    public interface OnCardSwipedListener {

        void onCardDismiss();
    }

    public void addOnCardSwipedListener(OnCardSwipedListener listener) {
        if (mCardSwipedListenrs == null) {
            mCardSwipedListenrs = new ArrayList<>();
            mCardSwipedListenrs.add(listener);
        } else if (!mCardSwipedListenrs.contains(listener)) {
            mCardSwipedListenrs.add(listener);
        }
    }

    public void removeOnCardSwipedListener(OnCardSwipedListener listener) {
        if (mCardSwipedListenrs != null && mCardSwipedListenrs.contains(listener)) {
            mCardSwipedListenrs.remove(listener);
        }
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

    /**
     * 设置可以滑动的方向<br/>
     *
     * @param direction
     * @see #SWIPE_ALL
     * @see #SWIPE_LEFT
     * @see #SWIPE_RIGHT
     * @see #SWIPE_TOP
     * @see #SWIPE_BOTTOM
     * @see #SWIPE_NONE
     */
    public void setSwipeDirection(int direction) {

    }

    public void setSwipeAllowed(boolean allowed) {
        if (mSwipeAllowed != allowed) {
            mSwipeAllowed = allowed;
        }
    }

    float getMaxRotation() {
        return mMaxRotation;
    }

    public boolean isSwipeAllowed() {
        return mSwipeAllowed;
    }

    public int getDismissDistance() {
        return mDismissDistance;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout,changed=" + changed + ",l=" + l + ",t=" + t + ",r=" + r + ",b=" + b);
        final int cnt = getChildCount();
        if (cnt == 0) {
            return;
        }
        final int centerX = (r - l - getPaddingLeft() - getPaddingRight()) / 2;
        final int centerY = (b - t - getPaddingBottom() - getPaddingTop()) / 2;
        int layerIndex = 0;
        mScaleArray = new float[cnt];
        mAlphaArray = new float[cnt];
        mTranslationYArray = new float[cnt];
        mScaleArray[0] = 1;
        mAlphaArray[0] = 1;
        mTranslationYArray[0] = 0;
        for (int i = 0; i < cnt; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int half_childWidth = child.getMeasuredWidth() / 2;
                int half_childHeight = child.getMeasuredHeight() / 2;
                int cl = centerX - half_childWidth;
                int ct = centerY - half_childHeight;
                int cr = centerX + half_childWidth;
                int cb = centerY + half_childHeight;
                Log.d(TAG, "onLayout: cl=" + cl + ",ct=" + ct + ",cr=" + cr + ",cb=" + cb + ",half_childWidth=" + half_childWidth + ",half_childHeight=" + half_childHeight
                        + ",centerX=" + centerX + ",centerY=" + centerY+",="+child.isLayoutRequested());
//                if (!child.isLayoutRequested()) {
//                    continue;
//                }
                child.layout(centerX - half_childWidth, centerY - half_childHeight, centerX + half_childWidth, centerY + half_childHeight);

                if (layerIndex > 0) {
                    float scale = 1 - layerIndex * (1 - mScaleFactor);
                    mScaleArray[i] = scale;
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                    float alpha = 1 - layerIndex * (1 - mAlphaFactor);
                    mAlphaArray[i] = alpha;
                    child.setAlpha(alpha);
                    float translationY = half_childHeight * (1 - scale) + mLayerEdgeHeight * layerIndex;
                    mTranslationYArray[i] = translationY;
                    child.setTranslationY(translationY);
                }
                if (layerIndex < mMaxVisibleCnt - 1) {
                    layerIndex++;
                }
            }
            if (mTouchHelper != null) {
                mTouchHelper.onChildLayouted();
            }
        }
    }

    void onCardDismissed() {
        if (mCardSwipedListenrs != null) {
            for (OnCardSwipedListener listener : mCardSwipedListenrs) {
                listener.onCardDismiss();
            }
        }
    }

    void onCoverScrolled(float progress) {
        final int cnt = getChildCount();
        float preScale;
        float preAlpha;
        float preTranslationY;
        float targetScale;
        float targetAlpha;
        float targetTranslationY;
        float progressScale;
        for (int i = 1; i < cnt; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                preScale = mScaleArray[i];
                preAlpha = mAlphaArray[i];
                preTranslationY = mTranslationYArray[i];
                targetScale = mScaleArray[i - 1];
                targetAlpha = mAlphaArray[i - 1];
                targetTranslationY = mTranslationYArray[i - 1];
                progressScale = preScale + (targetScale - preScale) * progress;
                child.setScaleX(progressScale);
                child.setScaleY(progressScale);
                child.setAlpha(preAlpha + (targetAlpha - preAlpha) * progress);
                child.setTranslationY(preTranslationY + (targetTranslationY - preTranslationY) * progress);
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
            mAdapter.unregisterDataObserver(mDataObserver);
            mHasRegisteredObserver = false;
        }
    }

    private void safeRegisterObserver() {
        safeUnRegisterObserver();
        if (mDataObserver == null) {
            mDataObserver = new InnerDataObserver();
        }
        mAdapter.registerDataObserver(mDataObserver);
        mHasRegisteredObserver = true;
    }

    private LayoutParams getDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    private void initChildren() {
        int cnt = mAdapter == null ? 0 : mAdapter.getCount();
        if (cnt == 0) {
            removeAllViewsInLayout();
        } else {
            removeAllViewsInLayout();
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

    private class InnerDataObserver extends CardDataObserver {

        @Override
        public void onDataSetChanged() {
            super.onDataSetChanged();
            initChildren();
        }

        @Override
        public void onItemInserted(int position) {
            super.onItemInserted(position);

        }

        @Override
        public void onItemRemoved(int position) {
            super.onItemRemoved(position);
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

    public static abstract class Adapter {

        private final CardDataObservable mObservable = new CardDataObservable();

        public void registerDataObserver(CardDataObserver observer) {
            mObservable.registerObserver(observer);
        }

        public void unregisterDataObserver(CardDataObserver observer) {
            mObservable.unregisterObserver(observer);
        }

        public abstract int getCount();

        public abstract View getView(int position, View convertView, ViewGroup parent);

        public final void notifyDataSetChanged() {
            mObservable.notifyDataSetChanged();
        }

        public final void notifyItemInserted(int position) {
            mObservable.notifyItemInserted(position);
        }

        public final void notifyItemRemoved(int position) {
            mObservable.notifyItemRemoved(position);
        }
    }

    public static abstract class CardDataObserver {

        public void onDataSetChanged() {

        }

        public void onItemInserted(int position) {

        }

        public void onItemRemoved(int position) {

        }
    }

    static class CardDataObservable extends Observable<CardDataObserver> {

        public void notifyDataSetChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onDataSetChanged();
            }
        }

        public void notifyItemInserted(int position) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemInserted(position);
            }
        }

        public void notifyItemRemoved(int position) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRemoved(position);
            }
        }
    }
}
