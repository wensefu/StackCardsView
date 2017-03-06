package com.beyondsw.widget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by wensefu on 2017/2/12.
 */

public class DemoActivity extends AppCompatActivity implements CardFragment.Callback{

    private static final String TAG = "DemoActivity";

    private MyViewPager mPager;
    private Fragment mSettingFragment;
    private CardFragment mCardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSettingFragment = new SettingFragment();
        mCardFragment = new CardFragment();
        mCardFragment.setCallback(this);
        mPager = Utils.findViewById(this,R.id.viewpager);
        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }

    @Override
    public void onViewPagerCbChanged(boolean checked) {
        mPager.setScrollable(checked);
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
