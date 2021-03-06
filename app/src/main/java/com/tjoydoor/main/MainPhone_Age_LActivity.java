package com.tjoydoor.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.tjoydoor.Global;
import com.tjoydoor.util.FileUtil;
import com.tjoydoor.util.ImageUtil;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.ScreenUtil;
import com.tjoydoor.util.ToastUtil;
import com.tjoydoor.util.threadpool.ServiceThreadPoolManager;
import com.tjoydoor.util.timer.CountDownTimer;
import com.tjoydoor.view.MyVideoView;
import com.tjoydoor.view.RoundImageView;
import com.tjoydoor.view.camera.FaceOverlayViewAge;
import com.tjoydoor.view.camera.FaceResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.thinkjoy.face.imp.FaceRegionCheckListener;
import cn.thinkjoy.face.manage.DetectManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceAttribute;
import cn.thinkjoy.face.model.FaceInfo;
import cn.tjoydoor.R;


/**
 * 横屏手机  年龄性别检测
 */

public class MainPhone_Age_LActivity extends Activity implements MediaPlayer.OnCompletionListener, SurfaceHolder.Callback, Camera.PreviewCallback {


    @BindView(R.id.videoView)
    MyVideoView videoView;
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.overlay)
    FaceOverlayViewAge overlay;
    @BindView(R.id.ivFace)
    ImageView ivFace;
    @BindView(R.id.flDetect)
    FrameLayout flDetect;
    @BindView(R.id.ivRotate)
    ImageView ivRotate;
    @BindView(R.id.tvSex)
    TextView tvSex;
    @BindView(R.id.tvBoy)
    TextView tvBoy;
    @BindView(R.id.tvGirl)
    TextView tvGirl;
    @BindView(R.id.tvAge)
    TextView tvAge;
    @BindView(R.id.ivHead)
    RoundImageView ivHead;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvSec)
    TextView tvSec;
    @BindView(R.id.flMsg)
    FrameLayout flMsg;

    private boolean isThreadWorking = false;      //标识人脸位置描绘线程是否正在进行
    private boolean isPosting = true;             //控制是否可以进行上传图片
    private boolean startFaceCheck = true;       //控制多次不停的发送过来的通知2   只启动一次

    private int MAX_FACE = 1;                     //最大人脸检测数量
    private int previewHeight, previewWidth, prevSettingWidth, prevSettingHeight;       //预览高度、宽度   预览设置宽度、高度
    private int mDisplayRotation;                       //屏幕角度
    private int mDisplayOrientation;
    private int Id = 0;
    private int counter = 0;         //用于记录截取的图片数量，计算每秒的帧数
    private int result = 0;
    private int resultNum = 0;
    private int faceNum = 0;
    private int countDown = Global.Const.timeMsgDisplay / 1000;
    private int countJump = 0;
    private int totalAge = 0;
    private double  totalBoy = 0, totalGirl = 0;

    private long start, end;                      //开始检测时间与结束时间
    private double fps;              //通过start 、end计算fps

    private String age = "", sex = "", boy = "", girl = "";

    private Bitmap faceCroped = null;      //截取人脸图片
    private FaceResult faces[];     //人脸结果
    private FaceDetector fdet;      //人脸检测类
    private Camera mCamera = null;         //摄像头

    //快速预览data -->bitmap argb565
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    private List<Integer> faceNumber = new ArrayList<>();               //4S内人脸数量

    private CountDownTimer countDownTimerBack;
    private CountDownTimer countDownTimerFaceCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);
        ScreenUtil.keepScreenLight(this);
        ScreenUtil.hideNavigation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainphone_age_l);
        ButterKnife.bind(this);

        startPlayVideo();  //视频播放配置

        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8(rs));

        faces = new FaceResult[MAX_FACE];

        for (int i = 0; i < MAX_FACE; i++) {
            faces[i] = new FaceResult();
        }

        initCountDownTimerBack();

        initCountDownTimerFaceCheck();

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setFormat(ImageFormat.NV21);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置
    }


    /**
     * 获取摄像头信息打开摄像头
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            if (mCamera == null) {
                mCamera = Camera.open(SPUtil.getInstance(MainPhone_Age_LActivity.this).getInt(Global.Const.cameraId));
            }

            Camera.getCameraInfo(SPUtil.getInstance(MainPhone_Age_LActivity.this).getInt(Global.Const.cameraId), cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                overlay.setFront(true);
            }

            mCamera.setPreviewDisplay(surfaceView.getHolder());

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showMessage(MainPhone_Age_LActivity.this, " Fail to connect to camera service");
        }
    }

    private int width, height;

    /**
     * 启动摄像头预览
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
        try {
            if (holder.getSurface() != null && mCamera != null) {

                mCamera.stopPreview();
                configureCamera(width, height);
                setDisplayOrientation();

                // 初始化人脸检测工具
                float aspect = (float) previewHeight / (float) previewWidth;
                fdet = new FaceDetector(prevSettingWidth, (int) (prevSettingWidth * aspect), MAX_FACE);

                //启动摄像头预览
                startPreview();
            }
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

                    long myStartTime = System.currentTimeMillis();

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

                    // TODO: 2017/4/6 2、开始检测人脸  
                    float aspect = (float) previewHeight / (float) previewWidth;
                    int w = prevSettingWidth;
                    int h = (int) (prevSettingWidth * aspect);

                    Bitmap bmp = Bitmap.createScaledBitmap(bitmap, w, h, false);
                    bmp = ImageUtil.checkBit(bmp);

                    float xScale = (float) previewWidth / (float) prevSettingWidth;
                    float yScale = (float) previewHeight / (float) h;

                    // TODO: 2017/4/6  3、相机拿到人脸的人脸角度矫正
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(SPUtil.getInstance(MainPhone_Age_LActivity.this).getInt(Global.Const.cameraId), info);

                    int rotate = mDisplayOrientation - Global.Const.degree;
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mDisplayRotation % 180 == 0) {
                        if (rotate + 180 > 360) {
                            rotate = rotate - 180;
                        } else {
                            rotate = rotate + 180;
                        }
                    }

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

                    // TODO: 2017/4/6  4、检测人脸
                    fdet = new FaceDetector(bmp.getWidth(), bmp.getHeight(), MAX_FACE);
                    final FaceDetector.Face[] fullResults = new FaceDetector.Face[MAX_FACE];
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
//                            Logger.i("eyesDis = " + eyesDis + " confidence = " + confidence + " pose = " + pose + " idFace = " + idFace);

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
                                    faceNumber.add(faceNum);
//                                    ImageUtil.saveBitmap2file(faceCroped, TimeUtil.getCurrentTime()+".jpg");
                                    //通知跳转界面
                                    Message messageStartDetect = Message.obtain();
                                    messageStartDetect.arg1 = 2;
                                    handlerMain.sendMessage(messageStartDetect);

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
                    messageSetFace.arg1 = 2;
                    handlerMsg.sendMessage(messageSetFace);

                    //只记录一次原图大小
                    if (Global.Varibale.isFirst) {
                        Global.Varibale.isFirst = false;
                        FileUtil.saveDataToFile(MainPhone_Age_LActivity.this, "w = " + bitmap.getWidth() + "  h = " + bitmap.getHeight() + "<br>", Global.FilePath.bmpSize);
                    }

                    //只记录十次android处理时间
                    if (++Global.Varibale.saveAndroidDealTimeCount <= 10) {
                        if (Global.Varibale.isSaveAndroidDealTime) {
                            FileUtil.saveDataToFile(MainPhone_Age_LActivity.this, (System.currentTimeMillis() - myStartTime) + "ms ", Global.FilePath.androidDealTime);
                        }
                    } else {
                        Global.Varibale.isSaveAndroidDealTime = false;
                        Global.Varibale.saveAndroidDealTimeCount = 0;
                    }

                    //每条线程走完，把bitmap回收
                    assert bitmap != null;
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            });
        }
    }

    // TODO: 2017/5/31 0031  1、截取头像上传 2、待机与识别过程界面转换
    private Handler handlerMain = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);

            switch (msg.arg1) {
                case 1:

                    if (faceCroped != null) {
                        ivFace.setImageBitmap(faceCroped);
                    }

                    if (isPosting) {
                        //控制是否启动图片上传  每启动五条上传线程 等待拿到三个响应后 才继续上传
                        if (++result == 3) {
                            isPosting = false;
                        }

                        final long startTime = System.currentTimeMillis();

                        DetectManage.newInstance(MainPhone_Age_LActivity.this).getFaceRegion(faceCroped, true, true, new FaceRegionCheckListener() {
                            @Override
                            public void onFaceRegionCheck(FaceInfo faceInfo, ErrorMsg error) {


                                String httpTime = String.valueOf((System.currentTimeMillis() - startTime));
//                                Logger.i("HTTP请求时间:" + httpTime + "ms  ");
                                Global.Varibale.httpTime = Double.parseDouble(httpTime);

                                if (++Global.Varibale.saveHttpRequestTimeCount <= 10) {
                                    if (Global.Varibale.isSaveHttpRequestTime) {
                                        FileUtil.saveDataToFile(MainPhone_Age_LActivity.this, (httpTime + "ms  "), Global.FilePath.httpTime);
                                    }
                                } else {
                                    Global.Varibale.saveHttpRequestTimeCount = 0;
                                    Global.Varibale.isSaveHttpRequestTime = false;
                                }

                                if (++resultNum == 3) {
                                    resultNum = 0;
                                    result = 0;
                                    isPosting = true;
                                }

                                if (error.getCode() == 0) {

                                    FaceAttribute faceAttribute = faceInfo.getFace().get(0).getAttribute();

                                    Logger.i("年龄 = " + faceAttribute.getAge() +
                                            "\n女性概率 = " + faceAttribute.getGender().get(0) +
                                            "\n男性概率 = " + faceAttribute.getGender().get(1));

                                    Global.Varibale.age = String.valueOf(faceAttribute.getAge());

                                    if (faceAttribute.getGender().get(1) >= faceAttribute.getGender().get(0)) {
                                        Global.Varibale.sex = "男";
                                    } else {
                                        Global.Varibale.sex = "女";
                                    }

                                    totalAge = (int) (totalAge + faceAttribute.getAge());
                                    totalBoy =  (totalBoy + faceAttribute.getGender().get(1));
                                    totalGirl =  (totalGirl + faceAttribute.getGender().get(0));

                                    Logger.i("totalAge = " + totalAge + "\ntotalBoy = " + totalBoy + "\ntotalGirl = " + totalGirl);

                                    if (++countJump >= 5) {
                                        countJump = 0;
                                        stopDetect();

                                        age = totalAge / 5 + "";

                                        if (totalBoy >= totalGirl) {
                                            sex = "男";
                                        } else {
                                            sex = "女";
                                        }

                                        int a = (int) totalBoy;
                                        int b = (int) totalGirl;

                                        boy = (a*100 / 5) + "";
                                        girl = (100-(a*100 / 5))+"";

                                        Logger.i("age = " + age + "\nsex = " + sex + "\nboy = " + boy + "\ngirl = " + girl);

                                        Message msg = Message.obtain();
                                        msg.arg1 = 1;
                                        handlerMsg.sendMessage(msg);

                                    }
                                } else {
                                    Logger.e("年龄检测失败");
                                }
                            }
                        });
                    }
                    break;
                case 2:
                    if (startFaceCheck) {
                        startFaceCheck = false;

                        // TODO: 2017/5/31 0031 进入识别界面入口
                        videoView.setVisibility(View.GONE);
                        overlay.setVisibility(View.VISIBLE);
                        ivFace.setVisibility(View.VISIBLE);

                        countDownTimerFaceCheck.start();
                    }
                    break;
            }
        }
    };

    private Handler handlerMsg = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 1:
                    ivHead.setImageBitmap(faceCroped);
                    tvSex.setText(sex);
                    tvBoy.setText(boy + "% 男");
                    tvGirl.setText(girl + "% 女");
                    tvAge.setText(age);

                    Animation animation = AnimationUtils.loadAnimation(MainPhone_Age_LActivity.this, R.anim.age_left);
                    ivLeft.startAnimation(animation);

                    Animation animation2 = AnimationUtils.loadAnimation(MainPhone_Age_LActivity.this, R.anim.age_right);
                    ivRight.startAnimation(animation2);

                    Animation animation3 = AnimationUtils.loadAnimation(MainPhone_Age_LActivity.this, R.anim.age_roate);
                    ivRotate.startAnimation(animation3);

                    countDownTimerBack.start();

                    flDetect.setVisibility(View.GONE);
                    flMsg.setVisibility(View.VISIBLE);

                    break;
                case 2:
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

                            if (counter == (Integer.MAX_VALUE - 1000)) {
                                counter = 0;
                            }
                        }
                        isThreadWorking = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };


    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        Logger.i("相机支持：" + parameters.getSupportedSceneModes());
        Logger.i("曝光：" + parameters.getExposureCompensation() + "---最大曝光：" + parameters.getMaxExposureCompensation());
        Logger.i("焦距：" + parameters.getZoom() + "---最远焦距：" + parameters.getMaxZoom());

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
        for (int i = 0; i < previewSizes.size(); i++) {
//            Logger.i( previewSizes.get(i).width + "*" + previewSizes.get(i).height);
        }
        float targetRatio = (float) width / height;
        Camera.Size previewSize = ScreenUtil.getOptimalPreviewSize(this, previewSizes, targetRatio);
        previewWidth = previewSize.width;
        previewHeight = previewSize.height;

        /**
         * 计算尺寸全帧位图规模较小的位图检测面临的比例比完整的位图位图具有很高的性能。
         * 较小的图像大小- >检测速度更快,但距离检测面临短,所以计算大小跟随你的目的
         */

        prevSettingWidth = 480;
        prevSettingHeight = 360;

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
        mDisplayRotation = ScreenUtil.getDisplayRotation(MainPhone_Age_LActivity.this);
        mDisplayOrientation = ScreenUtil.getDisplayOrientation(mDisplayRotation, 0);

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
    public void onCompletion(MediaPlayer mediaPlayer) {
        startPlayVideo();
    }

    private void startPlayVideo() {

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.thinkjoy));
        videoView.requestFocus();
        videoView.start();  //开始播放
        videoView.setOnCompletionListener(this); //循环播放
    }


    private void initCountDownTimerBack() {

        countDownTimerBack = new CountDownTimer(Global.Const.timeMsgDisplay, 1000) {

            @Override
            public synchronized void start() {
                super.start();
            }

            @Override
            public synchronized void cancel() {
                super.cancel();
            }

            @Override
            protected void onTick(long millisUntilFinished) {
                super.onTick(millisUntilFinished);
                tvSec.setText((--countDown) + " 秒后重新识别");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startDetect();
                    }
                });
            }
        };
    }

    private void initCountDownTimerFaceCheck() {
        countDownTimerFaceCheck = new CountDownTimer(Global.Const.timeFaceCheck, 1000) {

            @Override
            public synchronized void start() {
                super.start();
            }

            @Override
            public synchronized void cancel() {
                super.cancel();
                Logger.e("关闭检测");
            }

            @Override
            protected void onTick(long millisUntilFinished) {
                super.onTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Logger.i("时间到，启动检测判断");
                if (faceNumber.size() > 3) {
                    faceNumber.clear();
                    countDownTimerFaceCheck.start();
                } else {
                    Id = 0;
                    counter = 0;         //用于记录截取的图片数量，计算每秒的帧数
                    result = 0;
                    resultNum = 0;
                    countJump = 0;
                    totalAge = 0;
                    totalBoy = 0;
                    totalGirl = 0;
                    startFaceCheck = true;

                    age = "";
                    sex = "";
                    boy = "";
                    girl = "";

                    startPlayVideo();
                    videoView.setVisibility(View.VISIBLE);
                    overlay.setVisibility(View.GONE);
                    ivFace.setVisibility(View.GONE);
                }
            }
        };
    }

    private void stopDetect() {

        isThreadWorking = true;
        isPosting = false;

        countDownTimerFaceCheck.cancel();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCamera!=null){
                    mCamera.stopPreview();
                    mCamera.setPreviewCallbackWithBuffer(null);
                    mCamera.setErrorCallback(null);
                    mCamera.release();
                    mCamera = null;
                }
            }
        });
    }

    private void startDetect() {

        isThreadWorking = false;
        isPosting = true;
        startFaceCheck = true;

        countDown = Global.Const.timeMsgDisplay / 1000;
        Id = 0;
        counter = 0;         //用于记录截取的图片数量，计算每秒的帧数
        result = 0;
        resultNum = 0;
        countJump = 0;
        totalAge = 0;
        totalBoy = 0;
        totalGirl = 0;

        age = "";
        sex = "";
        boy = "";
        girl = "";

        faceNumber.clear();

        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            if (mCamera == null) {
                mCamera = Camera.open(SPUtil.getInstance(MainPhone_Age_LActivity.this).getInt(Global.Const.cameraId));
            }

            Camera.getCameraInfo(SPUtil.getInstance(MainPhone_Age_LActivity.this).getInt(Global.Const.cameraId), cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                overlay.setFront(true);
            }

            mCamera.setPreviewDisplay(surfaceView.getHolder());

            if (mCamera != null) {

                mCamera.stopPreview();
                configureCamera(width, height);
                setDisplayOrientation();

                // 初始化人脸检测工具
                float aspect = (float) previewHeight / (float) previewWidth;
                fdet = new FaceDetector(prevSettingWidth, (int) (prevSettingWidth * aspect), MAX_FACE);

                //启动摄像头预览
                startPreview();
            }

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showMessage(MainPhone_Age_LActivity.this, "相机启动失败");
        }

        startPlayVideo();

        videoView.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.GONE);
        ivFace.setVisibility(View.GONE);
        flDetect.setVisibility(View.VISIBLE);
        flMsg.setVisibility(View.GONE);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopDetect();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopDetect();

        Global.Varibale.httpTime = 0;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (handlerMain != null) {
//            handlerMain.removeCallbacksAndMessages(null);
//        }
//        handlerMain = null;
//
//        if (handlerMsg != null) {
//            handlerMsg.removeCallbacksAndMessages(null);
//        }
//        handlerMsg = null;
    }
}
