package com.jqorz.soundpassworddetection.frame;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;
import com.jqorz.soundpassworddetection.R;
import com.jqorz.soundpassworddetection.base.BaseEventActivity;
import com.jqorz.soundpassworddetection.constant.MSp;
import com.jqorz.soundpassworddetection.constant.MainEvent;
import com.jqorz.soundpassworddetection.util.Logg;
import com.jqorz.soundpassworddetection.util.ToastUtil;
import com.jqorz.soundpassworddetection.util.UserDataUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.OnClick;

public class App_Speech extends BaseEventActivity {
    private final int MODEL_EXIST = 1;//检查模型是否存在
    private final int MODEL_DELETE = 0;//删除模型
    private final int TYPE_NOT_PASS = 1;//未通过
    private final int TYPE_ERROR = 2;//错误
    private final int TYPE_PASS = 0;//通过
    @BindView(R.id.tv_Tip)
    TextView tv_Tip;
    @BindView(R.id.btn_Speak)
    Button btn_Speak;
    @BindView(R.id.mVoiceLineView)
    VoiceLineView mVoiceLineView;
    // 用HashMap存储听写结果
    private Animation BreathAnimation;//按钮的呼吸动画
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private SpeechRecognizer mIat;//语音听写对象
    private SpeechSynthesizer mTts;//语音合成对象
    private SpeakerVerifier mVerifier;//声纹识别对象
    // 请使用英文字母或者字母和数字的组合，勿使用中文字符
    private String mAuthId = "jq_sound";//用户身份名称
    private String mTextPwd = "芝麻开门";// 文本声纹密码内容
    private int mPwdType = 1;//使用文本密码
    private VerifierListener mRegisterListener = new VerifierListener() {


        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            mVoiceLineView.setVolume(volume * 5);
        }

        @Override
        public void onResult(VerifierResult result) {
            Logg.i(result.source);

            if (result.ret == ErrorCode.SUCCESS) {
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        showPicTip(TYPE_ERROR, "内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
                        showPicTip(TYPE_ERROR, "训练达到最大次数");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        showPicTip(TYPE_ERROR, "出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        showPicTip(TYPE_ERROR, "太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        showPicTip(TYPE_ERROR, "录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        showPicTip(TYPE_ERROR, "训练失败，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        showPicTip(TYPE_ERROR, "音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        showPicTip(TYPE_ERROR, "音频长达不到自由说的要求");
                        break;
                }

                if (result.suc == result.rgn) {

                    showTip("注册成功");
                    UserDataUtil.updateUserData(App_Speech.this, MSp.USER_TEXT_PSD, result.vid);
                    Logg.i("您的文本密码声纹ID：" + result.vid);
                } else {
                    int nowTimes = result.suc + 1;
                    int leftTimes = result.rgn - nowTimes;
                    showTip("请读出：" + mTextPwd + "\n训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
                }

            } else {

                showTip("注册失败，请重新开始。");
            }
        }

        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
        }

        @Override
        public void onError(SpeechError error) {
            showStopView();
            if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
                showPicTip(TYPE_ERROR, "模型已存在，如需重新注册，请先删除");
            } else {
                showTip(error.getPlainDescription(true));
                Logg.e(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            showStopView();
        }

        @Override
        public void onBeginOfSpeech() {
            showListenView();
        }
    };
    private VerifierListener mVerifyListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            mVoiceLineView.setVolume(volume * 5);
        }

        @Override
        public void onResult(VerifierResult result) {
            Logg.e(result.source);

            if (result.ret == 0) {
                // 验证通过
                showTip("验证通过");
                showPicTip(TYPE_PASS, "验证通过");
            } else {
                // 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        showPicTip(TYPE_ERROR, "内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        showPicTip(TYPE_ERROR, "出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        showPicTip(TYPE_ERROR, "太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        showPicTip(TYPE_ERROR, "录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        showPicTip(TYPE_ERROR, "验证不通过，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        showPicTip(TYPE_ERROR, "音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        showPicTip(TYPE_ERROR, "音频长达不到自由说的要求");
                        break;
                    default:
                        showPicTip(TYPE_NOT_PASS, "验证不通过");
                        showTip("验证不通过");
                        break;
                }
            }
        }

        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

        @Override
        public void onError(SpeechError error) {
            showStopView();
            switch (error.getErrorCode()) {
                case ErrorCode.MSP_ERROR_NOT_FOUND:
                    showPicTip(TYPE_ERROR, "模型不存在，请先注册");
                    break;

                default:
                    showPicTip(TYPE_ERROR, error.getPlainDescription(true));
                    Logg.e(error.getPlainDescription(true));
                    break;
            }
        }

        @Override
        public void onEndOfSpeech() {
            showStopView();
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        }

        @Override
        public void onBeginOfSpeech() {
            showListenView();
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
        }
    };
    private SpeechListener mModelOperationListener = new SpeechListener() {

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

            String result = new String(buffer);
            try {
                JSONObject object = new JSONObject(result);
                String cmd = object.getString("cmd");
                int ret = object.getInt("ret");

                if ("del".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        EventBus.getDefault().post(new MainEvent().setArg1(MainEvent.MODEL_DELETE).setArg2(MainEvent.SUCC));
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        EventBus.getDefault().post(new MainEvent().setArg1(MainEvent.MODEL_DELETE).setArg2(MainEvent.FAIL));
                    }
                } else if ("que".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        EventBus.getDefault().post(new MainEvent().setArg1(MainEvent.MODEL_EXIST).setArg2(MainEvent.SUCC));
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        EventBus.getDefault().post(new MainEvent().setArg1(MainEvent.MODEL_EXIST).setArg2(MainEvent.FAIL));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCompleted(SpeechError error) {

            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {//操作失败
                EventBus.getDefault().post(new MainEvent().setArg1(MainEvent.MODEL_EXIST).setArg2(MainEvent.FAIL));
                Logg.e(error.getPlainDescription(true));

            }
        }
    };

    @Override
    protected void init0() {
        BreathAnimation = AnimationUtils.loadAnimation(this, R.anim.breath_animator);
        initVerifier();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MainEvent event) {
        switch (event.getArg1()) {
            case MainEvent.MODEL_DELETE:
                if (event.getArg2() == MainEvent.SUCC) {
                    UserDataUtil.updateUserData(this, MSp.USER_TEXT_PSD, "");
                    showPicTip(TYPE_PASS, "删除成功");
                    if (mVerifier != null)
                        mVerifier.cancel();
                    showTip("点击按钮进行注册");
                } else {
                    showPicTip(TYPE_NOT_PASS, "模型不存在");
                }
                break;
            case MainEvent.MODEL_EXIST:
                if (event.getArg2() == MainEvent.SUCC) {
                    showTip("点击按钮进行身份验证");
                    startVerify();
                } else {
                    startRegister();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                showUserListDialog();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.btn_Speak})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Speak:

                if (mVerifier.isListening()) {
                    mVerifier.cancel();
                    showTip("");
                    showStopView();
                } else {
                    performModelOperation(MODEL_EXIST);
                }

                break;
        }

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.app_speech;
    }

