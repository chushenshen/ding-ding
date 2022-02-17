package com.ss.dingding;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.dingding.utils.HttpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Calendar;
import java.util.List;

import kotlin.jvm.functions.Function1;
import okhttp3.Call;

public class FristActivity extends AppCompatActivity {

    private Button start;
    private Button openFloat;
    private Switch autoPowerOffSwitch;
    private View bgLayout;
    private TextView levelText;
    private EditText noticeUrlEdit;
    private final static int randomAddMinute = MyAppliction.random.nextInt(3) + 1;
    private Toast toast;
    private static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    private static final String EXTRA_KEY_CONFIRM = "android.intent.extra.KEY_CONFIRM";
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        setContentView(R.layout.activity_frist);
        start = findViewById(R.id.start);
        openFloat = findViewById(R.id.openFloat);
        autoPowerOffSwitch = findViewById(R.id.autoPowerOffSwitch);
        bgLayout = findViewById(R.id.bg_layout);
        levelText = findViewById(R.id.levelText);
        noticeUrlEdit = findViewById(R.id.noticeUrlEdit);
        noticeUrlEdit.setText(MyAppliction.noticeUrl);

        autoPowerOffSwitch.setChecked(MyAppliction.autoPowerOff);

        noticeUrlEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                MyAppliction.noticeUrl = s.toString();
                MyAppliction.save(v -> {
                    v.putString("noticeUrl", MyAppliction.noticeUrl);
                    return null;
                });
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAppliction.stop = !MyAppliction.stop;
                if (MyAppliction.stop) {
                    start.setText("开始");
                } else {
                    start.setText("停止");
                }
            }
        });

        openFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FloatWindowService.class);
                intent.setAction(FloatWindowService.ACTION_INPUT);
                startService(intent);
            }
        });
        autoPowerOffSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                {
                    MyAppliction.save(v -> {
                        v.putBoolean("autoPowerOff", isChecked);
                        MyAppliction.autoPowerOff = isChecked;
                        return null;
                    });
                }
        );
//        openDing("com.alibaba.android.rimet", getApplicationContext());

//        Intent intent = new Intent(FristActivity.this, MyAccessibilityService.class);
//        intent.putExtra("type", MyAccessibilityService.SHUTDOWN_TYPE);
//        startService(intent);
//        https://api.day.app/KaqCunxxZYn5kxre3SHZt4/推送标题/这里改成你自己的推送内容
//        finish();
//        openDing("com.alibaba.android.rimet", getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final int systemBattery = getSystemBattery(getApplicationContext());
//        levelText.setText(String.format("当前电量：%d%s", systemBattery, systemBattery <= 20 ? "，该充电了" : ""));
        if (systemBattery <= 20) {
//            bgLayout.setBackgroundColor(Color.parseColor("#F44336"));
        } else {
//            bgLayout.setBackgroundColor(Color.parseColor("#45DD45"));
        }

        final Calendar instance = Calendar.getInstance();
        final int hour = instance.get(Calendar.HOUR_OF_DAY);
        final int minute = instance.get(Calendar.MINUTE);

        //到达签到时间
        boolean isStartActiviry = false;
        if ((hour == 8 && (minute >= (40 + randomAddMinute)))) {
            isStartActiviry = true;
        }

//        if (hour >= 11) {
        if (hour == 18 && minute >= 20 + randomAddMinute) {
//        if (hour == 18 && minute > 0) {
            isStartActiviry = true;
        }
//        isStartActiviry = true;
        toast.setText("运行中");
        HttpUtils.request(MyAppliction.logUrl +
                String.format("运行中 random：%s, MyAppliction.stop：%s，isStartActiviry: %s, 电量：", randomAddMinute, MyAppliction.stop, isStartActiviry)
                + systemBattery);

        toast.show();

        if (!MyAppliction.stop && isStartActiviry) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (MyAppliction.autoPowerOff) {
                        //定时关机
                        if (hour == 8 && minute >= (43 + randomAddMinute)) {
                            shutdown();
                            return;
                        }
                        //定时关机
//                        if (hour >= 11) {
                        if (hour == 18 && minute >= (23 + randomAddMinute)) {
                            shutdown();
                            return;
                        }
                    }

                    if (!MyAppliction.stop) {
                        openDing("com.alibaba.android.rimet", getApplicationContext());
                        HttpUtils.request(MyAppliction.logUrl +
                                "运行中 打开钉钉，电量："
                                + systemBattery);
                        if (MyAppliction.isFirst) {
                            HttpUtils.request(String.format("%s%s签到/电量：%s", MyAppliction.noticeUrl,
                                    hour == 8 ? "上班" : "下班", systemBattery));
                            MyAppliction.isFirst = false;
                        }
                    }
                }
            }, 2000);
        }
    }

    public void shutdown() {
//        start.setText("关机");
        final int systemBattery = FristActivity.getSystemBattery(getApplicationContext());
        HttpUtils.request(MyAppliction.logUrl + "关机，电量：" + systemBattery);

        MyAppliction.stop = true;
        Intent intent = new Intent(FristActivity.this, MyAccessibilityService.class);
        intent.putExtra("type", MyAccessibilityService.SHUTDOWN_TYPE);
        startService(intent);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void openDing(String packageName, Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveInfo = apps.iterator().next();
        if (resolveInfo != null) {
            String className = resolveInfo.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
        }

        Intent intent = new Intent(FristActivity.this, MyAccessibilityService.class);
        intent.putExtra("type", MyAccessibilityService.LOGIN);
        startService(intent);
    }

    public static int getSystemBattery(Context context) {
        int level = 0;
        Intent batteryInfoIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        level = batteryInfoIntent.getIntExtra("level", 0);
        int batterySum = batteryInfoIntent.getIntExtra("scale", 100);
        int percentBattery = 100 * level / batterySum;
        return percentBattery;
    }

}
