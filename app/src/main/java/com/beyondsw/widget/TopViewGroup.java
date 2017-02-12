package com.beyondsw.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Created by wensefu on 17-2-10.
 */
public class TopViewGroup extends LinearLayout{

    public TopViewGroup(Context context) {
        super(context);
    }

    public TopViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Log.d("lingchao", "TopViewGroup dispatchDraw");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d("lingchao", "TopViewGroup onLayout");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("lingchao", "TopViewGroup onMeasure");
    }
}
