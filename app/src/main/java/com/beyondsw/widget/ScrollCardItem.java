package com.beyondsw.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beyondsw.lib.widget.StackCardsView;

/**
 * Created by wensefu on 17-3-4.
 */
public class ScrollCardItem extends BaseCardItem {

    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 2;

    int direction = VERTICAL;

    public ScrollCardItem(Context context,int direction) {
        super(context);
        this.direction = direction;
        if (direction == VERTICAL) {
            swipeDir = StackCardsView.SWIPE_LEFT | StackCardsView.SWIPE_RIGHT;
        } else {
            swipeDir = StackCardsView.SWIPE_UP | StackCardsView.SWIPE_DOWN;
        }
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext, R.layout.item_scrollcard, null);
        RecyclerView recyclerView = Utils.findViewById(convertView, R.id.recyclerView);
        if (direction == VERTICAL) {
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new VerticalAdapter());
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setAdapter(new HorizontalAdapter());
        }
        return convertView;
    }

    private static class VerticalVH extends RecyclerView.ViewHolder {

        TextView textView;

        public VerticalVH(View itemView) {
            super(itemView);
            textView = Utils.findViewById(itemView,R.id.text_v);
        }
    }

    private static class HorizontalVH extends RecyclerView.ViewHolder {

        View colorView;

        public HorizontalVH(View itemView) {
            super(itemView);
            colorView = Utils.findViewById(itemView,R.id.color_view);
        }
    }

    private class VerticalAdapter extends RecyclerView.Adapter<VerticalVH> {

        private String[] array = {
                "My life is brilliant.",
                "My life is brilliant.",
                "My love is pure.",
                "I saw an angel.",
                "Of that I'm sure.",
                "She smiled at me on the subway.",
                "She was with another man.",
                "But I won't lose no sleep on that,",
                "'Cause I've got a plan.",
                "You're beautiful. You're beautiful,",
                "You're beautiful, it's true.",
                "I saw your face in a crowded place,",
                "And I don't know what to do,",
                "'Cause I'll never be with you.",
                "Yeah, she caught my eye,",
                "As we walked on by.",
                "She could see from my face that I was,",
                "flying high,",
                "And I don't think that I'll see her again,",
                "But we shared a moment that will last till the end.",
                "You're beautiful. You're beautiful.",
                "You're beautiful, it's true.",
                "I saw your face in a crowded place,",
                "And I don't know what to do,",
                "'Cause I'll never be with you.",
                "You're beautiful. You're beautiful.",
                "You're beautiful, it's true.",
                "There must be an angel with a smile on her face,",
                "When she thought up that I should be with you.",
                "But it's time to face the truth,",
                "I will never be with you.",
        };

        @Override
        public VerticalVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_v, parent, false);
            return new VerticalVH(itemView);
        }

        @Override
        public void onBindViewHolder(VerticalVH holder, int position) {
            holder.textView.setText(array[position]);
        }

        @Override
        public int getItemCount() {
            return array.length;
        }
    }


    private class HorizontalAdapter extends RecyclerView.Adapter<HorizontalVH> {

        int [] colors = {
                0xfff44336,
                0xff4527a0,
                0xff4caf50,
                0xffffa000,
                0xff546e7a,
                0xff76ff03,
                0xff4fc3f7,
                0xffffb74d,
                0xffe57373,
                0xff00897b,
                0xff4e342e,
        };

        @Override
        public HorizontalVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_recyclerview_h, null);
            return new HorizontalVH(itemView);
        }

        @Override
        public void onBindViewHolder(HorizontalVH holder, int position) {
            holder.colorView.setBackgroundColor(colors[position]);
        }

        @Override
        public int getItemCount() {
            return colors.length;
        }
    }
}
