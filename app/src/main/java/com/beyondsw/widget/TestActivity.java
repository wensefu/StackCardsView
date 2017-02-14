package com.beyondsw.widget;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beyondsw.lib.widget.StackCardsView;
import com.bumptech.glide.Glide;

/**
 * Created by wensefu on 2017/2/12.
 */

public class TestActivity extends AppCompatActivity {

    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setAdapter(new MyPagerAdapter());
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View pageView;
            if (position == 0) {
                pageView = View.inflate(TestActivity.this, R.layout.page1, null);
                StackCardsView cardsView = (StackCardsView) pageView.findViewById(R.id.cards);
                cardsView.setAdapter(new MyAdapter());
            } else {
                pageView = View.inflate(TestActivity.this, R.layout.page2, null);
            }
            container.addView(pageView);
            return pageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
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
    }

    private class MyAdapter extends BaseAdapter implements View.OnClickListener{

        @Override
        public int getCount() {
            return ImageUrls.images.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("BeyondSwipeCard", "getView: position=" + position);
            View view = View.inflate(TestActivity.this, R.layout.item, null);
            view.setOnClickListener(this);
            ImageView img = (ImageView) view.findViewById(R.id.img);
            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText("pos=" + position);
            Glide.with(TestActivity.this).load(ImageUrls.images[position])
                    .centerCrop()
                    .placeholder(R.drawable.img_dft)
                    .crossFade()
                    .into(img);
            return view;
        }

        @Override
        public void onClick(View v) {
            Log.d("SwipeTouchHelper", "item onClick");
        }
    }
}
