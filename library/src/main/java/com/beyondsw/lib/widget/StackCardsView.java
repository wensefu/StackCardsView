package com.beyondsw.lib.widget;

import android.content.Context;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wensefu on 2017/2/10.
 */

public class StackCardsView extends FrameLayout {

    private static final String TAG = "StackCardsView";

    public static boolean DEBUG = true;

    /**
     * 左滑
     */
    public static final int SWIPE_LEFT = 1;

    /**
     * 右滑
     */
    public static final int SWIPE_RIGHT = 1 << 1;

    /**
     * 上滑
     */
    public static final int SWIPE_UP = 1 << 2;

    /**
     * 下滑
     */
    public static final int SWIPE_DOWN = 1 << 3;

    /**
     * 任意方向滑动
     */
    public static final int SWIPE_ALL = SWIPE_LEFT | SWIPE_RIGHT | SWIPE_UP | SWIPE_DOWN;

    /**
     * 禁止滑动
     */
    public static final int SWIPE_NONE = 0;

    private Adapter mAdapter;

    /**
     * 静止时最多可以看到的卡片数
     */
    private int mMaxVisibleCnt = 3;

    /**
     * 同时最多add到控件的子view数
     */
    private int mMaxLayerCnt = 4;

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

    private static final float SWIPE_TO_DISMISS_FACTOR = .3f;
    private float mDismissFactor = SWIPE_TO_DISMISS_FACTOR;
    private float mDismissDistance;

    //卡片消失时的透明度
    private static final float DISMISS_ALPHA = 0.3f;
    private float mDismissAlpha = DISMISS_ALPHA;
    private static final float DRAG_SENSITIVITY = 1f;
    private float mDragSensitivity = DRAG_SENSITIVITY;
    private ISwipeTouchHelper mTouchHelper;
    private List<OnCardSwipedListener> mCardSwipedListenrs;

    private boolean mNeedAdjustChildren;

    private Runnable mPendingTask;

    private float[] mScaleArray;
    private float[] mAlphaArray;
    private float[] mTranslationYArray;

    private int mLastLeft;
    private int mLastTop;
    private int mLastRight;
    private int mLastBottom;

    Paint paint;

    public StackCardsView(Context context) {
        this(context, null);
    }

