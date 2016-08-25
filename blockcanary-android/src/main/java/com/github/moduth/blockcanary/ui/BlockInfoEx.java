package com.github.moduth.blockcanary.ui;

import android.util.Log;

import com.github.moduth.blockcanary.internal.BlockInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

final class BlockInfoEx extends BlockInfo {

    private static final String TAG = "BlockInfoEx";

    public File logFile;
    public String concernStackString;

    /**
     * Create {@link BlockInfoEx} from saved log file.
     *
     * @param file looper log file
     * @return LooperLog created from log file
     */
    public static BlockInfoEx newInstance(File file) {
        BlockInfoEx blockInfo = new BlockInfoEx();
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
                } else if (line.startsWith(KEY_CPU_CORE)) {
                    blockInfo.cpuCoreNum = Integer.valueOf(line.split(KV)[1]);
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
            Log.e(TAG, NEW_INSTANCE_METHOD, t);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                Log.e(TAG, NEW_INSTANCE_METHOD, e);
            }
        }
        blockInfo.flushString();
        return blockInfo;
    }
}
