package com.github.moduth.blockcanary;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import com.github.moduth.blockcanary.core.R;
import com.github.moduth.blockcanary.info.CpuSampler;
import com.github.moduth.blockcanary.info.ThreadStackSampler;
import com.github.moduth.blockcanary.log.Block;
import com.github.moduth.blockcanary.log.LogWriter;

import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by Abner on 16/1/22.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class BlockCanaryCore {

    private static final int MIN_INTERVAL_MILLIS = 300;

    private static BlockCanaryCore sInstance;
    public LooperPrinter mainLooperPrinter;
    public ThreadStackSampler threadStackSampler;
    public CpuSampler cpuSampler;
    public int blockThresholdMillis;

    public BlockCanaryCore() {
        blockThresholdMillis = BlockCanaryContextInner.get().getConfigBlockThreshold();
        long sampleIntervalMillis = sampleInterval(blockThresholdMillis);
        threadStackSampler = new ThreadStackSampler(Looper.getMainLooper().getThread(), sampleIntervalMillis);
        cpuSampler = new CpuSampler();
    }

    public void setMainLooperPrinter(LooperPrinter looperPrinter){
        mainLooperPrinter = looperPrinter;
    }

    /**
     * 获得BlockCanaryCore单例
     *
     * @return BlockCanaryCore实例
     */
    public static BlockCanaryCore get() {
        if (sInstance == null) {
            synchronized (BlockCanaryCore.class) {
                if (sInstance == null) {
                    sInstance = new BlockCanaryCore();
                }
            }
        }
        return sInstance;
    }


    private long sampleInterval(int blockThresholdMillis) {
        // 最小值保护
        long sampleIntervalMillis = blockThresholdMillis / 2;
        if (sampleIntervalMillis < MIN_INTERVAL_MILLIS) {
            sampleIntervalMillis = MIN_INTERVAL_MILLIS;
        }
        return sampleIntervalMillis;
    }
}
