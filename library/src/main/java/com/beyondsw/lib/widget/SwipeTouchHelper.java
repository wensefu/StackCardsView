package com.beyondsw.lib.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
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
public class SwipeTouchHelper implements ISwipeTouchHelper ,Handler.Callback{

    //// TODO: 2017/2/14
    //1,速度大于一定值时卡片滑出消失
//    2，滑动距离超过一定值后卡片消失，消失过程中改变alpha值
//    4，卡片消失后数据刷新
//    6，view缓存
    // 7,多点触控处理

    private static final String TAG = "StackCardsView-touch";

    private static final float SLOPE = 1.732f;
    private StackCardsView mSwipeView;
    private float mLastX;
    private float mLastY;
    private float mInitDownX;
    private float mInitDownY;
    private boolean mDownOnFirstChild;
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

    private boolean mShouldDisappear;

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
        updateCoverInfo(null);
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
            log(TAG, "onSpringAtRest: ");
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

            Log.d(TAG, "updateCoverInfo: left=" + mTouchChild.getLeft() + ",top=" + mTouchChild.getTop() + ",right=" + mTouchChild.getTop() + ",bottom=" + mTouchChild.getBottom());
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
        log(TAG, "onCoverChanged: ");
        updateCoverInfo(cover);
        mSwipeView.invalidate();
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
        log(TAG, "canDrag: dx=" + dx + ",dy=" + dy);
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
        float rotation = dx / (mDragSlop * 2) + cover.getRotation();
        final float maxRotation = mSwipeView.getMaxRotation();
        if (rotation > maxRotation) {
            rotation = maxRotation;
        } else if (rotation < -maxRotation) {
            rotation = -maxRotation;
        }
        Log.d(TAG, "performDrag: setRotation->" + rotation);
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
        log(TAG, "animateToDisappear: ");
        if (mTouchChild == null) {
            return;
        }
        mIsDisappearing = true;
        mIsBeingDragged = false;
        Rect rect = new Rect();
        mTouchChild.getGlobalVisibleRect(rect);
        float targetX;
        if (rect.left > 0) {
            targetX = mTouchChild.getX() + rect.width();
        } else {
            targetX = mTouchChild.getX() - rect.width();
        }
        final int direction = targetX > 0 ? StackCardsView.SWIPE_RIGHT : StackCardsView.SWIPE_LEFT;
        ObjectAnimator animator = ObjectAnimator.ofFloat(mTouchChild, "x", targetX).setDuration(180);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSwipeView.onCardDismissed(direction);
                mIsDisappearing = false;
                mSwipeView.onCoverStatusChanged(isCoverIdle());
                mSwipeView.adjustChildren();
                log(TAG, "onAnimationEnd: ");
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

    private void cancelSpringIfNeeded() {
        if (mSpring != null && !mSpring.isAtRest()) {
            mSpring.setAtRest();
            mSpring.removeAllListeners();
        }
    }

    private int[] calcScrollDistance(float dx, float dy) {
        int[] result = new int[2];
        float edgeDeltaX = 0;
        float edgeDeltaY = 0;
        if (dx > 0) {
            edgeDeltaX = mSwipeView.getWidth() - mTouchChild.getX();
        } else if (dx < 0) {
            edgeDeltaX = mTouchChild.getX() + mTouchChild.getWidth();
        }
        if (dy > 0) {
            edgeDeltaY = mSwipeView.getHeight() - mTouchChild.getY();
        } else if (dy < 0) {
            edgeDeltaY = mTouchChild.getHeight() + mTouchChild.getY();
        }
        float scrollDx;
        float scrollDy;
        if (edgeDeltaX * Math.abs(dy) >= edgeDeltaY * Math.abs(dx)) {
            scrollDy = dy > 0 ? edgeDeltaY : -edgeDeltaY;
            scrollDx = scrollDy * dx / dy;
        } else {
            scrollDx = dx > 0 ? edgeDeltaX : -edgeDeltaX;
            scrollDy = scrollDx * dy / dx;
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
        int[] fdxArray = calcScrollDistance(dx,dy);
        mScroller.startScroll((int) mTouchChild.getX(), (int) mTouchChild.getY(), fdxArray[0], fdxArray[1],200);
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
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();
                Log.d(TAG, "MSG_DO_DISAPPEAR_SCROLL: x=" + x + ",y=" + y);
                mTouchChild.setX(mScroller.getCurrX());
                mTouchChild.setY(mScroller.getCurrY());
                onCoverScrolled();
                Message m = mHandler.obtainMessage(MSG_DO_DISAPPEAR_SCROLL);
                mHandler.sendMessageDelayed(m, 30);
            }else{
                mSwipeView.onCardDismissed(0); //// TODO: 17-2-23
                mIsDisappearing = false;
                mSwipeView.onCoverStatusChanged(isCoverIdle());
                mSwipeView.adjustChildren();
                log(TAG, "do fling done ");
            }
        }
        return true;
    }

    @Override
    public void computeScroll() {
        Log.d(TAG, "computeScroll: ");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mTouchChild == null) {
            Log.d(TAG, "onInterceptTouchEvent: mTouchChild == null");
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "onTouchEvent: down,mVelocityTracker=" + mVelocityTracker);
            clearVelocityTracker();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