    public StackCardsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);

        if (DEBUG) {
            paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
        }
    }

    public interface OnCardSwipedListener {

        void onCardDismiss(int direction);
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

    float getDragSensitivity() {
        return mDragSensitivity;
    }

    public float getDismissDistance() {
        if (mDismissDistance > 0) {
            return mDismissDistance;
        }
        mDismissDistance = getWidth() * mDismissFactor;
        return mDismissDistance;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        log(TAG, "onMeasure");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        log(TAG, "onLayout start,mNeedAdjustChildren=" + mNeedAdjustChildren);
        super.onLayout(changed, left, top, right, bottom);
        if (mNeedAdjustChildren) {
            adjustChildren();
            if (mTouchHelper != null) {
                mTouchHelper.onChildChanged();
            }
            mNeedAdjustChildren = false;
        }
        int cnt = getChildCount();
        if (cnt > 0) {
            View last = getChildAt(cnt - 1);
            mLastLeft = last.getLeft();
            mLastTop = last.getTop();
            mLastRight = last.getRight();
            mLastBottom = last.getBottom();
        }
        log(TAG, "onLayout done");
    }

    private void adjustChildren() {
        final int cnt = getChildCount();
        if (cnt == 0) {
            return;
        }
        float scale = 0;
        float alpha;
        float translationY = 0;
        int half_childHeight = 0;
        int maxVisibleIndex = Math.min(cnt, mMaxVisibleCnt) - 1;
        mScaleArray = new float[cnt];
        mAlphaArray = new float[cnt];
        mTranslationYArray = new float[cnt];
        for (int i = 0; i <= maxVisibleIndex; i++) {
            View child = getChildAt(i);
            if (half_childHeight == 0) {
                half_childHeight = child.getMeasuredHeight() / 2;
            }
            scale = (float) Math.pow(mScaleFactor, i);
            mScaleArray[i] = scale;
            alpha = (float) Math.pow(mAlphaFactor, i);
            mAlphaArray[i] = alpha;
            translationY = half_childHeight * (1 - scale) + mLayerEdgeHeight * i;
            mTranslationYArray[i] = translationY;

            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setAlpha(alpha);
            child.setTranslationY(translationY);
        }
        for (int i = maxVisibleIndex + 1; i < cnt; i++) {
            View child = getChildAt(i);
            mScaleArray[i] = scale;
            mAlphaArray[i] = 0;
            mTranslationYArray[i] = translationY;
            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setAlpha(0);
            child.setTranslationY(translationY);
        }
    }

    void onCoverStatusChanged(boolean idle) {
        if (idle) {
            if (mPendingTask != null) {
                mPendingTask.run();
                mPendingTask = null;
            }
        }
    }

    void onCardDismissed(int direction) {
        log(TAG, "onCardDismissed,direction=" + direction);
        if (mCardSwipedListenrs != null) {
            for (OnCardSwipedListener listener : mCardSwipedListenrs) {
                listener.onCardDismiss(direction);
            }
        }
    }

    Rect rect = new Rect();


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (DEBUG) {
//            if (getChildCount() > 0) {
//                View cover = getChildAt(0);
//                cover.getHitRect(rect);
////                paint.setColor(Color.RED);
////                canvas.drawLine(0, cover.getY(), getWidth(), cover.getY(), paint);
////                canvas.drawLine(cover.getX(), 0, cover.getX(), getHeight(), paint);
////                canvas.drawLine(0, cover.getY() + cover.getHeight(), getWidth(), cover.getY() + cover.getHeight(), paint);
////                canvas.drawLine(cover.getX() + cover.getWidth(), 0, cover.getX() + cover.getWidth(), getHeight(), paint);
////                paint.setColor(Color.GREEN);
//                canvas.drawRect(rect, paint);
//            }
        }
    }

    void onDisappearStart(){
        final int childCount = getChildCount();
        log(TAG, "onDisappearStart,mAdapter.getCount()=" + mAdapter.getCount() + ",childCount=" + childCount);
        if (mAdapter.getCount() > childCount) {
            View view = mAdapter.getView(childCount, null, StackCardsView.this);
            addViewInLayout(view, -1, buildLayoutParams(mAdapter, childCount), true);
            view.layout(mLastLeft, mLastTop, mLastRight, mLastBottom);
            if (mTouchHelper != null) {
                mTouchHelper.onChildAppend();
            }
        }
    }

    void updateChildrenPosition(float progress, int startIndex) {
        if (DEBUG) {
            invalidate();
        }
        final int cnt = getChildCount();
        log(TAG, "updateChildrenPosition,startIndex=" + startIndex + ",cnt=" + cnt);
        if (startIndex >= cnt || startIndex < 1) {
            log(TAG, "--------------updateChildrenPosition err-------------------");
            return;
        }
        float oriScale;
        float oriAlpha;
        float oriTranslationY;
        float maxScale;
        float maxAlpha;
        float maxTranslationY;
        float progressScale;
        for (int i = startIndex; i < cnt; i++) {
            View child = getChildAt(i);
            int oriIndex = Math.min(mScaleArray.length-1,i - startIndex + 1);
            if (child.getVisibility() != View.GONE) {
                if (mScaleArray != null) {
                    oriScale = mScaleArray[oriIndex];
                    maxScale = mScaleArray[i - startIndex];
                    progressScale = oriScale + (maxScale - oriScale) * progress;
                    if (i == startIndex) {
                        log(TAG, "updateChildrenPosition,startIndex=" + startIndex + ",progress="+progress+",scale=" + progressScale);
                    }
                    child.setScaleX(progressScale);
                    child.setScaleY(progressScale);
                }

                if (mAlphaArray != null) {
                    oriAlpha = mAlphaArray[oriIndex];
                    maxAlpha = mAlphaArray[i - startIndex];
                    child.setAlpha(oriAlpha + (maxAlpha - oriAlpha) * progress);
                }

                if (mTranslationYArray != null) {
                    oriTranslationY = mTranslationYArray[oriIndex];
                    maxTranslationY = mTranslationYArray[i - startIndex];
                    child.setTranslationY(oriTranslationY + (maxTranslationY - oriTranslationY) * progress);
                }
            }
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return childCount - 1 - i;
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

    private static LayoutParams buildLayoutParams(Adapter adapter, int position) {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
                .swipeDirection(adapter.getSwipeDirection(position))
                .dismissDirection(adapter.getDismissDirection(position))
                .fastDismissAllowed(adapter.isFastDismissAllowed(position))
                .maxRotation(adapter.getMaxRotation(position));
    }

    private void initChildren() {
        log(TAG, "initChildren start");
        int cnt = mAdapter == null ? 0 : mAdapter.getCount();
        if (cnt == 0) {
            removeAllViewsInLayout();
        } else {
            removeAllViewsInLayout();
            cnt = Math.min(cnt, mMaxLayerCnt);
            for (int i = 0; i < cnt; i++) {
                addViewInLayout(mAdapter.getView(i, null, this), -1, buildLayoutParams(mAdapter, i), true);
            }
        }
        mNeedAdjustChildren = true;
        requestLayout();
        log(TAG, "initChildren end");
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
            if (mTouchHelper != null && !mTouchHelper.isCoverIdle()) {
                log(TAG, "onDataSetChanged, touch busy");
                mPendingTask = new Runnable() {
                    @Override
                    public void run() {
                        initChildren();
                    }
                };
            } else {
                log(TAG, "onDataSetChanged, touch idle");
                initChildren();
            }
        }

        @Override
        public void onItemInserted(int position) {
            super.onItemInserted(position);

        }

        @Override
        public void onItemRemoved(int position) {
            log(TAG, "onItemRemoved, position=" + position + ",childCnt=" + getChildCount());
            View toRemove = getChildAt(position);
            removeViewInLayout(toRemove);
            requestLayout();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
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

    public static class LayoutParams extends FrameLayout.LayoutParams {

        public int swipeDirection = SWIPE_ALL;
        public int dismissDirection = SWIPE_ALL;
        public boolean fastDismissAllowed = true;
        public float maxRotation;


        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }


        public LayoutParams swipeDirection(int direction) {
            this.swipeDirection = direction;
            return this;
        }

        public LayoutParams dismissDirection(int direction) {
            this.dismissDirection = direction;
            return this;
        }

        public LayoutParams fastDismissAllowed(boolean allowed) {
            this.fastDismissAllowed = allowed;
            return this;
        }

        public LayoutParams maxRotation(float maxRotation) {
            this.maxRotation = maxRotation;
            return this;
        }
    }

    public static abstract class Adapter {

        private final CardDataObservable mObservable = new CardDataObservable();

        public final void registerDataObserver(CardDataObserver observer) {
            mObservable.registerObserver(observer);
        }

        public final void unregisterDataObserver(CardDataObserver observer) {
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

        public int getSwipeDirection(int position) {
            return SWIPE_ALL;
        }

        public int getDismissDirection(int position) {
            return SWIPE_ALL;
        }

        public boolean isFastDismissAllowed(int position) {
            return true;
        }

        public int getMaxRotation(int position) {
            return 0;
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

    private static void log(String tag, String msg) {
        if (StackCardsView.DEBUG) {
            Log.d(tag, msg);
        }
    }
}
