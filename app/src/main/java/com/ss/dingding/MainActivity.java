package com.ss.dingding;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.ss.dingding.basefloat.FloatWindowParamManager;
import com.ss.dingding.utils.RomUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        entry();
    }

    public void entry() {

        if (!isStartAccessibilityService(this, "MyAccessibilityService")) {
            startAccessibilityService();
        } else {
            ComponentName localComponentName = new ComponentName(MyAppliction.sInstance, StartReceiver.class);
            int i = MyAppliction.sInstance.getPackageManager().getComponentEnabledSetting(localComponentName);
//
            Intent intentActivity = new Intent(getApplicationContext(), FristActivity.class);
            intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent intentService = new Intent(getApplicationContext(), MyService.class);
            startService(intentService);
            //自启动APP（Activity）
            getApplicationContext().startActivity(intentActivity);

            //检查悬浮窗权限
            boolean permission = FloatWindowParamManager.checkPermission(getApplicationContext());
            if (permission && !RomUtils.isVivoRom()) {
                Toast.makeText(MainActivity.this, "获取悬浮窗权限", Toast.LENGTH_SHORT).show();
                if (MyAppliction.floatWindow) {
                    Intent intent = new Intent(getApplicationContext(), FloatWindowService.class);
                    intent.setAction(FloatWindowService.ACTION_INPUT);
                    startService(intent);
                }
            } else {
                FloatWindowParamManager.tryJumpToPermissionPage(getApplicationContext());
            }

            finish();
        }


    }

    /**
     * 前往设置界面开启服务
     */
    private void startAccessibilityService() {
        new AlertDialog.Builder(this)
                .setTitle("开启辅助功能")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("使用此项功能需要您开启辅助功能")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 隐式调用系统设置界面
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    }
                }).create().show();
    }

    /**
     * 判断AccessibilityService服务是否已经启动
     *
     * @param context
     * @param name
     * @return
     */
    public static boolean isStartAccessibilityService(Context context, String name) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : serviceInfos) {
            String id = info.getId();
            if (id.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
