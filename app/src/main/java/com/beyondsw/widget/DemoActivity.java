package com.beyondsw.widget;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.beyondsw.lib.widget.StackCardsView;

/**
 * Created by wensefu on 2017/2/12.
 */

public class DemoActivity extends AppCompatActivity {

    private static final String TAG = "DemoActivity";

    private ViewPager mPager;
    private StackCardsView stackCardsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setAdapter(new MyPagerAdapter());
        mPager.setPageTransformer(false, new MyPageTransformer());
    }

    private static class MyPageTransformer implements ViewPager.PageTransformer{

        @Override
        public void transformPage(View page, float position) {
            Log.d(TAG, "transformPage: page=" + page + ",position=" + position);
        }
    }

    private class MyPagerAdapter extends PagerAdapter implements StackCardsView.OnCardSwipedListener {

        private CardAdapter adapter;


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View pageView;
            if (position == 0) {
                pageView = View.inflate(DemoActivity.this, R.layout.page1, null);
                stackCardsView = (StackCardsView) pageView.findViewById(R.id.cards);
                stackCardsView.addOnCardSwipedListener(this);
                adapter = new CardAdapter();
                stackCardsView.setAdapter(adapter);
            } else {
                pageView = View.inflate(DemoActivity.this, R.layout.page2, null);
            }
            container.addView(pageView);
            return pageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position == 0) {
                stackCardsView.removeOnCardSwipedListener(this);
                stackCardsView = null;
                adapter = null;
            }
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void onCardDismiss(int direction) {

        }
    }
}
