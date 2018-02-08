package com.tjoydoor.imp;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: hebin
 * Time : 2017/4/13 0013
 */

public interface DataReceiverCallBack {

    void dataListener(JSONObject object, Gson gson) throws JSONException;

}
