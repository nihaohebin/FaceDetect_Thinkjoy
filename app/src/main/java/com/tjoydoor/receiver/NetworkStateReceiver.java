package com.tjoydoor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.tjoydoor.Global;
import com.tjoydoor.util.ToastUtil;

import java.util.List;

import cn.thinkjoy.face.imp.FaceSetListener;
import cn.thinkjoy.face.imp.FaceSetQueryListener;
import cn.thinkjoy.face.imp.UserBindListener;
import cn.thinkjoy.face.manage.SetManage;
import cn.thinkjoy.face.manage.UserManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceSet;
import cn.thinkjoy.sdk.SDKInitializer;


/**
 * Author：hebin on 2016/10/18 0018
 * <p>
 * Annotations：网络监控广播
 */
@SuppressWarnings("ALL")
public class NetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = "TAG";
    
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Log.i(TAG,getConnectionType(info.getType()) + "连上");
                       
                        SDKInitializer.ReAuth(context.getApplicationContext());
                        bindUser();

                    }
                } else {
                      Log.i(TAG,getConnectionType(info.getType()) + "断开");
                    Toast.makeText(context,"网络已断开",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void bindUser() {

        UserManage.newInstance(context).bindUser(Global.Const.userId, new UserBindListener() {
            @Override
            public void onUserBind(String userId, ErrorMsg error) {
                if (error.getCode() == 0) {
                    ToastUtil.showMessage(context, Global.Const.userId + "用户绑定成功");
                    createSet();
                } else {
                    ToastUtil.showMessage(context, error.getMsg());
                }
            }
        });
    }

    private void createSet() {
        SetManage.newInstance(context).creatFaceSet(Global.Const.faceSetName, "", "", new FaceSetListener() {
            @Override
            public void onFaceSetListener(final FaceSet set, ErrorMsg error) {
                if (error.getCode() == 0) {
                      Log.i(TAG,"facesetID = " + set.getFacesetId());
                    Global.Varibale.faceSetId = set.getFacesetId();
                } else {
//                    Logger.e(error.getMsg());
                    SetManage.newInstance(context).getFaceSet("", 0, new FaceSetQueryListener() {
                        @Override
                        public void onFaceSetQueryListener(List<FaceSet> sets, ErrorMsg msg) {
                            if (msg.getCode() == 0) {
                                for (int i = 0; i < sets.size(); i++) {
                                    String setName = sets.get(i).getFacesetName();
                                    String setId = sets.get(i).getFacesetId();
//                                      Log.i(TAG,"setSize = " + sets.size() + "\nsetName = " + setName + "\nsetId = " + setId);
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

    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "移动网络";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }
}