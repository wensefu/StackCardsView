package com.beyondsw.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by wensefu on 17-3-4.
 */
public class ImageCardItem extends BaseCardItem {

    private static final String TAG = "ImageCardItem";

    private String url;
    private String label;

    public ImageCardItem(Context context, String url, String label) {
        super(context);
        this.url = url;
        this.label = label;
    }

    public  static class ViewHolder{
        ImageView left;
        ImageView right;
        ImageView up;
        ImageView down;
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext,R.layout.item_imagecard,null);
        ImageView imageView = Utils.findViewById(convertView,R.id.image);
        TextView labelview = Utils.findViewById(convertView,R.id.label);
        ImageView left = Utils.findViewById(convertView,R.id.left);
        ImageView right = Utils.findViewById(convertView,R.id.right);
        ImageView up = Utils.findViewById(convertView,R.id.up);
        ImageView down = Utils.findViewById(convertView,R.id.down);
        ViewHolder vh = new ViewHolder();
        vh.left = left;
        vh.right = right;
        vh.up = up;
        vh.down = down;
        convertView.setTag(vh);
        Glide.with(mContext)
                .load(url)
                .placeholder(R.drawable.img_dft)
                .centerCrop()
                .crossFade()
                .into(imageView);
        labelview.setText(label);
        return convertView;
    }
}
