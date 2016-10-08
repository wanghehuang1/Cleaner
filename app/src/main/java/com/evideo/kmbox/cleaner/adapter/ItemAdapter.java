package com.evideo.kmbox.cleaner.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.evideo.kmbox.cleaner.R;
import com.evideo.kmbox.cleaner.manager.CleanManager;
import com.evideo.kmbox.cleaner.model.CleanItem;

import java.util.ArrayList;


public class ItemAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<CleanItem> mItemList;
    private int patentI =0;

    public ItemAdapter(Context mContext, ArrayList<CleanItem> mItemList, int i) {
        this.mContext = mContext;
        this.mItemList = mItemList;
        this.patentI = i;
    }

    @Override
    public int getCount() {
        if (mItemList != null) {
            return mItemList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (mItemList != null) {
            return mItemList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        final ItemHold itemHold;
        if (view == null) {
            itemHold = new ItemHold();
            view = View.inflate(mContext, R.layout.item_adapter, null);
            itemHold.mItemNameTv = (TextView) view.findViewById(R.id.item_item_name_tv);
            itemHold.mItemStateCb = (CheckBox) view.findViewById(R.id.item_item_check_cb);
            view.setTag(itemHold);
        } else {
            itemHold = (ItemHold) view.getTag();
        }
        String name = mItemList.get(i).getName();

        itemHold.mItemNameTv.setText(name);

        //根据state值勾选子项CheckBox
        if (mItemList.get(i).getState() == 0) {
            itemHold.mItemStateCb.setChecked(false);
        } else if (mItemList.get(i).getState() == 1) {
            itemHold.mItemStateCb.setChecked(true);
        }

        itemHold.mItemStateCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemHold.mItemStateCb.isChecked()) {
                    mItemList.get(i).setState(1);
                } else {
                    mItemList.get(i).setState(0);
                }
                CleanManager.getInstance().getItemView().update(patentI, mItemList);
            }
        });

        return view;
    }

    public class ItemHold {
        TextView mItemNameTv = null;
        CheckBox mItemStateCb = null;
    }
}
