package com.beyondsw.lib.widget;

import android.view.MotionEvent;

/**
 * Created by wensefu on 17-2-12.
 */
public interface ISwipeTouchHelper {

    boolean onInterceptTouchEvent(MotionEvent ev);

    boolean onTouchEvent(MotionEvent ev);

    /**
     * 当ViewGroup的子view列表发生变化并且layout完成,设置好scale等属性后回调
     */
    void onChildChanged();

    void onChildAppend();

    /**
     * @return 当前是否有子view在拖动，做消失动画等,如果有则不进行数据刷新,等待空闲状态时再刷新
     */
    boolean isCoverIdle();

    void removeCover(int direction);
}
