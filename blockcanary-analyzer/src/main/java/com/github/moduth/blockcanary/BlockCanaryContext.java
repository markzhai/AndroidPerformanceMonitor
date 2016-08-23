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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * User should provide a real implementation of this class to use BlockCanary.
 */
public class BlockCanaryContext {

    private static Context sAppContext;
    private static BlockCanaryContext sInstance = null;

    public BlockCanaryContext() {
    }

    public static void init(Context context,
                            BlockCanaryContext blockCanaryContext) {
        sAppContext = context;
        sInstance = blockCanaryContext;
    }

    public static BlockCanaryContext get() {
        if (sInstance == null) {
            throw new RuntimeException("BlockCanaryContext not init");
        } else {
            return sInstance;
        }
    }

    public Context getContext() {
        return sAppContext;
    }

    /**
     * Implement in your project.
     * Qualifier which can specify this installation, like version + flavor
     *
     * @return apk qualifier
     */
    public String getQualifier() {
        return "Unspecified";
    }

    /**
     * Implement in your project.
     *
     * @return user id
     */
    public String getUid() {
        return "0";
    }

    /**
     * Network type
     *
     * @return String like 2G, 3G, 4G, wifi, etc.
     */
    public String getNetworkType() {
        return "UNKNOWN";
    }

    /**
     * Config monitor duration, after this time BlockCanary will stop, use
     * with {@code BlockCanary}'s isMonitorDurationEnd
     *
     * @return monitor last duration (in hour)
     */
    public int getConfigDuration() {
        return 99999;
    }

    /**
     * Config block threshold (in millis), dispatch over this duration is regarded as a BLOCK. You may set it
     * from performance of device.
     *
     * @return threshold in mills
     */
    public int getConfigBlockThreshold() {
        return 1000;
    }

    /**
     * If need notification to notice block.
     *
     * @return true if need, else if not need.
     */
    public boolean displayNotification() {
        return true;
    }

    /**
     * Path to save log, like "/blockcanary/log", will save to sdcard if can
     *
     * @return path of log files
     */
    public String getLogPath() {
        return "/blockcanary/";
    }

    /**
     * Implement in your project.
     *
     * @param src  files before compress
     * @param dest files compressed
     * @return true if compression is successful
     */
    public boolean zipLogFile(File[] src, File dest) {
        return false;
    }

    /**
     * Implement in your project.
     *
     * @param zippedFile zipped file
     */
    public void uploadLogFile(File zippedFile) {
        throw new UnsupportedOperationException();
    }

    /**
     * Config string prefix to determine how to fold stack
     *
     * @return string prefix, null if use process name.
     */
    public String getStackFoldPrefix() {
        return null;
    }

    /**
     * Thread stack dump interval, use when block happens, BlockCanary will dump on main thread
     * stack according to current sample cycle.
     * <p>
     * Notice: Because the implementation mechanism of Looper, real dump interval would be longer than
     * the period specified here (longer if cpu is busier)
     * </p>
     *
     * @return dump interval (in millis)
     */
    public int getConfigDumpInterval() {
        return getConfigBlockThreshold();
    }

    /**
     * Get white list, operations in white list will not be recorded.
     */
    public List<String> getWhiteList() {
        LinkedList<String> whiteList = new LinkedList<>();
        whiteList.add("com.android");
        whiteList.add("java");
        whiteList.add("android");
        return whiteList;
    }
}