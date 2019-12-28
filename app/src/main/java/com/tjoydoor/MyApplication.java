package com.tjoydoor;

import android.app.Application;
import android.content.Intent;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.view.CropImageView;
import com.orhanobut.logger.Logger;
import com.tjoydoor.main.WelcomeActivity;
import com.tjoydoor.util.GlideImageLoader;

import cn.thinkjoy.sdk.SDKInitializer;

/**
 * Author：hebin on 2016/10/22 0022
 * Annotations：初始化APP配置类
 */
public class MyApplication extends Application implements Thread.UncaughtExceptionHandler {


    @Override
    public void onCreate() {
        super.onCreate();

//        Bmob.initialize(this, "e8b2bb2b249c82678e70dd56281addfd");

        SDKInitializer.init(this);

        // setAppend是否为追加模式, setSimple是否是简单的log信息,
//        CarshHandlerUtil.init(this, "CarshHandler").setAppend(true).setSimple(false);
//        Thread.setDefaultUncaughtExceptionHandler(this);

        Logger.init("TAG").hideThreadInfo();

        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setMultiMode(false);    //false单选  true多选
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(false); //是否按矩形区域保存
        imagePicker.setSelectLimit(1);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.CIRCLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(200);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(200);//保存文件的高度。单位像素

    }


    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // 重启app ..上传日志等...
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
