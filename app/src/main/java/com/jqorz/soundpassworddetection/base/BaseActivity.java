package com.jqorz.soundpassworddetection.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iflytek.sunflower.FlowerCollector;

import butterknife.ButterKnife;

/**
 * 基类Activity
 */
public abstract class BaseActivity extends AppCompatActivity {


    @Override
    final public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止横屏
        setContentView(getLayoutResId());


        ButterKnife.bind(this);
        ButterKnife.setDebug(false);

        getIntentData();


        init();

    }

    @Override
    protected void onResume() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onPause(this);
        super.onPause();
    }

    protected void getIntentData() {//如果有Intent,在这里进行接收数据
    }


    protected abstract void init();

    protected abstract int getLayoutResId();


}
