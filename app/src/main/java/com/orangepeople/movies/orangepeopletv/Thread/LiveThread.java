package com.orangepeople.movies.orangepeopletv.Thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.orangepeople.movies.orangepeopletv.Model.Live;

import java.io.Serializable;

/**
 * Author: Jan
 * CreateTime:on 2016/10/29.
 */
public class LiveThread extends Thread {
    Handler handler;
    String aesJson;

    public LiveThread(String aesJson, Handler mHandler) {
        this.aesJson = aesJson;
        this.handler = mHandler;
    }

    @Override
    public void run() {
        super.run();
        Live threeVideo = JSONObject.parseObject(aesJson, Live.class);
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putSerializable("live", (Serializable) threeVideo);
        msg.setData(b);
        msg.what = 666;
        handler.sendMessage(msg);
        Log.i("abab", "2");
    }
}
