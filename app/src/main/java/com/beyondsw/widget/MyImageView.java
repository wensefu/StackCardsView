package com.beyondsw.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by wensefu on 2017/2/14.
 */

public class MyImageView extends ImageView {

    private int pos = -1;

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPos(int pos){
        this.pos = pos;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        return super.onTouchEvent(event);
    }
}
