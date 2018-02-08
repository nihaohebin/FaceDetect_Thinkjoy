package com.tjoydoor.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;

import com.tjoydoor.Global;
import com.tjoydoor.entity.UpdateApp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import cn.tjoydoor.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * APP更新工具
 * Created by whz on 2016/10/22.
 */

public class UpdateUtil {

    private static UpdateUtil update = null;
    private static Activity mActivity;
    /**
     * 该类用法 UpdateUtil.newInstance(getActivity()).checkUpdate();
     */
    public final int DOWN_ERROR = 1;                // 更新出错
    public final int GET_UNDATAINFO_ERROR = 2;      // 下载超时
    public final int UPDATA_CLIENT = 3;             // 通知弹出更新对话框
    public final int UPDATA_PRGESS = 4;             //更新进度条
    public final int DOWN_SUCCESS = 5;              //下载完成
    public NotificationManager mNotificationManager = null;
    public Notification mNotification = null;
    public int downLoadFileSize = 0;
    public int fileSize = 0;
    private UpdateApp updataInfo;
    @SuppressLint("HandlerLeak")
    private Handler downHandler = new Handler() {
        @SuppressLint("ShowToast")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_CLIENT:
                    // 对话框通知用户升级程序
                    showUpdataDialog(updataInfo);
                    break;
                case UPDATA_PRGESS:
                    int result = downLoadFileSize * 100 / fileSize;
                    mNotification.contentView.setTextViewText(R.id.content_view_text1, "正在下载" + result + "%");
                    mNotification.contentView.setProgressBar(R.id.content_view_progress, fileSize, downLoadFileSize, false);
                    mNotificationManager.notify(0, mNotification);
                    break;
                case DOWN_SUCCESS:
                    mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                    mNotification.contentView.setTextViewText(R.id.content_view_text1, "下载完成");
                    mNotification.contentView.setViewVisibility(R.id.content_view_progress, View.GONE);
                    mNotificationManager.notify(0, mNotification);
                    break;
                case GET_UNDATAINFO_ERROR:
                    // 服务器超时
//				Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", 1).show();
                    ToastUtil.showMessage(mActivity, "获取服务器更新信息失败");
                    break;
                case DOWN_ERROR:
                    // 下载apk失败
//				Toast.makeText(getApplicationContext(), "下载新版本失败", 1).show();
                    ToastUtil.showMessage(mActivity, "下载新版本失败");
                    mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                    mNotification.contentView.setTextViewText(R.id.content_view_text1, "下载失败");
                    mNotification.contentView.setViewVisibility(R.id.content_view_progress, View.GONE);
                    mNotificationManager.notify(0, mNotification);

                    break;
            }
        }
    };

    public static UpdateUtil newInstance(Activity activity) {
        mActivity = activity;
        if (update == null) {
            update = new UpdateUtil();
        }
        return update;
    }

    public void checkUpdate() {
        HashMap<String, String> params = new HashMap<>();
        params.put("packageName", mActivity.getPackageName());
//        NetAccess.request(mActivity).setParams(params).byGetNoEncry(Global.UrlPath.UpdateApp, new NetAccess.NetAccessListener() {
//            @Override
//            public void onResponse(String object, Boolean success, String flag) throws IOException {
//                if (object == null || object.equals("")) {
//                    return;
//                }
//                JSONObject jo = JSONObject.parseObject(object);
//                if (jo.getString("success").equals("0")) {
//                    JSONObject data = jo.getJSONObject("data");
//                    updataInfo = JSONObject.parseObject(data.toString(), UpdateApp.class);
//                    File file = new File(Global.FilePath.AppDown, "DoorSecurity" + AppUtil.getselfVersionName(mActivity) + ".apk");
//                    if (!file.exists()) {
//
//                    } else {
//                        file.delete();
//                    }
//                    if (updataInfo.getVersionCode() > AppUtil.getselfVersionCode(mActivity)) {
//                        Message msg = new Message();
//                        msg.what = UPDATA_CLIENT;
//                        downHandler.sendMessage(msg);
//                    }
//                }
//            }
//        });
    }


    public void showUpdataDialog(final UpdateApp info) {
        final UpdateApp updataInfo = info;
//        DialogUtil.showYNDialog(mActivity,"版本升级", "是否确认升级该版本", "下载", new );
        DialogUtil.showYN(mActivity, "更新", info.getVersionDesc(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(Global.FilePath.AppDown, "DoorSecurity" + info.getVersionName() + ".apk");
                if (!file.exists()) {
                    downApk(updataInfo);
                } else {
                    AppUtil.installApk(file, mActivity);
                }
            }
        });
    }


    @SuppressWarnings("deprecation")
    private void notificationInit() {
        //通知栏内显示下载进度条
        Intent intent = new Intent();//点击进度条，进入程序
        PendingIntent pIntent = PendingIntent.getActivity(mActivity, 0, intent, 0);
        mNotificationManager = (NotificationManager) mActivity.getSystemService(NOTIFICATION_SERVICE);
        mNotification = new Notification();
        mNotification.icon = R.mipmap.ic_launcher;
        mNotification.tickerText = "开始下载";
        mNotification.contentView = new RemoteViews(mActivity.getPackageName(), R.layout.content_view);//通知栏中进度布局
        mNotification.contentIntent = pIntent;
        //  mNotificationManager.notify(0,mNotification);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void downApk(UpdateApp updata) {
        final UpdateApp info = updata;
        notificationInit();
        mNotificationManager.notify(0, mNotification);
        new Thread() {
            @Override
            public void run() {
                try {
                    File file = downFile(info);
                    sleep(3000);

                    Message msg = new Message();
                    msg.what = DOWN_SUCCESS;
                    downHandler.sendMessage(msg);

                    AppUtil.installApk(file, mActivity);
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = DOWN_ERROR;
                    downHandler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public File downFile(UpdateApp info) throws IOException {
        File f = new File(Global.FilePath.AppDown);
        if (!f.exists()) {
            f.mkdirs();
        }

        URL url = new URL(info.getPath());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        // 获取到文件的大小
        InputStream is = conn.getInputStream();
        File file = new File(Global.FilePath.AppDown, "DoorSecurity" + info.getVersionName() + ".apk");
        FileOutputStream fos = new FileOutputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024];
        int len;
        int total = 0;
        int old_size = 0;
        int old_total = 0;
        fileSize = conn.getContentLength();
        while ((len = bis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            total += len;
            if (((total * 100 / fileSize) - old_size) >= 1 && total - old_total > 500) {
                old_size = total * 100 / fileSize;
                old_total = total;
                downLoadFileSize = total;
                Message msg = new Message();
                msg.what = UPDATA_PRGESS;
                downHandler.sendMessage(msg);
            }
        }
        fos.close();
        bis.close();
        is.close();
        return file;
    }
}
