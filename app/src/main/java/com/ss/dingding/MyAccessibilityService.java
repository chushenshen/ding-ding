package com.ss.dingding;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.RequiresApi;

import com.ss.dingding.utils.HttpUtils;

import java.util.List;

/**
 * Created by  on 2017/8/8.
 */

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    public static final int SHUTDOWN_TYPE = 0x00001;//关机
    public static final int REBOOT_TYPE = 0x00002;//重启
    public static final int LOGIN = 0x00004;//重启
    private static final int OPEN_SHUTDOWN_VIEW = 22;//打开关机重启界面
    private static final int CLICK_SHUTDOWN = 11;//点击关机
    private static final int CLICK_REBOOT = 44;//点击重启
    private static final int CLICK_CONFIRM = 33;//点击确定

    private Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OPEN_SHUTDOWN_VIEW://打开关机
                    performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
                    break;
                case CLICK_SHUTDOWN://点击关机
                    clickBtn("关机");
                    break;
                case CLICK_REBOOT://点击关机
                    clickBtn("重新启动");
                    break;
                case CLICK_CONFIRM://点击确定
                    clickBtn("确定");
                    break;
                case LOGIN://点击确定
                    login();
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int type = intent.getIntExtra("type", 0);
        Log.d(TAG, "类型" + type);
        switch (type) {
            case SHUTDOWN_TYPE://关机
                handler.sendEmptyMessage(OPEN_SHUTDOWN_VIEW);
                handler.sendEmptyMessageDelayed(CLICK_SHUTDOWN, 3000);
                break;
            case REBOOT_TYPE://重启
                handler.sendEmptyMessage(OPEN_SHUTDOWN_VIEW);
                handler.sendEmptyMessageDelayed(CLICK_REBOOT, 1000);
                handler.sendEmptyMessageDelayed(CLICK_CONFIRM, 2000);
                break;
            case LOGIN://登陆
                handler.sendEmptyMessageDelayed(LOGIN, 2000);
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void clickBtn(String text) {

        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(370, 720);
        path.lineTo(343, 1027);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 20, 50));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);


//        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//        if (nodeInfo != null) {
//            List<AccessibilityNodeInfo> list = nodeInfo
//                    .findAccessibilityNodeInfosByText(text);
//
//            if (list != null && list.size() > 0) {
//                for (AccessibilityNodeInfo n : list) {
//                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    n.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                }
//            }
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void login() {
        try {
//            SparseArray<List<AccessibilityWindowInfo>> windowsOnAllDisplays = getWindowsOnAllDisplays();
//            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
            List<AccessibilityWindowInfo> windows = getWindows();
            for (AccessibilityWindowInfo window : windows) {
                AccessibilityNodeInfo nodeInfo = window.getRoot();
//                Log.d(TAG, window.getTitle()+"" );
                if ("com.alibaba.android.rimet".contentEquals(nodeInfo.getPackageName())) {
                    AccessibilityNodeInfo loginButton = nodeInfo.findAccessibilityNodeInfosByText("登录").get(0);
                    if (loginButton.getText().equals("登录")) {
                        //需要登陆
                        AccessibilityNodeInfo password = nodeInfo.getChild(0).getChild(5);
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "Cc504640003");
                        password.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

                        //勾选复选框
                        AccessibilityNodeInfo checkbox = nodeInfo.getChild(0).getChild(8);
                        checkbox.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        HttpUtils.request(MyAppliction.logUrl +
                                "自动登录");
                        //点击登陆
                        nodeInfo.getChild(0).getChild(6).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        checkbox.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        nodeInfo.getChild(0).getChild(6).performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    }
                }

            }

        } catch (Exception e) {

        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String packageName = event.getPackageName().toString();
        String className = event.getClassName().toString();
        Log.d(TAG, "----1-----" + packageName);
        Log.d(TAG, "----2-----" + className);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {

    }
}
