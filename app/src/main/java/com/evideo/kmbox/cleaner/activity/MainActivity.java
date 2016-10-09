package com.evideo.kmbox.cleaner.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evideo.kmbox.cleaner.R;
import com.evideo.kmbox.cleaner.manager.CleanManager;
import com.evideo.kmbox.cleaner.model.CleanConfig;
import com.evideo.kmbox.cleaner.thread.CleanThread;
import com.evideo.kmbox.cleaner.thread.CountSizeThread;
import com.evideo.kmbox.cleaner.util.EvLog;
import com.evideo.kmbox.cleaner.util.FileUtil;
import com.evideo.kmbox.cleaner.view.ItemView;

import java.text.DecimalFormat;

public class MainActivity extends Activity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, CleanManager.IServiceCallBack {

    private Context mContext;

    private LinearLayout mLogDirLay = null;
    private LinearLayout mLostDirLay = null;
    private LinearLayout mCacheDirLay = null;
    private LinearLayout mAdDirLay = null;
    private LinearLayout mAppDirLay = null;
    private LinearLayout mEmptyDirLay = null;

    private TextView mLogTv = null;
    private TextView mLostTv = null;
    private TextView mCacheTv = null;
    private TextView mAdTv = null;
    private TextView mAppTv = null;
    private TextView mEmptyTv = null;

    private CheckBox mLogCb = null;
    private CheckBox mLostCb = null;
    private CheckBox mCacheCb = null;
    private CheckBox mAdCb = null;
    private CheckBox mAppCb = null;
    private CheckBox mEmptyCb = null;

    private Button mCleanAllBtn = null;
    private ImageView mBackIv = null;
    private TextView mTotalSizeTv = null;

    private CountSizeThread mCountSizeThread;
    private CleanThread mCleanThread;
    private ItemView mItemView;
    //空文件夹是否勾选
    private boolean isEmptyDirChecked = false;

    private long[] size = new long[6];
    private long totalSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EvLog.d("MainActivity onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mContext = this;
        CleanManager.getInstance().init();
        CleanManager.getInstance().setServeCallBack(this);
//        RunningAppManager.getInstance().getRunningAppLists(mContext);
        mItemView = CleanManager.getInstance().getItemView();
        initView();

        startCountSizeThread();
        EvLog.d("Sdcard Available Size is " + formateItemSize(FileUtil.getSDAvailableSize()));
//        mItemView.init();
    }


