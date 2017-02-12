package com.beyondsw.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Created by wensefu on 2017/2/12.
 */

public class ImageViewGroup extends LinearLayout{

    public ImageViewGroup(Context context) {
        super(context);
    }

    public ImageViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d("BeyondSwipeCard", "child" + hashCode() + " onLayout");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("BeyondSwipeCard", "child" + hashCode() + " onMeasure,width=" + width + ",height=" + height);
    }
}
