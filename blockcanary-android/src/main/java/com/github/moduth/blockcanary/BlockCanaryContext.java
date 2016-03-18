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

import android.content.Context;

import java.io.File;

/**
 * You should provide a real implementation of this class to use BlockCanary,
 * which provides runtime environment to library (including configuration
 * and app-related log like uid and network environment)
 *
 * @author markzhai on 2015/9/25.
 */
public class BlockCanaryContext implements IBlockCanaryContext {

    private static Context sAppContext;
    private static BlockCanaryContext sInstance = null;

    public BlockCanaryContext() {
    }

    public static void init(Context context, BlockCanaryContext blockCanaryContext) {
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
     * qualifier which can specify this installation, like version + flavor
     *
     * @return apk qualifier
     */
    public String getQualifier() {
        return "Unspecified";
    }

    /**
     * Get user id
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
     * with {@link BlockCanary}'s isMonitorDurationEnd
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
     * If need notification and block ui
     *
     * @return true if need, else if not need.
     */
    public boolean isNeedDisplay() {
        return true;
    }

    /**
     * Path to save log, like "/blockcanary/log"
     *
     * @return path of log files
     */
    @Override
    public String getLogPath() {
        return "/blockcanary/performance";
    }

    /**
     * Zip log file
     *
     * @param src  files before compress
     * @param dest files compressed
     * @return true if compression is successful
     */
    @Override
    public boolean zipLogFile(File[] src, File dest) {
        return false;
    }

    /**
     * Upload log file
     *
     * @param zippedFile zipped file
     */
    @Override
    public void uploadLogFile(File zippedFile) {
        throw new UnsupportedOperationException();
    }

    /**
     * Config string prefix to determine how to fold stack
     *
     * @return string prefix, null if use process name.
     */
    @Override
    public String getStackFoldPrefix() {
        return null;
    }

    /**
     * Thread stack dump interval, use when block happens, BlockCanary will dump on main thread
     * stack according to current sample cycle.
     * <p>
     * PS: Because the implementation mechanism of Looper, real dump interval would be longer than
     * the period specified here (longer if cpu is busier)
     * </p>
     * @return dump interval(in millis)
     */
    @Override
    public int getConfigDumpIntervalMillis() {
        return getConfigBlockThreshold();
    }
}