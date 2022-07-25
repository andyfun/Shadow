package com.tencent.shadow.sample.host;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.dynamic.host.PluginManager;
import com.tencent.shadow.sample.introduce_shadow_lib.InitApplication;

public class MainActivity extends Activity {

    public static final int FROM_ID_START_ACTIVITY = 1001;
    public static final int FROM_ID_CALL_SERVICE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        textView.setText("宿主App");

        Button button = new Button(this);
        button.setText("启动插件");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);//防止点击重入

                PluginManager pluginManager = InitApplication.getPluginManager();
                pluginManager.enter(MainActivity.this, FROM_ID_START_ACTIVITY, new Bundle(), new EnterCallback() {
                    @Override
                    public void onShowLoadingView(View view) {
                        MainActivity.this.setContentView(view);//显示Manager传来的Loading页面
                    }

                    @Override
                    public void onCloseLoadingView() {
                        MainActivity.this.setContentView(linearLayout);
                    }

                    @Override
                    public void onEnterComplete() {
                        v.setEnabled(true);
                    }
                });
            }
        });

        linearLayout.addView(textView);
        linearLayout.addView(button);

        Button callServiceButton = new Button(this);
        callServiceButton.setText("调用插件Service，结果打印到Log");
        LinearLayout.LayoutParams lpS = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpS.topMargin = 20;
        callServiceButton.setLayoutParams(lpS);
        callServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);//防止点击重入

                PluginManager pluginManager = InitApplication.getPluginManager();
                pluginManager.enter(MainActivity.this, FROM_ID_CALL_SERVICE, null, null);
            }
        });

        linearLayout.addView(callServiceButton);


        Button buttonInit = new Button(this);
        buttonInit.setText("初始化");
        buttonInit.setOnClickListener(this::Init);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 20;
        buttonInit.setLayoutParams(lp);
        linearLayout.addView(buttonInit);


        Button buttonLogin = new Button(this);
        buttonLogin.setText("登录");
        LinearLayout.LayoutParams lpLogin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpLogin.topMargin = 20;
        buttonLogin.setLayoutParams(lpLogin);
        buttonLogin.setOnClickListener(this::Login);
        linearLayout.addView(buttonLogin);

        setContentView(linearLayout);
    }

    private void Init(View view) {
        Toast.makeText(this,"Init",Toast.LENGTH_SHORT).show();
    }

    private void Login(View view) {
        Toast.makeText(this,"login",Toast.LENGTH_SHORT).show();
    }


}