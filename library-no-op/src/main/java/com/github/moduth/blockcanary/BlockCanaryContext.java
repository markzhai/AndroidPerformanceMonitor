package com.github.moduth.blockcanary;

import android.content.Context;

import java.io.File;

public abstract class BlockCanaryContext {

    private static Context sAppContext;
    private static BlockCanaryContext sInstance = null;

    public BlockCanaryContext() {
    }

    public static void init(Context c, BlockCanaryContext g) {
        sAppContext = c;
        sInstance = g;
    }

    public static BlockCanaryContext get() {
        if (sInstance == null) {
            throw new RuntimeException("BlockCanaryContext not init");
        } else {
            return sInstance;
        }
    }

    public Context getContext() {
        return sAppContext;
    }

    /**
     * 标示符，可以唯一标示该安装版本号，如版本+渠道名+编译平台
     *
     * @return apk唯一标示符
     */
    public abstract String getQualifier();

    /**
     * 用户id，方便联系用户和后台查询定位
     *
     * @return 用户id
     */
    public abstract String getUid();

    /**
     * 网络类型，应用通常都有自己的一套，且较重需要监听网络状态变化，不放在本库中实现
     *
     * @return 2G/3G/4G/wifi等
     */
    public abstract String getNetworkType();

    /**
     * 卡慢性能监控 持续时长(小时)，开启后到达时长则关闭，暂时未实现该功能
     *
     * @return 监控持续时长（小时）
     */
    public abstract int getConfigDuration();

    /**
     * 卡慢性能监控 间隔(毫秒)，超出该间隔判定为卡慢。建议根据机器性能设置不同的数值，如2000/Cpu核数
     *
     * @return 卡慢阙值（毫秒）
     */
    public abstract int getConfigBlockThreshold();

    /**
     * 是否需要展示卡慢界面，如仅在Debug包开启
     *
     * @return 是否需要展示卡慢界面
     */
    public abstract boolean isNeedDisplay();

    /**
     * Log文件保存的位置，如"/blockcanary/log"
     *
     * @return Log文件保存的位置
     */
    public abstract String getLogPath();

    /**
     * 压缩文件
     *
     * @param src  压缩前的文件
     * @param dest 压缩后的文件
     * @return 压缩是否成功
     */
    public abstract boolean zipLogFile(File[] src, File dest);

    /**
     * 上传日志
     *
     * @param zippedFile 压缩后的文件
     */
    public abstract void uploadLogFile(File zippedFile);
}