    private void showUserListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String userID = UserDataUtil.loadUserData(this, MSp.USER_TEXT_PSD);
        String[] ss = new String[1];
        if (!TextUtils.isEmpty(userID)) {
            ss[0] = "用户\nID=" + userID;
            builder.setNegativeButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    performModelOperation(MODEL_DELETE);
                }
            });
        } else {
            ss[0] = "无用户";
        }
        builder.setItems(ss, null);
        builder.setPositiveButton("取消", null);
        builder.show();
    }

    /**
     * 初始化SpeakerVerifier
     */
    private void initVerifier() {
        mVerifier = SpeakerVerifier.createVerifier(App_Speech.this, null);
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);

    }

    /**
     * 执行模型操作
     *
     * @param command 操作命令 que-查询模型是否存在 del-删除模型
     */
    private void performModelOperation(int command) {
        String operation;
        if (command == MODEL_DELETE) {
            operation = "del";
        } else if (command == MODEL_EXIST) {
            operation = "que";
        } else {
            return;
        }
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);

        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        // 设置auth_id，不能设置为空
        mVerifier.sendRequest(operation, mAuthId, mModelOperationListener);
    }


    /**
     * 显示开始录音的View
     */
    private void showListenView() {
        btn_Speak.setBackgroundResource(R.drawable.speak_btn_bg_active);
        btn_Speak.startAnimation(BreathAnimation);
        mVoiceLineView.setVisibility(View.VISIBLE);
    }

    /**
     * 显示停止录音的View
     */
    private void showStopView() {
        btn_Speak.setBackgroundResource(R.drawable.speak_btn_bg_gray);
        btn_Speak.clearAnimation();
        mVoiceLineView.setVisibility(View.GONE);
    }

    private void showTip(String text) {
        tv_Tip.setText(text);
    }

    private void showPicTip(int type, String tip) {
        switch (type) {
            case TYPE_PASS:
                ToastUtil.showToast(this, tip, R.drawable.ic_done);
                break;
            case TYPE_NOT_PASS:
                ToastUtil.showToast(this, tip, R.drawable.ic_undone);
                break;
            case TYPE_ERROR:
                ToastUtil.showToast(this, tip, R.drawable.ic_error);

        }
    }

    /**
     * 开始进行注册
     */
    public void startRegister() {
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/test.pcm");
        // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);

        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        showTip("请读出 ：芝麻开门\n训练 第" + 1 + "遍，剩余4遍");
        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
        // 设置业务类型为注册
        mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
        // 设置声纹密码类型
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        // 开始注册
        mVerifier.startListening(mRegisterListener);

    }

    public void startVerify() {
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/verify.pcm");
        mVerifier = SpeakerVerifier.getVerifier();
        // 设置业务类型为验证
        mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
        // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);


        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        showTip("请说:芝麻开门");

        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        // 开始验证
        mVerifier.startListening(mVerifyListener);

    }

    @Override
    protected void onDestroy() {
        if (mTts != null)
            mTts.stopSpeaking();
        ToastUtil.cancelPicToast();
        if (mVerifier != null)
            mVerifier.cancel();
        super.onDestroy();
    }
}
