/*
 * Copyright (C) 2014-2016  福建星网视易信息系统有限公司
 * All rights reserved by  福建星网视易信息系统有限公司
 *
 * Modification History:
 * Date        Author      Version     Description
 * -----------------------------------------------
 * 2016/9/20       Whh       1       [修订说明]
 *
 */

package com.evideo.kmbox.cleaner.manager;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.evideo.kmbox.cleaner.model.CleanItem;
import com.evideo.kmbox.cleaner.util.EvLog;
import com.evideo.kmbox.cleaner.util.FileUtil;
import com.evideo.kmbox.cleaner.view.ItemView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CleanManager {

    private static final String TAG = "CleanManager";
    private static CleanManager sInstance;
    /**
     * 日志文件
     */
    public static final String[] LOG_DIR_PATH = {"kmbox/log"};
    /**
     * 无用的系统文件
     */
    public static final String[] LOST_DIR_PATH = {"LOST.DIR"};
    /**
     * 广告文件
     */
    public static final String[] AD_DIR_PATH = {"baidu", "360Download", "wandoujia/app", "Tencent"};
    /**
     * 无用缓存文件
     */
    public static final String[] CACHE_DIR_PATH = {"Android/data"};
    /**
     * 无用安装包
     */
    public static final String[] APK_FILE_PATH = {"kmbox/assets/app/updateapk", "download", "com.togic.livevideo/cache", "bddownload"};
    /**
     * 空文件夹
     */
    public static String EMPTY_DIR_PATH[] = {""};

    private IServiceCallBack mCallBack;
    //sd卡根目录
    private String mRootPath = "";

    public static final String[][] PATHS = new String[][]{LOG_DIR_PATH, AD_DIR_PATH, CACHE_DIR_PATH,
            APK_FILE_PATH, LOST_DIR_PATH, EMPTY_DIR_PATH};

    private Handler mCountHandler;
    private ItemView mItemView;

    private ArrayList<CleanItem>[] itemLists = new ArrayList[6];

    private CleanManager() {
        if (Environment.getExternalStorageDirectory().getPath() != null) {
            mRootPath = Environment.getExternalStorageDirectory().getPath();
            EvLog.d(TAG, "mRootPath = " + mRootPath);
        }
    }

    public static synchronized CleanManager getInstance() {
        if (sInstance == null) {
            sInstance = new CleanManager();
        }
        return sInstance;
    }

    public void init() {
        if (mCountHandler == null) {
            mCountHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg != null) {
                        if (msg.what == 1) {
                            if (msg.arg1 == 0) {
                                long[] size = (long[]) msg.obj;
                                mCallBack.updateSize(size);
                            } else if (msg.arg1 == 1){
                                int i = msg.arg2;
                                long size = (long) msg.obj;
                                mCallBack.removeSize(i, size);
                            }
                        } else if (msg.what == 0) {
                            int i = msg.arg2;
                            mCallBack.hideItemCb(i);
                        }
                    }
                }
            };
        }

//        initItemLists();
    }

    public ArrayList<CleanItem>[] getItemLists() {
        return this.itemLists;
    }

//    private void initItemLists() {
//        for (int i = 0; i < 6; i++) {
//            if (i == 5) {
//                continue;
//            }
//
//            itemLists[i] = new ArrayList<CleanItem>();
//            try {
//                for (int j = 0; j < CleanManager.PATHS[i].length; j++) {
//                    String path = CleanManager.PATHS[i][j];
//                    itemLists[i].addAll(getItems(path));
//                }
//                //如果程序正在运行，则将该项移除列表
//                if (i == 2) {
//                    List<String> listApp = RunningAppManager.getInstance().getRunnigAppPackages();
//                    for (CleanItem cleanItem : itemLists[i]) {
//                        if (listApp.contains(cleanItem.getName())){
//                            itemLists[i].remove(cleanItem);
//                            EvLog.d(cleanItem.getName() + " isRunning, removed it");
//                        };
//                    }
//                }
//            } catch (Exception e) {
//                EvLog.e("get null items");
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * [获取垃圾文件目录路径下的子文件项]
     *
     * @param path
     * @return
     */
    private ArrayList<CleanItem> getItems(String path) throws Exception {
        ArrayList<CleanItem> itemList = new ArrayList<>();
        if (path == null) {
            return itemList;
        }
        path = CleanManager.getInstance().getRootPath() + File.separator + path;
        File file = new File(path);
        if (!file.isDirectory()) {
            itemList.add(new CleanItem(path, file.getName(), file.length()));
            return itemList;
        }
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return itemList;
        }
        for (File subfile : files) {
            //隐藏文件夹下的apk
            if (subfile.getName().startsWith(".")) {
                File[] hidefiles = subfile.listFiles();
                for (File hidefile : hidefiles) {
                    itemList.add(new CleanItem(hidefile.getAbsolutePath(),
                            hidefile.getName(), FileUtil.getFileSize(hidefile)));
                }
            } else {
                //空文件夹或文件过滤掉
                if (FileUtil.getFileSize(subfile) != 0) {
                    itemList.add(new CleanItem(subfile.getAbsolutePath(), subfile.getName(), FileUtil.getFileSize(subfile)));
                }
            }
        }
        return itemList;
    }


    public Handler getCountHandler() {
        return mCountHandler;
    }

    public String getRootPath() {
        return mRootPath;
    }

    //文件大小变更
    public void sendSizeMessage(long[] mTotalSize) {
        Message msg = new Message();
        msg.what = 1;
        msg.arg1 = 0;
        msg.obj = mTotalSize;
        Handler handler = getCountHandler();
        if (handler != null) {
            handler.sendMessage(msg);
        }
    }

    //将size为0的复选框隐藏
    public void sendHideCbMessage(int i) {
       Message msg = new Message();
        msg.what = 0;
        msg.arg2 = i;
        Handler handler = getCountHandler();
        if (handler != null) {
            handler.sendMessage(msg);
        }
    }

    //删除子项时size变更
    public void sendRemoveSizeMessage(int i, long size) {
        EvLog.d(TAG, "sendRemoveSizeMessage" + "i = " + i +"and size = " + size);
        Message msg = new Message();
        msg.what = 1;
        msg.arg1 = 1;
        msg.arg2 = i;
        msg.obj = size;
        Handler handler = getCountHandler();
        if (handler != null) {
            handler.sendMessage(msg);
        }
    }

    /**
     * [获取ItemVew实例]
     * @return mItemView
     */
    public synchronized ItemView getItemView() {
        if (mItemView == null) {
            mItemView = new ItemView();
        }
        return mItemView;
    }

    public void releaseItemView() {
        if (mItemView != null) {
            mItemView = null;
        }
    }

    //删除文件或文件夹
    public boolean deleteFile(String path) {
        boolean reg = false;
        if (TextUtils.isEmpty(path)) {
            EvLog.e(TAG, "TextUtils.isEmpty(path)");
            return reg;
        }
        if (!FileUtil.isFileExist(path)) {
            EvLog.e(TAG, "!FileUtil.isFileExist(path)");
            return reg;
        }
        if (FileUtil.isDirectory(path)) {
            FileUtil.deleteAllFiles(path);
            return true;
        }
        FileUtil.deleteFile(path);
        return true;
    }

    // 回调注册
    public void setServeCallBack(IServiceCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    // 回调接口
    public interface IServiceCallBack {
        public void removeSize(int i, long size);
        public void updateSize(long[] size);
        public void hideItemCb(int i);
    }
}
