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

import android.os.SystemClock;
import android.util.Printer;
/*自定义Printer*/
class LooperMonitor implements Printer {

    private static final int DEFAULT_BLOCK_THRESHOLD_MILLIS = 3000;

    private long mBlockThresholdMillis = DEFAULT_BLOCK_THRESHOLD_MILLIS;
    private long mStartTimestamp = 0;
    private long mStartThreadTimestamp = 0;
    private BlockListener mBlockListener = null;
    private boolean mPrintingStarted = false;

    public interface BlockListener {
        void onBlockEvent(long realStartTime,
                          long realTimeEnd,
                          long threadTimeStart,
                          long threadTimeEnd);
    }

    public LooperMonitor(BlockListener blockListener, long blockThresholdMillis) {
        if (blockListener == null) {
            throw new IllegalArgumentException("blockListener should not be null.");
        }
        mBlockListener = blockListener;
        mBlockThresholdMillis = blockThresholdMillis;
    }

    // TODO: 2017/3/3 重新Printer的println方法，监听Message Loop,同时记录该时间间隔的调用栈及CPU占用数据（6）
    @Override
    public void println(String x) {
        if (!mPrintingStarted) {/*记录println message 时当前时间，同时开始dump 堆栈及cpu数据*/
            mStartTimestamp = System.currentTimeMillis();
            mStartThreadTimestamp = SystemClock.currentThreadTimeMillis();
            mPrintingStarted = true;
            startDump();
        } else {/*记录end time ,停止dump 堆栈及cpu数据*/
            final long endTime = System.currentTimeMillis();
            mPrintingStarted = false;
            if (isBlock(endTime)) {/*isBlock 回调监听*/// TODO: 2017/3/3 触发线程卡顿 （7） 
                notifyBlockEvent(endTime);
            }
            stopDump();
        }
    }

    private boolean isBlock(long endTime) {
        return endTime - mStartTimestamp > mBlockThresholdMillis;
    }

    // TODO: 2017/3/3 卡顿时间超过设置:触发线程卡顿 （8） 
    private void notifyBlockEvent(final long endTime) {
        final long startTime = mStartTimestamp;
        final long startThreadTime = mStartThreadTimestamp;
        final long endThreadTime = SystemClock.currentThreadTimeMillis();
        HandlerThreadFactory.getWriteLogThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mBlockListener.onBlockEvent(startTime, endTime, startThreadTime, endThreadTime);
            }
        });
    }

    private void startDump() {
        if (null != BlockCanaryInternals.getInstance().stackSampler) {
            BlockCanaryInternals.getInstance().stackSampler.start();
        }

        if (null != BlockCanaryInternals.getInstance().cpuSampler) {
            BlockCanaryInternals.getInstance().cpuSampler.start();
        }
    }

    private void stopDump() {
        if (null != BlockCanaryInternals.getInstance().stackSampler) {
            BlockCanaryInternals.getInstance().stackSampler.stop();
        }

        if (null != BlockCanaryInternals.getInstance().cpuSampler) {
            BlockCanaryInternals.getInstance().cpuSampler.stop();
        }
    }
}