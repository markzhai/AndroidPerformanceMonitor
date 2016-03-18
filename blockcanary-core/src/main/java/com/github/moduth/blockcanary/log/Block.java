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
package com.github.moduth.blockcanary.log;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.moduth.blockcanary.BlockCanaryCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @author markzhai on 15/9/27.
 */
public final class Block {

    private static final String TAG = "Block";

    public static final String SEPARATOR = "\r\n";
    public static final String KV = " = ";

    public static final String KEY_QUA = "qualifier";
    public static final String KEY_MODEL = "model";
    public static final String KEY_API = "apilevel";
    public static final String KEY_IMEI = "imei";
    public static final String KEY_UID = "uid";
    public static final String KEY_CPU_CORE = "cpuCore";
    public static final String KEY_CPU_BUSY = "cpubusy";
    public static final String KEY_CPU_RATE = "cpurate";
    public static final String KEY_TIME_COST = "timecost";
    public static final String KEY_THREAD_TIME_COST = "threadtimecost";
    public static final String KEY_TIME_COST_START = "timestart";
    public static final String KEY_TIME_COST_END = "timeend";
    public static final String KEY_STACK = "stack";
    public static final String KEY_PROCESS_NAME = "processName";
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
    public ArrayList<String> threadStackEntries = new ArrayList<String>();
    public File logFile;

    private StringBuilder basicSb = new StringBuilder();
    private StringBuilder cpuSb = new StringBuilder();
    private StringBuilder timeSb = new StringBuilder();
    private StringBuilder stackSb = new StringBuilder();
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private static final String EMPTY_IMEI = "empty_imei";

    private Block() {
    }

    public static Block newInstance() {
        Block block = new Block();
        Context context = BlockCanaryCore.getContext().getContext();
        if (block.versionName == null || block.versionName.length() == 0) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                block.versionCode = info.versionCode;
                block.versionName = info.versionName;
            } catch (Throwable e) {
                Log.e(TAG, NEW_INSTANCE, e);
            }
        }

        if (block.imei == null || block.imei.length() == 0) {
            try {
                TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                block.imei = mTManager.getDeviceId();
            } catch (Exception e) {
                Log.e(TAG, NEW_INSTANCE, e);
                block.imei = EMPTY_IMEI;
            }
        }
        block.qualifier = BlockCanaryCore.getContext().getQualifier();
        block.apiLevel = Build.VERSION.SDK_INT + " " + VERSION.RELEASE;
        block.model = Build.MODEL;
        block.uid = BlockCanaryCore.getContext().getUid();
        block.cpuCoreNum = PerformanceUtils.getNumCores();
        block.processName = ProcessUtils.myProcessName();
        block.network = BlockCanaryCore.getContext().getNetworkType();
        block.freeMemory = String.valueOf(PerformanceUtils.getFreeMemory());
        block.totalMemory = String.valueOf(PerformanceUtils.getTotalMemory());
        return block;
    }

    /**
     * Create {@link Block} from saved log file.
     *
     * @param file looper log file
     * @return LooperLog created from log file
     */
    public static Block newInstance(File file) {
        Block block = new Block();
        block.logFile = file;

        BufferedReader reader = null;
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(file), "UTF-8");

            reader = new BufferedReader(in);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith(KEY_QUA)) {
                    block.qualifier = line.split(KV)[1];
                } else if (line.startsWith(KEY_MODEL)) {
                    block.model = line.split(KV)[1];
                } else if (line.startsWith(KEY_API)) {
                    block.apiLevel = line.split(KV)[1];
                } else if (line.startsWith(KEY_IMEI)) {
                    block.imei = line.split(KV)[1];
                } else if (line.startsWith(KEY_UID)) {
                    block.uid = line.split(KV)[1];
                } else if (line.startsWith(KEY_CPU_CORE)) {
                    block.cpuCoreNum = Integer.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_PROCESS_NAME)) {
                    block.processName = line.split(KV)[1];
                } else if (line.startsWith(KEY_VERSION_NAME)) {
                    block.versionName = line.split(KV)[1];
                } else if (line.startsWith(KEY_VERSION_CODE)) {
                    block.versionCode = Integer.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_NETWORK)) {
                    block.network = line.split(KV)[1];
                } else if (line.startsWith(KEY_TOTAL_MEMORY)) {
                    block.totalMemory = line.split(KV)[1];
                } else if (line.startsWith(KEY_FREE_MEMORY)) {
                    block.freeMemory = line.split(KV)[1];
                } else if (line.startsWith(KEY_CPU_BUSY)) {
                    block.cpuBusy = Boolean.valueOf(line.split(KV)[1]);
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
                        block.cpuRateInfo = cpuRateSb.toString();
                    }

                } else if (line.startsWith(KEY_TIME_COST_START)) {
                    block.timeStart = line.split(KV)[1];
                } else if (line.startsWith(KEY_TIME_COST_END)) {
                    block.timeEnd = line.split(KV)[1];
                } else if (line.startsWith(KEY_TIME_COST)) {
                    block.timeCost = Long.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_THREAD_TIME_COST)) {
                    block.threadTimeCost = Long.valueOf(line.split(KV)[1]);
                } else if (line.startsWith(KEY_STACK)) {
                    StringBuilder stackSb = new StringBuilder(line.split(KV)[1]);
                    line = reader.readLine();

                    // read until file ends
                    while (line != null) {
                        if (!line.equals("")) {
                            stackSb.append(line).append(SEPARATOR);
                        } else if (stackSb.length() > 0) {
                            // ignore continual blank lines
                            block.threadStackEntries.add(stackSb.toString());
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
                    reader = null;
                }
            } catch (Exception e) {
                Log.e(TAG, NEW_INSTANCE, e);
            }
        }
        block.flushString();
        return block;
    }

    public Block setCpuBusyFlag(boolean busy) {
        cpuBusy = busy;
        return this;
    }

    public Block setRecentCpuRate(String info) {
        cpuRateInfo = info;
        return this;
    }

    public Block setThreadStackEntries(ArrayList<String> threadStackEntries) {
        this.threadStackEntries = threadStackEntries;
        return this;
    }

    public Block setMainThreadTimeCost(long realTimeStart, long realTimeEnd, long threadTimeStart, long threadTimeEnd) {
        timeCost = realTimeEnd - realTimeStart;
        threadTimeCost = threadTimeEnd - threadTimeStart;
        timeStart = TIME_FORMATTER.format(realTimeStart);
        timeEnd = TIME_FORMATTER.format(realTimeEnd);
        return this;
    }

    public Block flushString() {
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
        basicSb.append(KEY_PROCESS_NAME).append(KV).append(processName).append(separator);
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
                String[] lines = stackEntry.split(Block.SEPARATOR);
                for (String line : lines) {
                    if (!line.startsWith("com.android") && !line.startsWith("java") && !line.startsWith("android")) {
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
