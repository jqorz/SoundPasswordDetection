package com.jqorz.soundpassworddetection.util;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

/**
 * Created by jqorz on 2017/10/18.
 */

public class ToolUtil {
    public static Point calcScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //获得屏幕的宽度
        Point p = new Point();
        p.x = wm.getDefaultDisplay().getWidth();
        p.y = wm.getDefaultDisplay().getHeight();
        return p;
    }

    /**
     * 自定义View所需的dp单位（像素密度）转px单位（像素）
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
