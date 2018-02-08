package com.tjoydoor.main;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ListView;

import com.orhanobut.logger.Logger;
import com.tjoydoor.Global;
import com.tjoydoor.adapter.BitmapListAdapter;
import com.tjoydoor.netokhttp.NetOkhttp;
import com.tjoydoor.util.ImageUtil;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.ScreenUtil;
import com.tjoydoor.util.ToastUtil;
import com.tjoydoor.util.threadpool.ServiceThreadPoolManager;
import com.tjoydoor.view.camera.FaceFPSOverlayView;
import com.tjoydoor.view.camera.FaceResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.thinkjoy.face.imp.FaceAddListener;
import cn.thinkjoy.face.manage.FaceManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceAddInfo;
import cn.tjoydoor.R;


@SuppressWarnings("ALL")
public class AddFaceDymicActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {


    @BindView(R.id.surfaceview)
    SurfaceView surfaceview;
    @BindView(R.id.faceoverlayview)
    FaceFPSOverlayView faceoverlayview;
    @BindView(R.id.lv_face)
    ListView lvFace;

    private int prevSettingWidth, prevSettingHeight;    //预览设置宽度、高度
    private long start, end;  //开始检测时间与结束时间
    private boolean isThreadWorking = false;      //标识人脸位置描绘线程是否正在进行
    private int MAX_FACE = 1;                     //最大人脸检测数量
    private int previewHeight, previewWidth;       //预览高度、宽度
    private int mDisplayRotation;                       //屏幕角度
    private int mDisplayOrientation;
    private int Id = 0;
    private Bitmap faceCroped = null;      //截取人脸图片
    private FaceResult faces[];     //人脸结果
    private FaceDetector fdet;      //人脸检测类
    private Thread detectThread;    //区域检测描绘线程
    private Camera mCamera = null;         //摄像头    //快速预览data -->bitmap argb565
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    private double fps;
    private int counter = 0;   //用于记录截取的图片数量，计算每秒的帧数

    //图片添加
    private List<Bitmap> bitmapList = new ArrayList<>();
    private BitmapListAdapter bmpAdapter;
    private boolean isAdd = true;  //控制是否获取一张人脸图片并上传
    private int addCount = 0;
    private long addTime = 0;
    private int facecount = 0;

    //子线程控制  UI线程控件变化
    private Handler handlermain = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int message = msg.arg1;

            if (message == 1) {
                if (System.currentTimeMillis() - addTime > 2000) {
                    addTime = System.currentTimeMillis();
                    bitmapList.add(faceCroped);
                    bmpAdapter.setData(bitmapList);
                    FaceManage.newInstance(AddFaceDymicActivity.this).addFace(bitmapList.get(bitmapList.size() - 1), Global.Varibale.faceSetId, Global.Varibale.dymicPersonId, new FaceAddListener() {
                        @Override
                        public void onFaceAddListener(FaceAddInfo faceAddInfo, ErrorMsg error) {
                            if (error.getCode() == 0) {
                                Logger.i("添加成功！");
                                addCount++;
                                NetOkhttp.refreshFaceSet(AddFaceDymicActivity.this);
                            } else {
                                ToastUtil.showMessage(AddFaceDymicActivity.this, error.getMsg());
                            }
                        }
                    });
                }
                if (addCount >= 4) {
                    ToastUtil.showMessage(AddFaceDymicActivity.this, "添加完毕！");
                    isAdd = false;
                    isThreadWorking = true;
                    finish();
                }
            } else if (message == 3) {
                try {
                    if (faces != null) {
                        faceoverlayview.setFaces(faces);

                        //计算FPS
                        end = System.currentTimeMillis();
                        counter++;
                        double time = (double) (end - start) / 1000;
                        if (time != 0)
                            fps = counter / time;

                        faceoverlayview.setFPS(fps);

                        if (counter == (Integer.MAX_VALUE - 1000))
                            counter = 0;

                    }
                    isThreadWorking = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Global.Varibale.isBigText) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        ScreenUtil.setFullScreen(this);//全屏
        ScreenUtil.hideNavigation(this);
        ScreenUtil.keepScreenLight(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face_dymic);
        ButterKnife.bind(this);

        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8(rs));

        faces = new FaceResult[MAX_FACE];

        for (int i = 0; i < MAX_FACE; i++) {
            faces[i] = new FaceResult();
        }

