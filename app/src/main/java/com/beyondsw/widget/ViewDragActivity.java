package com.beyondsw.widget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by wensefu on 17-2-25.
 */
public class ViewDragActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drag_test);
        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
