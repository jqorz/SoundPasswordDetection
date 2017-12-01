package com.jqorz.soundpassworddetection.mvp;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;
import com.jqorz.soundpassworddetection.constant.ConsShared;
import com.jqorz.soundpassworddetection.util.CommandUtil;
import com.jqorz.soundpassworddetection.util.JsonParseUtil;
import com.jqorz.soundpassworddetection.util.Logg;
import com.jqorz.soundpassworddetection.util.ToastUtil;
import com.jqorz.soundpassworddetection.util.UserDataUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.jqorz.soundpassworddetection.mvp.SpeechContract.RESULT_TYPE.TYPE_ERROR;
import static com.jqorz.soundpassworddetection.mvp.SpeechContract.RESULT_TYPE.TYPE_NOT_PASS;
import static com.jqorz.soundpassworddetection.mvp.SpeechContract.RESULT_TYPE.TYPE_PASS;


public class SpeechPresenter implements SpeechContract.Presenter {
    private final int MODEL_EXIST = 1;//检查模型是否存在
    private final int MODEL_DELETE = 0;//删除模型
    private SpeechContract.View mView;
    private boolean hasVerified = false;//标记是否验证通过
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private SpeechRecognizer mIat;//语音听写对象
    private SpeechSynthesizer mTts;//语音合成对象
    private SpeakerVerifier mVerifier;//声纹识别对象
    // 请使用英文字母或者字母和数字的组合，勿使用中文字符
    private String mAuthId = "jq_sound";//用户身份名称
    private String mTextPwd = "芝麻开门";// 文本声纹密码内容
    private int mPwdType = 1;//使用文本密码
    private Context mContext;
    private CommandUtil commandUtil = new CommandUtil();
    //语音听写监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            mView.showVoiceVolume(i * 5);
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEndOfSpeech() {
            mView.showListenView(false);
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParseUtil.parseIatResult(results.getResultString());
            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIatResults.put(sn, text);

            StringBuilder resultBuffer = new StringBuilder();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            String cmd = resultBuffer.toString();
            mView.showTip(resultBuffer.toString());
            doCommand(cmd);

        }

        @Override
        public void onError(SpeechError speechError) {
            mView.showToast(speechError.getErrorDescription());

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };
    //注册监听器
    private VerifierListener mRegisterListener = new VerifierListener() {


        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            mView.showVoiceVolume(volume * 5);
        }

