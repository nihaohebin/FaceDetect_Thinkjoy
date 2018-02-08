package com.tjoydoor;

import android.os.Environment;

import java.util.List;

/**
 * Author：hebin on 2016/10/27 0027
 * <p>
 * Annotations：存放全局变量值
 */
public class Global {

    /**
     * 常量
     */
    public static class Const {
        //贺斌用户
        public final static String userId = "hebin";

        //各个人脸集合
        public final static String faceSetName = "hebin";           //贺斌测试集合
//        public final static String faceSetName = "lijian";        //李健测试集合
//        public final static String faceSetName = "sunfangrui";    //孙芳蕊测试集合
//        public final static String faceSetName = "xuyecheng";     //业成测试集合
//        public final static String faceSetName = "chengguyu";     //陈谷雨测试集合
//        public final static String faceSetName = "heweibing";     //十牛何总测试集合
//        public final static String faceSetName = "tebieban";      //特别版测试集合
//        public final static String faceSetName = "jiadu";         //佳都测试集合
//        public final static String faceSetName = "test1";         //测试1集合
//        public final static String faceSetName = "test2";         //测试2集合
//        public final static String faceSetName = "hwdHG";         //海威达黄工测试集合
//        public final static String faceSetName = "test417";       //4月17日测试集合
//        public final static String faceSetName = "youdi";         //优地测试集合
//        public final static String faceSetName = "xiaoLianBao";   //校联宝测试集合
//        public final static String faceSetName = "SZWNYJY";       //深圳微纳研究院测试集合
//        public final static String faceSetName = "qiyuwen";       //小齐测试集合
//        public final static String faceSetName = "abzg";          //奥比中光测试集合
//        public final static String faceSetName = "tyss";            //平台用户测试集合
//        public final static String faceSetName = "cdm";            //平台用户测试集合
//        public final static String faceSetName = "wex";             //温熙测试集合
//        public final static String faceSetName = "gaojian";           //贺斌测试集合
//        public final static String faceSetName = "zhengjianbing";

        public final static int detectSize = 80;
        public final static double recognitionRate = 0.92;        //识别率
        public final static int PermissionCallBack = 5;          //权限回调
        public final static int PermissionSetting = 6;           //权限设置界面
        public final static int degree = 0;                         //普通手机参数

        /**
         * 注意下面的时间根据不同的设备会用不同感官效果  影响大
         */
        public final static int timeFaceCheck = 3000;         //是否有人脸检测器
        public final static int timeMsgDisplay = 6000;        //信息展示时间


        /**
         * sharedperference  key
         */
        public final static String cameraId = "cameraId";
        public final static String userInfoList = "userInfoList";
        public static final String focusValue = "focusValue";
        public final static String isFirstInApp = "isFirstInApp";


        /**
         * 海威达控制指令  及地址
         */
        // UDP广播IP和PORT
//    public static final String SERVERIP = "192.168.2.218";
//    public static final int SERVERPORT = 20106;

        public static final String SERVERIP = "192.168.2.42";
        public static final int SERVERPORT = 20105;

//    public static final String SERVERIP = "192.168.30.71";
//    public static final int SERVERPORT = 20105;


        public static final byte[] HWD_OPEN = new byte[22];

        static {
            HWD_OPEN[0] = (byte) 0xBA;
            HWD_OPEN[1] = 0x12;
            HWD_OPEN[2] = 0x00;
            HWD_OPEN[3] = 0x00;
            HWD_OPEN[4] = 0x00;
            HWD_OPEN[5] = 0x1E;
            HWD_OPEN[6] = 0x30;
            HWD_OPEN[7] = 0x6C;
            HWD_OPEN[8] = 0x1E;
            HWD_OPEN[9] = 0x01;
            HWD_OPEN[10] = (byte) 0x95;
            HWD_OPEN[11] = 0x02;
            HWD_OPEN[12] = 0x4E;
            HWD_OPEN[13] = 0x00;
            HWD_OPEN[14] = 0x00;
            HWD_OPEN[15] = 0x00;
            HWD_OPEN[16] = 0x32;
            HWD_OPEN[17] = 0x00;
            HWD_OPEN[18] = 0x00;
            HWD_OPEN[19] = 0x00;
            HWD_OPEN[20] = 0x00;
            HWD_OPEN[21] = (byte) 0xED;
        }
    }


    /**
     * 变量
     */
    public static class Varibale {

        public static boolean isBigText = false;
        public static boolean isFirst = true;
        public static boolean isSaveAndroidDealTime = true;
        public static boolean isSaveHttpRequestTime = true;

        public static int saveAndroidDealTimeCount = 0;
        public static int saveHttpRequestTimeCount = 0;

        public static double time = 0.0;
        public static double httpTime = 0.0;        //用于传递http请求到响应时间
        public static double confidence = 0.0;      //用于传递对比结果相似度

        public static String personId = "";          //人员列表查询该人的样本脸
        public static String compareName = "";       //用于传递对比结果名字
        public static String dymicPersonId = "";     //用于传递启动动态录入
        public static String faceSetId = "";          //用于传递用户facesetId
        public static String faceSetName = "";        //用于传递集合名称

        public static String age = "";
        public static String sex = "";
        public static String path = "";

        public static List<String> listFaceId = null;
    }

    /**
     * URL地址
     */
    public static class UrlPath {
        /**
         * HTTP接口
         */
        public static String MANAGER = "http://120.25.235.44:8080/appmanage";                //APP管理系统地址
        public static String PostError = MANAGER + "/app/appErrorInfo/add";                  //错误日志
        public static String UpdateApp = MANAGER + "/app/appUpdate/getNewestApp";            //更新APP

    }

    /**
     * 文件路径地址
     */
    public static class FilePath {
        public final static boolean HASSDCARD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        public final static String SDCARDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        public final static String APPPATH = HASSDCARD ? SDCARDPATH + "/jiadu/doorsecurity" : "/data/data/cn.doorsecurity/files";
        public final static String AppCrash = APPPATH + "/crash";  //异常捕捉文件存储类
        public final static String PIC = APPPATH + "/pic";
        public final static String AppDown = APPPATH + "/down";

        /**
         * 保存数据文件名
         */
        public final static String httpTime = "httpTime.txt";
        public final static String compareResult = "compareResult.txt";
        public final static String bmpSize = "bmpSize.txt";
        public final static String androidDealTime = "androidDealTime.txt";
    }
}
