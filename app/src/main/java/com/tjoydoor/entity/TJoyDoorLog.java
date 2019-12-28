package com.tjoydoor.entity;

import java.io.Serializable;

/**
 * Created by hebin
 * on 2017/3/27 0027.
 */

public class TJoyDoorLog   implements Serializable {
    
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
