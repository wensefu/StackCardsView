package com.beyondsw.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Created by wensefu on 17-2-10.
 */
public class MyViewGroup extends LinearLayout{

    private String name;

    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);


        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MyViewGroup, 0, 0);
        name = a.getString(R.styleable.MyViewGroup_name);
        a.recycle();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Log.d("lingchao", "MyViewGroup " + name + " dispatchDraw");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int cnt = getChildCount();
        Log.d("lingchao", "MyViewGroup "+name+" onLayout,childcnt ="+cnt);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("lingchao", "MyViewGroup " + name + " onMeasure");
    }
}