//        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
//            log(TAG, "onInterceptTouchEvent: action=" + action + ",reset touch");
//            mIsBeingDragged = false;
//            return false;
//        }
//        if (mIsBeingDragged && action != MotionEvent.ACTION_DOWN) {
//            Log.d(TAG, "onInterceptTouchEvent: mIsBeingDragged,not down event,return true");
//            return true;
//        }
        if (mIsDisappearing) {
            Log.d(TAG, "onInterceptTouchEvent: mIsDisappearing,not down event,return false");
            return false;
        }
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                log(TAG, "onInterceptTouchEvent: ACTION_DOWN,x=" + x);
                mDownOnFirstChild = isTouchOnFirstChild(x, y);
                if (!mDownOnFirstChild) {
                    log(TAG, "onInterceptTouchEvent: !isTouchOnFirstChild");
                    return false;
                }
                requestParentDisallowInterceptTouchEvent(true);
                mInitDownX = mLastX = x;
                mInitDownY = mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                log(TAG, "onInterceptTouchEvent: ACTION_MOVE");
                float dx = x - mInitDownX;
                float dy = y - mInitDownY;
                if (Math.sqrt(dx * dx + dy * dy) > mDragSlop && canDrag(dx, dy)) {
                    log(TAG, "onInterceptTouchEvent ACTION_MOVE: mIsBeingDragged = true");
                    cancelSpringIfNeeded();
                    mIsBeingDragged = true;
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                log(TAG, "onInterceptTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                log(TAG, "onInterceptTouchEvent: ACTION_POINTER_UP");
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        float x = ev.getX();
        float y = ev.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "onTouchEvent: down,mVelocityTracker=" + mVelocityTracker);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                log(TAG, "onTouchEvent: ACTION_DOWN,mIsBeingDragged=" + mIsBeingDragged + ",x=" + x);
                if (!mDownOnFirstChild) {
                    log(TAG, "onTouchEvent: !isTouchOnFirstChild");
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                log(TAG, "onTouchEvent: ACTION_MOVE,mIsBeingDragged=" + mIsBeingDragged);
                //子view未消费down事件时，mIsBeingDragged可能为false
                if (!mIsBeingDragged) {
                    cancelSpringIfNeeded();
                    mIsBeingDragged = true;
                }
                performDrag(x - mLastX, y - mLastY);
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                log(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                log(TAG, "onTouchEvent: ACTION_UP");
                if (mShouldDisappear) {
                    animateToDisappear();
                    mShouldDisappear = false;
                } else {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                    float xv = mVelocityTracker.getXVelocity();
                    float yv = mVelocityTracker.getYVelocity();
                    Log.d(TAG, "onTouchEvent: ACTION_UP,xv=" + xv + ",yv=" + yv + ",mMaxFlingVelocity=" + mMaxFlingVelocity + ",mMinFlingVelocity=" + mMinFlingVelocity);
                    if (!doFling(xv, yv)) {
                        animateToInitPos();
                    }
                }
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                log(TAG, "onTouchEvent: ACTION_POINTER_UP");
                break;
        }
        return true;
    }

    private static void log(String tag, String msg) {
        if (StackCardsView.DEBUG) {
            Log.d(tag, msg);
        }
    }
}
