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

import android.os.SystemClock;
import android.util.Printer;

/**
 * LooperPrinter, uses message dispatch time to do monitoring.
 * <p/>
 * Created by markzhai on 2015/9/25.
 */
class LooperPrinter implements Printer {

    private static final int DEFAULT_BLOCK_THRESHOLD_MILLIS = 3000;

    private long mBlockThresholdMillis = DEFAULT_BLOCK_THRESHOLD_MILLIS;
    private long mStartTimeMillis = 0;
    private long mStartThreadTimeMillis = 0;
    private BlockListener mBlockListener = null;

    private boolean mStartedPrinting = false;

    public LooperPrinter(BlockListener blockListener, long blockThresholdMillis) {
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
            startDump();
        } else {
            final long endTime = System.currentTimeMillis();
            mStartedPrinting = false;
            if (isBlock(endTime)) {
                notifyBlockEvent(endTime);
            }
            stopDump();
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
        HandlerThread.getWriteLogFileThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mBlockListener.onBlockEvent(startTime, endTime, startThreadTime, endThreadTime);
            }
        });
    }

    private void startDump() {
        if (null != BlockCanaryCore.get().threadStackSampler) {
            BlockCanaryCore.get().threadStackSampler.start();
        }

        if (null != BlockCanaryCore.get().cpuSampler) {
            BlockCanaryCore.get().cpuSampler.start();
        }
    }

    private void stopDump() {
        if (null != BlockCanaryCore.get().threadStackSampler) {
            BlockCanaryCore.get().threadStackSampler.stop();
        }

        if (null != BlockCanaryCore.get().cpuSampler) {
            BlockCanaryCore.get().cpuSampler.stop();
        }
    }
}