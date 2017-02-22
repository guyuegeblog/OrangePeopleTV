package com.orangepeople.movies.orangepeopletv.wxapi;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alipay.sdk.app.PayTask;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orangepeople.movies.orangepeopletv.Activity.JarpanActivity;
import com.orangepeople.movies.orangepeopletv.Activity.LiveActivity;
import com.orangepeople.movies.orangepeopletv.Activity.LoginActivity;
import com.orangepeople.movies.orangepeopletv.Activity.ThreeActivity;
import com.orangepeople.movies.orangepeopletv.Activity.UserUpdateActivity;
import com.orangepeople.movies.orangepeopletv.Bean.SharedBean.sharedLogin;
import com.orangepeople.movies.orangepeopletv.Bean.login.LoginBean;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.Interface.PayInterface;
import com.orangepeople.movies.orangepeopletv.Model.BigVideo;
import com.orangepeople.movies.orangepeopletv.Model.Comment;
import com.orangepeople.movies.orangepeopletv.Model.DoInfo;
import com.orangepeople.movies.orangepeopletv.Model.Live;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideo;
import com.orangepeople.movies.orangepeopletv.Net.OkHttp;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Tool.AppTool;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.MobClick;
import com.orangepeople.movies.orangepeopletv.Utils.Nick;
import com.orangepeople.movies.orangepeopletv.Utils.PhoneInfo;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.SystemBarTintManager;
import com.orangepeople.movies.orangepeopletv.WeiXin.WX;
import com.orangepeople.movies.orangepeopletv.WeiXin.WXYZ;
import com.orangepeople.movies.orangepeopletv.ZhiFuBao.Alipay;
import com.orangepeople.movies.orangepeopletv.ZhiFuBao.AplipayYZ;
import com.orangepeople.movies.orangepeopletv.ZhiFuBao.PayResult;
import com.orangepeople.movies.orangepeopletv.ZhiFuBao.SignUtils;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WXPayEntryActivity extends AppCompatActivity implements PayInterface, IWXAPIEventHandler, Handler.Callback,
        Runnable {

    private static final String gifUrl = "http://ww2.sinaimg.cn/mw690/d2d378b0tw1egg6up0b4wg20a005gkjl.gif";
    private TextView orange_tv, jarpan_video, three_video, user_vip;
    private TextView tv_three_price, tv_year_price, zfb_pay_three, zfb_pay_year;
    //    tv_six_price,
    private TextView tv_three_type, tv_year_type;
    //    tv_six_type,
    private ImageView userinfo, gifImage;
    private Activity context;
    private ImageView login, user_update;
    private LinearLayout btn_year, btn_one;
    //    btn_six,
    private RelativeLayout userxieyi;
    private ImageView WXPay, YinLianPay, ZhiFuBaoPay;
    public String VIP_TIME = "一年";//购买的会员时间
    private String VIP_LAST_TIME = null;//会员到期时间
    public String body = "H站大全年卡会员";

    //微信会员
    private IWXAPI api;
    public WXYZ entity = new WXYZ();
    public Dialog dialog, dialogyz;

    //支付宝
    // 商户PID
    public String PARTNER = "";
    // 商户收款账号
    public String SELLER = "";
    // 商户私钥，pkcs8格式
    public String RSA_PRIVATE = "";
    // 支付宝公钥
    private final int SDK_PAY_FLAG = 1;

    //支付宝验证信息
    private String Zfb_out_trade_no = "";//支付宝订单号
    private String userName;//用户名
    private String imeilLastId;//IMEL最后一个id数字
    private String nickName;
    private String password;
    private String imsi;
    private String show;
    private T t;
    private Util util;
    private AesUtils aesUtils;
    public String USER_VIP_STATUS = null;

    Handler yinLianHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 50) {
                yinLianHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setPay();
                    }
                }, 3000);
            }
            if (msg.what == 60) {
                setPay();
            }
            //微信验证
            if (msg.what == 101) {
                yinLianHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        weiXinYanZhen("");
                    }
                }, 3000);
            }
            //支付宝验证
            if (msg.what == 102) {
                yinLianHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        zhiFuBaoYanZhen("");
                    }
                }, 3000);

            }
            //微信有参验证
            if (msg.what == 8989) {
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("payJson", "wx开始执行");
                        String wxAesJson = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.WX_USER_PAY_FAILED_FILE, pay_key);
                        String wxJson = aesUtils.decrypt(wxAesJson);
                        weiXinYanZhen(wxJson);
                    }
                }, 3000);
            }

            //支付宝有参验证
            if (msg.what == 1919) {
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("payJson", "zfb开始执行");
                        String zfbAesJson = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.ZHI_FU_BAO_USER_PAY_FAILED_FILE, pay_key);
                        String zfbJson = aesUtils.decrypt(zfbAesJson);
                        zhiFuBaoYanZhen(zfbJson);
                    }
                }, 3000);
            }

        }
    };

    public void setPay() {
        dialog.dismiss();
//        YinLianPay.setEnabled(true);
        WXPay.setEnabled(true);
        ZhiFuBaoPay.setEnabled(true);
    }

    private final int GO_DOWN_VPN_FAILED = 1056;
    private final int GO_DOWN_VPN_SUCCES = 1089;
    private final int GO_DOWN_VPN_PROGRESS = 1058;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GO_DOWN_VPN_FAILED:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    apkDownLoad();
                    break;
                case GO_DOWN_VPN_SUCCES:
                    Log.d("h_bl", "文件下载安装");
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    try {
                        AppTool.installApk(WXPayEntryActivity.this, Util.getSDCardPath() + "/" + apkPath);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                case GO_DOWN_VPN_PROGRESS:
                    showProgressDialog(msg.arg1, msg.arg2);
                    break;
            }
        }
    };
    //三大支付是否成功
    public boolean isWx = false;
    public boolean isYinLian = false;
    public boolean isZhiFuBao = false;
    public int zfbFaildIjk = 0;
    public int ylFaildIjk = 0;
    public int wxFaildIjk = 0;
    //private View sanbaline;
    private PayInterface payInterface;
