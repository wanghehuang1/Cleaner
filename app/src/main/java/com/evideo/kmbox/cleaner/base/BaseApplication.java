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

package com.evideo.kmbox.cleaner.base;


import android.app.Application;

public class BaseApplication extends Application {

    private static BaseApplication sInstance;

    public static BaseApplication getInstance() {
        return sInstance;
    }
}
