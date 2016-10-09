/*
 * Copyright (C) 2014-2016  福建星网视易信息系统有限公司
 * All rights reserved by  福建星网视易信息系统有限公司
 *
 * Modification History:
 * Date        Author      Version     Description
 * -----------------------------------------------
 * 2016/9/21       Whh       1       [修订说明]
 *
 */

package com.evideo.kmbox.cleaner.thread;

import android.content.Context;

import com.evideo.kmbox.cleaner.manager.CleanManager;
import com.evideo.kmbox.cleaner.model.CleanItem;
import com.evideo.kmbox.cleaner.util.EvLog;
import com.evideo.kmbox.cleaner.util.FileUtil;

import java.io.File;
import java.util.ArrayList;


/**
 * 计算垃圾文件大小
 */
public class CountSizeThread extends Thread {

    private Context mContext;
    private ArrayList<CleanItem> mCleanItems = null;

    public CountSizeThread(Context mContext) {
        this.mContext = mContext;
    }

    public synchronized void closeThread() {
        try {
            notify();
            interrupt();
        } catch (Exception e) {
            EvLog.e("thread close exception");
        }
    }

    @Override
    public void run() {
        super.run();

        long[] size = new long[6];
        for (int i = 0; i < 6; i++) {
            size[i] = 0;
            if (i == 5) {
                continue;
            }
            for (int j = 0; j < CleanManager.PATHS[i].length; j++) {
                try {
                    size[i] += FileUtil.getFileSize(new File(CleanManager.getInstance().getRootPath() +
                            File.separator + CleanManager.PATHS[i][j]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (size[i] == 0) {
                CleanManager.getInstance().sendHideCbMessage(i);
            }
        }
        CleanManager.getInstance().sendSizeMessage(size);
        CleanManager.getInstance().getItemView().init();
    }


}
