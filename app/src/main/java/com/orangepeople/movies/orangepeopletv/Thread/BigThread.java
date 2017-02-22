package com.orangepeople.movies.orangepeopletv.Thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.orangepeople.movies.orangepeopletv.Model.BigVideo;

import java.io.Serializable;

/**
 * Author: Jan
 * CreateTime:on 2016/10/29.
 */
public class BigThread extends Thread {
    Handler handler;
    String aesJson;

    public BigThread(String aesJson, Handler mHandler) {
        this.aesJson = aesJson;
        this.handler = mHandler;
    }

    @Override
    public void run() {
        super.run();
        BigVideo threeVideo = JSONObject.parseObject(aesJson, BigVideo.class);
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putSerializable("big", (Serializable) threeVideo);
        msg.setData(b);
        msg.what = 666;
        handler.sendMessage(msg);
        Log.i("abab", "2");
    }
}
