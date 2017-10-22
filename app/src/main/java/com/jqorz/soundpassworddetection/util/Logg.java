package com.jqorz.soundpassworddetection.util;

import android.util.Log;

/**
 * Log管理的工具类
 */
public class Logg {

    /**
     * 对日志进行管理
     * 在DeBug模式开启，其它模式关闭
     */


    /**
     * 是否开启debug
     */
    public static boolean isDebug = true;


    /**
     * 错误
     */
    public static void e(String tag, Object msg) {
        Log.e(tag, msg.toString());
    }

    public static void e(Object msg) {
        Log.e("-----------------------", msg.toString() + "-----------------------");
    }

    /**
     * 信息
     */
    public static void i(String tag, Object msg) {
        if (isDebug) {
            Log.i(tag, msg.toString());
        }
    }

    public static void i(Object msg) {
        if (isDebug) {
            Log.i("-----------------------", msg.toString() + "-----------------------");
        }
    }

    /**
     * 警告
     */
    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, msg + "");
        }
    }


}
