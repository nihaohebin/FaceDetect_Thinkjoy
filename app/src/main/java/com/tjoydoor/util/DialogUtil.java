package com.tjoydoor.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * 通用提示框
 * Created by george.yang on 15/11/17.
 */
public class DialogUtil {
    private static AlertDialog alertDialog;
    private static ProgressDialog progressDialog;

    public static void showMsg(Activity activity, String title, String tip) {
        showDialog(activity, true, title, tip, "确定", "", null, null);
    }

    public static void showY(Activity activity, String tip) {
        showDialog(activity, true, "提示", tip, "确定", "", null, null);
    }

    public static void showYN(Activity activity, String tip, DialogInterface.OnClickListener okListener) {
        showDialog(activity, true, "提示", tip, "确定", "取消", okListener, null);
    }

    public static void showYN(Activity activity, String tip, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener noListener) {
        showDialog(activity, true, "提示", tip, "确定", "取消", okListener, noListener);
    }

    public static void showYN(Activity activity,String title, String tip, DialogInterface.OnClickListener okListener) {
        showDialog(activity, true, title, tip, "确定", "取消", okListener,null);
    }


    public static void showDialog(Activity activity, boolean canCancel, String title, String tip, String positiveText, String negativeText,
                                  final DialogInterface.OnClickListener poListener, 
                                  final DialogInterface.OnClickListener negativeListener) {
        
        if (alertDialog != null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);  //先得到构造器
        builder.setCancelable(canCancel);
        builder.setMessage(tip); //设置内容
        
        if (!TextUtil.isEmpty(title)) {
            builder.setTitle(title); //设置标题
        }
        if (!TextUtil.isEmpty(positiveText)) {
            builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() { //设置确定按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); //关闭dialog
                    if (poListener != null) {
                        poListener.onClick(dialog, which);
                    }
                }
            });
        }
        
        if (!TextUtil.isEmpty(negativeText)) {
            builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() { //设置取消按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (negativeListener != null) {
                        negativeListener.onClick(dialog, which);
                    }
                }
            });
        }
        //参数都设置完成了，创建并显示出来
        alertDialog = builder.create();
        alertDialog.show();
    }


    public static void showProgressDialog(Activity activity, boolean canCancel) {
        showProgressDialog(activity, "请稍候...", canCancel);
    }

    public static ProgressDialog showProgressDialog(Activity activity, String tip, boolean canCancel) {

        closeProgressDialog();

        if (activity == null) {
            return null;
        }
        if (activity.isDestroyed()) {
            return null;
        }
        progressDialog = ProgressDialog.show(activity, null, tip, true, canCancel);
        return progressDialog;
    }

    public static void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
