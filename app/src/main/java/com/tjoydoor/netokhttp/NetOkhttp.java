package com.tjoydoor.netokhttp;

import android.app.Activity;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.tjoydoor.Global;
import com.tjoydoor.imp.DataReceiverCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import cn.thinkjoy.face.imp.SuccessListener;
import cn.thinkjoy.face.manage.SetManage;
import cn.thinkjoy.face.model.ErrorMsg;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Author: hebin
 * Time : 2017/1/3 0003
 */

public class NetOkhttp {

    /**
     *  同步Get请求
     */
    public static void sync_doGet(final String URL, final DataReceiverCallBack receiveData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    client.newBuilder().readTimeout(9000, TimeUnit.MILLISECONDS);
                    Request request = new Request.Builder().url(URL).build();
                    Response response = client.newCall(request).execute();
                    Logger.i("请求数据：" + request.toString());
                    if (response.isSuccessful()) {
                        String data = response.body().string();
                        Logger.i("响应数据：" + data);
                        receiveData.dataListener(new JSONObject(data), new Gson());

                    } else {
                        Logger.i("响应失败");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 异步Get请求
     */
    public static void asyn_doGet(String url, final DataReceiverCallBack receiveData) {
        try {
            Logger.i("main thread id is " + Thread.currentThread().getId());
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Logger.i("请求数据：" + request.toString());
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) {

                    //可以做一些加密解密的东西
                    // 注：该回调是子线程，非主线程
                    try {
                        Logger.i("callback thread id is " + Thread.currentThread().getId());
                        if (response.isSuccessful()) {
                            String data = response.body().string();
                            Logger.i("响应数据：" + data);
                            receiveData.dataListener(new JSONObject(data), new Gson());
                        } else {
                            Logger.i("响应失败");
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Post请求
     */
    public static void doJsonPost(final String url, final Object info, final DataReceiverCallBack receiveData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(MediaType.parse("Application/json; charset=utf-8"), new Gson().toJson(info));
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Logger.i("请求数据：" + request.toString());
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String data = response.body().string();
                        Logger.i("响应数据：" + data);
                        receiveData.dataListener(new JSONObject(data), new Gson());
                    } else {
                        Logger.i("响应失败");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 带参数POST请求
     */
    public static void doPostFromParameters(final String url, final FormBody formBody, final DataReceiverCallBack receiveData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder().url(url).post(formBody).build();
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String data = response.body().string();
                        Logger.i("响应数据：" + data);
                        receiveData.dataListener(new JSONObject(data), new Gson());
                    } else {
                        Logger.i("响应失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 人脸识别刷新
     */
    public static void refreshFaceSet(Activity activity) {
        SetManage.newInstance(activity).refreshFaceSet(Global.Varibale.faceSetId, new SuccessListener() {
            @Override
            public void onSuccessListener(boolean success, ErrorMsg errorMsg) {
                Logger.i("刷新：" + success);
            }
        });
    }

    /**
     * 门禁控制指令
     */
    public static void send() {
        // UDP广播IP和PORT

        DatagramSocket socket = null;

        // 向局域网UDP广播信息：Hello, World!
        try {
            InetAddress serverAddress = InetAddress.getByName(Global.Const.SERVERIP);
            System.out.println("Client: Start connecting\n");
            socket = new DatagramSocket(Global.Const.SERVERPORT);

            byte[] buf = Global.Const.HWD_OPEN;

            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, Global.Const.SERVERPORT);
            System.out.println("Client: Sending\n");

            socket.send(packet);

            System.out.println("Client: Message sent\n");
            System.out.println("Client: Succeed!\n");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            assert socket != null;
            socket.close();
        }
    }

}
