package com.beyondsw.widget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

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
        mPager.setPageTransformer(false, new MyPageTransformer());
    }

    private static class MyPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {
            Log.d(TAG, "transformPage: page=" + page + ",position=" + position);
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return position == 0 ? mSettingFragment : mCardFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
