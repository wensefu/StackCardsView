package com.beyondsw.widget;

import android.app.Activity;
import android.view.View;

/**
 * Created by wensefu on 17-3-4.
 */
public class Utils {

    @SuppressWarnings("unchecked")
    public static <T> T findViewById(Activity act, int id){
        return (T)act.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T> T findViewById(View parent,int id){
        return (T)parent.findViewById(id);
    }

}
