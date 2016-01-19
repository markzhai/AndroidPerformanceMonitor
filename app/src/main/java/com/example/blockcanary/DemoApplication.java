package com.example.blockcanary;

import android.app.Application;
import android.content.Context;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.github.moduth.blockcanary.BlockCanary;

public class DemoApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        BlockCanary.install(this, new AppBlockCanaryContext()).start();

//        boolean shouldStart = true;
//        if (!BuildConfig.DEBUG) {
//            if (BlockCanary.get().isMonitorDurationEnd()) {
//                shouldStart = false;
//            }
//        }
//
//        if (shouldStart) {
//            BlockCanary.get().start();
//        }
    }

    public static Context getAppContext() {
        return sContext;
    }
}
