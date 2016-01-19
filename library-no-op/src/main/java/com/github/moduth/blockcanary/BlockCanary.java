package com.github.moduth.blockcanary;

/**
 * <p>looper线程监控</p>
 * Created by markzhai on 2015/9/25.
 */
public class BlockCanary {

    private static BlockCanary sInstance = null;

    private BlockCanary() {
    }

    /**
     * 获得BlockCanary单例
     *
     * @return BlockCanary实例
     */
    public static BlockCanary get() {
        if (sInstance == null) {
            synchronized (BlockCanary.class) {
                if (sInstance == null) {
                    sInstance = new BlockCanary();
                }
            }
        }
        return sInstance;
    }

    /**
     * 开始主进程的主线程监控
     */
    public void startMainLooperMonitor() {
    }

    /**
     * 停止主进程的主线程监控
     */
    public void stopMainLooperMonitor() {
    }

    /**
     * 上传监控log文件
     */
    public void uploadMonitorLogFile() {
    }

    /**
     * 记录开启监控的时间到preference，可以在release包收到push通知后调用。
     */
    public void recordStartTime() {
    }

    /**
     * 是否监控时间结束，根据上次开启的时间(recordStartTime)和getConfigDuration计算出来。
     *
     * @return true则结束
     */
    public boolean isMonitorDurationEnd() {
        return true;
    }
}