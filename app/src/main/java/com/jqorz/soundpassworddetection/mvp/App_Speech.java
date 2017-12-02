package com.jqorz.soundpassworddetection.mvp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.jqorz.soundpassworddetection.R;
import com.jqorz.soundpassworddetection.base.BaseActivity;
import com.jqorz.soundpassworddetection.util.CommandUtil;
import com.jqorz.soundpassworddetection.util.ToastUtil;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class App_Speech extends BaseActivity implements SpeechContract.View {

    @BindView(R.id.tv_Tip)
    TextView tv_Tip;
    @BindView(R.id.btn_Speak)
    Button btn_Speak;
    @BindView(R.id.mVoiceLineView)
    VoiceLineView mVoiceLineView;
    @BindView(R.id.iv_Setting)
    ImageView iv_Setting;
    private CommandUtil commandUtil = new CommandUtil();
    private Animation BreathAnimation;//按钮的呼吸动画
    private SpeechContract.Presenter mPresenter;

    @Override
    protected void init() {
        BreathAnimation = AnimationUtils.loadAnimation(this, R.anim.breath_animator);
        new SpeechPresenter(this, this);
    }

    @OnClick({R.id.btn_Speak, R.id.iv_Setting})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Speak:
                mPresenter.onClickSpeakBtn();
                break;
            case R.id.iv_Setting:
                mPresenter.onClickSettingBtn(iv_Setting);
                break;
        }

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.app_speech;
    }

    @Override
    public void showUserListDialog(String userID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] ss = new String[1];
        if (!TextUtils.isEmpty(userID)) {
            ss[0] = "用户\nID=" + userID;
            builder.setNegativeButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPresenter.deleteModel();
                }
            });
        } else {
            ss[0] = "无用户";
        }
        builder.setItems(ss, null);
        builder.setPositiveButton("取消", null);
        builder.show();
    }

    @Override
    public void switchFlashLight(boolean flag) {
        commandUtil.lightSwitch(flag, this);
    }

    @Override
    public void toCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void toOpenApp(String appName) {
        PackageManager packageManager = this.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(appName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);

    }

    @Override
    public void showVoiceVolume(int db) {
        mVoiceLineView.setVolume(db);
    }

    /**
     * 控制View的效果
     */
    @Override
    public void showListenView(boolean start) {
        if (start) {
            btn_Speak.setBackgroundResource(R.drawable.speak_btn_bg_active);
            btn_Speak.startAnimation(BreathAnimation);
            mVoiceLineView.setVisibility(View.VISIBLE);
        } else {
            btn_Speak.setBackgroundResource(R.drawable.speak_btn_bg_gray);
            btn_Speak.clearAnimation();
            mVoiceLineView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showTip(String text) {
        tv_Tip.setText(text);
    }

    @Override
    public void showPicTip(SpeechContract.RESULT_TYPE type, String tip) {
        switch (type) {
            case TYPE_PASS:
                ToastUtil.showToast(this, tip, R.drawable.ic_flag_done);
                break;
            case TYPE_NOT_PASS:
                ToastUtil.showToast(this, tip, R.drawable.ic_flag_undone);
                break;
            case TYPE_ERROR:
                ToastUtil.showToast(this, tip, R.drawable.ic_flag_error);

        }
    }

    @Override
    public void showToast(String tip) {
        ToastUtil.showToast(this, tip);
    }

    @Override
    public void rotateAni(View v) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_animator);
        v.startAnimation(animation);
        mPresenter.onRotateAniEnd(animation);

    }

    @OnLongClick(R.id.iv_Setting)
    public boolean onLong(View v) {
        mPresenter.deleteModel();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroyView();
    }

    @Override
    public void setPresenter(SpeechContract.Presenter presenter) {
        mPresenter = presenter;
        mPresenter.start();
    }
}
