package com.github.moduth.blockcanary;

import android.os.Handler;

/**
 * Handler thread wrapper
 *
 * @author markzhai
 */
class HandlerThread {

    private static HandlerThreadWrapper sLoopThread = new HandlerThreadWrapper("loop");
    private static HandlerThreadWrapper sWriteLogThread = new HandlerThreadWrapper("writelog");

    private HandlerThread() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Get handler of looper thread
     */
    public static Handler getTimerThreadHandler() {
        return sLoopThread.getHandler();
    }

    /**
     * Get handler of log-writer thread
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
