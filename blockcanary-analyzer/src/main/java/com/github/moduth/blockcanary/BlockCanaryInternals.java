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

import android.content.Context;
import android.os.Environment;
import android.os.Looper;

import com.github.moduth.blockcanary.interceptor.DefaultBlockInterceptor;
import com.github.moduth.blockcanary.interceptor.BlockInterceptor;
import com.github.moduth.blockcanary.internal.BlockInfo;
import com.github.moduth.blockcanary.sampler.CpuSampler;
import com.github.moduth.blockcanary.sampler.StackSampler;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class BlockCanaryInternals {

    LooperMonitor monitor;
    StackSampler stackSampler;
    CpuSampler cpuSampler;

    /**
     * Null object pattern
     */
    private static final BlockInterceptor NULL_OBJ = new DefaultBlockInterceptor();

    private static BlockCanaryInternals sInstance;
    private static Context sContext;

    private List<BlockInterceptor> mInterceptorChain = new LinkedList<>();

    public BlockCanaryInternals() {

        stackSampler = new StackSampler(
                Looper.getMainLooper().getThread(),
                getInterceptor(0).provideDumpInterval());

        cpuSampler = new CpuSampler(getInterceptor(0).provideDumpInterval());

        setMonitor(new LooperMonitor(new LooperMonitor.BlockListener() {

            @Override
            public void onBlockEvent(long realTimeStart, long realTimeEnd,
                                     long threadTimeStart, long threadTimeEnd) {
                // Get recent thread-stack entries and cpu usage
                ArrayList<String> threadStackEntries = stackSampler
                        .getThreadStackEntries(realTimeStart, realTimeEnd);
                if (!threadStackEntries.isEmpty()) {
                    BlockInfo blockInfo = BlockInfo.newInstance()
                            .setQualifier(getInterceptor(0).provideQualifier())
                            .setUid(getInterceptor(0).provideUid())
                            .setNetwork(getInterceptor(0).provideNetworkType())
                            .setMainThreadTimeCost(realTimeStart, realTimeEnd, threadTimeStart, threadTimeEnd)
                            .setCpuBusyFlag(cpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                            .setRecentCpuRate(cpuSampler.getCpuRateInfo())
                            .setThreadStackEntries(threadStackEntries)
                            .flushString();
                    LogWriter.save(blockInfo.toString());

                    if (mInterceptorChain.size() != 0) {
                        for (BlockInterceptor interceptor : mInterceptorChain) {
                            interceptor.onBlock(BlockCanaryInternals.getContext(), blockInfo);
                        }
                    }
                }
            }
        }, getInterceptor(0).provideBlockThreshold(), getInterceptor(0).stopWhenDebugging()));

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
     * set {@link DefaultBlockInterceptor} implementation
     *
     * @param context context
     */
    public static void setContext(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }

    void addBlockInterceptor(BlockInterceptor blockInterceptor) {
        mInterceptorChain.add(blockInterceptor);
    }

    public BlockInterceptor getInterceptor(int pos) {
        if ( pos < 0 || mInterceptorChain.size() <= pos ) {
            return NULL_OBJ;
        }
        return mInterceptorChain.get(pos) ;
    }

    private void setMonitor(LooperMonitor looperPrinter) {
        monitor = looperPrinter;
    }

    public long getSampleDelay() {
        return (long) (getInterceptor(0).provideBlockThreshold() * 0.8f);
    }

    static String getPath() {
        String state = Environment.getExternalStorageState();
        String logPath = BlockCanaryInternals.getInstance().getInterceptor(0) == null ? "" : BlockCanaryInternals.getInstance().getInterceptor(0).providePath();

        if (Environment.MEDIA_MOUNTED.equals(state)
                && Environment.getExternalStorageDirectory().canWrite()) {
            return Environment.getExternalStorageDirectory().getPath() + logPath;
        }
        return BlockCanaryInternals.getContext().getFilesDir() + BlockCanaryInternals.getInstance().getInterceptor(0).providePath();
    }

    static File detectedBlockDirectory() {
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

    private static class BlockLogFileFilter implements FilenameFilter {

        private String TYPE = ".log";

        BlockLogFileFilter() {

        }

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(TYPE);
        }
    }
}