    private void initView() {
        mLogDirLay = (LinearLayout) findViewById(R.id.log_dir_lay);
        mLostDirLay = (LinearLayout) findViewById(R.id.lost_dir_lay);
        mCacheDirLay = (LinearLayout) findViewById(R.id.cache_dir_lay);
        mAdDirLay = (LinearLayout) findViewById(R.id.ad_dir_lay);
        mAppDirLay = (LinearLayout) findViewById(R.id.app_dir_lay);
        mEmptyDirLay = (LinearLayout) findViewById(R.id.empty_dir_lay);

        mLogTv = (TextView) findViewById(R.id.log_size_tv);
        mLostTv = (TextView) findViewById(R.id.lost_size_tv);
        mCacheTv = (TextView) findViewById(R.id.cache_size_tv);
        mAdTv = (TextView) findViewById(R.id.ad_size_tv);
        mAppTv = (TextView) findViewById(R.id.app_size_tv);
        mEmptyTv = (TextView) findViewById(R.id.empty_size_tv);

        mLogCb = (CheckBox) findViewById(R.id.log_check_cb);
        mLostCb = (CheckBox) findViewById(R.id.lost_check_cb);
        mCacheCb = (CheckBox) findViewById(R.id.cache_check_cb);
        mAdCb = (CheckBox) findViewById(R.id.ad_check_cb);
        mAppCb = (CheckBox) findViewById(R.id.app_check_cb);
        mEmptyCb = (CheckBox) findViewById(R.id.empty_check_cb);

        mBackIv = (ImageView) findViewById(R.id.common_back_iv);
        mBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCleanAllBtn = (Button) findViewById(R.id.clean_all_btn);
        mCleanAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCleanThread();

            }
        });

        mTotalSizeTv = (TextView) findViewById(R.id.clean_size_tv);

        mLogCb.setOnClickListener(this);
        mLostCb.setOnClickListener(this);
        mCacheCb.setOnClickListener(this);
        mAdCb.setOnClickListener(this);
        mAppCb.setOnClickListener(this);
        mEmptyCb.setOnClickListener(this);

        mLogDirLay.setOnClickListener(this);
        mLostDirLay.setOnClickListener(this);
        mCacheDirLay.setOnClickListener(this);
        mAdDirLay.setOnClickListener(this);
        mAppDirLay.setOnClickListener(this);

    }

    private void startCleanThread() {
        mCleanThread = new CleanThread(mContext, isEmptyDirChecked);
        mCleanThread.start();
    }

    private void startCountSizeThread() {
        if (mCountSizeThread == null) {
            mCountSizeThread = new CountSizeThread(mContext);
            mCountSizeThread.start();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    /**
     * [点击主CheckBox时更新子项中CheckBox的状态]
     *
     * @param checkBox
     * @param i
     */
    private void checkUpdate(CheckBox checkBox, int i) {
        if (checkBox.isChecked()) {
            mItemView.update(i, CleanConfig.CHECKBOX_CHECKED);
        } else {
            mItemView.update(i, CleanConfig.CHECKBOX_UNCHECKED);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.log_check_cb:
                checkUpdate(mLogCb, CleanConfig.LOG_ID);
                break;
            case R.id.ad_check_cb:
                checkUpdate(mAdCb, CleanConfig.AD_ID);
                break;
            case R.id.cache_check_cb:
                checkUpdate(mCacheCb, CleanConfig.CACHE_ID);
                break;
            case R.id.app_check_cb:
                checkUpdate(mAppCb, CleanConfig.APK_ID);
                break;
            case R.id.lost_check_cb:
                checkUpdate(mLostCb, CleanConfig.LOST_ID);
                break;
            case R.id.empty_check_cb:
                if (mEmptyCb.isChecked()) {
                    isEmptyDirChecked = true;
                } else {
                    isEmptyDirChecked = false;
                }
                EvLog.d("isEmptyDirChecked: " + isEmptyDirChecked);
                break;

            case R.id.log_dir_lay:
                mItemView.show(mContext, CleanConfig.LOG_ID, mLogDirLay);
                break;
            case R.id.ad_dir_lay:
                mItemView.show(mContext, CleanConfig.AD_ID, mAdDirLay);
                break;
            case R.id.cache_dir_lay:
                mItemView.show(mContext, CleanConfig.CACHE_ID, mCacheDirLay);
                break;
            case R.id.app_dir_lay:
                mItemView.show(mContext, CleanConfig.APK_ID, mAppDirLay);
                break;
            case R.id.lost_dir_lay:
                mItemView.show(mContext, CleanConfig.LOST_ID, mLostDirLay);
                break;
        }
    }

    //将总大小格式化，保留一位小数，以MB为单位
    private String fomateTotalSize(long totalSize) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(totalSize / (1024 * 1024));
    }

    //格式化各项大小
    private String formateItemSize(long size) {
        return Formatter.formatFileSize(mContext, size);
    }

    @Override
    public void removeSize(int i, long itemsize) {
        switch (i) {
            case CleanConfig.LOG_ID:
                size[0] -= itemsize;
                mLogTv.setText(formateItemSize(size[0]));
                break;
            case CleanConfig.AD_ID:
                size[1] -= itemsize;
                mAdTv.setText(formateItemSize(size[1]));
                break;
            case CleanConfig.CACHE_ID:
                size[2] -= itemsize;
                mCacheTv.setText(formateItemSize(size[2]));
                break;
            case CleanConfig.APK_ID:
                size[3] -= itemsize;
                mAppTv.setText(formateItemSize(size[3]));
                break;
            case CleanConfig.LOST_ID:
                size[4] -= itemsize;
                mLostTv.setText(formateItemSize(size[4]));
                break;
        }
        totalSize -= itemsize;
        EvLog.d("MainActivity", "total size down to " + totalSize);
        mTotalSizeTv.setText(fomateTotalSize(totalSize));
    }

    @Override
    public void updateSize(long[] size) {
        this.size = size;
        mLogTv.setText(formateItemSize(size[0]));
        mAdTv.setText(formateItemSize(size[1]));
        mCacheTv.setText(formateItemSize(size[2]));
        mAppTv.setText(formateItemSize(size[3]));
        mLostTv.setText(formateItemSize(size[4]));

        for (long s : size) {
            totalSize += s;
        }
        mTotalSizeTv.setText(fomateTotalSize(totalSize));
    }

    @Override
    public void hideItemCb(int i) {
        switch (i) {
            case CleanConfig.LOG_ID:
                mLogCb.setVisibility(View.GONE);
                mLogTv.setVisibility(View.GONE);
                mLogDirLay.setEnabled(false);
                break;
            case CleanConfig.AD_ID:
                mAdCb.setVisibility(View.GONE);
                mAdTv.setVisibility(View.GONE);
                mAdDirLay.setEnabled(false);
                break;
            case CleanConfig.CACHE_ID:
                mCacheCb.setVisibility(View.GONE);
                mCacheTv.setVisibility(View.GONE);
                mCacheDirLay.setEnabled(false);
                break;
            case CleanConfig.APK_ID:
                mAppCb.setVisibility(View.GONE);
                mAppTv.setVisibility(View.GONE);
                mAppDirLay.setEnabled(false);
                break;
            case CleanConfig.LOST_ID:
                mLostCb.setVisibility(View.GONE);
                mLostTv.setVisibility(View.GONE);
                mLostDirLay.setEnabled(false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        EvLog.d("MainActivity", "onDesteoy");
        if (mCountSizeThread != null) {
            mCountSizeThread.closeThread();
        }
        if (mCleanThread != null) {
            mCleanThread.closeThread();
        }
        if (mItemView != null) {
            CleanManager.getInstance().releaseItemView();
        }
        super.onDestroy();

    }
}