//    private String vipYearPrice = "365";
//    private String vipSixPrice = "260";
//    private String vipThreePrice = "150";

    private String vipYearPrice = "150";
    private String vipThreePrice = "66";

    private String vipYearType = "一年";
    //  private String vipSixType = "半年";
    private String vipOneType = "一个月";
    private String vipThreeType = "三个月";

    public String body_three = "橙人TV三个月会员";
    //  public String body_six = "橙人TV半年会员";
    public String body_year = "橙人TV一年会员";
    public String body_one = "橙人TV一月会员";

    private String pay_Type_Choice = "zfb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
        setContentView(R.layout.activity_wxpay_entry);
        context = this;
        payInterface = this;
        initView();
        initData();

        //微信注册
        api = WXAPIFactory.createWXAPI(this, null);
        api.registerApp("wx5c9f8c91af605d16");
        api.handleIntent(getIntent(), this);
    }

    private void initStatus() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.orange_page);//通知栏所需颜色
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void initView() {
        if (t == null) {
            t = new T();
        }
        if (util == null) {
            util = new Util(context);
        }
        if (aesUtils == null) {
            aesUtils = new AesUtils();
        }

        zfb_pay_three = (TextView) findViewById(R.id.zfb_pay_three);
        zfb_pay_year = (TextView) findViewById(R.id.zfb_pay_year);

        tv_three_type = (TextView) findViewById(R.id.tv_three_type);
//        tv_six_type = (TextView) findViewById(R.id.tv_six_type);
        tv_year_type = (TextView) findViewById(R.id.tv_year_type);

        tv_three_price = (TextView) findViewById(R.id.tv_three_price);
//        tv_six_price = (TextView) findViewById(R.id.tv_six_price);
        tv_year_price = (TextView) findViewById(R.id.tv_year_price);

        if (Constant.priceInfoList == null || Constant.priceInfoList.size() == 0) {
            //没有获取到价格数据(使用默认)
            tv_three_price.setText(vipThreePrice + "元");
//            tv_six_price.setText(vipSixPrice + "元");
            tv_year_price.setText(vipYearPrice + "元");

            tv_three_type.setText(vipOneType);
//            tv_six_type.setText(vipSixType);
            tv_year_type.setText(vipThreeType);

            zfb_pay_three.setText("支付宝支付" + (Integer.parseInt(vipThreePrice) - 10) + "元");
            zfb_pay_year.setText("支付宝支付" + (Integer.parseInt(vipYearPrice) - 10) + "元");
        } else {
            //获取到了服务器的价格数据(从低到高)
            if (Constant.isPay) {
                vipThreePrice = Constant.priceInfoList.get(1).getPrice();
                vipYearPrice = Constant.priceInfoList.get(2).getPrice();
                tv_three_price.setText(vipThreePrice + "元");
//            tv_six_price.setText(vipSixPrice + "元");
                tv_year_price.setText(vipYearPrice + "元");

                vipThreeType = URLDecoder.decode(Constant.priceInfoList.get(1).getType());
//            vipSixType = URLDecoder.decode(Constant.priceInfoList.get(1).getType());
                vipYearType = URLDecoder.decode(Constant.priceInfoList.get(2).getType());

                tv_three_type.setText(vipThreeType);
//            tv_six_type.setText(vipSixType);
                tv_year_type.setText(vipYearType);

                body_three = URLDecoder.decode(Constant.priceInfoList.get(1).getDescription());
//            body_six = URLDecoder.decode(Constant.priceInfoList.get(1).getDescription());
                body_year = URLDecoder.decode(Constant.priceInfoList.get(2).getDescription());

                zfb_pay_three.setText("支付宝支付" + (Integer.parseInt(vipThreePrice) - 10) + "元");
                zfb_pay_year.setText("支付宝支付" + (Integer.parseInt(vipYearPrice) - 10) + "元");
            } else {
                vipThreePrice = Constant.priceInfoList.get(0).getPrice();
                vipYearPrice = Constant.priceInfoList.get(1).getPrice();
//                vipYearPrice = Constant.priceInfoList.get(2).getPrice();
                tv_three_price.setText(vipThreePrice + "元");
//            tv_six_price.setText(vipSixPrice + "元");
                tv_year_price.setText(vipYearPrice + "元");

                vipOneType = URLDecoder.decode(Constant.priceInfoList.get(0).getType());
                vipThreeType = URLDecoder.decode(Constant.priceInfoList.get(1).getType());
//                vipYearType = URLDecoder.decode(Constant.priceInfoList.get(2).getType());

                tv_three_type.setText(vipOneType);
//            tv_six_type.setText(vipSixType);
                tv_year_type.setText(vipThreeType);

                body_one = URLDecoder.decode(Constant.priceInfoList.get(0).getDescription());
//            body_six = URLDecoder.decode(Constant.priceInfoList.get(1).getDescription());
                body_three = URLDecoder.decode(Constant.priceInfoList.get(1).getDescription());

                zfb_pay_three.setText("支付宝支付" + (Integer.parseInt(vipThreePrice) - 10) + "元");
                zfb_pay_year.setText("支付宝支付" + (Integer.parseInt(vipYearPrice) - 10) + "元");
            }
        }

        login = (ImageView) findViewById(R.id.login);
        userinfo = (ImageView) findViewById(R.id.userinfo);

        orange_tv = (TextView) findViewById(R.id.orange_tv);
        user_vip = (TextView) findViewById(R.id.user_vip);
        three_video = (TextView) findViewById(R.id.three_video);
        jarpan_video = (TextView) findViewById(R.id.jarpan_video);

        btn_one = (LinearLayout) findViewById(R.id.btn_three);
//        btn_six = (LinearLayout) findViewById(R.id.btn_six);
        btn_year = (LinearLayout) findViewById(R.id.btn_year);

        orange_tv.setBackgroundResource(R.mipmap.orange_tv);
        jarpan_video.setBackgroundResource(R.mipmap.jarpan_video);
        three_video.setBackgroundResource(R.mipmap.three_video);
        user_vip.setBackgroundResource(R.mipmap.user_vip_select);

        gifImage = (ImageView) findViewById(R.id.gif);
        Glide.with(context).load(gifUrl).asGif().placeholder(R.mipmap.screen_loading)
                .error(R.mipmap.screen_error_images).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(gifImage);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
            }
        });

        userinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserUpdateActivity.class);
                startActivity(intent);
            }
        });


        three_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ThreeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        orange_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LiveActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        jarpan_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, JarpanActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });

        WXPay = (ImageView) findViewById(R.id.wxpay);
        ZhiFuBaoPay = (ImageView) findViewById(R.id.zfbpay);
        login = (ImageView) findViewById(R.id.login);
        user_update = (ImageView) findViewById(R.id.userinfo);
        user_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WXPayEntryActivity.this, UserUpdateActivity.class);
                startActivity(intent);
            }
        });

        btn_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                btn_one.setImageResource(R.mipmap.three_month_select);
//                btn_six.setImageResource(R.mipmap.six_month);
//                btn_year.setImageResource(R.mipmap.year);
                if (Constant.isPay) {
                    VIP_TIME = "三个月";
                    body = body_three;
                    vipThreePrice = "150";
                    payInterface.payClick(pay_Type_Choice);
                    send_Do();
                } else {
                    VIP_TIME = "一个月";
                    body = body_one;
                    vipThreePrice = "66";
                    payInterface.payClick(pay_Type_Choice);
                    send_Do();
                }
            }
        });

//        btn_six.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                btn_one.setImageResource(R.mipmap.three_month);
////                btn_six.setImageResource(R.mipmap.six_month_select);
////                btn_year.setImageResource(R.mipmap.year);
//                MobclickAgent.onEvent(context, MobClick.Six_id);//埋点统计
//                VIP_TIME = vipSixType;
//                body = body_six;
//                payInterface.payClick(pay_Type_Choice);
//                send_Do();
//            }
//        });

        btn_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobclickAgent.onEvent(context, MobClick.Year_id);//埋点统计
                if (Constant.isPay) {
                    VIP_TIME = vipYearType;
                    body = body_year;
                    vipYearPrice = "365";
                    payInterface.payClick(pay_Type_Choice);
                    send_Do();
                } else {
                    VIP_TIME = "三个月";
                    body = body_three;
                    vipThreePrice = "150";
                    payInterface.payClick(pay_Type_Choice);
                    send_Do();
                }
            }
        });

        String usernameStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName");
        String passWordStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
        String showStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");

        if (TextUtils.isEmpty(showStr) || TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(passWordStr)) {
            login.setImageResource(R.mipmap.login);
        } else {
            login.setImageResource(R.mipmap.cancel);
        }
        login.setOnClickListener(loginClick);

