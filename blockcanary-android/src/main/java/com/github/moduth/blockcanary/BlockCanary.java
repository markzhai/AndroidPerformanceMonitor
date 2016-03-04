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
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.reflect.Constructor;

/**
 * Looper thread monitor.
 *
 * @author markzhai on 2015/9/25.
 */
public class BlockCanary {

    private static final String TAG = "BlockCanary";

    private static BlockCanary sInstance;
    private BlockCanaryCore mBlockCanaryCore;
    private boolean mLooperLoggingStarted = false;

    private BlockCanary() {
        BlockCanaryCore.setIBlockCanaryContext(BlockCanaryContext.get());
        mBlockCanaryCore = BlockCanaryCore.get();
        initNotification();
    }

    /**
     * Install {@link BlockCanary}
     *
     * @param context            application context
     * @param blockCanaryContext implementation for {@link BlockCanaryContext}
     * @return {@link BlockCanary}
     */
    public static BlockCanary install(Context context, BlockCanaryContext blockCanaryContext) {
        BlockCanaryContext.init(context, blockCanaryContext);
        return get();
    }

    /**
     * Get {@link BlockCanary} singleton.
     *
     * @return {@link BlockCanary} instance
     */
    public static BlockCanary get() {
        if (sInstance == null) {
            synchronized (BlockCanary.class) {
                if (sInstance == null) {
                    sInstance = new BlockCanary();
                }
            }
        }
        return sInstance;
    }

    /**
     * Start main-thread monitoring.
     */
    public void start() {
        if (!mLooperLoggingStarted) {
            mLooperLoggingStarted = true;
            Looper.getMainLooper().setMessageLogging(mBlockCanaryCore.mainLooperPrinter);
        }
    }

    /**
     * Stop monitoring.
     */
    public void stop() {
        if (mLooperLoggingStarted) {
            mLooperLoggingStarted = false;
            Looper.getMainLooper().setMessageLogging(null);
            mBlockCanaryCore.threadStackSampler.stop();
            mBlockCanaryCore.cpuSampler.stop();
        }
    }

    /**
     * Zip and upload log files.
     */
    public void upload() {
        UploadMonitorLog.forceZipLogAndUpload();
    }

    /**
     * Record monitor start time to preference, you may use it when after push which tells start BlockCanary.
     */
    public void recordStartTime() {
        PreferenceManager.getDefaultSharedPreferences(BlockCanaryContext.get().getContext())
                .edit().putLong("BlockCanary_StartTime", System.currentTimeMillis()).commit();
    }

    /**
     * Is monitor duration end, compute from recordStartTime end getConfigDuration.
     *
     * @return true if ended
     */
    public boolean isMonitorDurationEnd() {
        long startTime = PreferenceManager.getDefaultSharedPreferences(
                BlockCanaryContext.get().getContext()).getLong("BlockCanary_StartTime", 0);
        return startTime != 0 &&
                System.currentTimeMillis() - startTime >
                        BlockCanaryContext.get().getConfigDuration() * 3600 * 1000;
    }

    @SuppressWarnings("unchecked")
    private void initNotification() {
        if (!BlockCanaryContext.get().isNeedDisplay()) {
            return;
        }

        try {
            Class notifier = Class.forName("com.github.moduth.blockcanary.ui.Notifier");
            if (notifier == null) {
                return;
            }
            Constructor<? extends OnBlockEventInterceptor> constructor = notifier.getConstructor();
            mBlockCanaryCore.setOnBlockEventInterceptor(constructor.newInstance());
        } catch (Exception e) {
            Log.e(TAG, "initNotification: ", e);
        }
    }
}
