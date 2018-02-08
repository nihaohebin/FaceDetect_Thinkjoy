package com.tjoydoor.entity;

import java.io.Serializable;

/**
 * Created by whz on 2016/10/22.
 */

public class UpdateApp implements Serializable {

    private int versionCode;       //版本号
    private String versionName;    //版本名称
    private String path;           //应用下载地址
    private String versionDesc;       //描述


    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }
}
