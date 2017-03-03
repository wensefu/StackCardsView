package com.beyondsw.widget;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wensefu on 17-3-4.
 */
public abstract class BaseCardItem {

    boolean clickable;
    boolean fastDismissAllowed;
    int dismissDir;

    public abstract View getView(View convertView, ViewGroup parent);
}
