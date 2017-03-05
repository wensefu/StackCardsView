package com.beyondsw.widget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by wensefu on 2017/2/12.
 */

public class DemoActivity extends AppCompatActivity {

    private static final String TAG = "DemoActivity";

    private ViewPager mPager;
    private Fragment mSettingFragment;
    private Fragment mCardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSettingFragment = new SettingFragment();
        mCardFragment = new CardFragment();
        mPager = Utils.findViewById(this,R.id.viewpager);
        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return position == 0 ? mCardFragment : mSettingFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
