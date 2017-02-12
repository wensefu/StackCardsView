package com.beyondsw.widget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    View topView,myGroup,view1,view2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topView = findViewById(R.id.topview);
        myGroup = findViewById(R.id.mygroup);
        view1 = findViewById(R.id.myview);
        view1.setOnClickListener(this);
        view2 = findViewById(R.id.myview2);
        findViewById(R.id.button).setOnClickListener(this);
    }

    int i = 1;

    @Override
    public void onClick(View v) {
        if(v==view1){
            Log.d("lingchao", "onClick: ");
        }else{
            Log.d("lingchao", "onClick-before: getTop=" + view1.getTop()+",getty="+view1.getTranslationY());
            view1.setY(view1.getY()+2);
            Log.d("lingchao", "onClick-after: getTop=" + view1.getTop()+",getty="+view1.getTranslationY());
//            Log.d("lingchao", "onClick-before: getTop=" + view1.getTop());
//            view1.setTranslationY((i++) * 5);
//            Log.d("lingchao", "onClick-after: getTop=" + view1.getTop());
        }

    }
}
