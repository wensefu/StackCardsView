package com.beyondsw.lib.widget;

import android.view.MotionEvent;

/**
 * Created by wensefu on 17-2-12.
 */
public interface ISwipeTouchHelper {

    boolean onInterceptTouchEvent(MotionEvent ev);

    boolean onTouchEvent(MotionEvent ev);

    void onChildLayouted();
}
