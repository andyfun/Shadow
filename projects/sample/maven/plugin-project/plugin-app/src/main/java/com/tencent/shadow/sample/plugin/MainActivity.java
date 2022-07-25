package com.tencent.shadow.sample.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ztgame.mobileappsdk.common.IZTLibBase;
import com.ztgame.mobileappsdk.common.IZTListener;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}