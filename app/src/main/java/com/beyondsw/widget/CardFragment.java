package com.beyondsw.widget;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyondsw.lib.widget.StackCardsView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wensefu on 17-3-4.
 */
public class CardFragment extends Fragment implements Handler.Callback,StackCardsView.OnCardSwipedListener,View.OnClickListener{

    private static final String TAG ="Demo-CardFragment";

    private StackCardsView mCardsView;
    private CardAdapter mAdapter;
    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private Handler mMainHandler;
    private static final int MSG_START_LOAD_DATA = 1;
    private static final int MSG_DATA_LOAD_DONE = 2;
    private volatile int mStartIndex;
    private static final int PAGE_COUNT = 10;

    private View mLeftBtn;
    private View mRightBtn;
    private View mUpBtn;
    private View mDownBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.page1,null);

        mLeftBtn = Utils.findViewById(root,R.id.left);
        mRightBtn = Utils.findViewById(root,R.id.right);
        mUpBtn = Utils.findViewById(root,R.id.up);
        mDownBtn = Utils.findViewById(root, R.id.down);
        mLeftBtn.setOnClickListener(this);
        mRightBtn.setOnClickListener(this);
        mUpBtn.setOnClickListener(this);
        mDownBtn.setOnClickListener(this);

        mCardsView = Utils.findViewById(root,R.id.cards);
        mCardsView.addOnCardSwipedListener(this);
        mAdapter = new CardAdapter();
        mCardsView.setAdapter(mAdapter);
        mMainHandler = new Handler(this);
        mWorkThread = new HandlerThread("data_loader");
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper(),this);
        mWorkHandler.obtainMessage(MSG_START_LOAD_DATA).sendToTarget();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCardsView.removeOnCardSwipedListener(this);
        mWorkThread.quit();
        mWorkHandler.removeMessages(MSG_START_LOAD_DATA);
        mMainHandler.removeMessages(MSG_DATA_LOAD_DONE);
        mStartIndex = 0;
    }

    @Override
    public void onClick(View v) {
        if (v == mLeftBtn) {
            mCardsView.removeCover(StackCardsView.SWIPE_LEFT);
        } else if (v == mRightBtn) {
            mCardsView.removeCover(StackCardsView.SWIPE_RIGHT);
        } else if (v == mUpBtn) {
            mCardsView.removeCover(StackCardsView.SWIPE_UP);
        } else if (v == mDownBtn) {
            mCardsView.removeCover(StackCardsView.SWIPE_DOWN);
        }
    }

    @Override
    public void onCardDismiss(int direction) {
        Log.d(TAG, "onCardDismiss");
        mAdapter.remove(0);
        if (mAdapter.getCount() < 3) {
            if (!mWorkHandler.hasMessages(MSG_START_LOAD_DATA)) {
                mWorkHandler.obtainMessage(MSG_START_LOAD_DATA).sendToTarget();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case MSG_START_LOAD_DATA:{
                Log.d(TAG, "MSG_START_LOAD_DATA");
                List<BaseCardItem> data = loadData(mStartIndex);
                mMainHandler.obtainMessage(MSG_DATA_LOAD_DONE,data).sendToTarget();
                break;
            }
            case MSG_DATA_LOAD_DONE:{
                Log.d(TAG, "MSG_DATA_LOAD_DONE");
                List<BaseCardItem> data = (List<BaseCardItem>) msg.obj;
                mAdapter.appendItems(data);
                int size = data == null ? 0 : data.size();
                mStartIndex += size;
                break;
            }
        }
        return true;
    }

    private List<BaseCardItem> loadData(int startIndex) {
        if (startIndex < ImageUrls.images.length) {
            final int endIndex = Math.min(mStartIndex + PAGE_COUNT, ImageUrls.images.length - 1);
            List<BaseCardItem> result = new ArrayList<>(endIndex - startIndex + 1);
            for (int i = startIndex; i <= endIndex; i++) {
                ImageCardItem item = new ImageCardItem(getActivity(), ImageUrls.images[i], ImageUrls.labels[i]);
                item.dismissDir = StackCardsView.SWIPE_ALL;
                item.fastDismissAllowed = true;
                result.add(item);
            }
            if (startIndex == 0) {
                ScrollCardItem item = new ScrollCardItem(getActivity(), ScrollCardItem.VERTICAL);
                result.add(1, item);
            }
            return result;
        }
        return null;
    }
}
