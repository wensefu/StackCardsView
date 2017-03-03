package com.beyondsw.widget;

import android.view.View;
import android.view.ViewGroup;

import com.beyondsw.lib.widget.StackCardsView;

import java.util.List;

/**
 * Created by wensefu on 17-3-4.
 */
public class CardAdapter extends StackCardsView.Adapter {

    private List<? extends BaseCardItem> mItems;

    public void setItems(List<BaseCardItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mItems.get(position).getView(convertView,parent);
    }

    @Override
    public int getSwipeDirection(int position) {
        BaseCardItem item = mItems.get(position);
        if (item instanceof ImageCardItem) {
            return StackCardsView.SWIPE_ALL;
        } else if (item instanceof ScrollCardItem) {
            int direction = ((ScrollCardItem) item).direction;
            if (direction == ScrollCardItem.HORIZONTAL) {
                return StackCardsView.SWIPE_UP | StackCardsView.SWIPE_DOWN;
            } else {
                return StackCardsView.SWIPE_LEFT | StackCardsView.SWIPE_RIGHT;
            }
        }
        return super.getSwipeDirection(position);
    }

    @Override
    public int getDismissDirection(int position) {
        BaseCardItem item = mItems.get(position);
        return item.dismissDir;
    }

    @Override
    public boolean isFastDismissAllowed(int position) {
        BaseCardItem item = mItems.get(position);
        return item.fastDismissAllowed;
    }

    @Override
    public int getMaxRotation(int position) {
        return 8;
    }
}
