package com.tjoydoor.main;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;

import com.orhanobut.logger.Logger;
import com.tjoydoor.Global;
import com.tjoydoor.entity.TJoyDoorLog;
import com.tjoydoor.util.DialogUtil;
import com.tjoydoor.util.FileUtil;
import com.tjoydoor.util.SPUtil;
import com.tjoydoor.util.ScreenUtil;
import com.tjoydoor.util.TimeUtil;
import com.tjoydoor.util.ToastUtil;

import java.util.List;

import cn.thinkjoy.face.imp.FaceSetListener;
import cn.thinkjoy.face.imp.FaceSetQueryListener;
import cn.thinkjoy.face.imp.UserBindListener;
import cn.thinkjoy.face.manage.SetManage;
import cn.thinkjoy.face.manage.UserManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceSet;
import cn.tjoydoor.R;


@SuppressWarnings("ALL")
public class WelcomeActivity extends Activity {

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);//全屏
        ScreenUtil.keepScreenLight(this);
        ScreenUtil.hideNavigation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Logger.i(ScreenUtil.getWidthInPx(this) + "*" + ScreenUtil.getHeightInPx(this));

        //上传跟踪使用数据
//        postTimeLog();

        //上传错误日志
//        if (!TextUtil.isEmpty(CarshHandlerUtil.getLogContent())) {
//            CarshHandlerUtil.postError(CarshHandlerUtil.getLogContent());
//        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //绑定用户并创建人脸集合
                bindUser();

                //android 6.0以上包括6.0
                if (Build.VERSION.SDK_INT >= 23) {
                    CheckPermissions();
                } else {
                    //小于6.0直接登陆进去
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                }
            }
        }, 3000);
    }

    /**
     * 权限检测回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Global.Const.PermissionCallBack) {

            if (grantResults.length != 0) {
                int n = 0;
                for (int grantResult : grantResults) {
                    if (grantResult == PermissionChecker.PERMISSION_GRANTED) {
                        n++;
                    }
                }
                if (n != grantResults.length) {
                    ToastUtil.showMessage(WelcomeActivity.this, "允许权限后应用才能运行！");
                    CheckPermissions();
                    return;
                }

                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();

            } else {
                ToastUtil.showMessage(WelcomeActivity.this, "允许权限后应用才能运行！");
                CheckPermissions();
            }
        }
    }

    /**
     * 设置回调
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Global.Const.PermissionSetting) {
            CheckPermissions();
        }
    }

    /**
     * 权限检测
     */
    public void CheckPermissions() {
        //检测权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!isFirst) {
                //检测权限是否拒绝过
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    DialogUtil.showYN(this, "您已拒绝权限，请手动打开设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToastUtil.showMessage(WelcomeActivity.this, "当前无权限，请授权！");
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", WelcomeActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, Global.Const.PermissionSetting);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
                    return;
                }
            }
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, Global.Const.PermissionCallBack);
            isFirst = false;
        } else {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }
    }

    private void bindUser() {

        UserManage.newInstance(this).bindUser(Global.Const.userId, new UserBindListener() {
            @Override
            public void onUserBind(String userId, ErrorMsg error) {
                if (error.getCode() == 0) {
                    ToastUtil.showMessage(WelcomeActivity.this, Global.Const.userId + "用户绑定成功");
                    createSet();
                } else {
                    ToastUtil.showMessage(WelcomeActivity.this, error.getMsg());
                }
            }
        });
    }

    private void createSet() {

        SetManage.newInstance(this).creatFaceSet(Global.Const.faceSetName, "", "", new FaceSetListener() {
            @Override
            public void onFaceSetListener(final FaceSet set, ErrorMsg error) {
                if (error.getCode() == 0) {
//                    Logger.i("facesetID = " + set.getFacesetId());
                    Global.Varibale.faceSetId = set.getFacesetId();
                } else {
                    SetManage.newInstance(WelcomeActivity.this).getFaceSet("", 0, new FaceSetQueryListener() {
                        @Override
                        public void onFaceSetQueryListener(List<FaceSet> sets, ErrorMsg msg) {
                            if (msg.getCode() == 0) {
                                for (int i = 0; i < sets.size(); i++) {
                                    String setName = sets.get(i).getFacesetName();
                                    String setId = sets.get(i).getFacesetId();
//                                    Logger.i("setSize = " + sets.size() + "\nsetName = " + setName + "\nsetId = " + setId);
                                    if (setName.equals(Global.Const.faceSetName)) {
                                        Global.Varibale.faceSetId = setId;
                                        Global.Varibale.faceSetName = setName;
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void postTimeLog() {

        if (SPUtil.getInstance(WelcomeActivity.this).getBoolean(Global.Const.isFirstInApp,false)) {

            String content = "android系统版本: " + Build.VERSION.RELEASE +
                    "<br>android系统定制商：" + Build.BRAND +
                    "<br>androidSDK版本：" + Build.VERSION.SDK_INT +
                    "<br>android设备名称: " + Build.MODEL +
                    "<br>人脸识别应用使用时间: " + TimeUtil.getCurrentTime() +
                    "<br>人脸样本集合名称: " + Global.Const.faceSetName +
                    "<br>图片上传花费时间：:" + FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.httpTime) +
                    "<br><br>机子图片转换处理时间:" + FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.androidDealTime) +
                    "<br>预览图片分辨率：" + FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.bmpSize) +
                    "<br>识别结果返回：" + FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.compareResult);

            TJoyDoorLog testBomb = new TJoyDoorLog();
            testBomb.setContent(content);
//            testBomb.save(new SaveListener<String>() {
//                @Override
//                public void done(String s, BmobException e) {
//
//                    if (e == null) {
//                        Logger.i("时间日志上传成功");
//
//                        FileUtil.clearFile(WelcomeActivity.this, "tjoy", Global.FilePath.httpTime);
//                        FileUtil.clearFile(WelcomeActivity.this, "tjoy", Global.FilePath.bmpSize);
//                        FileUtil.clearFile(WelcomeActivity.this, "tjoy", Global.FilePath.androidDealTime);
//                        FileUtil.clearFile(WelcomeActivity.this, "tjoy", Global.FilePath.compareResult);
//                    } else {
//                        Logger.i("创建数据失败：" + e.getMessage() + "------" + e.getErrorCode());
//                    }
//                }
//            });
        }else {
            FileUtil.clearFile(WelcomeActivity.this, "tjoy", Global.FilePath.httpTime);
            FileUtil.clearFile(WelcomeActivity.this, "tjoy", Global.FilePath.bmpSize);
            FileUtil.clearFile(WelcomeActivity.this, "tjoy", Global.FilePath.androidDealTime);
            FileUtil.clearFile(WelcomeActivity.this, "tjoy", Global.FilePath.compareResult);
        }
    }
}
