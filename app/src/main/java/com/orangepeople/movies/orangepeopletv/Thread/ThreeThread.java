package com.orangepeople.movies.orangepeopletv.Thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideo;

import java.io.Serializable;

/**
 * Author: Jan
 * CreateTime:on 2016/10/29.
 */
public class ThreeThread extends Thread {
    Handler handler;
    String aesJson;

    public ThreeThread(String aesJson, Handler mHandler) {
        this.aesJson = aesJson;
        this.handler = mHandler;
    }

    @Override
    public void run() {
        super.run();
        ThreeVideo threeVideo = JSONObject.parseObject(aesJson, ThreeVideo.class);
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putSerializable("three", (Serializable) threeVideo);
        msg.setData(b);
        msg.what = 666;
        handler.sendMessage(msg);
        Log.i("abab", "2");
    }
}
