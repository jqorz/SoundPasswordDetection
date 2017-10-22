package com.jqorz.soundpassworddetection.constant;

/**
 * Created by jqorz on 2017/7/8.
 * 用于在EventBus中传递信息所需要的实体类
 * 类似线程通信中的Message
 */

public class MainEvent {
    public final static int SUCC = 0;//事件成功
    public final static int FAIL = 1;//事件失败

    public final static int MODEL_DELETE = 0;
    public final static int MODEL_EXIST = 1;


    private int arg1;
    private int arg2;
    private Object what;
    private Object what2;

    public Object getWhat2() {
        return what2;
    }

    public MainEvent setWhat2(Object what2) {
        this.what2 = what2;
        return this;
    }

    public int getArg1() {
        return arg1;
    }

    public MainEvent setArg1(int arg1) {
        this.arg1 = arg1;
        return this;
    }

    public int getArg2() {
        return arg2;
    }

    public MainEvent setArg2(int arg2) {
        this.arg2 = arg2;
        return this;
    }

    public Object getWhat() {
        return what;
    }

    public MainEvent setWhat(Object what) {
        this.what = what;
        return this;
    }


}
