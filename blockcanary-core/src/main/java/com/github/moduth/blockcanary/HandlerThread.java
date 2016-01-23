package com.github.moduth.blockcanary;

import android.os.Handler;

/**
 * Created by markzhai on 16/1/23
 *
 * @author markzhai
 */
class HandlerThread {

    private static HandlerThreadWrapper sLoopThread = new HandlerThreadWrapper("loop");
    private static HandlerThreadWrapper sWriteLogThread = new HandlerThreadWrapper("writelog");

    /**
     * 获得loop线程的handler
     *
     * @return loop线程
     */
    public static Handler getTimerThreadHandler() {
        return sLoopThread.getHandler();
    }

    /**
     * 获得写log线程的handler
     *
     * @return 写log线程的handler
     */
    public static Handler getWriteLogFileThreadHandler() {
        return sWriteLogThread.getHandler();
    }

    private static class HandlerThreadWrapper {
        private Handler handler = null;

        public HandlerThreadWrapper(String name) {
            android.os.HandlerThread handlerThread = new android.os.HandlerThread("BlockCanaryThread_" + name);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        public Handler getHandler() {
            return handler;
        }
    }
}