        bmpAdapter = new BitmapListAdapter(this);
        bmpAdapter.setData(bitmapList);
        lvFace.setAdapter(bmpAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
//        Logger.i("onPostCreate");
        super.onPostCreate(savedInstanceState);
        SurfaceHolder holder = surfaceview.getHolder();
        holder.addCallback(this);
        holder.setFormat(ImageFormat.NV21);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置
    }

    @Override
    protected void onResume() {
//        Logger.i("onResume");
        super.onResume();
        startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        Logger.i("surfaceCreated");
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            if (mCamera == null) {
                mCamera = Camera.open(SPUtil.getInstance(this).getInt(Global.Const.cameraId));
            }

            Camera.getCameraInfo(SPUtil.getInstance(this).getInt(Global.Const.cameraId), cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                faceoverlayview.setFront(true);
            }

            mCamera.setPreviewDisplay(surfaceview.getHolder());

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showMessage(AddFaceDymicActivity.this," Fail to connect to camera service");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Logger.i("surfaceChanged");
        try {
            //没有预览结果
            if (holder.getSurface() == null) {
                Logger.e("没有预览结果");
                return;
            }
            if (mCamera == null) {
                Logger.e("mCamera 为空");
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
    public void surfaceDestroyed(SurfaceHolder holder) {
//        Logger.i("surfaceDestroyed");
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
//        Logger.i("未经压缩的大小:" + FileUtil.formetFileSize(data.length));
        if (!isThreadWorking) {
            if (counter == 0) {
                start = System.currentTimeMillis();
            }
            isThreadWorking = true;
            ServiceThreadPoolManager.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    /**
                     * 快速data-->bitmap
                     */
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

                    float aspect = (float) previewHeight / (float) previewWidth;
                    int w = prevSettingWidth;
                    int h = (int) (prevSettingWidth * aspect);

                    Bitmap bmp = Bitmap.createScaledBitmap(bitmap, w, h, false);
                    bmp = checkBit(bmp);

                    float xScale = (float) previewWidth / (float) prevSettingWidth;
                    float yScale = (float) previewHeight / (float) h;

                    //相机拿到人脸的人脸角度矫正
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(SPUtil.getInstance(AddFaceDymicActivity.this).getInt(Global.Const.cameraId), info);
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

                    //检测人脸
                    fdet = new FaceDetector(bmp.getWidth(), bmp.getHeight(), MAX_FACE);
                    final FaceDetector.Face[] fullResults = new FaceDetector.Face[MAX_FACE];
                    fdet.findFaces(bmp, fullResults);

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
                                    facecount++;
                                    if (facecount >= 2) {
                                        if (isAdd) {
                                            Message msgFaceCroped = Message.obtain();
                                            msgFaceCroped.arg1 = 1;
                                            handlermain.sendMessage(msgFaceCroped);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Message messageSetFace = Message.obtain();
                    messageSetFace.arg1 = 3;
                    handlermain.sendMessage(messageSetFace);
                }
            });
        }
    }


    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
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
        faceoverlayview.setPreviewWidth(previewWidth);
        faceoverlayview.setPreviewHeight(previewHeight);
    }

    private void setAutoFocus(Camera.Parameters cameraParameters) {
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
    }

    private void setDisplayOrientation() {
        // Now set the display orientation:
        mDisplayRotation = ScreenUtil.getDisplayRotation(AddFaceDymicActivity.this);
        mDisplayOrientation = ScreenUtil.getDisplayOrientation(mDisplayRotation, 0);

        mCamera.setDisplayOrientation(mDisplayOrientation);

        if (faceoverlayview != null) {
            faceoverlayview.setDisplayOrientation(mDisplayOrientation);
        }
    }

    private void startPreview() {
        if (mCamera != null) {
            isThreadWorking = false;
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        }
    }

    /**
     * 检测图片格式是否符合本地检测，不符合自动转换
     */
    private Bitmap checkBit(Bitmap bitmap) {
        Bitmap bit = bitmap;
        if (bitmap.getConfig() != Bitmap.Config.RGB_565) {
            bit = bitmap.copy(Bitmap.Config.RGB_565, true);
        }

        if (bitmap.getWidth() % 2 != 0) {
            bit = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() - 1, bitmap.getHeight(), true);
        }
        return bit;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isAdd = false;
        isThreadWorking = true;
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAdd = false;
        isThreadWorking = true;
        bitmapList = null;

        if (mCamera!=null){
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
}
