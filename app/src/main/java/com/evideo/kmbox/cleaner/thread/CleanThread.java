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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.evideo.kmbox.cleaner.activity.DeepCLeanActivity;
import com.evideo.kmbox.cleaner.activity.MainActivity;
import com.evideo.kmbox.cleaner.base.BaseApplication;
import com.evideo.kmbox.cleaner.manager.CleanManager;
import com.evideo.kmbox.cleaner.util.EvLog;
import com.evideo.kmbox.cleaner.util.FileUtil;


import java.io.File;


/**
 * 清理垃圾文件
 */
public class CleanThread extends Thread {

    private boolean isEmptyDirChecked = false;
    private Context mContext;

    public CleanThread(Context context, boolean isEmptyDirChecked) {
        this.mContext = context;
        this.isEmptyDirChecked = isEmptyDirChecked;
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
        for (int i=0; i<5; i++) {
            CleanManager.getInstance().getItemView().remove(i);
        }
        if (isEmptyDirChecked) {
            cleanEmptyFolder(CleanManager.getInstance().getRootPath());
        }
        closeThread();

        //如果sd卡剩余空间还是小于某个值，比如200MB，则进行深度清理
        if (FileUtil.getSDAvailableSize() < 500 * 1024 *1024) {
            Intent intent = new Intent();
            intent.setClass(mContext, DeepCLeanActivity.class);
            mContext.startActivity(intent);
        }
    }

    /**
     * [删除空文件夹]
     * @param rootPath
     */
    private void cleanEmptyFolder(String rootPath) {
        if (!FileUtil.isDirectory(rootPath)) {
            return;
        }
        if (FileUtil.isEmptyDir(rootPath)) {
            FileUtil.deleteFile(rootPath);
        } else {
            File file = new File(rootPath);
            File[] files = file.listFiles();
            for (File subFile : files) {
                cleanEmptyFolder(subFile.getAbsolutePath());
            }
        }
    }

}
