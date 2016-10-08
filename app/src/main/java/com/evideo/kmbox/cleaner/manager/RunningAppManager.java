package com.evideo.kmbox.cleaner.manager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.evideo.kmbox.cleaner.model.RunningAppInfo;
import com.evideo.kmbox.cleaner.util.EvLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunningAppManager {

    private static RunningAppManager sInstance;
    private static final String TAG = "RunningAppManager";
    private PackageManager pm;
    private List<RunningAppInfo> mRunningAppLists;
    private List<String> mRunningAppPackages;
    private Context mContext;

    private RunningAppManager() {
    }

    public static synchronized RunningAppManager getInstance() {
        if (sInstance == null) {
            sInstance = new RunningAppManager();
        }
        return sInstance;
    }

    public List<RunningAppInfo> getRunningAppLists(Context mContext) {
        this.mContext = mContext;
        mRunningAppLists = queryAllRunningAppInfo();
        return mRunningAppLists;
    }

    //获取正在运行的应用程序包名
    public List<String> getRunnigAppPackages() {
        if (mRunningAppPackages == null) {
            mRunningAppPackages = new ArrayList<>();
            for (RunningAppInfo runningAppInfo : mRunningAppLists) {
                mRunningAppPackages.add(runningAppInfo.getPkgName());
            }
        }
        return mRunningAppPackages;
    }

    private List<RunningAppInfo> queryAllRunningAppInfo() {
        pm = mContext.getPackageManager();

        List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(pm));

        Map<String, ActivityManager.RunningAppProcessInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningAppProcessInfo>();

        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
                .getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            int pid = appProcess.pid;
            String processName = appProcess.processName;
            EvLog.i(TAG, "processName: " + processName + "  pid: " + pid);

            String[] pkgNameList = appProcess.pkgList;

            for (int i = 0; i < pkgNameList.length; i++) {
                String pkgName = pkgNameList[i];
                EvLog.i(TAG, "packageName " + pkgName + " at index " + i + " in process " + pid);

                pgkProcessAppMap.put(pkgName, appProcess);
            }
        }

        List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>();

        for (ApplicationInfo app : listAppcations) {

            if (pgkProcessAppMap.containsKey(app.packageName)) {

                int pid = pgkProcessAppMap.get(app.packageName).pid;
                String processName = pgkProcessAppMap.get(app.packageName).processName;
                runningAppInfos.add(getAppInfo(app, pid, processName));
            }
        }

        return runningAppInfos;

    }

    private RunningAppInfo getAppInfo(ApplicationInfo app, int pid, String processName) {
        RunningAppInfo appInfo = new RunningAppInfo();
        appInfo.setAppLabel((String) app.loadLabel(pm));
        appInfo.setAppIcon(app.loadIcon(pm));
        appInfo.setPkgName(app.packageName);

        appInfo.setPid(pid);
        appInfo.setProcessName(processName);

        return appInfo;
    }
}
