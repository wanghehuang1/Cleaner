/*
 * Copyright (C) 2014-2016  福建星网视易信息系统有限公司
 * All rights reserved by  福建星网视易信息系统有限公司
 *
 * Modification History:
 * Date        Author      Version     Description
 * -----------------------------------------------
 * 2016/10/9       Whh       1       [修订说明]
 *
 */

package com.evideo.kmbox.cleaner.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.evideo.kmbox.cleaner.R;
import com.evideo.kmbox.cleaner.util.FileUtil;

public class DeepCLeanActivity extends Activity {

    private Context mContext;
    private TextView mSdAvailableSizeTv = null;
    private ImageView mBackIv = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_clean);
        mContext = this;
        initView();
    }

    private void initView() {
        mSdAvailableSizeTv = (TextView) findViewById(R.id.sd_available_size_tv);
        mSdAvailableSizeTv.setText(formateFileSize(FileUtil.getSDAvailableSize()));

        mBackIv = (ImageView) findViewById(R.id.common_back_iv);
        mBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //格式化文件大小
    private String formateFileSize(long size) {
        return Formatter.formatFileSize(mContext, size);
    }
}
