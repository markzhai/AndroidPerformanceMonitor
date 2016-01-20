package com.github.moduth.blockcanary.info;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.github.moduth.blockcanary.log.Block;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * 定期获取线程的堆栈调用情况，保存最近几次信息
 * <p>
 * Created by markzhai on 2015/9/25.
 */
public class ThreadStackSampler {

    private static final LinkedHashMap<Long, String> mThreadStackEntries = new LinkedHashMap<Long, String>();
    private static final long DEFAULT_SAMPLE_INTERVAL_MILLIS = 3000;
    private static final int DEFAULT_MAX_ENTRY_COUNT = 10;

    private int mMaxEntryCount = DEFAULT_MAX_ENTRY_COUNT;
    private long mSampleIntervalMillis = DEFAULT_SAMPLE_INTERVAL_MILLIS;
    private Thread mThread;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doSample();

            BlockCanaryContext.get().getTimerThreadHandler().postDelayed(mRunnable, mSampleIntervalMillis);
        }
    };

    private void doSample() {
//        Log.d("BlockCanary", "sample thread stack: [" + mThreadStackEntries.size() + ", " + mMaxEntryCount + "]");
        StringBuilder stringBuilder = new StringBuilder();

        // 获取线程堆栈信息
        for (StackTraceElement stackTraceElement : mThread.getStackTrace()) {
            stringBuilder.append(stackTraceElement.toString())
                        .append(Block.SEPARATOR);
        }

        // 淘汰最早的信息
        synchronized (mThreadStackEntries) {
            if (mThreadStackEntries.size() == mMaxEntryCount && mMaxEntryCount > 0) {
                mThreadStackEntries.remove(mThreadStackEntries.keySet().iterator().next());
            }
            mThreadStackEntries.put(System.currentTimeMillis(), stringBuilder.toString());
        }
    }

    public ThreadStackSampler(Thread thread) {
        this(thread, DEFAULT_MAX_ENTRY_COUNT, DEFAULT_SAMPLE_INTERVAL_MILLIS);
    }

    public ThreadStackSampler(Thread thread, long sampleIntervalMillis) {
        this(thread, DEFAULT_MAX_ENTRY_COUNT, sampleIntervalMillis);
    }

    public ThreadStackSampler(Thread thread, int maxEntryCount, long sampleIntervalMillis) {
        mThread = thread;
        mMaxEntryCount = maxEntryCount;
        mSampleIntervalMillis = sampleIntervalMillis;
    }

    public void start() {
        BlockCanaryContext.get().getTimerThreadHandler().removeCallbacks(mRunnable);
        BlockCanaryContext.get().getTimerThreadHandler().postDelayed(mRunnable, mSampleIntervalMillis);
    }

    public void stop() {
        BlockCanaryContext.get().getTimerThreadHandler().removeCallbacks(mRunnable);
    }

    public ArrayList<String> getThreadStackEntries(long startTime, long endTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
        ArrayList<String> result = new ArrayList<>();
        synchronized (mThreadStackEntries) {
            for (Long entryTime : mThreadStackEntries.keySet()) {
                if (startTime < entryTime && entryTime < endTime) {
                    result.add(dateFormat.format(entryTime) + Block.SEPARATOR + Block.SEPARATOR + mThreadStackEntries.get(entryTime));
                }
            }
        }
        return result;
    }
}