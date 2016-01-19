package com.github.moduth.blockcanary;

import android.os.SystemClock;
import android.util.Printer;


/**
 * 打印looper线程的message执行时间监控
 * <p>
 * Created by markzhai on 2015/9/25.
 */
class LooperMonitorPrinter implements Printer {

    private static final int DEFAULT_BLOCK_THRESHOLD_MILLIS = 3000;
    private long mBlockThresholdMillis = DEFAULT_BLOCK_THRESHOLD_MILLIS;
    private long mStartTimeMillis = 0;
    private long mStartThreadTimeMillis = 0;
    private BlockListener mBlockListener = null;

    private boolean mStartedPrinting = false;

    public LooperMonitorPrinter(BlockListener blockListener, long blockThresholdMillis) {
        if (blockListener == null) {
            throw new IllegalArgumentException("blockListener should not be null.");
        }
        mBlockListener = blockListener;
        mBlockThresholdMillis = blockThresholdMillis;
    }

    @Override
    public void println(String x) {
        if (!mStartedPrinting) {
            mStartTimeMillis = System.currentTimeMillis();
            mStartThreadTimeMillis = SystemClock.currentThreadTimeMillis();
            mStartedPrinting = true;
        } else {
            final long endTime = System.currentTimeMillis();
            mStartedPrinting = false;
            if (isBlock(endTime)) {
                notifyBlockEvent(endTime);
            }
        }
    }

    private boolean isBlock(long endTime) {
        return endTime - mStartTimeMillis > mBlockThresholdMillis;
    }

    private void notifyBlockEvent(final long endTime) {
//        Log.d("BlockCanary", "notifyBlockEvent: " + endTime + " - " + mStartTimeMillis + ">" + mBlockThresholdMillis);
        final long startTime = mStartTimeMillis;
        final long startThreadTime = mStartThreadTimeMillis;
        final long endThreadTime = SystemClock.currentThreadTimeMillis();
        BlockCanaryContext.get().getWriteLogFileThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mBlockListener.onBlockEvent(startTime, endTime, startThreadTime, endThreadTime);
            }
        });
    }
}