package com.ss.dingding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @author MrLiu
 * @date 2020/5/8
 * desc 广播处理
 */
public class StartReceiver extends BroadcastReceiver {
    public StartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        toast.setText("自启动");
        toast.show();
        //此处及是重启的之后，打开我们app的方法
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent intentActivity = new Intent(context, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //非常重要，如果缺少的话，程序将在启动时报错
            intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //自启动APP（Activity）
            context.startActivity(intentActivity);
            //自启动服务（Service）
            //context.startService(intent);
        }
    }
}