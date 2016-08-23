/*
 * Copyright (C) 2016 MarkZhai (http://zhaiyifan.cn).
 *
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

import android.os.Environment;
import android.os.Looper;

import com.github.moduth.blockcanary.internal.BlockInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public final class BlockCanaryInternals {

    public LooperMonitor monitor;
    public StackSampler stackSampler;
    public CpuSampler cpuSampler;

    private static BlockCanaryInternals sInstance;
    private static BlockCanaryContext sContext;

    private BlockInterceptor mInterceptor;

    public BlockCanaryInternals() {

        stackSampler = new StackSampler(
                Looper.getMainLooper().getThread(),
                sContext.getConfigDumpInterval());

        cpuSampler = new CpuSampler(sContext.getConfigDumpInterval());

        setMonitor(new LooperMonitor(new BlockListener() {

            @Override
            public void onBlockEvent(long realTimeStart, long realTimeEnd,
                                     long threadTimeStart, long threadTimeEnd) {
                // Get recent thread-stack entries and cpu usage
                ArrayList<String> threadStackEntries = stackSampler
                        .getThreadStackEntries(realTimeStart, realTimeEnd);
                if (!threadStackEntries.isEmpty()) {
                    BlockInfo blockInfo = BlockInfo.newInstance()
                            .setMainThreadTimeCost(realTimeStart, realTimeEnd, threadTimeStart, threadTimeEnd)
                            .setCpuBusyFlag(cpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                            .setRecentCpuRate(cpuSampler.getCpuRateInfo())
                            .setThreadStackEntries(threadStackEntries)
                            .flushString();
                    LogWriter.save(blockInfo.toString());

                    if (getContext().isNeedDisplay() && mInterceptor != null) {
                        mInterceptor.onBlock(getContext().getContext(), blockInfo.timeStart);
                    }
                }
            }
        }, getContext().getConfigBlockThreshold()));

        LogWriter.cleanObsolete();
    }

    /**
     * Get BlockCanaryInternals singleton
     *
     * @return BlockCanaryInternals instance
     */
    public static BlockCanaryInternals getInstance() {
        if (sInstance == null) {
            synchronized (BlockCanaryInternals.class) {
                if (sInstance == null) {
                    sInstance = new BlockCanaryInternals();
                }
            }
        }
        return sInstance;
    }

    /**
     * set {@link BlockCanaryContext} implementation
     *
     * @param context context
     */
    public static void setContext(BlockCanaryContext context) {
        sContext = context;
    }

    public static BlockCanaryContext getContext() {
        return sContext;
    }

    public void setOnBlockInterceptor(BlockInterceptor blockInterceptor) {
        mInterceptor = blockInterceptor;
    }

    public void setMonitor(LooperMonitor looperPrinter) {
        monitor = looperPrinter;
    }

    public long getSampleDelay() {
        return (long) (BlockCanaryInternals.getContext().getConfigBlockThreshold() * 0.8f);
    }

    public static String getPath() {
        String state = Environment.getExternalStorageState();
        String logPath = BlockCanaryInternals.getContext()
                == null ? "" : BlockCanaryInternals.getContext().getLogPath();

        if (Environment.MEDIA_MOUNTED.equals(state)
                && Environment.getExternalStorageDirectory().canWrite()) {
            return Environment.getExternalStorageDirectory().getPath() + logPath;
        }
        return Environment.getDataDirectory().getAbsolutePath() + BlockCanaryInternals.getContext().getLogPath();
    }

    public static File detectedBlockDirectory() {
        File directory = new File(getPath());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public static File[] getLogFiles() {
        File f = detectedBlockDirectory();
        if (f.exists() && f.isDirectory()) {
            return f.listFiles(new BlockLogFileFilter());
        }
        return null;
    }

    static class BlockLogFileFilter implements FilenameFilter {

        private String TYPE = ".log";

        public BlockLogFileFilter() {

        }

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(TYPE);
        }
    }
}
