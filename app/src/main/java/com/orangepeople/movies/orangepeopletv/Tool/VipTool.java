package com.orangepeople.movies.orangepeopletv.Tool;

import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.Utils.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ASUS on 2016/12/27.
 */
public class VipTool {
    public static boolean isThanSendTvTime = false;

    public static boolean judgeIsThanSendTvTime() {
        if (SendTvTimeTool.SEND_TV_TIME_TYPE == SendTvTimeTool.SEND_TV_TIME_DAY_TYPE) {
            //判断天数
            String createDatabaseDate = Util.readFileToSDFile(Constant.send_Tv);//yyyy-MMM-dd
            try {
                long[] times = Util.compareSendTvTime(new Date(), Util.simpleDateFormat.parse(createDatabaseDate));
//               time[0] = days;//天数
//               time[1] = hours;//小时
//               time[2] = minutes;//分钟
//               time[3] = second;
                isThanSendTvTime = times[0] >= SendTvTimeTool.Time_Day ? true : false;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (SendTvTimeTool.SEND_TV_TIME_TYPE == SendTvTimeTool.SEND_TV_TIME_HOURS_TYPE) {
            //判断小时数
            String createDatabaseDate = Util.readFileToSDFile(Constant.send_Tv);//yyyy-MMM-dd
            try {
                long[] times = Util.compareSendTvTime(new Date(), Util.simpleDateFormat.parse(createDatabaseDate));
//               time[0] = days;//天数
//               time[1] = hours;//小时
//               time[2] = minutes;//分钟
//               time[3] = second;
                isThanSendTvTime = times[1] >= SendTvTimeTool.Time_Hours ? true : false;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return isThanSendTvTime;
    }

}