//        if (Constant.isShowOneMonthPay == false) {
//            sanbaline.setVisibility(View.GONE);
//            sanbapay.setVisibility(View.GONE);
//            openSixPay();
//        }

        //微信支付点击事件
        WXPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                WXPay.setImageResource(R.mipmap.wxpay_select);
                ZhiFuBaoPay.setImageResource(R.mipmap.zfb_pay);
                pay_Type_Choice = "wx";
            }
        });
//        //银联支付点击事件
//        YinLianPay.setTag(0);
//        YinLianPay.setOnClickListener(mClickListener);
        //支付宝支付点击事件
        ZhiFuBaoPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZhiFuBaoPay.setImageResource(R.mipmap.zfbpay_select);
                WXPay.setImageResource(R.mipmap.wx_pay);
                pay_Type_Choice = "zfb";
            }
        });
    }

    private void send_Do() {
        if (VIP_TIME.equals("一个月")) {
            MobclickAgent.onEvent(context, MobClick.One_id);//埋点统计
            sendUserDoData("5", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        } else if (VIP_TIME.equals("三个月")) {
            MobclickAgent.onEvent(context, MobClick.Three_id);//埋点统计
            sendUserDoData("6", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        } else if (VIP_TIME.equals("半年")) {
            MobclickAgent.onEvent(context, MobClick.Six_id);//埋点统计
            sendUserDoData("7", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        } else if (VIP_TIME.equals("一年")) {
            MobclickAgent.onEvent(context, MobClick.Year_id);//埋点统计
            sendUserDoData("8", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        }
    }


    private void initData() {
        dialog = createLoadingDialog(this, "正在初始化环境...请稍候");
        dialogyz = createLoadingDialog(this, "正在验证您的支付情况...请稍候");
        //获取用户账户数据
        getUserDataInfo();
    }

    public void getUserDataInfo() {
        show = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");
        imsi = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "imsi");
        nickName = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "nickName");
        userName = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName");
        password = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
        USER_VIP_STATUS = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status");
        VIP_LAST_TIME = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_lastTme");
//
//        if (TextUtils.isEmpty(nickName) || TextUtils.isEmpty(USER_VIP_STATUS) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(VIP_LAST_TIME)) {
//            //没有登陆
//            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//            String username = util.getAndroidId(this);//用户名
//            tv_username.setText(username);
//            tv_usertype.setText("普通用户");
//            tv_viplasttime.setText("您还没有开通VIP哦");
//        } else {
//            //已经登陆,但要区分会员和非会员状态
//            String statusStr = aesUtils.getInstance().decrypt(USER_VIP_STATUS);
//            if (TextUtils.isEmpty(statusStr)) {
//                return;
//            } else if (statusStr.equals("1")) {
//                //会员
//                TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//                String username = util.getAndroidId(this);//用户名
//                tv_username.setText(username);
//                tv_usertype.setText("VIP高级会员");
//                String time = aesUtils.getInstance().decrypt(VIP_LAST_TIME);
//                time = time.substring(0, 10);
//                tv_viplasttime.setText(time);
//            } else if (statusStr.equals("2")) {
//                //不是会员
//                TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//                String username = util.getAndroidId(this);//用户名
//                tv_username.setText(username);
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//                String oldDate = aesUtils.decrypt(Util.readFileToSDFile(VPN_SHIYONG_FILE));
//                Date newDate = new Date();//
//                if (TextUtils.isEmpty(oldDate)) {
//                    tv_usertype.setText("试用用户");
//                    newDate.setHours(newDate.getHours() + 2);
//                    String ltime = sdf.format(newDate);
//                    tv_viplasttime.setText(ltime);
//                    return;
//                } else {
//                    tv_usertype.setText("试用用户");
//                    try {
//                        Date old = sdf.parse(oldDate);
//                        long[] time = getTime(newDate, old);
//                        if (time == null) {
//                            tv_viplasttime.setText("您的试用期已过期");
//                            return;
//                        }
//                        if (time[0] >= 1) {
//                            tv_viplasttime.setText("您的试用期已过期");
//                            return;
//                        }
//                        if (time[1] >= 2) {
//                            tv_viplasttime.setText("您的试用期已过期");
//                            return;
//                        } else {
//                            old.setHours(old.getHours() + 2);
//                            String ltime = sdf1.format(old);
//                            tv_viplasttime.setText(ltime);
//                        }
//                    } catch (ParseException e) {
//                    }
//                }
//            }
//        }
    }


    //字体需要的设置
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    public boolean handleMessage(Message message) {
        return false;
    }


    @Override
    public void run() {

    }

    @Override
    public void payClick(String type) {
        if (type.equals("wx")) {
            //微信支付友盟统计
            MobclickAgent.onEvent(this, "41");//埋点统计
            isWx = false;
            WinXinPay();
        } else if (type.equals("zfb")) {
            //支付宝支付友盟统计
            MobclickAgent.onEvent(this, "42");//埋点统计
            isZhiFuBao = false;
            ZFBPay();
        }
    }

    View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String usernameStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName");
            String passWordStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
            String showStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");

            if (TextUtils.isEmpty(showStr) || TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(passWordStr)) {
                //没有登陆
                login.setImageResource(R.mipmap.login);
                Intent intent = new Intent(WXPayEntryActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            } else {
                //已经登陆过
                MobclickAgent.onEvent(WXPayEntryActivity.this, MobClick.Cancle_id);//埋点统计
                Dialog dialogZ = createLoadingDialog(WXPayEntryActivity.this, "正在退出登陆...");
                dialogZ.show();
                dialogZ.setCancelable(false);
                util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);
                dialogZ.dismiss();
                t.centershow(WXPayEntryActivity.this, "用户退出了登陆", 1000);
                login.setImageResource(R.mipmap.login);
                sendUserDoData("2", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            }
        }
    };

    public long[] getTime(Date endDate, Date curDate) {
        long[] time = new long[2];
        if (curDate == null || endDate == null) {
            return null;
        }
        long diff = endDate.getTime() - curDate.getTime();

        long days = diff / (1000 * 60 * 60 * 24);
        long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        //long minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
        time[0] = days;//天数
        time[1] = hours;//小时
        return time;
    }

    /***
     * 加载窗
     *
     * @param context
     * @param msg
     * @return
     */

    public Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.progressdialog_no_deal, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.anim);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(false);// 可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        loadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else {
                    return true;
                }
            }
        });
        return loadingDialog;
    }

    /***
     * 微信调起控件开始支付
     */
    private String pay_key = "payfail";

    public void WinXinPay() {
        //判断微信版本是否支持支付
        dialog.show();
        dialog.setCancelable(false);
        WXPay.setEnabled(false);//设置按钮不可用
        boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        if (isPaySupported) {
            //微信版本支持支付
            if (!util.isNetworkConnected(this)) {
                Toast.makeText(WXPayEntryActivity.this, "网络没有连接,请查看您的网络", Toast.LENGTH_LONG).show();
                yinLianHandler.sendEmptyMessage(50);
                return;
            }
            if (TextUtils.isEmpty(nickName) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
                setPay();//
                t.show(WXPayEntryActivity.this, "您还未登录哦", 1000);
                startActivity(new Intent(WXPayEntryActivity.this, LoginActivity.class).putExtra("qud", "qud"));
                finish();
                return;
            }
            if (TextUtils.isEmpty(VIP_TIME)) {
                t.show(WXPayEntryActivity.this, "请选择购买会员时间", 1000);
                yinLianHandler.sendEmptyMessage(50);
                return;
            }
            VIP_TIME.toString();
            //获取服务器参数
            WX wx = new WX();
            if (VIP_TIME.equals(vipYearType)) {
                wx.setTotal_fee(vipYearPrice);//支付金额
                body = body_year;
            }

//            else if (VIP_TIME.equals(vipSixType)) {
//                wx.setTotal_fee(vipSixPrice);//支付金额
//            }

            else if (VIP_TIME.equals(vipThreeType)) {
                wx.setTotal_fee(vipThreePrice);//支付金额
                body = body_three;
            } else if (VIP_TIME.equals(vipOneType)) {
                wx.setTotal_fee(vipThreePrice);//支付金额
                body = body_one;
            }
            wx.setBody(URLEncoder.encode(body));//商品描述
            wx.setSpbill_create_ip("null");//ip
            String json = JSON.toJSONString(wx);
            //发起请求
            RequestParams params = new RequestParams(Constant.WX_PAY_ORDER);
            params.setCacheMaxAge(0);//最大数据缓存时间
            params.setConnectTimeout(8000);//连接超时时间
            params.setCharset("UTF-8");
            params.addQueryStringParameter("data", json);

            org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    String jsonStr = result.toString();
                    try {
                        JSONObject json = new JSONObject(jsonStr);
                        //初始化是否成功
                        String respCode = json.getString("respCode");
                        if (respCode.equals("000")) {
                            PayReq req = new PayReq();
                            //保存验证信息
                            Constant.wxyz.setOut_trade_no(json.getString("out_trade_no"));
                            //支付调起参数
                            req.appId = json.getString("appid");
                            req.partnerId = json.getString("partnerid");
                            req.prepayId = json.getString("prepayid");
                            req.nonceStr = json.getString("noncestr");
                            req.timeStamp = json.getString("timestamp");
                            req.packageValue = json.getString("package");
                            req.sign = json.getString("sign");
                            req.extData = "app data"; // optional

                            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                            api.sendReq(req);

                            yinLianHandler.sendEmptyMessage(50);

                            //存储用户支付信息，应对支付验证失败情况(重要)
                            WXYZ wxyz = new WXYZ();
                            imeilLastId = aesUtils.getInstance().decrypt(userName).trim().substring(aesUtils.getInstance().decrypt(userName).length() - 1);
                            wxyz.setUserName(aesUtils.getInstance().decrypt(userName));
                            wxyz.setImeiLastId(imeilLastId);
                            wxyz.setOut_trade_no(Constant.wxyz.getOut_trade_no());//订单号
                            wxyz.setPayTime(URLEncoder.encode(VIP_TIME));
                            String wxjson = JSON.toJSONString(wxyz);
                            String aesJson = aesUtils.encrypt(wxjson);
                            util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.WX_USER_PAY_FAILED_FILE, pay_key, aesJson);

                        } else if (respCode.equals("111")) {
                            t.show(WXPayEntryActivity.this, "微信初始化环境错误", 2000);
                            yinLianHandler.sendEmptyMessage(50);
                            return;
                        }

                    } catch (JSONException e) {
                        yinLianHandler.sendEmptyMessage(50);
                        //e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    WinXinPay();
                    yinLianHandler.sendEmptyMessage(50);
                }

                @Override
                public void onCancelled(CancelledException cex) {
                }

                @Override
                public void onFinished() {
                }
            });

        } else {
            //微信版本不支持支付
            yinLianHandler.sendEmptyMessage(50);
            t.show(WXPayEntryActivity.this, "您还没有使用微信应用,或者您的微信版本不支持支付,请下载微信最新版本", 3000);
        }

    }

    /**
     * 微信回调
     *
     * @param
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        Log.i("spl", "回调1");
    }


    //微信后台验证
    @Override
    public void onResp(BaseResp resp) {
        dialogyz.show();
        dialogyz.setCancelable(false);
        if (!util.isNetworkConnected(this)) {
            dialogyz.dismiss();
            Toast.makeText(WXPayEntryActivity.this, "网络没有连接,请查看您的网络", Toast.LENGTH_LONG).show();
            return;
        }
        String errorStr = resp.errStr;
        int code = resp.errCode;
        //回调后验证用户是否支付成功
        switch (code) {
            case 0://支付成功后的界面
                //后台验证用户微信是否支付成功
                Log.i("viptimeStr", VIP_TIME);
                dialogyz.dismiss();
                weiXinYanZhen("");
                break;
            case -1:
                dialogyz.dismiss();
                alertFaild();
                //签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。" + String.valueOf(resp.errCode)
                //t.show(WXPayEntryActivity.this, "您的账号在另外一处登陆,请重新登陆微信再支付", 3000);
                t.show(WXPayEntryActivity.this, "支付异常", 3000);
                break;
            case -2://用户取消支付后的界面
                dialogyz.dismiss();
                alertFaild();
                t.show(WXPayEntryActivity.this, "您取消了支付", 3000);
                //用户取消支付删除应对微信支付失败的记录
                util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.WX_USER_PAY_FAILED_FILE);
                break;
        }
    }

    private boolean wxIsSend = true;

    //微信验证方法
    public void weiXinYanZhen(final String payJson) {
        if (wxIsSend == false) {
            return;
        }
        Log.i("sendStr", "执行了一次这个方法weiXinYanZhen");
        wxIsSend = false;
        dialogyz.show();
        dialogyz.setCancelable(false);
        if (TextUtils.isEmpty(nickName) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            wxIsSend = true;
            t.show(WXPayEntryActivity.this, "您还未登录哦", 2000);
            dialogyz.dismiss();
            startActivity(new Intent(WXPayEntryActivity.this, LoginActivity.class).putExtra("qud", "qud"));
            finish();
            return;
        }
        RequestParams params = null;
        if (TextUtils.isEmpty(payJson)) {
            if (TextUtils.isEmpty(Constant.wxyz.getOut_trade_no())) {
                wxIsSend = true;
                t.show(WXPayEntryActivity.this, "您还没有支付哦", 2000);
                dialogyz.dismiss();
                return;
            }
            WXYZ wxyz = new WXYZ();
            imeilLastId = aesUtils.getInstance().decrypt(userName).trim().substring(aesUtils.getInstance().decrypt(userName).length() - 1);
            wxyz.setUserName(aesUtils.getInstance().decrypt(userName));
            wxyz.setImeiLastId(imeilLastId);
            wxyz.setOut_trade_no(Constant.wxyz.getOut_trade_no());//订单号
            wxyz.setPayTime(URLEncoder.encode(VIP_TIME));
            String wxjson = JSON.toJSONString(wxyz);
            //发起请求
            params = new RequestParams(Constant.WX_PAY_YZ);
            params.setCacheMaxAge(0);//最大数据缓存时间
            params.setConnectTimeout(5000);//连接超时时间
            params.setCharset("UTF-8");
            params.addQueryStringParameter("data", wxjson);
        } else {
            //发起请求
            params = new RequestParams(Constant.WX_PAY_YZ);
            params.setCacheMaxAge(0);//最大数据缓存时间
            params.setConnectTimeout(5000);//连接超时时间
            params.setCharset("UTF-8");
            params.addQueryStringParameter("data", payJson);
        }

        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                wxIsSend = true;
                dialogyz.dismiss();
                String jsonStr = result.toString();
                if (TextUtils.isEmpty(jsonStr)) {
                    return;
                }
                try {
                    JSONObject object = new JSONObject(jsonStr);
                    Iterator keys = object.keys();
                    boolean isHave = false;//是否有错误节点
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (key.equals("error")) {
                            isHave = true;
                            break;
                        }
                    }
                    if (isHave == false) {
                        String msg = object.getString("respCode");
                        String respMsg = object.getString("respMsg");
                        if (msg.equals("000")) {
                            //微信支付成功
                            if (respMsg.equals("SUCCESS")) {
                                wxIsSend = true;
                                wxFaildIjk = 0;
                                isWx = true;
                                alertSucces();
                                //隐藏38元支付
//                                sanbapay.setVisibility(View.GONE);
//                                sanbaline.setVisibility(View.GONE);
                                //支付成功后删除应对微信支付失败的记录
                                util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.WX_USER_PAY_FAILED_FILE);
                                //支付处理
                                util.sharedPreferencesDelOrderData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status");
                                util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status", aesUtils.getInstance().encrypt("1"));
                                syncLocalData();
                                SendYouMeng();
                                alertDownApk();
                            } else {
                                wxFaildIjk++;
                                if (wxFaildIjk <= 3) {
                                    wxIsSend = true;
                                    t.show(WXPayEntryActivity.this, "正在验证您的支付情况,请耐心等待。", 1000);
                                    if (TextUtils.isEmpty(payJson)) {
                                        //不是做失败记录验证
                                        yinLianHandler.sendEmptyMessage(101);
                                    } else {
                                        //做失败记录验证
//                                        String json = payJson;
//                                        weiXinYanZhen(json);
                                        yinLianHandler.sendEmptyMessage(8989);
                                    }
                                } else {
                                    t.show(WXPayEntryActivity.this, "验证失败", 1000);
                                    alerPayFailTiShi();
                                }
                            }
                        } else {
                            wxFaildIjk++;
                            wxIsSend = true;
                            if (wxFaildIjk <= 3) {
                                t.show(WXPayEntryActivity.this, "正在验证您的支付情况,请不要离开哦", 1000);
                                if (TextUtils.isEmpty(payJson)) {
                                    //不是做失败记录验证
                                    yinLianHandler.sendEmptyMessage(101);//验证3次
                                } else {
                                    //做失败记录验证
//                                    String json = payJson;
//                                    weiXinYanZhen(json);
                                    yinLianHandler.sendEmptyMessage(8989);
                                }
                            } else {
                                t.show(WXPayEntryActivity.this, "验证失败", 1000);
                                alerPayFailTiShi();
                            }
                        }
                    } else if (isHave == true) {
                        //用户取消支付删除应对微信支付失败的记录
                        util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.WX_USER_PAY_FAILED_FILE);
                    }

                } catch (JSONException e) {
                    //wxFaildIjk++;
                    wxIsSend = true;
                    dialogyz.dismiss();
                    syncLocalData();
                    t.show(WXPayEntryActivity.this, "由于网络原因,请您重新进入支付界面，完成您的支付验证。", 3000);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                wxIsSend = true;
                dialogyz.dismiss();
                syncLocalData();
                t.show(WXPayEntryActivity.this, "由于服务器连接异常超时原因,请您重新进入支付界面，完成您的支付验证。", 3000);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    //弹窗
    private AlertDialog myDialog1 = null;

    private void alertSucces() {
        if (myDialog1 == null) {
            myDialog1 = new AlertDialog.Builder(WXPayEntryActivity.this).create();

            myDialog1.show();

            myDialog1.getWindow().setLayout(2 * util.getWidth() / 2, 2 * util.getHeight() / 7);

            myDialog1.getWindow().setContentView(R.layout.alert_succes);

            myDialog1.getWindow()
                    .findViewById(R.id.alert_btn)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            myDialog1.dismiss();
                        }

                    });
        } else {
            myDialog1.show();
        }
    }

    private AlertDialog myDialog = null;

    private void alertFaild() {

        if (myDialog == null) {
            myDialog = new AlertDialog.Builder(WXPayEntryActivity.this).create();

            myDialog.show();

            myDialog.getWindow().setLayout(2 * util.getWidth() / 2, 2 * util.getHeight() / 7);

            myDialog.getWindow().setContentView(R.layout.alert_faild);

            myDialog.getWindow()
                    .findViewById(R.id.alert_btn)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            myDialog.dismiss();
                        }

                    });
        } else {
            myDialog.show();
        }
    }

    //支付宝支付
    /**
     * call alipay sdk pay. 调用SDK支付
     * Alipay保存支付参数信息
     */

    Alipay alipayInfo = new Alipay();//保存支付信息
    public String notify_Url = null;

    public void ZFBPay() {
        //获取支付宝订单信息
        ZhiFuBaoPay.setEnabled(false);//按钮不可用
        dialog.show();
        dialog.setCancelable(false);
        if (TextUtils.isEmpty(nickName) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            setPay();//
            t.show(WXPayEntryActivity.this, "您还未登录哦", 2000);
            startActivity(new Intent(WXPayEntryActivity.this, LoginActivity.class).putExtra("qud", "qud"));
            finish();
            return;
        }
        if (!util.isNetworkConnected(WXPayEntryActivity.this)) {
            t.show(WXPayEntryActivity.this, "您的网络没有连接", 2000);
            yinLianHandler.sendEmptyMessage(50);
            return;
        }
        if (TextUtils.isEmpty(VIP_TIME)) {
            t.show(WXPayEntryActivity.this, "请选择购买会员时间", 2000);
            yinLianHandler.sendEmptyMessage(50);
            return;
        }
        //发起请求
        RequestParams params = new RequestParams(Constant.ZFB_QUERY);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(5000);//连接超时时间
        params.setCharset("UTF-8");

        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                String json = result.toString();
                try {
                    alipayInfo = JSON.parseObject(json, Alipay.class);
                    //支付宝初始化是否成功
                    if (alipayInfo.getRespCode().equals("000")) {
                        //获得支付宝支付参数
                        PARTNER = alipayInfo.getPartner();
                        SELLER = alipayInfo.getSeller();
                        RSA_PRIVATE = alipayInfo.getRsa_private();
                        notify_Url = alipayInfo.getNotify_url();
                        //RSA_PUBLIC = alipayInfo.getRsa_public();
                        startZFBPayHandler.sendEmptyMessage(10);
                        yinLianHandler.sendEmptyMessage(50);

                    } else {
                        t.show(WXPayEntryActivity.this, "支付宝初始化环境失败", 2000);
                        yinLianHandler.sendEmptyMessage(50);
                        return;
                    }
                } catch (Exception e) {
                    t.show(WXPayEntryActivity.this, "支付宝初始化环境失败", 2000);
                    yinLianHandler.sendEmptyMessage(50);
                    return;
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                yinLianHandler.sendEmptyMessage(50);
                ZFBPay();
                return;
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    Handler startZFBPayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                //支付参数确定
                if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE) || TextUtils.isEmpty(SELLER)) {
                    new AlertDialog.Builder(WXPayEntryActivity.this).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    //
                                    finish();
                                }
                            }).show();
                    return;
                }
                String bodyTitle = null;
                String payMoney = "";//
                if (VIP_TIME.equals(vipYearType)) {
                    //payMoney = "120";//支付金额
                    payMoney = (Double.parseDouble(vipYearPrice) - 10) + "";//支付金额
                    bodyTitle = body_year;
                }

