package com.jqorz.soundpassworddetection.util;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jqorz.soundpassworddetection.R;

/**
 * Toast工具类
 */
public class ToastUtil {
    /**
     * Toast方法
     *
     * @param text 需要展示的文本
     * @param context  所需上下文
     */

    private static Toast mToast = null;
    private static Toast mPicToast = null;

    public static void showToast(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showToast(Context context) {
        String text = "点击";
        showToast(context, text);
    }

    public static void showToast(Context context, String text, @DrawableRes int picId) {

        View v = LayoutInflater.from(context).inflate(R.layout.tip_dialog, null);

        mPicToast = new Toast(context);
        mPicToast.setGravity(Gravity.CENTER, 0, 0);
        //设置Tosat的属性，如显示时间
        mPicToast.setDuration(Toast.LENGTH_SHORT);
        ImageView iv = (ImageView) v.findViewById(R.id.iv_Tip);
        TextView tv = (TextView) v.findViewById(R.id.tv_Tip);
        tv.setWidth(ToolUtil.dp2px(context, 200));
        tv.setText(text);
        iv.setImageResource(picId);
        mPicToast.setView(v);

        //显示提示
        mPicToast.show();
    }

    public static void cancelPicToast() {
        if (mPicToast != null)
            mPicToast.cancel();
    }
}
