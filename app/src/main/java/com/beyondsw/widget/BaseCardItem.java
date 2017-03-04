package com.beyondsw.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.beyondsw.lib.widget.StackCardsView;

/**
 * Created by wensefu on 17-3-4.
 */
public abstract class BaseCardItem {

    boolean fastDismissAllowed = true;
    int swipeDir = StackCardsView.SWIPE_ALL;
    int dismissDir = StackCardsView.SWIPE_ALL;
    int maxRotation = 8;

    protected Context mContext;

    public BaseCardItem(Context context) {
        mContext = context;
    }

    public abstract View getView(View convertView, ViewGroup parent);
}