//                else if (VIP_TIME.equals(vipSixType)) {
//                    //payMoney = "99";//支付金额
//                    payMoney = vipSixPrice;//支付金额
//                    bodyTitle = body_six;
//                }
                else if (VIP_TIME.equals(vipThreeType)) {
                    //payMoney = "20";//支付金额
                    payMoney = (Double.parseDouble(vipThreePrice) - 10) + "";//支付金额
                    bodyTitle = body_three;
                } else if (VIP_TIME.equals(vipOneType)) {
                    //payMoney = "20";//支付金额
                    payMoney = (Double.parseDouble(vipThreePrice) - 10) + "";//支付金额
                    bodyTitle = body_one;
                }

                String orderInfo = getOrderInfo(URLEncoder.encode(bodyTitle), URLEncoder.encode(body), payMoney);

                //保存支付宝支付信息，应对支付失败的情况（重要）,代码之所以在这里，是因为订单号在这里才进行生成。
                AplipayYZ aplipayYZ = new AplipayYZ();
                aplipayYZ.setOut_trade_no(Zfb_out_trade_no);
                aplipayYZ.setUserName(aesUtils.getInstance().decrypt(userName));
                imeilLastId = aesUtils.getInstance().decrypt(userName).trim().substring(aesUtils.getInstance().decrypt(userName).length() - 1);
                aplipayYZ.setImeiLastId(imeilLastId);
                aplipayYZ.setPayTime(URLEncoder.encode(VIP_TIME));
                final String zfbJson = JSON.toJSONString(aplipayYZ);
                String aesJson = aesUtils.encrypt(zfbJson);
                util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.ZHI_FU_BAO_USER_PAY_FAILED_FILE, pay_key, aesJson);

                /**
                 * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
                 */
                String sign = sign(orderInfo);
                try {
                    /**
                     * 仅需对sign 做URL编码
                     */
                    sign = URLEncoder.encode(sign, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                /**
                 * 完整的符合支付宝参数规范的订单信息
                 */
                final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

                Runnable payRunnable = new Runnable() {

                    @Override
                    public void run() {
                        // 构造PayTask 对象
                        PayTask alipay = new PayTask(WXPayEntryActivity.this);
                        // 调用支付接口，获取支付结果
                        String result = alipay.pay(payInfo, true);

                        Message msg = new Message();
                        msg.what = SDK_PAY_FLAG;
                        msg.obj = result;
                        zfbHandler.sendMessage(msg);
                    }
                };

                // 必须异步调用
                Thread payThread = new Thread(payRunnable);
                payThread.start();
            }
            super.handleMessage(msg);
        }
    };


    /**
     * get the sdk version. 获取SDK版本号
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(this);
        String version = payTask.getVersion();
        Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
    }

    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(String subject, String body, String price) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        Zfb_out_trade_no = getOutTradeNo();
        orderInfo += "&out_trade_no=" + "\"" + Zfb_out_trade_no + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + URLDecoder.decode(subject) + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + URLDecoder.decode(body) + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + notify_Url + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }


    //支付宝后台验证
    @SuppressLint("HandlerLeak")
    private Handler zfbHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            dialogyz.show();
            dialogyz.setCancelable(false);
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    final String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        //后台验证支付宝支付是否成功
                        //支付宝后台验证
                        isZfbSend = true;
                        dialogyz.dismiss();
                        zhiFuBaoYanZhen("");

                    } else {
                        //支付宝支付失败
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            alertFaild();
                            isZfbSend = true;
                            dialogyz.dismiss();
                            Toast.makeText(WXPayEntryActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();

                        } else {
                            isZfbSend = false;
                            alertFaild();
                            dialogyz.dismiss();
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(WXPayEntryActivity.this, "您取消了支付", Toast.LENGTH_SHORT).show();
                            //删除支付宝应对支付失败的记录
                            util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.ZHI_FU_BAO_USER_PAY_FAILED_FILE);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    //支付宝验证方法
    private boolean isZfbSend = false;

    public void zhiFuBaoYanZhen(final String payJson) {
        if (isZfbSend == false) {
            return;
        }
        Log.i("sendStr", "执行了一次这个方法zhiFuBaoYanZhen");
        isZfbSend = false;
        dialogyz.show();
        dialogyz.setCancelable(false);
        if (TextUtils.isEmpty(nickName) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            isZfbSend = true;
            t.show(WXPayEntryActivity.this, "您还未登录哦", 2000);
            dialogyz.dismiss();
            startActivity(new Intent(WXPayEntryActivity.this, LoginActivity.class).putExtra("qud", "qud"));
            finish();
            return;
        }
        RequestParams params = null;
        if (TextUtils.isEmpty(payJson)) {
            if (TextUtils.isEmpty(Zfb_out_trade_no)) {
                isZfbSend = true;
                t.show(WXPayEntryActivity.this, "您还没有支付哦", 2000);
                dialogyz.dismiss();
                return;
            }
            AplipayYZ aplipayYZ = new AplipayYZ();
            aplipayYZ.setOut_trade_no(Zfb_out_trade_no);
            aplipayYZ.setUserName(aesUtils.getInstance().decrypt(userName));
            imeilLastId = aesUtils.getInstance().decrypt(userName).trim().substring(aesUtils.getInstance().decrypt(userName).length() - 1);
            aplipayYZ.setImeiLastId(imeilLastId);
            aplipayYZ.setPayTime(URLEncoder.encode(VIP_TIME));
            final String zfbJson = JSON.toJSONString(aplipayYZ);
            //发起请求
            params = new RequestParams(Constant.ZFB_YZ);
            params.setCacheMaxAge(0);//最大数据缓存时间
            params.setConnectTimeout(5000);//连接超时时间
            params.setCharset("UTF-8");
            params.addQueryStringParameter("data", zfbJson);
        } else {
            params = new RequestParams(Constant.ZFB_YZ);
            params.setCacheMaxAge(0);//最大数据缓存时间
            params.setConnectTimeout(5000);//连接超时时间
            params.setCharset("UTF-8");
            params.addQueryStringParameter("data", payJson);
        }

        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                String ZFBJson = result.toString();
                if (TextUtils.isEmpty(ZFBJson)) {
                    isZfbSend = true;
                    return;
                }
                try {
                    JSONObject object = new JSONObject(ZFBJson);
                    Iterator keys = object.keys();
                    boolean isHave = false;
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (key.equals("error")) {
                            isHave = true;
                            break;
                        }
                    }
                    if (isHave == false) {
                        String respCode = object.getString("respCode");
                        String respMsg = object.getString("respMsg");
                        if (respCode.equals("000")) {
                            if (respMsg.equals("TRADE_SUCCESS") || respMsg.equals("TRADE_FINISHED")) {
                                isZfbSend = true;
                                zfbFaildIjk = 0;
                                isZhiFuBao = true;
                                alertSucces();
                                dialogyz.dismiss();
                                //隐藏38元支付
//                                sanbapay.setVisibility(View.GONE);
//                                sanbaline.setVisibility(View.GONE);
                                //删除支付宝应对支付失败的记录
                                util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.ZHI_FU_BAO_USER_PAY_FAILED_FILE);

                                //修改用户会员状态
                                util.sharedPreferencesDelOrderData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status");
                                util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status", aesUtils.getInstance().encrypt("1"));

                                syncLocalData();
                                SendYouMeng();
                                alertDownApk();
                            } else {
                                isZfbSend = true;
                                zfbFaildIjk++;
                                dialogyz.dismiss();
                                if (zfbFaildIjk <= 3) {
                                    t.show(WXPayEntryActivity.this, "正在验证您的支付情况,请耐心等待。", 1000);
                                    if (TextUtils.isEmpty(payJson)) {
                                        //不是做失败记录验证
                                        yinLianHandler.sendEmptyMessage(102);
                                    } else {
                                        //做失败记录验证
//                                        String json = payJson;
//                                        zhiFuBaoYanZhen(json);
                                        yinLianHandler.sendEmptyMessage(1919);
                                    }
                                } else {
                                    t.show(WXPayEntryActivity.this, "验证失败", 1000);
                                    alerPayFailTiShi();
                                }
                                return;
                            }

                        } else {
                            zfbFaildIjk++;
                            isZfbSend = true;
                            dialogyz.dismiss();
                            if (zfbFaildIjk <= 3) {
                                t.show(WXPayEntryActivity.this, "正在验证您的支付情况,请耐心等待.", 2000);
                                if (TextUtils.isEmpty(payJson)) {
                                    //不是做失败记录验证
                                    yinLianHandler.sendEmptyMessage(102);//验证3次
                                } else {
                                    //做失败记录验证
//                                    String json = payJson;
//                                    zhiFuBaoYanZhen(json);
                                    yinLianHandler.sendEmptyMessage(1919);
                                }
                            } else {
                                t.show(WXPayEntryActivity.this, "验证失败", 1000);
                                alerPayFailTiShi();
                            }
                            return;
                        }
                    } else if (isHave == true) {
                        //支付成功后删除应对微信支付失败的记录
                        util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.ZHI_FU_BAO_USER_PAY_FAILED_FILE);
                    }

                } catch (JSONException e) {
                    isZfbSend = true;
                    dialogyz.dismiss();
                    alertFaild();
                    //t.show(WXPayEntryActivity.this, "正在验证您的支付情况,请不要离开哦", 2000);
                    //zhiFuBaoYanZhen();//
                    syncLocalData();
                    t.show(WXPayEntryActivity.this, "由于网络原因,请您重新进入支付界面完成您的支付验证。", 3000);
                    return;
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                isZfbSend = true;
                dialogyz.dismiss();
                syncLocalData();
                t.show(WXPayEntryActivity.this, "由于网络原因,请您重新进入支付界面完成您的支付验证。", 3000);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }


    //支付成功后同步本地数据
    private String tel_phone;

    public void syncLocalData() {
        dialogyz.show();
        dialogyz.setCancelable(false);
        if (!util.isNetworkConnected(this)) {
            dialog.dismiss();
            t.show(this, "网络没有连接,请检查您的网络", 1000);
            return;
        }
        LoginBean info = new LoginBean();
        //手机铭文数据
        final String nickname = new String(Nick.getName());//昵称
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        final String username = util.getAndroidId(this);//用户名
        String iMeilLastId = username.substring(username.length() - 1);//lastid
        PhoneInfo phoneInfo = new PhoneInfo(WXPayEntryActivity.this);
        phoneInfo.getProvidersName();
        imsi = phoneInfo.getIMSI();//
        if (TextUtils.isEmpty(imsi)) {
            imsi = "1234567890";//手机卡号
        }
        final String email = "18376542390@163.com";//邮箱
        String mobieBrand = android.os.Build.BRAND;//手机品牌
        if (TextUtils.isEmpty(mobieBrand)) {
            mobieBrand = "mobieBrand is null";
        }
        String mobileModel = android.os.Build.MODEL; // 手机型号
        if (TextUtils.isEmpty(mobileModel)) {
            mobileModel = "mobileModel is null";
        }
        tel_phone = tm.getLine1Number();//手机号码
        if (TextUtils.isEmpty(tel_phone)) {
            tel_phone = "tel_phone is null";
        }
        String tele_supo = util.getTele_Supo(imsi, this);//运营商
        if (TextUtils.isEmpty(tele_supo)) {
            tele_supo = "telesupo is null";
        }
        String area = util.getAppMetaData(this, "UMENG_CHANNEL");//暂时测试渠道号
        if (TextUtils.isEmpty(area)) {
            area = "area is null";
        }
        password = aesUtils.decrypt(util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.PASS_DATA, "pass"));
        show = password;
        info.setUserName(username);
        info.setPassWord(util.getMD5Str(password));
        info.setNickName(URLEncoder.encode(nickname));
        info.setEmail(email);
        info.setImei(username);
        info.setImeiLastId(iMeilLastId);
        info.setImsi(imsi);
        info.setMobieBrand(mobieBrand);
        info.setMobileModel(mobileModel);
        info.setTel_phone(tel_phone);
        info.setTele_supo(tele_supo);
        info.setArea(area);

        final String json = JSON.toJSONString(info);
        String aesJson = aesUtils.encrypt(json);

        //发起请求
        RequestParams params = new RequestParams(Constant.LOGIN_INTERFACE);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(15000);//连接超时时间
        params.setCharset("UTF-8");
        params.addQueryStringParameter("data", aesJson);

        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                String jsonStr = result.toString();
                String aesJson = aesUtils.decrypt(jsonStr);
                if (TextUtils.isEmpty(aesJson)) {
                    syncLocalData();
                    return;
                }
                try {
                    JSONObject jo = new JSONObject(aesJson);//拿到整体json
                    String loginStatus = jo.getString("respMsg");//登陆是否成功信息判断
                    sharedLogin info = new sharedLogin();//shared保存
                    if (loginStatus.equals("fail")) {
                        Log.i("payJson", "同步失败");
                        dialogyz.dismiss();
                        //getServerInterface("2");
                        util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);
                        t.centershow(WXPayEntryActivity.this, "会员验证失败,请您重新登录进行验证。", 500);
                        Intent intent = new Intent(WXPayEntryActivity.this, LoginActivity.class);
                        startActivity(intent);
                        return;
                    } else if (loginStatus.equals("success")) {
                        //登陆后处理
                        Log.i("payJson", "同步成功");
                        JSONObject data = jo.getJSONObject("json");//成功后的信息
                        info.setId(data.getString("id"));
                        info.setVip_status(data.getString("vip_status"));
                        info.setUserName(data.getString("userName"));
                        info.setVip_lastTme(data.getString("vip_lastTime"));
                        info.setPay_count(data.getString("pay_count"));
                        info.setEmail(email);
                        info.setTel_phone(tel_phone);
                        //保存到shared
                        //保存数据之前清空旧的数据
                        util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);

                        //保存用户信息
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "id", aesUtils.getInstance().encrypt(info.getId()));
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "nickName", aesUtils.getInstance().encrypt(nickname));
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status", aesUtils.getInstance().encrypt(info.getVip_status()));
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName", aesUtils.getInstance().encrypt(info.getUserName()));
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_lastTme", aesUtils.getInstance().encrypt(info.getVip_lastTme()));
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord", aesUtils.getInstance().encrypt(show));
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "email", aesUtils.getInstance().encrypt(info.getEmail()));
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "tel_phone", aesUtils.getInstance().encrypt(info.getTel_phone()));

                        //存储一个特殊字符
                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show", aesUtils.getInstance().encrypt(show));

                        util.sharedPreferencesWriteData(WXPayEntryActivity.this, KeyFile.PASS_DATA, "pass", aesUtils.getInstance().encrypt(show));
                        dialogyz.dismiss();
                        if (Integer.parseInt(info.getPay_count()) >= 1) {
                            //隐藏38元支付
//                            sanbapay.setVisibility(View.GONE);
//                            sanbaline.setVisibility(View.GONE);
                        }
                        //getUserDataInfo();
                        //getServerInterface(info.getVip_status());
                    } else {
                        //t.show(WXPayEntryActivity.this, "未知异常,程序员调试", 3000);
                        Log.i("payJson", "同步失败");
                        dialogyz.dismiss();
                        util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);
                        t.centershow(WXPayEntryActivity.this, "会员验证失败,请您重新登录进行验证。", 500);
                        Intent intent = new Intent(WXPayEntryActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    //t.show(WXPayEntryActivity.this, "未知异常,程序员调试", 3000);
                    dialogyz.dismiss();
                    //getServerInterface("2");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.i("payJson", "同步失败");
                dialogyz.dismiss();
                util.sharedPreferencesDelByFileAllData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);
                t.centershow(WXPayEntryActivity.this, "会员验证失败,请您重新登录进行验证。", 500);
                Intent intent = new Intent(WXPayEntryActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    //友盟发送
    public void SendYouMeng() {
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    //应对微信支付宝支付失败无法上报的情况
    public void payFailProcess() {
        String wxAesJson = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.WX_USER_PAY_FAILED_FILE, pay_key);
        String zfbAesJson = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.ZHI_FU_BAO_USER_PAY_FAILED_FILE, pay_key);

        String wxJson = aesUtils.decrypt(wxAesJson);
        String zfbJson = aesUtils.decrypt(zfbAesJson);
        if (TextUtils.isEmpty(wxJson) && TextUtils.isEmpty(zfbJson)) {
            //不需要支付失败的处理
            return;
        }
        if (!TextUtils.isEmpty(wxJson)) {
            //有微信失败记录
            weiXinYanZhen(wxJson);
        }
        if (!TextUtils.isEmpty(zfbJson)) {
            zhiFuBaoYanZhen(zfbJson);
        }
    }

    private void initLoginUUi() {
        String usernameStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName");
        String passWordStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
        String showStr = util.sharedPreferencesReadData(WXPayEntryActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");
        if (TextUtils.isEmpty(showStr) || TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(passWordStr)) {
            //没有登陆
            login.setImageResource(R.mipmap.login);
        } else {
            login.setImageResource(R.mipmap.cancel);
        }
    }

    public void alerPayFailTiShi() {
        new android.support.v7.app.AlertDialog.Builder(this).setTitle("支付消息提示")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .setMessage("由于网络原因,验证您的支付状态失败,请您切换到应用的'海外导航'菜单界面,其他菜单亦可,再回到'会员'支付信息界面,系统会自动为您完成会员验证。")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        dialog.dismiss();
                    }
                }).show();
    }

    /***
     * 友盟统计
     */
    public void onResume() {
        super.onResume();
        //应对微信支付宝支付失败无法上报的情况
        initLoginUUi();
        payFailProcess();
        MobclickAgent.onPageStart("直播首页面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("直播首页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    private android.support.v7.app.AlertDialog.Builder dialog_Max;

    public void alertDownApk() {
//        String packageName = apkInfo.getPackName();
//        if (TextUtils.isEmpty(packageName)) {
//            return;
//        }
        if (!util.appIsExist(context, Constant.vpnVipPackageName)) {
            if (dialog_Max == null) {
                dialog_Max = new android.support.v7.app.AlertDialog.Builder(this);
                dialog_Max.setCancelable(false);
                dialog_Max.setTitle("消息提示");
                dialog_Max.setIcon(android.R.drawable.ic_dialog_info);
                dialog_Max.setMessage("送您VPN福利，马上下载吧。");
                dialog_Max.setPositiveButton("开始下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        apkDownLoad();
                    }
                }).show();

//            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            })
            } else {
                dialog_Max.show();
            }
        }

    }

    private String apkPath = "tvtovpn.apk";

    public void apkDownLoad() {
        /***
         * apk文件下载
         */
        if (util.isNetworkConnected(this)) {
            //网络已连接
        } else {
            util.showTextToast(context, "世界上最远的距离就是没网");
            return;
        }
        if (TextUtils.isEmpty(LiveActivity.apkUrl)) {
            return;
        }
        File file = new File(Util.getSDCardPath() + "/" + apkPath);
        if (file.exists()) {
            Util.deleteFile(file);
        }

        Request request = new Request.Builder().url(LiveActivity.apkUrl).build();
        OkHttp.getInstance().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("h_bl", "文件下载失败");
                mHandler.sendEmptyMessage(GO_DOWN_VPN_FAILED);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                String SDPath = Util.getSDCardPath();
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(SDPath, apkPath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        Log.d("h_bl", "progress=" + progress);
                        Message msg = mHandler.obtainMessage();
                        msg.what = GO_DOWN_VPN_PROGRESS;
                        msg.arg1 = progress;
                        msg.arg2 = (int) total;
                        mHandler.sendMessage(msg);
                    }
                    fos.flush();
                    Log.d("h_bl", "文件下载成功");
                    mHandler.sendEmptyMessage(GO_DOWN_VPN_SUCCES);
                } catch (Exception e) {
                    Log.d("h_bl", "文件下载失败");
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        Log.d("h_bl", "文件下载失败" + e.getMessage());
                    }
                }
            }
        });

    }

    ProgressDialog progressDialog;

    public void showProgressDialog(int current, int total) {
        //改变样式，水平样式的进度条可以显示出当前百分比进度
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("应用正在下载中，请稍候...");
        }
        //设置进度条最大值
        progressDialog.setProgress(current);
//      progressDialog.setMax(total / 1024 / 1024);
        progressDialog.setProgressNumberFormat(total / 1024 / 1024 + "MB");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }


    private void sendUserDoData(String type, String oprationtime) {
        //发起请求
        DoInfo data = new DoInfo();
        data.setUserName(util.getAndroidId(this));
        data.setType(type);
        data.setOperationTime(oprationtime);
        String json = com.alibaba.fastjson.JSONObject.toJSONString(data);
        String aesJson = aesUtils.encrypt(json);
        RequestParams params = new RequestParams(Constant.USER_DO_INTERFACE);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(5000);//连接超时时间
        params.setCharset("UTF-8");
        params.addQueryStringParameter("data", aesJson);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }


}
