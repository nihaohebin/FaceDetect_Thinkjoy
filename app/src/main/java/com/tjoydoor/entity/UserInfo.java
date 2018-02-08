package com.tjoydoor.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/28 0028.
 */

public class UserInfo implements Serializable {


    /**
     * deptName : 习悦信息技术有限公司
     * userName : 王志鹏
     * groupName : 总经理
     * photo : 王志鹏.jpg
     * deptId : 10001
     * userId : 2
     */

    private String deptName;
    private String userName;
    private String groupName;
    private String photo;
    private int deptId;
    private int userId;
    private String clockIn;
    private String picPath;

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getClockIn() {
        return clockIn;
    }

    public void setClockIn(String clockIn) {
        this.clockIn = clockIn;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
