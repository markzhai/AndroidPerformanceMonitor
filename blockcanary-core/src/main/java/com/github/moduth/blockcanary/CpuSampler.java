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

import android.util.Log;

import com.github.moduth.blockcanary.log.Block;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CPU Sampler, dumps cpu usage information.
 * <p>
 * Created by markzhai on 2015/9/25.
 */
class CpuSampler extends Sampler {

    private static final String TAG = "CpuSampler";

    /**
     * TODO: Explain how we define cpu busy in README
     */
    private final int BUSY_TIME;
    private static final int MAX_ENTRY_COUNT = 10;

    private final LinkedHashMap<Long, String> mCpuInfoEntries = new LinkedHashMap<>();
    private int mPid = 0;
    private long mUserLast = 0;
    private long mSystemLast = 0;
    private long mIdleLast = 0;
    private long mIoWaitLast = 0;
    private long mTotalLast = 0;
    private long mAppCpuTimeLast = 0;

    public CpuSampler(long sampleIntervalMillis) {
        super(sampleIntervalMillis);
        BUSY_TIME = (int) (mSampleIntervalMillis * 1.2f);
    }

    @Override
    public void start() {
        super.start();
        reset();
    }

    /**
     * Get cpu rate information
     *
     * @return string show cpu rate information
     */
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
        if (end - start > mSampleIntervalMillis) {
            long s = start - mSampleIntervalMillis;
            long e = start + mSampleIntervalMillis;
            long last = 0;
            synchronized (mCpuInfoEntries) {
                for (Map.Entry<Long, String> entry : mCpuInfoEntries.entrySet()) {
                    long time = entry.getKey();
                    if (s < time && time < e) {
                        if (last != 0 && time - last > BUSY_TIME) {
                            return true;
                        }
                        last = time;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void doSample() {
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
            Log.e(TAG, "doSample: ", ex);
        } finally {
            try {
                if (cpuReader != null) {
                    cpuReader.close();
                }
                if (pidReader != null) {
                    pidReader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "doSample: ", e);
            }
        }
    }

    private void reset() {
        mUserLast = 0;
        mSystemLast = 0;
        mIdleLast = 0;
        mIoWaitLast = 0;
        mTotalLast = 0;
        mAppCpuTimeLast = 0;
    }

    private void parseCpuRate(String cpuRate, String pidCpuRate) {
        String[] cpuInfoArray = cpuRate.split(" ");
        if (cpuInfoArray.length < 9) {
            return;
        }
        // 从系统启动开始累计到当前时刻，用户态的CPU时间，不包含 nice值为负进程
        long user = Long.parseLong(cpuInfoArray[2]);
        // 从系统启动开始累计到当前时刻，nice值为负的进程所占用的CPU时间
        long nice = Long.parseLong(cpuInfoArray[3]);
        // 从系统启动开始累计到当前时刻，核心时间
        long system = Long.parseLong(cpuInfoArray[4]);
        // 从系统启动开始累计到当前时刻，除硬盘IO等待时间以外其它等待时间
        long idle = Long.parseLong(cpuInfoArray[5]);
        // 从系统启动开始累计到当前时刻，硬盘IO等待时间
        long ioWait = Long.parseLong(cpuInfoArray[6]);
        // CPU总时间 = 以上所有加上irq（硬中断）和softirq（软中断）的时间
        long total = user + nice + system + idle + ioWait + Long.parseLong(cpuInfoArray[7]) + Long.parseLong(cpuInfoArray[8]);

        String[] pidCpuInfos = pidCpuRate.split(" ");
        if (pidCpuInfos.length < 17) {
            return;
        }

        /*
         * utime  Amount of time that this process has been scheduled in user mode
         * stime  Amount of time that this process has been scheduled in kernel mode
         * cutime Amount of time that this process's waited-for children have been scheduled in user mode
         * cstime Amount of time that this process's waited-for children have been scheduled in kernel mode
         * processCpuTime = utime + stime + cutime + cstime, which includes all its threads' cpu time
         */
        long appCpuTime = Long.parseLong(pidCpuInfos[13]) + Long.parseLong(pidCpuInfos[14])
                + Long.parseLong(pidCpuInfos[15]) + Long.parseLong(pidCpuInfos[16]);

        if (mTotalLast != 0) {
            StringBuilder sb = new StringBuilder();
            long idleTime = idle - mIdleLast;
            long totalTime = total - mTotalLast;
            sb.append("cpu:").append((totalTime - idleTime) * 100L / totalTime).append("% ")
              .append("app:").append((appCpuTime - mAppCpuTimeLast) * 100L / totalTime).append("% ")
              .append("[").append("user:").append((user - mUserLast) * 100L / totalTime).append("% ")
              .append("system:").append((system - mSystemLast) * 100L / totalTime).append("% ")
              .append("ioWait:").append((ioWait - mIoWaitLast) * 100L / totalTime).append("% ]");
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
}