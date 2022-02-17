package com.ss.dingding.basefloat;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ss.dingding.MainActivity;
import com.ss.dingding.MyAppliction;
import com.ss.dingding.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * @author sun on 2018/12/27.
 */
public class InputWindow extends AbsFloatBase {
    public InputWindow(Context context) {
        super(context);
    }

    EditText editText;

    @Override
    public void create() {
        super.create();

        mViewMode = FULLSCREEN_TOUCHABLE;

        final View inflate = inflate(R.layout.main_layout_input_window);
        editText = findView(R.id.editText);

        requestFocus(true);

        inflate.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = editText.getText().toString();
                if ("001100".equals(password)) {
                    MyAppliction.floatWindow = false;
                    remove();
                } else {
                    try {
                        OkHttpUtils
                                .get()
                                .url(String.format("%s密码错误：%s", MyAppliction.noticeUrl,
                                        password))
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e, int id) {

                                    }

                                    @Override
                                    public void onResponse(String response, int id) {

                                    }
                                });
                    } catch (Exception e) {

                    }
                    Toast.makeText(inflate.getContext(), "密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onAddWindowFailed(Exception e) {

    }
}