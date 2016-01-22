/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.moduth.blockcanary;

import android.os.Looper;

import com.github.moduth.blockcanary.info.CpuSampler;
import com.github.moduth.blockcanary.info.ThreadStackSampler;
import com.github.moduth.blockcanary.log.Block;
import com.github.moduth.blockcanary.log.LogWriter;

import java.util.ArrayList;

/**
 * Created by Abner on 16/1/22.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class BlockCanaryCore {

    public LooperPrinter mainLooperPrinter;
    public ThreadStackSampler threadStackSampler;
    public CpuSampler cpuSampler;

    private static final int MIN_INTERVAL_MILLIS = 300;

    private static BlockCanaryCore sInstance;
    private static IBlockCanaryContext sBlockCanaryContext;

    private OnBlockEventInterceptor mOnBlockEventInterceptor;

    public BlockCanaryCore() {
        int blockThresholdMillis = getContext().getConfigBlockThreshold();
        long sampleIntervalMillis = sampleInterval(blockThresholdMillis);
        threadStackSampler = new ThreadStackSampler(
                Looper.getMainLooper().getThread(), sampleIntervalMillis);
        cpuSampler = new CpuSampler();

        setMainLooperPrinter(new LooperPrinter(new BlockListener() {

            @Override
            public void onBlockEvent(long realTimeStart, long realTimeEnd,
                                     long threadTimeStart, long threadTimeEnd) {
                // 查询这段时间内的线程堆栈调用情况，CPU使用情况
                ArrayList<String> threadStackEntries = threadStackSampler
                        .getThreadStackEntries(realTimeStart, realTimeEnd);
                // Log.d("BlockCanary", "threadStackEntries: " + threadStackEntries.size());
                if (threadStackEntries.size() > 0) {
                    Block block = Block.newInstance()
                            .setMainThreadTimeCost(realTimeStart, realTimeEnd,
                                    threadTimeStart, threadTimeEnd)
                            .setCpuBusyFlag(cpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                            .setRecentCpuRate(cpuSampler.getCpuRateInfo())
                            .setThreadStackEntries(threadStackEntries)
                            .flushString();
                    LogWriter.saveLooperLog(block.toString());

                    if (getContext().isNeedDisplay() && mOnBlockEventInterceptor != null) {
                        mOnBlockEventInterceptor.onBlockEvent(getContext().getContext(), block.timeStart);
                    }
                }
            }
        }, blockThresholdMillis));
        LogWriter.cleanOldFiles();
    }

    public void setMainLooperPrinter(LooperPrinter looperPrinter) {
        mainLooperPrinter = looperPrinter;
    }

    /**
     * Get BlockCanaryCore singleton
     *
     * @return BlockCanaryCore instance
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
        // minimum protect
        long sampleIntervalMillis = blockThresholdMillis / 2;
        if (sampleIntervalMillis < MIN_INTERVAL_MILLIS) {
            sampleIntervalMillis = MIN_INTERVAL_MILLIS;
        }
        return sampleIntervalMillis;
    }

    public static void setIBlockCanaryContext(IBlockCanaryContext blockCanaryContext) {
        sBlockCanaryContext = blockCanaryContext;
    }

    public void setOnBlockEventInterceptor(OnBlockEventInterceptor onBlockEventInterceptor) {
        mOnBlockEventInterceptor = onBlockEventInterceptor;
    }

    public static IBlockCanaryContext getContext() {
        return sBlockCanaryContext;
    }
}
