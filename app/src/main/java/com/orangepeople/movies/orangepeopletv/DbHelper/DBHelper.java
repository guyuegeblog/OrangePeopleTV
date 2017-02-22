package com.orangepeople.movies.orangepeopletv.DbHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ASUS on 2016/11/21.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "oranges_tvs_database_5.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("LogISTR", "数据库创建");
        //存储tv时间文件
        Util.writeFileToSDFile(Constant.send_Tv,Util.simpleDateFormat.format(new Date()));

        //创建三级表
        db.execSQL("CREATE TABLE IF NOT EXISTS ThreeVideo" +
                "(id INTEGER PRIMARY KEY, createtime VARCHAR,name VARCHAR,area VARCHAR," +
                "address_sd VARCHAR,address_hd VARCHAR,pic VARCHAR,pic_heng VARCHAR," +
                "lasttime VARCHAR,score VARCHAR,showtime VARCHAR,state VARCHAR,isLook VARCHAR)");

        //创建大片表
        db.execSQL("CREATE TABLE IF NOT EXISTS BigVideoInfo" +
                "(id INTEGER PRIMARY KEY, createtime VARCHAR,name VARCHAR,area VARCHAR," +
                "address_sd VARCHAR,address_hd VARCHAR,pic VARCHAR,pic_heng VARCHAR," +
                "lasttime VARCHAR,score VARCHAR,showtime VARCHAR,state VARCHAR,isLook VARCHAR)");

        //创建评论表
        db.execSQL("CREATE TABLE IF NOT EXISTS CommentInfo" +
                "(id INTEGER PRIMARY KEY, hand VARCHAR,info VARCHAR,name VARCHAR," +
                "pic VARCHAR,state VARCHAR,createtime VARCHAR)");

        //创建直播表
        db.execSQL("CREATE TABLE IF NOT EXISTS LiveInfo" +
                "(id INTEGER PRIMARY KEY, isLook VARCHAR,live_Url VARCHAR,logo_url VARCHAR," +
                "remarks VARCHAR,video_name VARCHAR,wonderful VARCHAR,tv_name VARCHAR,description VARCHAR," +
                "create_Time VARCHAR,clientPic_Url VARCHAR,client_FirstPic_Url VARCHAR,orderBy VARCHAR)");

    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE ThreeVideo ADD COLUMN other STRING");
        db.execSQL("ALTER TABLE BigVideoInfo ADD COLUMN other STRING");
        db.execSQL("ALTER TABLE CommentInfo ADD COLUMN other STRING");
        db.execSQL("ALTER TABLE LiveInfo ADD COLUMN other STRING");
    }
}
