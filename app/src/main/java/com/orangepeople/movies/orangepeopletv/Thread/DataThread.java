package com.orangepeople.movies.orangepeopletv.Thread;

import android.animation.FloatArrayEvaluator;
import android.os.Handler;
import android.util.Log;

import com.orangepeople.movies.orangepeopletv.DBManager.DBManager;
import com.orangepeople.movies.orangepeopletv.Model.BigVideoInfo;
import com.orangepeople.movies.orangepeopletv.Model.CommentInfo;
import com.orangepeople.movies.orangepeopletv.Model.LiveInfo;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideoInfo;
import com.orangepeople.movies.orangepeopletv.Utils.Util;

import java.io.File;
import java.util.List;

/**
 * Author: Jan
 * CreateTime:on 2016/10/29.
 */
public class DataThread extends Thread {
    Handler handler;
    boolean isDoData = false;
    List<ThreeVideoInfo> threeVideoInfoList;
    List<BigVideoInfo> bigVideoInfoList;
    List<CommentInfo> commentInfoList;
    List<LiveInfo> liveInfoList;
    DBManager dbManager;

    public DataThread(List<ThreeVideoInfo> threeVideoInfoList,
                      List<BigVideoInfo> bigVideoInfoList,
                      List<CommentInfo> commentInfoList,
                      List<LiveInfo> liveInfoList,
                      Handler handler,
                      boolean isDoData,
                      DBManager dbManager

    ) {
        this.threeVideoInfoList = threeVideoInfoList;
        this.bigVideoInfoList = bigVideoInfoList;
        this.commentInfoList = commentInfoList;
        this.liveInfoList = liveInfoList;
        this.handler = handler;
        this.isDoData = isDoData;
        this.dbManager = dbManager;

    }

    @Override
    public void run() {
        super.run();

        dbManager.addThreeVideoData(threeVideoInfoList);
        dbManager.addBigVideoData(bigVideoInfoList);
        dbManager.addCommentData(commentInfoList);
        dbManager.addLiveData(liveInfoList);

        if (isDoData) {
            handler.sendEmptyMessage(000);
        } else {
            handler.sendEmptyMessage(123456);
        }

    }
}
