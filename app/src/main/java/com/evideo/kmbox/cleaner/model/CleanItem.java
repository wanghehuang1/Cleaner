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

package com.evideo.kmbox.cleaner.model;


public class CleanItem {

    private String mName = "";
    private long mSize = 0;
    private int mState = 1;
    private String path = "";

    public CleanItem() {}

    public CleanItem(String mName) {
        this.mName = mName;
    }

    public CleanItem(String path, String mName, long mSize) {
        this.path = path;
        this.mName = mName;
        this.mSize = mSize;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long mSize) {
        this.mSize = mSize;
    }

    public int getState() {
        return mState;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

}
