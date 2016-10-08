package com.evideo.kmbox.cleaner.view;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.evideo.kmbox.cleaner.R;
import com.evideo.kmbox.cleaner.adapter.ItemAdapter;
import com.evideo.kmbox.cleaner.manager.CleanManager;
import com.evideo.kmbox.cleaner.manager.RunningAppManager;
import com.evideo.kmbox.cleaner.model.CleanItem;
import com.evideo.kmbox.cleaner.model.RunningAppInfo;
import com.evideo.kmbox.cleaner.util.EvLog;
import com.evideo.kmbox.cleaner.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ItemView implements AdapterView.OnItemClickListener {

    private static ArrayList<CleanItem>[] itemLists = new ArrayList[6];

    private View addview = null;
    private ListView listView = null;
    private Context mContext;
    private int itemId = 0;

    public ItemView() {
//        itemLists = CleanManager.getInstance().getItemLists();
    }

    //初始化子项文件列表
    public void init() {
        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                continue;
            }

            itemLists[i] = new ArrayList<CleanItem>();
            try {
                for (int j = 0; j < CleanManager.PATHS[i].length; j++) {
                    String path = CleanManager.PATHS[i][j];
                    //如果程序正在运行，则将该项移除列表
                    if (i == 2) {
                        List<String> listApp = RunningAppManager.getInstance().getRunnigAppPackages();
                        for (CleanItem cleanItem : getItems(path)) {
                            if (!listApp.contains(cleanItem.getName())) {
                                itemLists[i].add(cleanItem);
                                EvLog.d(cleanItem.getName() + " isNotRunning");
                            } else {
                                EvLog.d(cleanItem.getName() + " isRunning");
                                CleanManager.getInstance().sendRemoveSizeMessage(i, cleanItem.getSize());
                            }
//                            for (int k=0; k<listApp.size(); k++) {
//                                if (listApp.get(k).equals(itemLists[i].get(j).getName())){
//                                    EvLog.d(itemLists[i].get(j).getName() + " isRunning, removed it");
//                                    CleanManager.getInstance().sendRemoveSizeMessage(i, itemLists[i].get(j).getSize());
//                                    itemLists[i].remove(j);
//                                }
//                            }
                        }
                    }else {
                        itemLists[i].addAll(getItems(path));
                    }

                }
            } catch (Exception e) {
                EvLog.e("get null items");
                e.printStackTrace();
            }
        }
    }


    public void show(Context mContext, int i, View view) {

        this.mContext = mContext;
        this.itemId = i;

        ItemAdapter itemAdapter = new ItemAdapter(mContext, itemLists[i], i);

        if (addview == null || listView == null) {
            addview = View.inflate(mContext, R.layout.item_lv, null);
            listView = (ListView) addview.findViewById(R.id.item_item_lv);
        }

        listView.setAdapter(itemAdapter);
//        itemAdapter.notifyDataSetChanged();
        itemAdapter.notifyDataSetInvalidated();

        listView.setOnItemClickListener(this);
        addPopView(addview, view);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addPopView(View addView, View rootView) {
        final PopupWindow popupWindow = new PopupWindow(addView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(
                R.color.colorPrimary, null));

        // 设置好参数之后再show
        popupWindow.showAsDropDown(rootView);
    }

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
//        if (!file.isDirectory()) {
//            itemList.add(new CleanItem(path, file.getName(), file.length()));
//            return itemList;
//        }
        File[] files = file.listFiles();
        if (files == null) {
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

    /**
     * [更新部分子项state]
     *
     * @param i
     * @param itemList
     */
    public void update(int i, ArrayList<CleanItem> itemList) {
        itemLists[i] = itemList;
    }

    /**
     * [更新全部子项state]
     *
     * @param i
     * @param state
     */
    public void update(int i, int state) {
        for (CleanItem cleanItem : itemLists[i]) {
            cleanItem.setState(state);
        }
    }

    /**
     * [删除文件子项]
     *
     * @param i
     */
    public void remove(int i) {
        EvLog.d("i = " + i);
        if (itemLists[i] == null || itemLists[i].size() == 0) {
            EvLog.e("null array" + i);
            return;
        }

        int len = itemLists[i].size();
        EvLog.i("itemlist[" + i + "]len = " + len);
        for (int k = len - 1; k >= 0; k--) {
            if (itemLists[i].get(k).getState() == 1) {
                long size = itemLists[i].get(k).getSize();
                if (CleanManager.getInstance().deleteFile(itemLists[i].get(k).getPath())) {
                    itemLists[i].remove(k);
                    EvLog.i("----" + i + "-----" + k);
                    CleanManager.getInstance().sendRemoveSizeMessage(i, size);
                } else {
                    EvLog.e("delete file failed: " + itemLists[i].get(k).getPath());
                }
            } else {
                continue;
            }
        }

        //某一项全部删除的情况下隐藏勾选框
        if (itemLists[i] == null || itemLists[i].size() == 0) {
            CleanManager.getInstance().sendHideCbMessage(i);
            return;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String path = itemLists[itemId].get(i).getPath();
        TextView ttv = (TextView) view.findViewById(R.id.item_item_name_tv);
        ttv.setText("path: " + path);
    }
}
