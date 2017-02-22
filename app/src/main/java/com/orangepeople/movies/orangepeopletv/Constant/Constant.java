package com.orangepeople.movies.orangepeopletv.Constant;

import com.orangepeople.movies.orangepeopletv.Model.PriceInfo;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.WeiXin.WXYZ;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口存储
 */
public class Constant {
    /**
     * 登陆接口
     */
    public static final String LOGIN_INTERFACE = "http://139.129.97.33:8080/LiveVideo/UserServlet.shtml";

    /**
     * 用户信息修改接口
     */
    public static final String USER_UPDATE = "http://139.129.97.33:8080/LiveVideo/UpUserServlet.shtml";


    /**
     * 操作记录
     */
    public static final String USER_DO_INTERFACE = "http://139.129.97.33:8080/LiveVideo/OperationServlet.shtml";

    /****
     * 微信支付
     */

    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String APP_ID = "wx5c9f8c91af605d16";

    public static final String WX_PAY_ORDER = "http://114.215.28.26:80/LiveVideo/wxPayInit.shtml";

    //微信支付接口 支付验证

    public static final String WX_PAY_YZ = "http://114.215.28.26:80/LiveVideo/payQuery.shtml";

    public static WXYZ wxyz = new WXYZ();

    /***
     * 支付宝支付
     */
    /**
     * 支付宝获取订单信息接口
     */
    public static final String ZFB_QUERY = "http://114.215.28.26:80/LiveVideo/aliPayInit.shtml";

    /**
     * 支付宝验证接口
     */
    public static final String ZFB_YZ = "http://114.215.28.26:80/LiveVideo/aliPayQuery.shtml";


    /***
     * 渠道接口
     */
    public static final String YOU_MENG_AREA = "";


    /***
     * app所有数据接口
     */
    public static String APP_DATA = "http://139.129.97.33:8080/LiveVideo/VideoServlet.shtml";


    /***
     * apk升級接口
     */
    public static final String APK_UDPATE_INTERFACE = "";

    /***
     * 添加评论接口
     */
    public static final String USER_ADD_MESSAGE_INTERFACE = "";

    /***
     * tv搜索接口
     */
    public static final String DATA_SEARCH = "http://139.129.97.33:8080/LiveVideo/SearchServlet.shtml";


    /***
     * tv节目单接口
     */
    public static final String TV_JIEMU_LIST = "http://139.129.97.33:8080/LiveVideo/ProgramServlet.shtml";


    /***
     * 试用文件路径
     */
    public static final String TV_SHIYONG_MP4 = Util.getSDCardPath() + "//" + ".tvandroidas";

    public static final String TV_SHIYONG_MP4_FILE = ".timetvaa";

    public static final String TV_SHIYONG_MP4_ALL = Constant.TV_SHIYONG_MP4 + "/" + Constant.TV_SHIYONG_MP4_FILE;


    public static final String TV_SHIYONG_M3U8 = Util.getSDCardPath() + "//" + ".bkeets";

    public static final String TV_SHIYONG_FILE = ".saalayg";//.saal

    public static final String TV_SHIYONG_M3U8_ALL = Constant.TV_SHIYONG_M3U8 + "/" + Constant.TV_SHIYONG_FILE;

    public static final String TV_FIRST_REGISTER = ".saasfdalsew";

    public static final String TV_SHIYONG_ALL = Constant.TV_SHIYONG_M3U8 + "/" + Constant.TV_FIRST_REGISTER;


    /***
     * uuid文件路径
     */
    public static final String UUID_AUTO_CREATE_DIRECTORY = Util.getSDCardPath() + "//.tvSAnOS";

    //二级目录
    public static final String UUID_AUTO_TWOFILE_DIRECTORY = "//.tvSAQsiv";


    public static final String UUID_AUTO_FILE_PATH = ".tv43vzaAadl";


    public static final String USER_AUTO_DIRECTORY = Util.getSDCardPath() + "//.tvQsSAnOS";

    public static final String USER_AUTO_GUOQI = USER_AUTO_DIRECTORY + ".tvBAiAb";


    /****
     * 第一次注册
     */
    public static boolean isFirstRegister = false;

    /****
     * 存储数据
     */
    public static String saveData = "";

    /****
     * 存储数据完毕
     */
    public static boolean saveDataSucces = false;

    public static List<PriceInfo> priceInfoList = new ArrayList<>();

    public static final int doDate = 7200;
    public static String vpnVipPackageName = "de.blinkt.openvpn.newuivpnmovies.vipversion";

    public static boolean barra_show = true;

    //送tv时间
    public static final String send_Tv_Directory = Util.getSDCardPath() + "//.sendtvdire1";

    public static final String send_Tv_File = ".send";

    public static final String send_Tv = send_Tv_Directory + "/" + send_Tv_File;

    public static  boolean isPay = false;
}
