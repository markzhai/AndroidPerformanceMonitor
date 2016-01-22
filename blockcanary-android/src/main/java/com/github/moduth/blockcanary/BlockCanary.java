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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.github.moduth.blockcanary.android.R;
import com.github.moduth.blockcanary.info.CpuSampler;
import com.github.moduth.blockcanary.info.ThreadStackSampler;
import com.github.moduth.blockcanary.log.Block;
import com.github.moduth.blockcanary.log.BlockCanaryInternals;
import com.github.moduth.blockcanary.log.LogWriter;
import com.github.moduth.blockcanary.log.UploadMonitorLog;

import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

/**
 * <p>looper线程监控</p>
 * Created by markzhai on 2015/9/25.
 */
public class BlockCanary {

    private static final int MIN_INTERVAL_MILLIS = 300;
    private static BlockCanary sInstance;
    private LooperPrinter mMainLooperPrinter;
    private ThreadStackSampler mThreadStackSampler;
    private CpuSampler mCpuSampler;
    private boolean mLooperLoggingStarted = false;
    private Class mDisplayBlockClass;

    private BlockCanary() {
        int blockThresholdMillis = BlockCanaryContext.get().getConfigBlockThreshold();

        long sampleIntervalMillis = sampleInterval(blockThresholdMillis);
        BlockCanaryContextInner.setIBlockCanaryContext(BlockCanaryContext.get());
        mThreadStackSampler = new ThreadStackSampler(Looper.getMainLooper().getThread(), sampleIntervalMillis);
        mCpuSampler = new CpuSampler();
        initDisplayBlockClass();

        mMainLooperPrinter = new LooperPrinter(new BlockListener() {

            @Override
            public void onBlockEvent(long realTimeStart, long realTimeEnd, long threadTimeStart, long threadTimeEnd) {
                // 查询这段时间内的线程堆栈调用情况，CPU使用情况
                ArrayList<String> threadStackEntries = mThreadStackSampler.getThreadStackEntries(realTimeStart, realTimeEnd);
                // Log.d("BlockCanary", "threadStackEntries: " + threadStackEntries.size());
                if (threadStackEntries.size() > 0) {
                    Block block = Block.newInstance()
                            .setMainThreadTimeCost(realTimeStart, realTimeEnd, threadTimeStart, threadTimeEnd)
                            .setCpuBusyFlag(mCpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                            .setRecentCpuRate(mCpuSampler.getCpuRateInfo())
                            .setThreadStackEntries(threadStackEntries)
                            .flushString();
                    LogWriter.saveLooperLog(block.toString());

                    if (BlockCanaryContext.get().isNeedDisplay() && mDisplayBlockClass != null) {
                        Context context = BlockCanaryContext.get().getContext();
                        Intent intent = new Intent(context, mDisplayBlockClass);
                        intent.putExtra("show_latest", block.timeStart);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, FLAG_UPDATE_CURRENT);
                        String contentTitle = context.getString(R.string.block_canary_class_has_blocked, block.timeStart);
                        String contentText = context.getString(R.string.block_canary_notification_message);
                        BlockCanary.this.notify(contentTitle, contentText, pendingIntent);
                    }
                }
            }
        }, blockThresholdMillis);

        LogWriter.cleanOldFiles();
    }


    private void initDisplayBlockClass() {
        try {
            mDisplayBlockClass = Class.forName("com.github.moduth.blockcanary.ui.DisplayBlockActivity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Install BlockCanary
     *
     * @param context            application context
     * @param blockCanaryContext implementation for {@link BlockCanaryContext}
     * @return BlockCanary
     */
    public static BlockCanary install(Context context, BlockCanaryContext blockCanaryContext) {
        BlockCanaryContext.init(context, blockCanaryContext);
        return get();
    }

    /**
     * 获得BlockCanary单例
     *
     * @return BlockCanary实例
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
     * 开始主进程的主线程监控
     */
    public void start() {
        if (BlockCanaryContext.get().isNeedDisplay()) {
            BlockCanaryInternals.setEnabled(
                    BlockCanaryContext.get().getContext(), mDisplayBlockClass, true);
        }
        if (!mLooperLoggingStarted) {
            mLooperLoggingStarted = true;
            Looper.getMainLooper().setMessageLogging(mMainLooperPrinter);
            mThreadStackSampler.start();
            mCpuSampler.start();
        }
    }

    /**
     * 记录开启监控的时间到preference，可以在release包收到push通知后调用。
     */
    public void recordStartTime() {
        PreferenceManager.getDefaultSharedPreferences(BlockCanaryContext.get().getContext())
                .edit().putLong("BlockCanary_StartTime", System.currentTimeMillis()).commit();
    }

    /**
     * 是否监控时间结束，根据上次开启的时间(recordStartTime)和getConfigDuration计算出来。
     *
     * @return true则结束
     */
    public boolean isMonitorDurationEnd() {
        long startTime = PreferenceManager.getDefaultSharedPreferences(
                BlockCanaryContext.get().getContext()).getLong("BlockCanary_StartTime", 0);
        return startTime != 0 &&
                System.currentTimeMillis() - startTime > BlockCanaryContext.get().getConfigDuration() * 3600 * 1000;
    }

    /**
     * 停止主进程的主线程监控
     */
    public void stop() {
        if (mLooperLoggingStarted) {
            mLooperLoggingStarted = false;
            Looper.getMainLooper().setMessageLogging(null);
            mThreadStackSampler.stop();
            mCpuSampler.stop();
        }
    }

    /**
     * 上传监控log文件
     */
    public void upload() {
        UploadMonitorLog.forceZipLogAndUpload();
    }

    private long sampleInterval(int blockThresholdMillis) {
        // 最小值保护
        long sampleIntervalMillis = blockThresholdMillis / 2;
        if (sampleIntervalMillis < MIN_INTERVAL_MILLIS) {
            sampleIntervalMillis = MIN_INTERVAL_MILLIS;
        }
        return sampleIntervalMillis;
    }

    @TargetApi(HONEYCOMB)
    private void notify(String contentTitle, String contentText, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager)
                BlockCanaryContext.get().getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification;
        if (SDK_INT < HONEYCOMB) {
            notification = new Notification();
            notification.icon = R.drawable.block_canary_notification;
            notification.when = System.currentTimeMillis();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults = Notification.DEFAULT_SOUND;// add sound by chiahaolu
            notification.setLatestEventInfo(BlockCanaryContext.get().getContext(), contentTitle, contentText, pendingIntent);
        } else {
            Notification.Builder builder = new Notification.Builder(BlockCanaryContext.get().getContext())
                    .setSmallIcon(R.drawable.block_canary_notification)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND);// add sound by chiahaolu
            if (SDK_INT < JELLY_BEAN) {
                notification = builder.getNotification();
            } else {
                notification = builder.build();
            }
        }
        notificationManager.notify(0xDEAFBEEF, notification);
    }
}
