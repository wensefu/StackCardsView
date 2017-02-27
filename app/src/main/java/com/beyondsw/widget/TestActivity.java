package com.beyondsw.widget;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.beyondsw.lib.widget.StackCardsView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wensefu on 2017/2/12.
 */

public class TestActivity extends AppCompatActivity {

    private ViewPager mPager;
    private StackCardsView stackCardsView;
    private final List<String> mImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setAdapter(new MyPagerAdapter());
    }

    Random random = new Random();

    private class MyPagerAdapter extends PagerAdapter implements StackCardsView.OnCardSwipedListener {

        private CardAdapter adapter;


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View pageView;
            if (position == 0) {
                pageView = View.inflate(TestActivity.this, R.layout.page1, null);
                stackCardsView = (StackCardsView) pageView.findViewById(R.id.cards);
                stackCardsView.addOnCardSwipedListener(this);
                adapter = new CardAdapter();
                stackCardsView.setAdapter(adapter);
//                stackCardsView.removeSwipeDirection(StackCardsView.SWIPE_ALL);
            } else {
                pageView = View.inflate(TestActivity.this, R.layout.page2, null);
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
            adapter.remove(0);
            if (adapter.getCount() < 2) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        try {
//                            TimeUnit.MILLISECONDS.sleep(Math.min(random.nextInt(1000),200));
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        for (int i = 0; i < ImageUrls.images.length; i++) {
                            mImages.add(ImageUrls.images[i]);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
            }
        }
    }


    private class TextAdapter extends BaseAdapter {

        String[] textArray = {
                "1111111111111111111",
                "2222222222222",
                "3333333333333333",
                "44444444444444",
                "555555555555",
                "666666666666666",
                "7777777777",
                "88888888888",
                "999999",
                "aaaaaaaaaa",
                "bbbbbbbbbbb",
                "cccccccccccccccccc",
                "ddddddddddddddd",
                "eeeeeeeeeeeee",
                "fffffffffffffffff",
                "ggggggggggg",
                "hhhhhhhhhhhhhhhhhhhhhh",
                "iiiiiiiiiiiiiiiiiiiiiii",
                "jjjjjjjjjjjjjjjjjjjjjjjjj",
                "kkkkkkkkkkkkkkkkkkkkk",
                "lllllllllllllll",
                "mmmmmmmmmmmmmm",
                "nnnnnnnnnnnnnnn",
                "ooooooooooooo",
                "pppppppppp",
        };


        @Override
        public int getCount() {
            return textArray.length;
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
            TextView textView;
            if (convertView == null) {
                convertView = View.inflate(TestActivity.this, R.layout.child_item, null);
                textView = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(textView);
            } else {
                textView = (TextView) convertView.getTag();
            }
            textView.setText(textArray[position]);
            return convertView;
        }
    }

    private class CardAdapter extends StackCardsView.Adapter implements View.OnClickListener {

        boolean hasListView = false;

        CardAdapter() {
            for (int i = 0; i < ImageUrls.images.length; i++) {
                mImages.add(ImageUrls.images[i]);
            }
        }

        @Override
        public int getSwipeDirection(int position) {
//            if (position == getCount() - 1) {
//                return StackCardsView.SWIPE_NONE;
//            }
//            return super.getSwipeDirection(position);
            return StackCardsView.SWIPE_ALL;
        }

        @Override
        public int getDismissDirection(int position) {
            return super.getDismissDirection(position);
        }

        @Override
        public boolean isFastDismissAllowed(int position) {
            return super.isFastDismissAllowed(position);
        }

        @Override
        public int getMaxRotation(int position) {
            return 8;
        }

        @Override
        public int getCount() {
            return hasListView ? mImages.size() + 2 : mImages.size() + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("BeyondSwipeCard", "getView: position=" + position);
            View view;
            if (hasListView) {
                if (position == 0) {
                    view = View.inflate(TestActivity.this, R.layout.item2, null);
                    ListView listView = (ListView) view.findViewById(R.id.list);
                    listView.setAdapter(new TextAdapter());
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("SwipeTouchHelper", "AdapterView onItemClick: position=" + position);
                        }
                    });
                } else {
                    view = View.inflate(TestActivity.this, R.layout.item, null);
                    view.setOnClickListener(this);
                    MyImageView img = (MyImageView) view.findViewById(R.id.img);
                    img.setPos(position);
                    TextView textView = (TextView) view.findViewById(R.id.text);
                    textView.setText("pos=" + position);
                    Glide.with(TestActivity.this).load(mImages.get(position - 1))
                            .centerCrop()
                            .placeholder(R.drawable.img_dft)
                            .crossFade()
                            .into(img);
                }
            } else {
                if (position == getCount() - 1) {
                    return View.inflate(TestActivity.this, R.layout.child_item2, null);
                }
                view = View.inflate(TestActivity.this, R.layout.item, null);
                view.setTag(position);
//                view.setOnClickListener(this);
                MyImageView img = (MyImageView) view.findViewById(R.id.img);
                img.setPos(position);
                TextView textView = (TextView) view.findViewById(R.id.text);
                textView.setText("pos=" + position);
                Glide.with(TestActivity.this).load(mImages.get(position))
                        .centerCrop()
                        .placeholder(R.drawable.img_dft)
                        .crossFade()
                        .into(img);
                view.requestLayout();
//                view.setAlpha(0.3f);
//                view.animate().alpha(1).setDuration(100).start();
            }
            return view;
        }

        @Override
        public void onClick(View v) {
            Integer pos = (Integer) v.getTag();
            Log.d("StackCardsView", "item onClick,pos=" + pos);
        }

        private void remove(int pos) {
            if (hasListView) {
                hasListView = false;
                notifyDataSetChanged();
            } else {
                if (pos >= 0 && pos < mImages.size()) {
                    mImages.remove(pos);
                    notifyItemRemoved(0);
                }
            }
        }
    }
}
