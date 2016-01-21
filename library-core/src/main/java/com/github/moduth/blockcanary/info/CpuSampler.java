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
package com.github.moduth.blockcanary.info;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.github.moduth.blockcanary.log.Block;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CPU监控
 * <p>
 * Created by markzhai on 2015/9/25.
 */
public class CpuSampler {

    private static final String TAG = CpuSampler.class.getSimpleName();
    private static final int SAMPLE_INTERVAL_MILLIS = 1000;
    private static final int BUSY_TIME = (int) (SAMPLE_INTERVAL_MILLIS * 1.2f);
    private static final int MAX_ENTRY_COUNT = 10;

    private final LinkedHashMap<Long, String> mCpuInfoEntries = new LinkedHashMap<>();
    private int mPid = 0;
    private long mUserLast = 0;
    private long mSystemLast = 0;
    private long mIdleLast = 0;
    private long mIoWaitLast = 0;
    private long mTotalLast = 0;
    private long mAppCpuTimeLast = 0;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doSample();

            BlockCanaryContext.get().getTimerThreadHandler().postDelayed(mRunnable, SAMPLE_INTERVAL_MILLIS);
        }
    };

    public void start() {
        reset();
        BlockCanaryContext.get().getTimerThreadHandler().removeCallbacks(mRunnable);
        BlockCanaryContext.get().getTimerThreadHandler().postDelayed(mRunnable, SAMPLE_INTERVAL_MILLIS);
    }

    public void stop() {
        BlockCanaryContext.get().getTimerThreadHandler().removeCallbacks(mRunnable);
    }

    private void reset() {
        mUserLast = 0;
        mSystemLast = 0;
        mIdleLast = 0;
        mIoWaitLast = 0;
        mTotalLast = 0;
        mAppCpuTimeLast = 0;
    }

    /**
     * 获取系统总CPU使用时间
     */
    private void doSample() {
        BufferedReader cpuReader = null;
        BufferedReader pidReader = null;
        try {
            cpuReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String cpuRate = cpuReader.readLine();
            if (cpuRate == null) {
                cpuRate = "";
            }

            if (mPid == 0) {
                mPid = android.os.Process.myPid();
            }
            pidReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + mPid + "/stat")), 1000);
            String pidCpuRate = pidReader.readLine();
            if (pidCpuRate == null) {
                pidCpuRate = "";
            }

            parseCpuRate(cpuRate, pidCpuRate);
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (cpuReader != null) {
                    cpuReader.close();
                }
                if (pidReader != null) {
                    pidReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseCpuRate(String cpuRate, String pidCpuRate) {
        String[] cpuInfos = cpuRate.split(" ");
        if (cpuInfos.length < 9) {
            return;
        }
        // 从系统启动开始累计到当前时刻，用户态的CPU时间，不包含 nice值为负进程
        long user = Long.parseLong(cpuInfos[2]);
        // 从系统启动开始累计到当前时刻，nice值为负的进程所占用的CPU时间
        long nice = Long.parseLong(cpuInfos[3]);
        // 从系统启动开始累计到当前时刻，核心时间
        long system = Long.parseLong(cpuInfos[4]);
        // 从系统启动开始累计到当前时刻，除硬盘IO等待时间以外其它等待时间
        long idle = Long.parseLong(cpuInfos[5]);
        // 从系统启动开始累计到当前时刻，硬盘IO等待时间
        long ioWait = Long.parseLong(cpuInfos[6]);

        // CPU总时间 = 以上所有加上irq（硬中断）和softirq（软中断）的时间
        long total = user + nice + system + idle + ioWait + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);

        String[] pidCpuInfos = pidCpuRate.split(" ");
        if (pidCpuInfos.length < 17) {
            return;
        }

        /*
         * utime  该任务在用户态运行的时间（第14个字段）
         * stime  该任务在核心态运行的时间（第15个字段）
         * cutime 所有已死线程在用户态运行的时间（第16个字段）
         * cstime 所有已死在核心态运行的时间（第17个字段）
         * 进程的总Cpu时间processCpuTime = utime + stime + cutime + cstime，该值包括其所有线程的cpu时间
         */
        long appCpuTime = Long.parseLong(pidCpuInfos[13]) + Long.parseLong(pidCpuInfos[14])
                + Long.parseLong(pidCpuInfos[15]) + Long.parseLong(pidCpuInfos[16]);

        if (mTotalLast != 0) {
            StringBuilder sb = new StringBuilder();
            long idleTime = idle - mIdleLast;
            long totalTime = total - mTotalLast;
            sb.append("cpu:").append((totalTime - idleTime) * 100L / totalTime).append("% ");
            sb.append("app:").append((appCpuTime - mAppCpuTimeLast) * 100L / totalTime).append("% ");
            sb.append("[").append("user:").append((user - mUserLast) * 100L / totalTime).append("% ");
            sb.append("system:").append((system - mSystemLast) * 100L / totalTime).append("% ");
            sb.append("ioWait:").append((ioWait - mIoWaitLast) * 100L / totalTime).append("% ]");
            synchronized (mCpuInfoEntries) {
                mCpuInfoEntries.put(System.currentTimeMillis(), sb.toString());
                if (mCpuInfoEntries.size() > MAX_ENTRY_COUNT) {
                    for (Map.Entry<Long, String> entry : mCpuInfoEntries.entrySet()) {
                        Long key = entry.getKey();
                        mCpuInfoEntries.remove(key);
                        break;
                    }
                }
            }
        }
        mUserLast = user;
        mSystemLast = system;
        mIdleLast = idle;
        mIoWaitLast = ioWait;
        mTotalLast = total;

        mAppCpuTimeLast = appCpuTime;
    }


    public String getCpuRateInfo() {
        StringBuilder sb = new StringBuilder();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        synchronized (mCpuInfoEntries) {
            for (Map.Entry<Long, String> entry : mCpuInfoEntries.entrySet()) {
                long time = entry.getKey();
                sb.append(dateFormat.format(time))
                        .append(' ')
                        .append(entry.getValue())
                        .append(Block.SEPARATOR);
            }
        }
        return sb.toString();
    }

    public boolean isCpuBusy(long start, long end) {
        if (end - start > SAMPLE_INTERVAL_MILLIS) {
            long s = start - SAMPLE_INTERVAL_MILLIS;
            long e = start + SAMPLE_INTERVAL_MILLIS;
            long last = 0;
            synchronized (mCpuInfoEntries) {
                for (Map.Entry<Long, String> entry : mCpuInfoEntries.entrySet()) {
                    long time = entry.getKey();
                    if (s < time && time < e) {
                        if (last != 0) {
                            if (time - last > BUSY_TIME) {
                                return true;
                            }
                        }
                        last = time;
                    }
                }
            }
        }
        return false;
    }
}