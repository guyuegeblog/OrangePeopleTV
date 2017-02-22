package com.orangepeople.movies.orangepeopletv.DBManager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.orangepeople.movies.orangepeopletv.DbHelper.DBHelper;
import com.orangepeople.movies.orangepeopletv.DbHelper.DatabaseContext;
import com.orangepeople.movies.orangepeopletv.Model.BigVideoInfo;
import com.orangepeople.movies.orangepeopletv.Model.CommentInfo;
import com.orangepeople.movies.orangepeopletv.Model.LiveInfo;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideoInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS on 2016/11/21.
 */
public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;
    private static DBManager dbManager;
    private DatabaseContext databaseContext;

    public DBManager(Context context) {
        databaseContext = new DatabaseContext(context);
        helper = new DBHelper(databaseContext);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    public static DBManager getDBManager(Activity context) {
        if (dbManager == null) dbManager = new DBManager(context);
        return dbManager;
    }


    /**
     * add persons======================================================三级表操作start=============================================
     *
     * @param threeVideoInfoList
     */
    public void addThreeVideoData(List<ThreeVideoInfo> threeVideoInfoList) {
        clearThreeTable();
        db.beginTransaction();    //开始事务
        try {
            for (ThreeVideoInfo person : threeVideoInfoList) {
                db.execSQL("INSERT INTO ThreeVideo VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new Object[]{person.getId(), person.getCreatetime(), person.getName(), person.getArea(),
                                person.getAddress_sd(), person.getAddress_hd(), person.getPic(), person.getPic_heng(),
                                person.getLasttime(), person.getScore(), person.getShowtime(), person.getState(),
                                person.getIsLook()});
            }
            db.setTransactionSuccessful();    //设置事务成功完成
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    /**
     * update person's age
     *
     * @param person
     */
    public void updateThreeVideo_Pic(ThreeVideoInfo person) {
        ContentValues cv = new ContentValues();
        cv.put("pic", person.getPic());//需要更新的数据
        db.update("ThreeVideo", cv, "id = ?", new String[]{person.getId()});
    }

    /**
     * delete old person
     *
     * @param person
     */
    public void deleteThreeVideoById(ThreeVideoInfo person) {
        db.delete("ThreeVideo", " id= ?", new String[]{String.valueOf(person.getId())});
    }

    /**
     * query all persons, return list
     *
     * @return List<Person>
     */
    public List<ThreeVideoInfo> queryThreeVideoAll() {
        List<ThreeVideoInfo> persons = new ArrayList<>();
        Cursor c = queryThreeVideoInfoTheCursor();
        while (c.moveToNext()) {
            ThreeVideoInfo person = new ThreeVideoInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.createtime = c.getString(c.getColumnIndex("createtime"));
            person.name = c.getString(c.getColumnIndex("name"));
            person.area = c.getString(c.getColumnIndex("area"));
            person.address_sd = c.getString(c.getColumnIndex("address_sd"));
            person.address_hd = c.getString(c.getColumnIndex("address_hd"));
            person.pic = c.getString(c.getColumnIndex("pic"));
            person.pic_heng = c.getString(c.getColumnIndex("pic_heng"));
            person.lasttime = c.getString(c.getColumnIndex("lasttime"));
            person.score = c.getString(c.getColumnIndex("score"));
            person.showtime = c.getString(c.getColumnIndex("showtime"));
            person.state = c.getString(c.getColumnIndex("state"));
            person.isLook = c.getString(c.getColumnIndex("isLook"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    public List<ThreeVideoInfo> queryPagerThreeVideo(int pageSize, int page) {
        List<ThreeVideoInfo> persons = new ArrayList<>();
        Cursor c = queryThreeVideoPagerTheCursor(pageSize, page);
        while (c.moveToNext()) {
            ThreeVideoInfo person = new ThreeVideoInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.createtime = c.getString(c.getColumnIndex("createtime"));
            person.name = c.getString(c.getColumnIndex("name"));
            person.area = c.getString(c.getColumnIndex("area"));
            person.address_sd = c.getString(c.getColumnIndex("address_sd"));
            person.address_hd = c.getString(c.getColumnIndex("address_hd"));
            person.pic = c.getString(c.getColumnIndex("pic"));
            person.pic_heng = c.getString(c.getColumnIndex("pic_heng"));
            person.lasttime = c.getString(c.getColumnIndex("lasttime"));
            person.score = c.getString(c.getColumnIndex("score"));
            person.showtime = c.getString(c.getColumnIndex("showtime"));
            person.state = c.getString(c.getColumnIndex("state"));
            person.isLook = c.getString(c.getColumnIndex("isLook"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    /**
     * query all persons, return cursor
     *
     * @return Cursor
     */
    public Cursor queryThreeVideoInfoTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM ThreeVideo", null);
        return c;
    }

    public Cursor queryThreeVideoPagerTheCursor(int pageSize, int page) {
        //使用limit分页查询
        Cursor c = db.rawQuery("SELECT * FROM ThreeVideo limit " + (page - 1) * pageSize + "," + pageSize + "", null);
        return c;
    }

    public void clearThreeTable() {
        db.execSQL("DELETE FROM ThreeVideo");
//        SQLiteDatabase.execSQL("DROP TABLE CUSTOMERS")
//        清除表中所有记录：
//        SQLiteDatabase.execSQL("DELETE FROM CUSTOMERS")
    }
    /**
     * query all persons, return cursor=======================三级表操作end=====================================================
     *
     * @return Cursor
     */


    /**
     * add persons======================================================大片表操作start=============================================
     *
     * @param
     */
    public void addBigVideoData(List<BigVideoInfo> bigVideoInfoList) {
        db.beginTransaction();    //开始事务
        clearBigTable();
        try {
            for (BigVideoInfo person : bigVideoInfoList) {
                db.execSQL("INSERT INTO BigVideoInfo VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new Object[]{person.getId(), person.getCreatetime(), person.getName(), person.getArea(),
                                person.getAddress_sd(), person.getAddress_hd(), person.getPic(), person.getPic_heng(),
                                person.getLasttime(), person.getScore(), person.getShowtime(), person.getState(),
                                person.getIsLook()});
            }
            db.setTransactionSuccessful();    //设置事务成功完成
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public void clearBigTable() {
        db.execSQL("DELETE FROM BigVideoInfo");
//        SQLiteDatabase.execSQL("DROP TABLE CUSTOMERS")
//        清除表中所有记录：
//        SQLiteDatabase.execSQL("DELETE FROM CUSTOMERS")
    }

    /**
     * update person's age
     *
     * @param person
     */
    public void updateBigVideo_Pic(BigVideoInfo person) {
        ContentValues cv = new ContentValues();
        cv.put("pic", person.getPic());//需要更新的数据
        db.update("BigVideoInfo", cv, "id = ?", new String[]{person.getId()});
    }

    /**
     * delete old person
     *
     * @param person
     */
    public void deleteBigVideoById(BigVideoInfo person) {
        db.delete("BigVideoInfo", " id= ?", new String[]{String.valueOf(person.getId())});
    }

    /**
     * query all persons, return list
     *
     * @return List<Person>
     */
    public List<BigVideoInfo> queryBigVideoAll() {
        List<BigVideoInfo> persons = new ArrayList<>();
        Cursor c = queryBigVideoTheCursor();
        while (c.moveToNext()) {
            BigVideoInfo person = new BigVideoInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.createtime = c.getString(c.getColumnIndex("createtime"));
            person.name = c.getString(c.getColumnIndex("name"));
            person.area = c.getString(c.getColumnIndex("area"));
            person.address_sd = c.getString(c.getColumnIndex("address_sd"));
            person.address_hd = c.getString(c.getColumnIndex("address_hd"));
            person.pic = c.getString(c.getColumnIndex("pic"));
            person.pic_heng = c.getString(c.getColumnIndex("pic_heng"));
            person.lasttime = c.getString(c.getColumnIndex("lasttime"));
            person.score = c.getString(c.getColumnIndex("score"));
            person.showtime = c.getString(c.getColumnIndex("showtime"));
            person.state = c.getString(c.getColumnIndex("state"));
            person.isLook = c.getString(c.getColumnIndex("isLook"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    public List<BigVideoInfo> queryPagerBigVideo(int pageSize, int page) {
        List<BigVideoInfo> persons = new ArrayList<>();
        Cursor c = queryBigVideoPagerTheCursor(pageSize, page);
        while (c.moveToNext()) {
            BigVideoInfo person = new BigVideoInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.createtime = c.getString(c.getColumnIndex("createtime"));
            person.name = c.getString(c.getColumnIndex("name"));
            person.area = c.getString(c.getColumnIndex("area"));
            person.address_sd = c.getString(c.getColumnIndex("address_sd"));
            person.address_hd = c.getString(c.getColumnIndex("address_hd"));
            person.pic = c.getString(c.getColumnIndex("pic"));
            person.pic_heng = c.getString(c.getColumnIndex("pic_heng"));
            person.lasttime = c.getString(c.getColumnIndex("lasttime"));
            person.score = c.getString(c.getColumnIndex("score"));
            person.showtime = c.getString(c.getColumnIndex("showtime"));
            person.state = c.getString(c.getColumnIndex("state"));
            person.isLook = c.getString(c.getColumnIndex("isLook"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    /**
     * query all persons, return cursor
     *
     * @return Cursor
     */
    public Cursor queryBigVideoTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM BigVideoInfo", null);
        return c;
    }

    public Cursor queryBigVideoPagerTheCursor(int pageSize, int page) {
        //使用limit分页查询
        Cursor c = db.rawQuery("SELECT * FROM BigVideoInfo limit " + (page - 1) * pageSize + "," + pageSize + "", null);
        return c;
    }
    /**
     * query all persons, return cursor=======================大片表操作end=====================================================
     *
     * @return Cursor
     */


    /**
     * add persons======================================================评论表操作start=============================================
     *
     * @param
     */
    public void addCommentData(List<CommentInfo> commentInfoList) {
        db.beginTransaction();    //开始事务
        try {
            for (CommentInfo person : commentInfoList) {
                db.execSQL("INSERT INTO CommentInfo VALUES(?, ?, ?, ?, ?, ?, ?)",
                        new Object[]{person.getId(), person.getHand(), person.getInfo(), person.getName(), person.getPic(),
                                person.getState(), person.getCreatetime()});
            }
            db.setTransactionSuccessful();    //设置事务成功完成
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    /**
     * update person's age
     *
     * @param person
     */
    public void updateComment(CommentInfo person) {
//        ContentValues cv = new ContentValues();
//        cv.put("pic", person.getPic());//需要更新的数据
//        db.update("BigVideoInfo", cv, "id = ?", new String[]{person.getId()});
    }

    /**
     * delete old person
     *
     * @param person
     */
    public void deleteCommentById(CommentInfo person) {
        db.delete("CommentInfo", " id= ?", new String[]{String.valueOf(person.getId())});
    }

    /**
     * query all persons, return list
     *
     * @return List<Person>
     */
    public List<CommentInfo> queryCommentAll() {
        List<CommentInfo> persons = new ArrayList<>();
        Cursor c = queryCommentTheCursor();
        while (c.moveToNext()) {
            CommentInfo person = new CommentInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.hand = c.getString(c.getColumnIndex("hand"));
            person.info = c.getString(c.getColumnIndex("info"));
            person.name = c.getString(c.getColumnIndex("name"));
            person.pic = c.getString(c.getColumnIndex("pic"));
            person.state = c.getString(c.getColumnIndex("state"));
            person.createtime = c.getString(c.getColumnIndex("createtime"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    public List<CommentInfo> queryPagerComment(int pageSize, int page) {
        List<CommentInfo> persons = new ArrayList<>();
        Cursor c = queryCommentPagerTheCursor(pageSize, page);
        while (c.moveToNext()) {
            CommentInfo person = new CommentInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.hand = c.getString(c.getColumnIndex("hand"));
            person.info = c.getString(c.getColumnIndex("info"));
            person.name = c.getString(c.getColumnIndex("name"));
            person.pic = c.getString(c.getColumnIndex("pic"));
            person.state = c.getString(c.getColumnIndex("state"));
            person.createtime = c.getString(c.getColumnIndex("createtime"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    /**
     * query all persons, return cursor
     *
     * @return Cursor
     */
    public Cursor queryCommentTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM CommentInfo", null);
        return c;
    }

    public Cursor queryCommentPagerTheCursor(int pageSize, int page) {
        //使用limit分页查询
        Cursor c = db.rawQuery("SELECT * FROM CommentInfo limit " + (page - 1) * pageSize + "," + pageSize + "", null);
        return c;
    }
    /**
     * query all persons, return cursor=======================评论表操作end=====================================================
     *
     * @return Cursor
     */


    /**
     * query all persons, return cursor=========================直播表操作start============================================
     *
     * @return Cursor
     */

    public void addLiveData(List<LiveInfo> liveInfoList) {
        clearLiveTable();
        db.beginTransaction();    //开始事务
        try {
            for (LiveInfo person : liveInfoList) {
                db.execSQL("INSERT INTO LiveInfo VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new Object[]{person.getId(), person.getIsLook(), person.getLive_Url(), person.getLogo_url(),
                                person.getRemarks(), person.getVideo_name(), person.getWonderful(), person.getTv_name(),
                                person.getDescription(), person.getCreate_Time(), person.getClientPic_Url(), person.getClient_FirstPic_Url(),
                                person.getClient_FirstPic_Url()});
            }
            db.setTransactionSuccessful();    //设置事务成功完成
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public void clearLiveTable() {
        db.execSQL("DELETE FROM LiveInfo");
//        SQLiteDatabase.execSQL("DROP TABLE CUSTOMERS")
//        清除表中所有记录：
//        SQLiteDatabase.execSQL("DELETE FROM CUSTOMERS")
    }

    /**
     * update person's age
     *
     * @param person
     */
    public void updateLiveInfo(LiveInfo person) {
//        ContentValues cv = new ContentValues();
//        cv.put("pic", person.getPic());//需要更新的数据
//        db.update("BigVideoInfo", cv, "id = ?", new String[]{person.getId()});
    }

    /**
     * delete old person
     *
     * @param person
     */
    public void deleteLiveInfoById(LiveInfo person) {
        db.delete("LiveInfo", " id= ?", new String[]{String.valueOf(person.getId())});
    }

    /**
     * query all persons, return list
     *
     * @return List<Person>
     */
    public List<LiveInfo> queryLiveInfoAll() {
        List<LiveInfo> persons = new ArrayList<>();
        Cursor c = queryLiveInfoTheCursor();
        while (c.moveToNext()) {
            LiveInfo person = new LiveInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.isLook = c.getString(c.getColumnIndex("isLook"));
            person.live_Url = c.getString(c.getColumnIndex("live_Url"));
            person.logo_url = c.getString(c.getColumnIndex("logo_url"));
            person.remarks = c.getString(c.getColumnIndex("remarks"));
            person.video_name = c.getString(c.getColumnIndex("video_name"));
            person.wonderful = c.getString(c.getColumnIndex("wonderful"));
            person.tv_name = c.getString(c.getColumnIndex("tv_name"));
            person.description = c.getString(c.getColumnIndex("description"));
            person.create_Time = c.getString(c.getColumnIndex("create_Time"));
            person.clientPic_Url = c.getString(c.getColumnIndex("clientPic_Url"));
            person.client_FirstPic_Url = c.getString(c.getColumnIndex("client_FirstPic_Url"));
            person.orderBy = c.getInt(c.getColumnIndex("orderBy"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    public List<LiveInfo> queryPagerLiveInfo(int pageSize, int page) {
        List<LiveInfo> persons = new ArrayList<>();
        Cursor c = queryLiveInfoPagerTheCursor(pageSize, page);
        while (c.moveToNext()) {
            LiveInfo person = new LiveInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.isLook = c.getString(c.getColumnIndex("isLook"));
            person.live_Url = c.getString(c.getColumnIndex("live_Url"));
            person.logo_url = c.getString(c.getColumnIndex("logo_url"));
            person.remarks = c.getString(c.getColumnIndex("remarks"));
            person.video_name = c.getString(c.getColumnIndex("video_name"));
            person.wonderful = c.getString(c.getColumnIndex("wonderful"));
            person.tv_name = c.getString(c.getColumnIndex("tv_name"));
            person.description = c.getString(c.getColumnIndex("description"));
            person.create_Time = c.getString(c.getColumnIndex("create_Time"));
            person.clientPic_Url = c.getString(c.getColumnIndex("clientPic_Url"));
            person.client_FirstPic_Url = c.getString(c.getColumnIndex("client_FirstPic_Url"));
            person.orderBy = c.getInt(c.getColumnIndex("orderBy"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    /**
     * query all persons, return cursor
     *
     * @return Cursor
     */
    public Cursor queryLiveInfoTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM LiveInfo", null);
        return c;
    }

    public Cursor queryLiveInfoPagerTheCursor(int pageSize, int page) {
        //使用limit分页查询
        Cursor c = db.rawQuery("SELECT * FROM LiveInfo limit " + (page - 1) * pageSize + "," + pageSize + "", null);
        return c;
    }

    //根据id查询
    public List<LiveInfo> queryLiveById(String id) {
        List<LiveInfo> persons = new ArrayList<>();
        Cursor c = queryLiveByIdCusor(id);
        while (c.moveToNext()) {
            LiveInfo person = new LiveInfo();
            person.id = c.getString(c.getColumnIndex("id"));
            person.isLook = c.getString(c.getColumnIndex("isLook"));
            person.live_Url = c.getString(c.getColumnIndex("live_Url"));
            person.logo_url = c.getString(c.getColumnIndex("logo_url"));
            person.remarks = c.getString(c.getColumnIndex("remarks"));
            person.video_name = c.getString(c.getColumnIndex("video_name"));
            person.wonderful = c.getString(c.getColumnIndex("wonderful"));
            person.tv_name = c.getString(c.getColumnIndex("tv_name"));
            person.description = c.getString(c.getColumnIndex("description"));
            person.create_Time = c.getString(c.getColumnIndex("create_Time"));
            person.clientPic_Url = c.getString(c.getColumnIndex("clientPic_Url"));
            person.client_FirstPic_Url = c.getString(c.getColumnIndex("client_FirstPic_Url"));
            person.orderBy = c.getInt(c.getColumnIndex("orderBy"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    public Cursor queryLiveByIdCusor(String id) {
        //使用limit分页查询
        Cursor c = db.rawQuery("SELECT * FROM LiveInfo Where id=" + id + "", null);
        return c;
    }


    /**
     * query all persons, return cursor=======================直播表操作end=====================================================
     *
     * @return Cursor
     */


    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}