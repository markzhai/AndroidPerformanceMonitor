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
package com.github.moduth.blockcanary.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.moduth.blockcanary.BlockCanaryInternals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Information to trace a block.
 */
public final class BlockInfo {

    private static final String TAG = "BlockInfo";

    public static final SimpleDateFormat TIME_FORMATTER =
            new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);

    public static final String SEPARATOR = "\r\n";
    public static final String KV = " = ";

    public static final String KEY_QUA = "qua";
    public static final String KEY_MODEL = "model";
    public static final String KEY_API = "api-level";
    public static final String KEY_IMEI = "imei";
    public static final String KEY_UID = "uid";
    public static final String KEY_CPU_CORE = "cpu-core";
    public static final String KEY_CPU_BUSY = "cpu-busy";
    public static final String KEY_CPU_RATE = "cpu-rate";
    public static final String KEY_TIME_COST = "time";
    public static final String KEY_THREAD_TIME_COST = "thread-time";
    public static final String KEY_TIME_COST_START = "time-start";
    public static final String KEY_TIME_COST_END = "time-end";
    public static final String KEY_STACK = "stack";
    public static final String KEY_PROCESS = "process";
    public static final String KEY_VERSION_NAME = "versionName";
    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_NETWORK = "network";
    public static final String KEY_TOTAL_MEMORY = "totalMemory";
    public static final String KEY_FREE_MEMORY = "freeMemory";
    public static final String NEW_INSTANCE = "newInstance: ";

    public String qualifier;
    public String model;
    public String apiLevel = "";
    public String imei = "";
    public String uid;
    public int cpuCoreNum;
    public String processName;
    public String versionName = "";
    public int versionCode;
    public String network;
    public String freeMemory;
    public String totalMemory;
    public long timeCost;
    public long threadTimeCost;
    public String timeStart;
    public String timeEnd;
    public boolean cpuBusy;
    public String cpuRateInfo;
    public ArrayList<String> threadStackEntries = new ArrayList<>();
    public File logFile;

    private StringBuilder basicSb = new StringBuilder();
    private StringBuilder cpuSb = new StringBuilder();
    private StringBuilder timeSb = new StringBuilder();
    private StringBuilder stackSb = new StringBuilder();
    private static final String EMPTY_IMEI = "empty_imei";

    private BlockInfo() {
    }

    public static BlockInfo newInstance() {
        BlockInfo blockInfo = new BlockInfo();
        Context context = BlockCanaryInternals.getContext().getContext();
        if (blockInfo.versionName == null || blockInfo.versionName.length() == 0) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                blockInfo.versionCode = info.versionCode;
                blockInfo.versionName = info.versionName;
            } catch (Throwable e) {
                Log.e(TAG, NEW_INSTANCE, e);
            }
        }

        if (blockInfo.imei == null || blockInfo.imei.length() == 0) {
            try {
                TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                blockInfo.imei = mTManager.getDeviceId();
            } catch (Exception e) {
                Log.e(TAG, NEW_INSTANCE, e);
                blockInfo.imei = EMPTY_IMEI;
            }
        }
        blockInfo.qualifier = BlockCanaryInternals.getContext().getQualifier();
        blockInfo.apiLevel = Build.VERSION.SDK_INT + " " + VERSION.RELEASE;
        blockInfo.model = Build.MODEL;
        blockInfo.uid = BlockCanaryInternals.getContext().getUid();
        blockInfo.cpuCoreNum = PerformanceUtils.getNumCores();
        blockInfo.processName = ProcessUtils.myProcessName();
        blockInfo.network = BlockCanaryInternals.getContext().getNetworkType();
        blockInfo.freeMemory = String.valueOf(PerformanceUtils.getFreeMemory());
        blockInfo.totalMemory = String.valueOf(PerformanceUtils.getTotalMemory());
        return blockInfo;
    }

    /**
     * Create {@link BlockInfo} from saved log file.
     *
     * @param file looper log file
     * @return LooperLog created from log file
     */
    public static BlockInfo newInstance(File file) {
        BlockInfo blockInfo = new BlockInfo();
        blockInfo.logFile = file;

        BufferedReader reader = null;
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(file), "UTF-8");

            reader = new BufferedReader(in);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith(KEY_QUA)) {
                    blockInfo.qualifier = line.split(KV)[1];
                } else if (line.startsWith(KEY_MODEL)) {
                    blockInfo.model = line.split(KV)[1];
                } else if (line.startsWith(KEY_API)) {
                    blockInfo.apiLevel = line.split(KV)[1];
                } else if (line.startsWith(KEY_IMEI)) {
                    blockInfo.imei = line.split(KV)[1];
                } else if (line.startsWith(KEY_UID)) {
                    blockInfo.uid = line.split(KV)[1];
                } else if (line.startsWith(KEY_TIME_COST_START)) {
                    blockInfo.timeStart = line.split(KV)[1];
                } else if (line.startsWith(KEY_TIME_COST_END)) {
                    blockInfo.timeEnd = line.split(KV)[1];
                } else if (line.startsWith(KEY_TIME_COST)) {
                    blockInfo.timeCost = Long.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_THREAD_TIME_COST)) {
                    blockInfo.threadTimeCost = Long.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_PROCESS)) {
                    blockInfo.processName = line.split(KV)[1];
                } else if (line.startsWith(KEY_VERSION_NAME)) {
                    blockInfo.versionName = line.split(KV)[1];
                } else if (line.startsWith(KEY_VERSION_CODE)) {
                    blockInfo.versionCode = Integer.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_NETWORK)) {
                    blockInfo.network = line.split(KV)[1];
                } else if (line.startsWith(KEY_TOTAL_MEMORY)) {
                    blockInfo.totalMemory = line.split(KV)[1];
                } else if (line.startsWith(KEY_FREE_MEMORY)) {
                    blockInfo.freeMemory = line.split(KV)[1];
                } else if (line.startsWith(KEY_CPU_CORE)) {
                    blockInfo.cpuCoreNum = Integer.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_CPU_BUSY)) {
                    blockInfo.cpuBusy = Boolean.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_CPU_RATE)) {
                    String[] split = line.split(KV);
                    if (split.length > 1) {
                        StringBuilder cpuRateSb = new StringBuilder(split[1]);
                        cpuRateSb.append(line.split(KV)[1]).append(SEPARATOR);
                        line = reader.readLine();

                        // read until SEPARATOR appears
                        while (line != null) {
                            if (!line.equals("")) {
                                cpuRateSb.append(line).append(SEPARATOR);
                            } else {
                                break;
                            }
                            line = reader.readLine();
                        }
                        blockInfo.cpuRateInfo = cpuRateSb.toString();
                    }
                } else if (line.startsWith(KEY_STACK)) {
                    StringBuilder stackSb = new StringBuilder(line.split(KV)[1]);
                    line = reader.readLine();

                    // read until file ends
                    while (line != null) {
                        if (!line.equals("")) {
                            stackSb.append(line).append(SEPARATOR);
                        } else if (stackSb.length() > 0) {
                            // ignore continual blank lines
                            blockInfo.threadStackEntries.add(stackSb.toString());
                            stackSb = new StringBuilder();
                        }
                        line = reader.readLine();
                    }
                }
            }
            reader.close();
            reader = null;
        } catch (Throwable t) {
            Log.e(TAG, NEW_INSTANCE, t);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                Log.e(TAG, NEW_INSTANCE, e);
            }
        }
        blockInfo.flushString();
        return blockInfo;
    }

    public BlockInfo setCpuBusyFlag(boolean busy) {
        cpuBusy = busy;
        return this;
    }

    public BlockInfo setRecentCpuRate(String info) {
        cpuRateInfo = info;
        return this;
    }

    public BlockInfo setThreadStackEntries(ArrayList<String> threadStackEntries) {
        this.threadStackEntries = threadStackEntries;
        return this;
    }

    public BlockInfo setMainThreadTimeCost(long realTimeStart, long realTimeEnd, long threadTimeStart, long threadTimeEnd) {
        timeCost = realTimeEnd - realTimeStart;
        threadTimeCost = threadTimeEnd - threadTimeStart;
        timeStart = TIME_FORMATTER.format(realTimeStart);
        timeEnd = TIME_FORMATTER.format(realTimeEnd);
        return this;
    }

    public BlockInfo flushString() {
        String separator = SEPARATOR;
        basicSb.append(KEY_QUA).append(KV).append(qualifier).append(separator);
        basicSb.append(KEY_VERSION_NAME).append(KV).append(versionName).append(separator);
        basicSb.append(KEY_VERSION_CODE).append(KV).append(versionCode).append(separator);
        basicSb.append(KEY_IMEI).append(KV).append(imei).append(separator);
        basicSb.append(KEY_UID).append(KV).append(uid).append(separator);
        basicSb.append(KEY_NETWORK).append(KV).append(network).append(separator);
        basicSb.append(KEY_MODEL).append(KV).append(Build.MODEL).append(separator);
        basicSb.append(KEY_API).append(KV).append(apiLevel).append(separator);
        basicSb.append(KEY_CPU_CORE).append(KV).append(cpuCoreNum).append(separator);
        basicSb.append(KEY_PROCESS).append(KV).append(processName).append(separator);
        basicSb.append(KEY_FREE_MEMORY).append(KV).append(freeMemory).append(separator);
        basicSb.append(KEY_TOTAL_MEMORY).append(KV).append(totalMemory).append(separator);

        timeSb.append(KEY_TIME_COST).append(KV).append(timeCost).append(separator);
        timeSb.append(KEY_THREAD_TIME_COST).append(KV).append(threadTimeCost).append(separator);
        timeSb.append(KEY_TIME_COST_START).append(KV).append(timeStart).append(separator);
        timeSb.append(KEY_TIME_COST_END).append(KV).append(timeEnd).append(separator);

        cpuSb.append(KEY_CPU_BUSY).append(KV).append(cpuBusy).append(separator);
        cpuSb.append(KEY_CPU_RATE).append(KV).append(cpuRateInfo).append(separator);

        if (threadStackEntries != null && !threadStackEntries.isEmpty()) {
            StringBuilder temp = new StringBuilder();
            for (String s : threadStackEntries) {
                temp.append(s);
                temp.append(separator);
            }
            stackSb.append(KEY_STACK).append(KV).append(temp.toString()).append(separator);
        }
        return this;
    }

    public String getBasicString() {
        return basicSb.toString();
    }

    public String getCpuString() {
        return cpuSb.toString();
    }

    public String getTimeString() {
        return timeSb.toString();
    }

    public String getKeyStackString() {
        String result = "";
        for (String stackEntry : threadStackEntries) {
            if (Character.isLetter(stackEntry.charAt(0))) {
                String[] lines = stackEntry.split(BlockInfo.SEPARATOR);
                for (String line : lines) {
                    if (!line.startsWith("com.android")
                            && !line.startsWith("java")
                            && !line.startsWith("android")) {
                        result = line.substring(line.indexOf('(') + 1, line.indexOf(')'));
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public String toString() {
        return String.valueOf(basicSb) + timeSb + cpuSb + stackSb;
    }
}
