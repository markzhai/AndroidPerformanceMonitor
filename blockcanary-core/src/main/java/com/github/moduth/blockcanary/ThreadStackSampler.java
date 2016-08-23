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

import com.github.moduth.blockcanary.log.Block;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * {@link ThreadStackSampler} dumps main thread stack and saves last recent stack piece locally.
 * <p>
 * Created by markzhai on 2015/9/25.
 */
class ThreadStackSampler extends Sampler {

    private static final LinkedHashMap<Long, String> mThreadStackEntries = new LinkedHashMap<>();
    private static final int DEFAULT_MAX_ENTRY_COUNT = 10;

    private int mMaxEntryCount = DEFAULT_MAX_ENTRY_COUNT;

    private Thread mThread;

    public ThreadStackSampler(Thread thread, long sampleIntervalMillis) {
        this(thread, DEFAULT_MAX_ENTRY_COUNT, sampleIntervalMillis);
    }

    public ThreadStackSampler(Thread thread, int maxEntryCount, long sampleIntervalMillis) {
        super(sampleIntervalMillis);
        mThread = thread;
        mMaxEntryCount = maxEntryCount;
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

    @Override
    protected void doSample() {
//        Log.d("BlockCanary", "sample thread stack: [" + mThreadStackEntries.size() + ", " + mMaxEntryCount + "]");
        StringBuilder stringBuilder = new StringBuilder();

        // Fetch thread stack info
        for (StackTraceElement stackTraceElement : mThread.getStackTrace()) {
            stringBuilder.append(stackTraceElement.toString())
                    .append(Block.SEPARATOR);
        }

        // Eliminate obsolete entry
        synchronized (mThreadStackEntries) {
            if (mThreadStackEntries.size() == mMaxEntryCount && mMaxEntryCount > 0) {
                mThreadStackEntries.remove(mThreadStackEntries.keySet().iterator().next());
            }
            mThreadStackEntries.put(System.currentTimeMillis(), stringBuilder.toString());
        }
    }
}