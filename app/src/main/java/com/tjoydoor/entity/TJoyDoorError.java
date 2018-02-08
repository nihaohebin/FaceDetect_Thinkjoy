package com.tjoydoor.entity;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * Created by hebin
 * on 2017/3/27 0027.
 */

public class TJoyDoorError extends BmobObject implements Serializable {
    
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
