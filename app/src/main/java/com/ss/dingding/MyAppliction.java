package com.ss.dingding;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;

import com.ss.dingding.utils.HttpUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;

public class MyAppliction extends Application {
    public static MyAppliction sInstance;

    static int cntStart = 0;


    PowerManager.WakeLock m_wklk;

    public static boolean stop = false;
    public static boolean floatWindow = true;
    public static Random random = new Random();
    public static boolean isFirst = true;
    private static SharedPreferences setting;
    public static boolean autoPowerOff = true;
    public static String noticeUrl = "";
    public static String logUrl = "http://q-jiang.com:8080/dingdinglog/api/v1/dingdingcss/log?log=";

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        setting = getSharedPreferences("setting", MODE_PRIVATE);
        autoPowerOff = setting.getBoolean("autoPowerOff", false);
        noticeUrl = setting.getString("noticeUrl", "");

        final int systemBattery = FristActivity.getSystemBattery(getApplicationContext());

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);

        HttpUtils.request(MyAppliction.logUrl + "开机，电量：" + systemBattery);

        sInstance = this;
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "cn");
        stop = false;
        m_wklk.acquire(); //设置保持唤醒
        registerActivityLifecycleCallbacks();
    }

    public static void save(Function1<SharedPreferences.Editor, Void> function0) {
        SharedPreferences.Editor editor = setting.edit();
        function0.invoke(editor);
//提交保存数据
        editor.commit();
    }

    private void registerActivityLifecycleCallbacks() {
//        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
//            @Override
//            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//
//            }
//
//            @Override
//            public void onActivityStarted(Activity activity) {
//                mCount++;
//                if (!mFront) {
//                    mFront = false;
//                    Log.e("", "AppContext------->处于前台");
//                }
//            }
//
//            @Override
//            public void onActivityResumed(Activity activity) {
//
//            }
//
//            @Override
//            public void onActivityPaused(Activity activity) {
//
//            }
//
//            @Override
//            public void onActivityStopped(Activity activity) {
//                mCount--;
//                if (mCount == 0) {
//                    mFront = false;
//                    Log.e("", "AppContext------->处于后台");
//                    timerTask();
//                }
//            }
//
//            @Override
//            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//
//            }
//
//            @Override
//            public void onActivityDestroyed(Activity activity) {
//
//            }
//        });
    }
}