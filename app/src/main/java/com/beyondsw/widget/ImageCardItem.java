package com.beyondsw.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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

    @Override
    public View getView(View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext,R.layout.item_imagecard,null);
        ImageView imageView = Utils.findViewById(convertView,R.id.image);
        TextView labelview = Utils.findViewById(convertView,R.id.label);
        Glide.with(mContext)
                .load(url)
                .placeholder(R.drawable.img_dft)
                .centerCrop()
                .crossFade()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Log.d(TAG, "onException: model=" + model);
                        if (e != null) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.d(TAG, "onResourceReady: model=" + model);
                        return false;
                    }
                })
                .into(imageView);
        labelview.setText(label);
        return convertView;
    }
}
