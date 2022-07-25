package com.tencent.shadow.sample.manager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.tencent.shadow.core.manager.installplugin.InstalledPlugin;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.dynamic.loader.PluginServiceConnection;
import com.tencent.shadow.sample.plugin.IMyAidlInterface;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SamplePluginManager extends FastPluginManager {
    public static final String KEY_PLUGIN_ZIP_PATH = "pluginZipPath";
    public static final String KEY_ACTIVITY_CLASSNAME = "KEY_ACTIVITY_CLASSNAME";
    public static final String KEY_EXTRAS = "KEY_EXTRAS";
    public static final String KEY_PLUGIN_PART_KEY = "KEY_PLUGIN_PART_KEY";
    public static final String PART_KEY_PLUGIN_MAIN_APP = "sample-plugin-app";
    public static final String PART_KEY_PLUGIN_ANOTHER_APP = "sample-plugin-app2";
    //TODO 修改
    public static final String PART_KEY_PLUGIN_BASE = "sample-plugin";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Context mCurrentContext;

    public SamplePluginManager(Context context) {
        super(context);
        mCurrentContext = context;
    }

    /**
     * @return PluginManager实现的别名，用于区分不同PluginManager实现的数据存储路径
     */
    @Override
    protected String getName() {
        return "sample-manager";
    }

    /**
     * @return 宿主中注册的PluginProcessService实现的类名
     */
    @Override
    protected String getPluginProcessServiceName(String partKey) {

        return "com.tencent.shadow.sample.introduce_shadow_lib.MainPluginProcessService";
    }

    @Override
    public void enter(final Context context, long fromId, Bundle bundle, final EnterCallback callback) {
        if (fromId == Constant.FROM_ID_START_ACTIVITY) {
            bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, "/data/local/tmp/plugin-debug.zip");
            bundle.putString(Constant.KEY_PLUGIN_PART_KEY, "sample-plugin");
            bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, "com.tencent.shadow.sample.plugin.MainActivity");
            onStartActivity(context, bundle, callback);
        } else if (fromId == Constant.FROM_ID_CALL_SERVICE) {
            callPluginService(context);
        } else {
            throw new IllegalArgumentException("不认识的fromId==" + fromId);
        }
    }

    private void onStartActivity(final Context context, Bundle bundle, final EnterCallback callback) {
        final String pluginZipPath = bundle.getString(Constant.KEY_PLUGIN_ZIP_PATH);
        final String partKey = bundle.getString(Constant.KEY_PLUGIN_PART_KEY);
        final String className = bundle.getString(Constant.KEY_ACTIVITY_CLASSNAME);
        if (className == null) {
            throw new NullPointerException("className == null");
        }
        final Bundle extras = bundle.getBundle(Constant.KEY_EXTRAS);

        if (callback != null) {
            final View view = LayoutInflater.from(mCurrentContext).inflate(R.layout.activity_load_plugin, null);
            callback.onShowLoadingView(view);
        }

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 加载插件
                    InstalledPlugin installedPlugin = installPlugin(pluginZipPath, null, true);

                    loadPlugin(installedPlugin.UUID, PART_KEY_PLUGIN_BASE);
//                    loadPlugin(installedPlugin.UUID, PART_KEY_PLUGIN_MAIN_APP);
                    callApplicationOnCreate(PART_KEY_PLUGIN_BASE);
                    callApplicationOnCreate(PART_KEY_PLUGIN_MAIN_APP);
                    // 创建插件 Intent
                    Intent pluginIntent = new Intent();
                    pluginIntent.setClassName(
                            context.getPackageName(),
                            className
                    );
                    if (extras != null) {
                        pluginIntent.replaceExtras(extras);
                    }
                    //转换为主宿主intent
                    Intent intent = mPluginLoader.convertActivityIntent(pluginIntent);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 启动插件 Activity
                    mPluginLoader.startActivityInPluginProcess(intent);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (callback != null) {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onCloseLoadingView();
                            callback.onEnterComplete();
                        }
                    });
                }
            }
        });
    }

    private void callPluginService(final Context context) {
        final String pluginZipPath = "/data/local/tmp/plugin-debug.zip";
        final String partKey = "sample-plugin";
        final String className = "com.tencent.shadow.sample.plugin.MyService";

        Intent pluginIntent = new Intent();
        pluginIntent.setClassName(context.getPackageName(), className);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InstalledPlugin installedPlugin
                            = installPlugin(pluginZipPath, null, true);//这个调用是阻塞的

                    loadPlugin(installedPlugin.UUID, partKey);

                    Intent pluginIntent = new Intent();
                    pluginIntent.setClassName(context.getPackageName(), className);

                    boolean callSuccess = mPluginLoader.bindPluginService(pluginIntent, new PluginServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(iBinder);
                            try {
                                String s = iMyAidlInterface.basicTypes(1, 2, true, 4.0f, 5.0, "6");
                                Log.i("SamplePluginManager", "iMyAidlInterface.basicTypes : " + s);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {
                            throw new RuntimeException("onServiceDisconnected");
                        }
                    }, Service.BIND_AUTO_CREATE);

                    if (!callSuccess) {
                        throw new RuntimeException("bind service失败 className==" + className);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
