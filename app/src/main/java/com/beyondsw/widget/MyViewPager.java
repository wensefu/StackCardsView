package com.beyondsw.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by wensefu on 17-3-6.
 */
public class MyViewPager extends ViewPager{

    private boolean mScrollAble;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollable(boolean scrollable) {
        if (mScrollAble != scrollable) {
            mScrollAble = scrollable;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mScrollAble) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mScrollAble) {
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
