package com.ss.dingding;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.ss.dingding.utils.HttpUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    private Runnable runnable;
    private Handler handler;


    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * 方式一：采用Handler的postDelayed(Runnable, long)方法
         */
        handler = new Handler();
        runnable = new Runnable() {

            @Override
            public void run() {
                // handler自带方法实现定时器
                handler.postDelayed(this, 1000 * 10);//每隔10s执行
                MyAppliction.cntStart++;
                Log.e("", "run: " + MyAppliction.cntStart);
                try {

                    final Calendar instance = Calendar.getInstance();
                    final int hour = instance.get(Calendar.HOUR_OF_DAY);

                    if (MyAppliction.autoPowerOff) {
                        //定时关机
                        if (hour > 9 && hour < 18) {
                            shutdown();
                            return;
                        }
                        //定时关机
                        if (hour > 18) {
                            shutdown();
                            return;
                        }
                    }
                } catch (Exception e) {
                    HttpUtils.request(MyAppliction.logUrl + "关机Service异常，电量：" + e.getMessage());
                }

                if (!MyAppliction.stop) {
                    Log.e("timerTask", "timerTask   stop" + MyAppliction.stop);
                    Log.e("timerTask", "timerTask   " + MyAppliction.cntStart);
                    isRunningForegroundToApp1(getApplicationContext(), FristActivity.class);
                }

            }
        };
        handler.postDelayed(runnable, 0);//延时多长时间启动定时器
    }

    public void shutdown() {
//        start.setText("关机");
        final int systemBattery = FristActivity.getSystemBattery(getApplicationContext());
        HttpUtils.request(MyAppliction.logUrl + "关机Service，电量：" + systemBattery);

        Intent intent = new Intent(getApplicationContext(), MyAccessibilityService.class);
        intent.putExtra("type", MyAccessibilityService.SHUTDOWN_TYPE);
        startService(intent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("NewApi")
    public static void isRunningForegroundToApp1(Context context, final Class Class) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(20);
        /**枚举进程*/

        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            //*找到本应用的 task，并将它切换到前台
            if (taskInfo.baseActivity.getPackageName().equals(context.getPackageName())) {
                activityManager.moveTaskToFront(taskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                Intent intent = new Intent(context, Class);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setAction(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 如果Service被杀死，干掉通知
//        Log.d("timerTask", "DaemonService---->onDestroy，前台service被杀死");
        // 重启自己
        Intent intent = new Intent(getApplicationContext(), MyService.class);
        startService(intent);
    }

}