        @Override
        public void onResult(VerifierResult result) {
            Logg.i(result.source);

            if (result.ret == ErrorCode.SUCCESS) {
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        mView.showPicTip(TYPE_ERROR, "内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
                        mView.showPicTip(TYPE_ERROR, "训练达到最大次数");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        mView.showPicTip(TYPE_ERROR, "出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        mView.showPicTip(TYPE_ERROR, "太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        mView.showPicTip(TYPE_ERROR, "录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        mView.showPicTip(TYPE_ERROR, "训练失败，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        mView.showPicTip(TYPE_ERROR, "音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        mView.showPicTip(TYPE_ERROR, "音频长达不到自由说的要求");
                        break;
                }

                if (result.suc == result.rgn) {

                    mView.showTip("注册成功");
                    UserDataUtil.updateUserData(mContext, ConsShared.USER_TEXT_PSD, result.vid);
                    Logg.i("您的文本密码声纹ID：" + result.vid);
                } else {
                    int nowTimes = result.suc + 1;
                    int leftTimes = result.rgn - nowTimes;
                    mView.showTip("请读出：" + mTextPwd + "\n训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
                }

            } else {

                mView.showTip("注册失败，请重新开始。");
            }
        }

        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
        }

        @Override
        public void onError(SpeechError error) {
            mView.showListenView(false);
            if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
                mView.showPicTip(TYPE_ERROR, "模型已存在，如需重新注册，请先删除");
            } else {
                mView.showTip(error.getPlainDescription(true));
                Logg.e(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            mView.showListenView(false);
        }

        @Override
        public void onBeginOfSpeech() {
            mView.showListenView(true);
        }
    };
    //验证监听器
    private VerifierListener mVerifyListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            mView.showVoiceVolume(volume * 5);
        }

        @Override
        public void onResult(VerifierResult result) {
            Logg.e(result.source);

            if (result.ret == 0) {
                // 验证通过
                mView.showTip("验证通过");
                mView.showPicTip(TYPE_PASS, "验证通过");
                hasVerified = true;
                startListenCommend();

            } else {
                // 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        mView.showPicTip(TYPE_ERROR, "内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        mView.showPicTip(TYPE_ERROR, "出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        mView.showPicTip(TYPE_ERROR, "太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        mView.showPicTip(TYPE_ERROR, "录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        mView.showPicTip(TYPE_ERROR, "验证不通过，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        mView.showPicTip(TYPE_ERROR, "音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        mView.showPicTip(TYPE_ERROR, "音频长达不到自由说的要求");
                        break;
                    default:
                        mView.showPicTip(TYPE_NOT_PASS, "验证不通过");
                        mView.showTip("验证不通过");
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
            mView.showListenView(false);
            switch (error.getErrorCode()) {
                case ErrorCode.MSP_ERROR_NOT_FOUND:
                    mView.showPicTip(TYPE_ERROR, "模型不存在，请先注册");
                    break;

                default:
                    mView.showPicTip(TYPE_ERROR, error.getPlainDescription(true));
                    Logg.e(error.getPlainDescription(true));
                    break;
            }
        }

        @Override
        public void onEndOfSpeech() {
            mView.showListenView(false);
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        }

        @Override
        public void onBeginOfSpeech() {
            mView.showListenView(true);
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
        }
    };
    //模型操作监听器
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
                Logg.e("object=" + object.toString());
                if ("del".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        UserDataUtil.updateUserData(mContext, ConsShared.USER_TEXT_PSD, "");
                        mView.showPicTip(TYPE_PASS, "删除成功");
                        if (mVerifier != null)
                            mVerifier.cancel();
                        mView.showTip("点击按钮进行注册");
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        mView.showPicTip(TYPE_NOT_PASS, "模型不存在");
                    }
                } else if ("que".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        if (hasVerified) {
                            startListenCommend();
                        } else {
                            mView.showTip("点击按钮进行身份验证");
                            startVerify();
                        }
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        startRegister();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCompleted(SpeechError error) {

            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {//操作失败
                Logg.e("操作失败", error.getPlainDescription(true));
                if (error.getErrorCode() == ErrorCode.MSP_ERROR_NOT_FOUND) {//错误码为模型不存在则进行注册
                    startRegister();
                }

            }
        }
    };

    SpeechPresenter(SpeechContract.View view, Context context) {
        mView = view;
        mContext = context;
        view.setPresenter(this);
    }

    private void doCommand(String cmd) {
        switch (cmd) {
            case "打开手电筒":
            case "打开闪光灯":
                mView.switchFlashLight(true);
                break;
            case "关闭闪光灯":
            case "关闭手电筒":
                mView.switchFlashLight(false);
                break;
        }
    }


    @Override
    public void start() {
        initVerifier();
        initParameter();
        initTTS();
    }


    private void initTTS() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(mContext, null);
        //设置参数
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        String voicer = "xiaoyan";
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);

        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");


    }

    /**
     * 初始化SpeakerVerifier
     */
    private void initVerifier() {
        mVerifier = SpeakerVerifier.createVerifier(mContext, null);
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);

    }

    private void initParameter() {
        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        mIat = SpeechRecognizer.createRecognizer(mContext, null);
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        // 简体中文:"zh_cn", 美式英文:"en_us"
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //普通话：mandarin(默认)
        //粤 语：cantonese
        //四川话：lmz
        //河南话：henanese
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        //设置是否带标点符号 0表示不带标点，1则表示带标点。
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理1000~10000
        mIat.setParameter(SpeechConstant.VAD_BOS, "5000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音0~10000
        mIat.setParameter(SpeechConstant.VAD_EOS, "1800");
        mIat.setParameter(SpeechConstant.VAD_ENABLE, "1");
    }

    /**
     * 开始进行注册
     */
    private void startRegister() {
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/test.pcm");
        // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);

        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        mView.showTip("请读出 ：芝麻开门\n训练 第" + 1 + "遍，剩余4遍");
        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
        // 设置业务类型为注册
        mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
        // 设置声纹密码类型
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        // 开始注册
        mVerifier.startListening(mRegisterListener);

    }

    private void startVerify() {
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
        mView.showTip("请说:芝麻开门");

        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        // 开始验证
        mVerifier.startListening(mVerifyListener);

    }

    private void startListenCommend() {
        mView.showTip("你好，你想让我做什么呢");
        mTts.startSpeaking("你好，你想让我做什么呢", new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {

            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {

            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {

            }

            @Override
            public void onCompleted(SpeechError speechError) {
                mIatResults.clear();
                mIat.startListening(mRecognizerListener);
                mView.showListenView(true);
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });


    }

    /**
     * 执行模型操作
     *
     * @param command 操作命令 que-查询模型是否存在 del-删除模型
     */
    private void performModelOperation(int command) {
        Logg.e("cmd=" + command);
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

    @Override
    public void destroyView() {
        if (mTts != null)
            mTts.stopSpeaking();
        ToastUtil.cancelPicToast();
        if (mVerifier != null)
            mVerifier.cancel();
    }

    @Override
    public void deleteModel() {
        performModelOperation(MODEL_DELETE);
    }

    @Override
    public void onRotateAniEnd(Animation animation) {
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String userID = UserDataUtil.loadUserData(mContext, ConsShared.USER_TEXT_PSD);
                mView.showUserListDialog(userID);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onClickSettingBtn(View v) {
        mView.rotateAni(v);
    }


    @Override
    public void onClickSpeakBtn() {
        if (mVerifier.isListening()) {
            mVerifier.cancel();
            mView.showTip("");
            mView.showListenView(false);
        } else {
            performModelOperation(MODEL_EXIST);
        }
    }
}
