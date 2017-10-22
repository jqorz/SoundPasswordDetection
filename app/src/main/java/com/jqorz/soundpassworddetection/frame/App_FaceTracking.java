package com.jqorz.soundpassworddetection.frame;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.faceplusplus.api.FaceDetecter;
import com.jqorz.soundpassworddetection.R;
import com.jqorz.soundpassworddetection.base.BaseActivity;
import com.jqorz.soundpassworddetection.constant.Global;
import com.jqorz.soundpassworddetection.util.Logg;
import com.jqorz.soundpassworddetection.util.ToastUtil;
import com.jqorz.soundpassworddetection.widget.FaceMask;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;


public class App_FaceTracking extends BaseActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    @BindView(R.id.sv_preview)
    SurfaceView svPreview;
    @BindView(R.id.fm_mask)
    FaceMask fmMask;
    private Camera mCamera;
    private HandlerThread handleThread;
    private Handler detectHandler;
    private SurfaceHolder holder;
    private int width = 640;
    private int height = 640;
    private FaceDetecter facedetecter;
    private boolean has = false;//是否已经检测到人脸

    @Override
    protected void init() {
        initParams();

    }

    private void initParams() {
        handleThread = new HandlerThread("dt");
        handleThread.start();
        detectHandler = new Handler(handleThread.getLooper());
        holder = svPreview.getHolder();
        holder.addCallback(this);
        svPreview.setKeepScreenOn(true);

        facedetecter = new FaceDetecter();
        facedetecter.init(this, Global.FACEPP_KEY);
        facedetecter.setHighAccuracy(true);//设置为高灵敏
        facedetecter.setTrackingMode(true);//设置为跟踪模式

    }

    /**
     * 获取最合适的预览尺寸
     *
     * @param parameters       相机参数实体
     * @param screenResolution 希望匹配的尺寸
     * @return 得到匹配结果
     */
    private Point getBestCameraResolution(Camera.Parameters parameters, Point screenResolution) {
        float tmp;
        float mindiff = 100f;
        float x_d_y = (float) screenResolution.x / (float) screenResolution.y;
        Camera.Size best = parameters.getPreviewSize();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size s : supportedPreviewSizes) {
            tmp = Math.abs(((float) s.height / (float) s.width) - x_d_y);
            if (tmp < mindiff) {
                mindiff = tmp;
                best = s;
            }
        }
        return new Point(best.width, best.height);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.app_face_tracking;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            //设置参数 1表示前置摄像头
            mCamera = Camera.open(1);
            //摄像头画面显示在Surface上
            mCamera.setPreviewDisplay(holder);
            Camera.Parameters parameters = mCamera.getParameters();
            Point p1 = new Point(240, 240);//SurfaceView尺寸
            Point p2 = getBestCameraResolution(parameters, p1);
            Logg.i("x=" + p2.x + ",y=" + p2.y);
            parameters.setPreviewSize(p2.x, p2.y);
            mCamera.setParameters(parameters);
        } catch (IOException e) {
            if (mCamera != null) mCamera.release();
            mCamera = null;
        }

        if (mCamera == null) {
            finish();
            return;
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        try {
            //摄像头画面显示在Surface上
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            if (mCamera != null) mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        camera.setPreviewCallback(null);
        if (mCamera == null) return;
        detectHandler.post(new Runnable() {
            @Override
            public void run() {
                int is = 0;

                byte[] ori = new byte[width * height];
                for (int x = width - 1; x >= 0; x--) {
                    for (int y = height - 1; y >= 0; y--) {
                        ori[is++] = data[y * width + x];//将捕捉到的图像二维数组转为一维数组
                    }
                }
                //调用findFaces方法得到所有识别到的人脸
                final FaceDetecter.Face[] faceinfo = facedetecter.findFaces(ori, height, width);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fmMask.setFaceInfo(faceinfo);
                        if (!has && faceinfo != null && faceinfo.length > 0) {
                            has = true;
                            ToastUtil.showToast(App_FaceTracking.this, "识别到人脸数据");
                        }

                        if (has && detectHandler != null) {
                            detectHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    App_FaceTracking.this.finish();
                                    startActivity(new Intent(App_FaceTracking.this, App_Speech.class));
                                }
                            }, 1000);

                        }
                    }
                });
                try {
                    camera.setPreviewCallback(App_FaceTracking.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (facedetecter != null)
            facedetecter.release(this);
        if (handleThread != null)
            handleThread.quit();
        if (detectHandler != null)
            detectHandler = null;
    }

}
