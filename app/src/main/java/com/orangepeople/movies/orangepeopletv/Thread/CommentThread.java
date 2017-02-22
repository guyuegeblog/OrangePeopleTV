package com.orangepeople.movies.orangepeopletv.Thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.orangepeople.movies.orangepeopletv.Model.Comment;

import java.io.Serializable;

/**
 * Author: Jan
 * CreateTime:on 2016/10/29.
 */
public class CommentThread extends Thread {
    Handler handler;
    String aesJson;

    public CommentThread(String aesJson, Handler mHandler) {
        this.aesJson = aesJson;
        this.handler = mHandler;
    }

    @Override
    public void run() {
        super.run();
        Comment threeVideo = JSONObject.parseObject(aesJson, Comment.class);
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putSerializable("comment", (Serializable) threeVideo);
        msg.setData(b);
        msg.what = 666;
        handler.sendMessage(msg);
    }
}
