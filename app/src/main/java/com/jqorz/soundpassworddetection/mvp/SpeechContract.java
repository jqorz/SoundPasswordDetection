package com.jqorz.soundpassworddetection.mvp;

import com.jqorz.soundpassworddetection.base.BasePresenter;
import com.jqorz.soundpassworddetection.base.BaseView;


class SpeechContract {
    public enum RESULT_TYPE {
        TYPE_PASS, TYPE_NOT_PASS, TYPE_ERROR
    }

    interface View extends BaseView<Presenter> {
        void showVoiceVolume(int db);

        void showListenView(boolean start);

        void showTip(String tip);

        void showPicTip(RESULT_TYPE type, String tip);

        void showToast(String tip);


        void showUserListDialog(String userID);

        void switchFlashLight(boolean flag);

        void toCall(String number);

        void toOpenApp(String name);
    }

    interface Presenter extends BasePresenter {
        void onClickSpeakBtn();

        void destroyView();

        void deleteModel();


        void onClickSettingBtn();

    }


}
