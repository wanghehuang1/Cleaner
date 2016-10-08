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

import com.evideo.kmbox.cleaner.manager.CleanManager;
import com.evideo.kmbox.cleaner.util.EvLog;
import com.evideo.kmbox.cleaner.util.FileUtil;


import java.io.File;


/**
 * 清理垃圾文件
 */
public class CleanThread extends Thread {

    private boolean isEmptyDirChecked = false;

    public CleanThread( boolean isEmptyDirChecked) {
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
