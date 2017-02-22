package com.orangepeople.movies.orangepeopletv.Activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.orangepeople.movies.orangepeopletv.Bean.SharedBean.sharedLogin;
import com.orangepeople.movies.orangepeopletv.Bean.login.LoginBean;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.Model.DoInfo;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.MobClick;
import com.orangepeople.movies.orangepeopletv.Utils.Nick;
import com.orangepeople.movies.orangepeopletv.Utils.PhoneInfo;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.SystemBarTintManager;
import com.orangepeople.movies.orangepeopletv.wxapi.WXPayEntryActivity;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private ImageButton login, back;
    private EditText et_username, et_userpass;
    private String nickname;
    private String username;
    private String password;
    private String iMeilLastId;
    private String imsi;
    private String email;
    private String mobieBrand;
    private String mobileModel;
    private String tel_phone;
    private String tele_supo;
    private String area;
    private String show;
    private Util util;
    private AesUtils aesUtils;
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
        setContentView(R.layout.activity_login);
        context = this;
        initView();
        initData();
    }

    private void initStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
        login = (ImageButton) findViewById(R.id.login);
        back = (ImageButton) findViewById(R.id.back);
        et_username = (EditText) findViewById(R.id.et_username);
        et_userpass = (EditText) findViewById(R.id.et_pass);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.finish();
            }
        });
    }

    private void initData() {
        aesUtils = new AesUtils();
        util = new Util(this);
        String usernameStr = util.sharedPreferencesReadData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName");
        String passWordStr = util.sharedPreferencesReadData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
        String showStr = util.sharedPreferencesReadData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");

        if (TextUtils.isEmpty(showStr) || TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(passWordStr)) {
            //分配默认的昵称和密码
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            username = util.getAndroidId(this);//用户名
            et_username.setText(username);
            String twoPass = util.sharedPreferencesReadData(LoginActivity.this, KeyFile.PASS_DATA, "pass");
            String pa = aesUtils.decrypt(twoPass);
            if (TextUtils.isEmpty(pa)) {
                et_userpass.setText("123456");
            } else {
                et_userpass.setText(pa);
            }
            return;
        } else {
            et_username.setText(aesUtils.getInstance().decrypt(usernameStr));
            et_userpass.setText(aesUtils.getInstance().decrypt(passWordStr));
            return;
        }

    }

    /**
     * 登录逻辑处理
     *
     * @param
     */
    public void login() {
        MobclickAgent.onEvent(this, MobClick.Login_id);//埋点统计
        sendUserDoData("1", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        //已经登陆则显示注销登陆
//        if (isLogin) {
//            Dialog dialogZ = createLoadingDialog(LoginActivity.this, "正在退出登陆...");
//            dialogZ.show();
//            dialogZ.setCancelable(false);
//            stopVpnConnection();
//            util.sharedPreferencesDelByFileAllData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);
//            dialogZ.dismiss();
//            t.show(LoginActivity.this, "用户退出了登陆", 1000);
//            finish();
//            return;
//        } else {
        //否则开始登陆
        if (isEmty()) {
            //登陆逻辑处理
            final Dialog dialog = createLoadingDialog(this, "登录中...请稍候");
            dialog.show();
            dialog.setCancelable(false);
            if (!util.isNetworkConnected(this)) {
                dialog.dismiss();
                util.showTextToast(context, "世界上最远的距离就是没网");
                return;
            }
            LoginBean info = new LoginBean();
            //手机铭文数据
            nickname = new String(Nick.getName());//昵称
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            username = util.getAndroidId(this);//用户名
            iMeilLastId = username.substring(username.length() - 1);//lastid
            PhoneInfo phoneInfo = new PhoneInfo(LoginActivity.this);
            phoneInfo.getProvidersName();
            imsi = phoneInfo.getIMSI();//
            if (TextUtils.isEmpty(imsi)) {
                imsi = "1234567890";//手机卡号
            }
            email = "18376542390@163.com";//邮箱
            mobieBrand = android.os.Build.BRAND;//手机品牌
            if (TextUtils.isEmpty(mobieBrand)) {
                mobieBrand = "mobieBrand is null";
            }
            mobileModel = android.os.Build.MODEL; // 手机型号
            if (TextUtils.isEmpty(mobileModel)) {
                mobileModel = "mobileModel is null";
            }
            tel_phone = tm.getLine1Number();//手机号码
            if (TextUtils.isEmpty(tel_phone)) {
                tel_phone = "tel_phone is null";
            }
            tele_supo = util.getTele_Supo(imsi, this);//运营商
            if (TextUtils.isEmpty(tele_supo)) {
                tele_supo = "telesupo is null";
            }
            area = util.getAppMetaData(this, "UMENG_CHANNEL");//暂时测试渠道号
            if (TextUtils.isEmpty(area)) {
                area = "area is null";
            }
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

            String json = JSON.toJSONString(info);
            String aesJson = aesUtils.encrypt(json);
            //发起请求
            RequestParams params = new RequestParams(Constant.LOGIN_INTERFACE);
            params.setCacheMaxAge(0);//最大数据缓存时间
            params.setConnectTimeout(5000);//连接超时时间
            params.setCharset("UTF-8");
            params.addQueryStringParameter("data", aesJson);

            x.http().post(params, new Callback.CommonCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    Log.i("indexStr", "手动登陆发送成功");
                    String jsonStr = result.toString();
                    String aesJson = aesUtils.decrypt(jsonStr);
                    try {
                        if (TextUtils.isEmpty(aesJson)) {
                            Log.i("indexStr", "手动登陆发送成功后数据返回为空");
                            dialog.dismiss();
                            //getServerInterface("2");
                            return;
                        }
                        Log.i("aesStr", aesJson);
                        JSONObject jo = new JSONObject(aesJson);//拿到整体json
                        String loginStatus = jo.getString("respMsg");//登陆是否成功信息判断
                        sharedLogin info = new sharedLogin();//shared保存
                        if (loginStatus.equals("fail")) {
                            util.showTextToast(context, "登录失败!请核实您的账户信息或到会员菜单修改您的密码");
                            util.sharedPreferencesDelByFileAllData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);
                            dialog.dismiss();
                            //getServerInterface("2");
                            return;
                        } else if (loginStatus.equals("success")) {
                            //登陆后处理
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
                            util.sharedPreferencesDelByFileAllData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);

                            //保存用户信息
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "id", aesUtils.getInstance().encrypt(info.getId()));
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "nickName", aesUtils.getInstance().encrypt(nickname));
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status", aesUtils.getInstance().encrypt(info.getVip_status()));
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName", aesUtils.getInstance().encrypt(info.getUserName()));
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_lastTme", aesUtils.getInstance().encrypt(info.getVip_lastTme()));
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord", aesUtils.getInstance().encrypt(show));
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "email", aesUtils.getInstance().encrypt(info.getEmail()));
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "tel_phone", aesUtils.getInstance().encrypt(info.getTel_phone()));

                            //存储一个特殊字符
                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show", aesUtils.getInstance().encrypt(show));

                            util.sharedPreferencesWriteData(LoginActivity.this, KeyFile.PASS_DATA, "pass", aesUtils.getInstance().encrypt(show));

                            //isLogin = true;//已经登陆
                            util.showTextToast(LoginActivity.this, "登录成功");
                            dialog.dismiss();
                            if (Integer.parseInt(info.getPay_count()) >= 1) {
                                //隐藏38元支付
                                //Constant.isShowOneMonthPay = false;
                            }
                            String intenStr = getIntent().getStringExtra("qud");
                            //getServerInterface(info.getVip_status());
                            if (TextUtils.isEmpty(intenStr)) {//标识没有接受到字符串
                                Intent intent = new Intent(LoginActivity.this, WXPayEntryActivity.class);
                                startActivity(intent);
                                finish();
                            } else {//接收到了支付界面穿过来的要登陆的字符串,为了刷新支付界面数据
                                Intent intent = new Intent(LoginActivity.this, WXPayEntryActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            util.showTextToast(LoginActivity.this, "系统繁忙,请稍候再试");
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        util.showTextToast(LoginActivity.this, "系统繁忙,请稍候再试");
                        dialog.dismiss();
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    dialog.dismiss();
                    util.showTextToast(LoginActivity.this, "请重新登录" + ex.getMessage());
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


    /**
     * 对登陆非空判断
     */
    public boolean isEmty() {
        boolean flag = false;
        username = et_username.getText().toString();
        password = et_userpass.getText().toString();
        if (TextUtils.isEmpty(username)) {
            flag = false;
            util.showTextToast(LoginActivity.this, "请输入用户名");
        }
        if (TextUtils.isEmpty(password)) {
            flag = false;
            util.showTextToast(LoginActivity.this, "请输入密码");
        }
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            flag = true;
        }
        return flag;
    }

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
        return loadingDialog;
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
                String log = aesUtils.decrypt(result);
                log.toString();
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this, WXPayEntryActivity.class);
        startActivity(intent);
        finish();
    }


    //字体需要的设置
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /***
     * 友盟统计
     */
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("登录页面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("登录页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }
}

