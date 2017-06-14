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
import java.util.LinkedList;
import java.util.List;

public final class BlockCanaryInternals {

    LooperMonitor monitor;
    StackSampler stackSampler;
    CpuSampler cpuSampler;

    private static BlockCanaryInternals sInstance;
    private static BlockCanaryContext sContext;

    private List<BlockInterceptor> mInterceptorChain = new LinkedList<>();

    // TODO: 2017/3/3  BlockCanaryInternals构造函数 (4)
    public BlockCanaryInternals() {/*初始化*/

        stackSampler = new StackSampler(/*堆栈数据Sampler*/
                Looper.getMainLooper().getThread(),
                sContext.provideDumpInterval());

        cpuSampler = new CpuSampler(sContext.provideDumpInterval());/*Cpu数据Sampler*/

        /*设置自定义Printer,打印Looper dispatchMessage(msg)前后的时间*/
        setMonitor(new LooperMonitor(new LooperMonitor.BlockListener() {

            // TODO: 2017/3/3 卡顿时间超过设置:触发线程卡顿回调 （9）
            @Override
            public void onBlockEvent(long realTimeStart, long realTimeEnd,
                                     long threadTimeStart, long threadTimeEnd) {/*当页面卡顿时回调*/
                // Get recent thread-stack entries and cpu usage
                /*获取最新显存堆栈信息及Cpu使用情况*/
                ArrayList<String> threadStackEntries = stackSampler
                        .getThreadStackEntries(realTimeStart, realTimeEnd);
                if (!threadStackEntries.isEmpty()) {
                    BlockInfo blockInfo = BlockInfo.newInstance()
                            .setMainThreadTimeCost(realTimeStart, realTimeEnd, threadTimeStart, threadTimeEnd)
                            .setCpuBusyFlag(cpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                            .setRecentCpuRate(cpuSampler.getCpuRateInfo())
                            .setThreadStackEntries(threadStackEntries)
                            .flushString();
                    LogWriter.save(blockInfo.toString());/*保存卡顿信息*/

                    if (mInterceptorChain.size() != 0) {/*检查拦截器*/
                        for (BlockInterceptor interceptor : mInterceptorChain) {
                            // TODO: 2017/3/3 通知DisplayService等监听，弹出Notification提示出现线程卡顿 （12）
                            interceptor.onBlock(getContext().provideContext(), blockInfo);
                        }
                    }
                }
            }
        }, getContext().provideBlockThreshold()));

        LogWriter.cleanObsolete();/*重置过期数据*/
    }

    /**
     * Get BlockCanaryInternals singleton
     *
     * @return BlockCanaryInternals instance
     */
    // TODO: 2017/3/3 初始化BlockCanaryInternals (3)
    static BlockCanaryInternals getInstance() {
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
    /*添加拦截器*/
    void addBlockInterceptor(BlockInterceptor blockInterceptor) {
        mInterceptorChain.add(blockInterceptor);
    }
    /*设置自定义Printer*/
    private void setMonitor(LooperMonitor looperPrinter) {
        monitor = looperPrinter;
    }
    /*取样间隔*/
    long getSampleDelay() {
        return (long) (BlockCanaryInternals.getContext().provideBlockThreshold() * 0.8f);
    }
    /*数据保存路径*/
    static String getPath() {
        String state = Environment.getExternalStorageState();
        String logPath = BlockCanaryInternals.getContext()
                == null ? "" : BlockCanaryInternals.getContext().providePath();

        if (Environment.MEDIA_MOUNTED.equals(state)
                && Environment.getExternalStorageDirectory().canWrite()) {
            return Environment.getExternalStorageDirectory().getPath() + logPath;
        }
        return Environment.getDataDirectory().getAbsolutePath() + BlockCanaryInternals.getContext().providePath();
    }
    /*创建文件夹*/
    static File detectedBlockDirectory() {
        File directory = new File(getPath());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }
    /*获取所有Log文件*/
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
