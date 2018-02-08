package com.tjoydoor.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.RequiresApi;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;
import com.tjoydoor.Global;
import com.tjoydoor.util.ImageUtil;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.ScreenUtil;
import com.tjoydoor.util.ToastUtil;
import com.tjoydoor.util.threadpool.ServiceThreadPoolManager;
import com.tjoydoor.view.camera.FaceOverlayViewCompare;
import com.tjoydoor.view.camera.FaceResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.thinkjoy.face.imp.FaceSearchListener;
import cn.thinkjoy.face.manage.SearchManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceSearchInfo;
import cn.tjoydoor.R;

/**
 * 手机  多人脸检测识别
 */
@SuppressWarnings("ALL")
public class MainPhone_MutiFaceActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {


    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.overlay)
    FaceOverlayViewCompare overlay;
    @BindView(R.id.ivFace)
    ImageView ivFace;
    private boolean isThreadWorking = false;      //标识人脸位置描绘线程是否正在进行
    private boolean isPosting = true;             //控制是否可以进行上传图片

    private int MAX_FACE = 9;                     //最大人脸检测数量
    private int previewHeight, previewWidth, prevSettingWidth, prevSettingHeight;       //预览高度、宽度   预览设置宽度、高度
    private int mDisplayRotation;                       //屏幕角度
    private int mDisplayOrientation;
    private int Id = 0;
    private int result = 0;
    private int resultNum = 0;
    private int httpCount = 0;
    private int counter = 0;         //用于记录截取的图片数量，计算每秒的帧数
    private long start, end;                      //开始检测时间与结束时间

    private double fps;              //通过start 、end计算fps

    private List<Long> timeS = new ArrayList<>();
    private List<Long> timeE = new ArrayList<>();

    private Bitmap faceCroped = null;      //截取人脸图片
    private FaceResult faces[];     //人脸结果
    private FaceDetector fdet;      //人脸检测类
    private Camera mCamera = null;         //摄像头

    //快速预览data -->bitmap argb565
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    //子线程控制UI线程 控件变化
    private Handler handlerMain = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.arg1) {
                case 1:

                    if (faceCroped != null) {
                        ivFace.setImageBitmap(faceCroped);
                    }

                    if (isPosting) {
                        //控制是否启动图片上传  每启动五条上传线程 等待拿到三个响应后 才继续上传
                        if (++result == 5) {
                            isPosting = false;
                        }

                        timeS.add(System.currentTimeMillis());

                        //人脸识别
                        SearchManage.newInstance(MainPhone_MutiFaceActivity.this).faceSearch(faceCroped, Global.Varibale.faceSetId, 1, new FaceSearchListener() {
                            @Override
                            public void onFaceSearchListener(FaceSearchInfo search, ErrorMsg error) {

                                timeE.add(System.currentTimeMillis());
                                String httpTime = String.valueOf(timeE.get(httpCount) - timeS.get(httpCount));
//                                Logger.i("HTTP请求时间:" + httpTime + "ms  ");
                                Global.Varibale.httpTime = Double.parseDouble(httpTime);
                                httpCount++;

                                if (++resultNum == 5) {
                                    resultNum = 0;
                                    result = 0;
                                    isPosting = true;
                                }

                                if (error.getCode() == 0) {

                                    double confidence = search.getResultFace().get(0).getConfidence();
                                    String name = search.getResultFace().get(0).getPersonId();
                                    Global.Varibale.confidence = confidence;
                                    Global.Varibale.compareName = name;
                                    Logger.i("识别成功  名 字:" + name + "-----相似度：" + confidence);
                                }
                            }
                        });
                    }
                    break;
                case 3:
                    //预览界面画框
                    try {
                        if (faces != null) {
                            overlay.setFaces(faces);

                            //计算FPS
                            end = System.currentTimeMillis();
                            counter++;
                            double time = (double) (end - start) / 1000;
                            if (time != 0) {
                                fps = counter / time;
                            }

                            overlay.setFPS(fps);

                            if (counter == (Integer.MAX_VALUE - 1000))
                                counter = 0;
                        }
                        isThreadWorking = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);//全屏
        ScreenUtil.keepScreenLight(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_detect);
        ButterKnife.bind(this);

        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8(rs));

        faces = new FaceResult[MAX_FACE];

        for (int i = 0; i < MAX_FACE; i++) {
            faces[i] = new FaceResult();
        }

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setFormat(ImageFormat.NV21);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置
    }


    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
    }


    /**
     * 获取摄像头信息打开摄像头
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            if (mCamera == null) {
                mCamera = Camera.open(SPUtil.getInstance(MainPhone_MutiFaceActivity.this).getInt(Global.Const.cameraId));
            }

            Camera.getCameraInfo(SPUtil.getInstance(MainPhone_MutiFaceActivity.this).getInt(Global.Const.cameraId), cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                overlay.setFront(true);
            }

            mCamera.setPreviewDisplay(surfaceView.getHolder());

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showMessage(MainPhone_MutiFaceActivity.this, " Fail to connect to camera service");
        }
    }

    /**
     * 启动摄像头预览
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            //没有预览结果
            if (holder.getSurface() == null) {
                return;
            }
            if (mCamera == null) {
                return;
            }
            mCamera.stopPreview();
            configureCamera(width, height);
            setDisplayOrientation();
            // 初始化人脸检测工具
            float aspect = (float) previewHeight / (float) previewWidth;
            fdet = new FaceDetector(prevSettingWidth, (int) (prevSettingWidth * aspect), MAX_FACE);

            //启动摄像头预览
            startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        if (!isThreadWorking) {
            isThreadWorking = true;

            if (counter == 0) {
                start = System.currentTimeMillis();
            }

            ServiceThreadPoolManager.getInstance().execute(new Runnable() {
                @Override
                public void run() {

                    // TODO: 2017/4/6  1、快速data-->bitmap  
                    if (yuvType == null) {
                        yuvType = new Type.Builder(rs, Element.YUV(rs)).setX(data.length);
                        in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
                        rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(previewWidth).setY(previewHeight);
                        out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
                    }
                    in.copyFrom(data);
                    yuvToRgbIntrinsic.setInput(in);
                    yuvToRgbIntrinsic.forEach(out);
                    Bitmap bitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
                    out.copyTo(bitmap);

                    // TODO: 2017/4/6 2、将图片reSize 和图片矫正   相机拿到人脸的人脸角度矫正
                    float aspect = (float) previewHeight / (float) previewWidth;
                    int w = prevSettingWidth;
                    int h = (int) (prevSettingWidth * aspect);

                    Bitmap bmp = Bitmap.createScaledBitmap(bitmap, w, h, false);
                    bmp = ImageUtil.checkBit(bmp);

                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(SPUtil.getInstance(MainPhone_MutiFaceActivity.this).getInt(Global.Const.cameraId), info);

                    int rotate = mDisplayOrientation - Global.Const.degree;

                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mDisplayRotation % 180 == 0) {
                        if (rotate + 180 > 360) {
                            rotate = rotate - 180;
                        } else {
                            rotate = rotate + 180;
                        }
                    }

                    float xScale = (float) previewWidth / (float) prevSettingWidth;
                    float yScale = (float) previewHeight / (float) h;

                    switch (rotate) {
                        case 90:
                            bmp = ImageUtil.rotate(bmp, 90);
                            xScale = (float) previewHeight / bmp.getWidth();
                            yScale = (float) previewWidth / bmp.getHeight();
                            break;
                        case 180:
                            bmp = ImageUtil.rotate(bmp, 180);
                            break;
                        case 270:
                            bmp = ImageUtil.rotate(bmp, 270);
                            xScale = (float) previewHeight / (float) h;
                            yScale = (float) previewWidth / (float) prevSettingWidth;
                            break;
                    }

                    // TODO: 2017/4/6  3、检测人脸
                    fdet = new FaceDetector(bmp.getWidth(), bmp.getHeight(), MAX_FACE);
                    FaceDetector.Face[] fullResults = new FaceDetector.Face[MAX_FACE];
                    fdet.findFaces(bmp, fullResults);

                    if (!bmp.isRecycled()) {
                        bmp.recycle();
                    }

                    for (int i = 0; i < MAX_FACE; i++) {
                        if (fullResults[i] == null) {
                            faces[i].clear();
                        } else {
                            PointF mid = new PointF();
                            fullResults[i].getMidPoint(mid);

                            mid.x *= xScale;
                            mid.y *= yScale;

                            float eyesDis = fullResults[i].eyesDistance() * xScale;
                            float confidence = fullResults[i].confidence();
                            float pose = fullResults[i].pose(FaceDetector.Face.EULER_Y);//以Y轴定位左右
                            int idFace = Id;

                            Rect rect = new Rect(
                                    (int) (mid.x - eyesDis * 1.50f),
                                    (int) (mid.y - eyesDis * 2.00f),
                                    (int) (mid.x + eyesDis * 1.50f),
                                    (int) (mid.y + eyesDis * 2.00f));

                            /**
                             * Only detect face size > 100x100
                             */
                            if (rect.height() * rect.width() > Global.Const.detectSize * Global.Const.detectSize) {
                                if (idFace == Id) {
                                    Id++;
                                }

                                faces[i].setFace(idFace, mid, eyesDis, confidence, pose, System.currentTimeMillis());
                                faceCroped = ImageUtil.getFace(fullResults[i], bitmap, w, h, rotate);
                                //根据要求，最小边设置在200
                                int min = Math.min(faceCroped.getWidth(), faceCroped.getHeight());
                                double be = 1;
                                if (min > 175) {
                                    be = (double) 175 / min;
                                    faceCroped = ImageUtil.compressSize(faceCroped, be);
                                }

                                //如果截图截到人脸进行和数据库比对
                                if (faceCroped != null) {

                                    //通知上传图片对比
                                    Message msgPost = Message.obtain();
                                    msgPost.arg1 = 1;
                                    handlerMain.sendMessage(msgPost);
                                }
                            }
                        }
                    }
                    //通知画框
                    Message messageSetFace = Message.obtain();
                    messageSetFace.arg1 = 3;
                    handlerMain.sendMessage(messageSetFace);


                    //每条线程走完，把bitmap回收
                    assert bitmap != null;
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            });

        }
    }

    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        Logger.i("设备相机：" + parameters.getSupportedSceneModes());
        setOptimalPreviewSize(parameters, width, height);
        if (SPUtil.getInstance(this).getInt(Global.Const.focusValue, 0) == 0) {
            setAutoFocus(parameters);
        } else {
            parameters.setZoom(SPUtil.getInstance(this).getInt(Global.Const.focusValue, 0));
        }

        mCamera.setParameters(parameters);

    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = ScreenUtil.getOptimalPreviewSize(this, previewSizes, targetRatio);
        previewWidth = previewSize.width;
        previewHeight = previewSize.height;

        /**
         * 计算尺寸全帧位图规模较小的位图检测面临的比例比完整的位图位图具有很高的性能。
         * 较小的图像大小- >检测速度更快,但距离检测面临短,所以计算大小跟随你的目的
         */

        prevSettingWidth = 480;
        prevSettingHeight = 270;

        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
        overlay.setPreviewWidth(previewWidth);
        overlay.setPreviewHeight(previewHeight);
    }

    private void setAutoFocus(Camera.Parameters cameraParameters) {
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
    }

    private void setDisplayOrientation() {
        // Now set the display orientation:
        mDisplayRotation = ScreenUtil.getDisplayRotation(MainPhone_MutiFaceActivity.this);
        mDisplayOrientation = ScreenUtil.getDisplayOrientation(mDisplayRotation, SPUtil.getInstance(MainPhone_MutiFaceActivity.this).getInt(Global.Const.cameraId));

        mCamera.setDisplayOrientation(mDisplayOrientation);

        if (overlay != null) {
            overlay.setDisplayOrientation(mDisplayOrientation);
        }
    }

    private void startPreview() {
        if (mCamera != null) {
            isThreadWorking = false;
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
            counter = 0;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        isPosting = false;
        isThreadWorking = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera!=null){
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
        }
        Global.Varibale.httpTime = 0;
        Global.Varibale.confidence = 0;
        Global.Varibale.compareName = "正在识别中...";

        isPosting = false;
        isThreadWorking = true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handlerMain != null) {
            handlerMain.removeCallbacksAndMessages(null);
        }
        handlerMain = null;
    }
}
