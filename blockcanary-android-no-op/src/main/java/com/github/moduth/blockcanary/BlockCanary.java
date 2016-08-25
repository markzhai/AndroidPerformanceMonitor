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
import android.util.Log;

/**
 * No-op implementation.
 */
public final class BlockCanary {

    private static final String TAG = "BlockCanary-no-op";
    private static BlockCanary sInstance = null;

    private BlockCanary() {
    }

    public static BlockCanary install(Context context, BlockCanaryContext blockCanaryContext) {
        BlockCanaryContext.init(context, blockCanaryContext);
        return get();
    }

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

    public void start() {
        Log.i(TAG, "start");
    }

    public void stop() {
        Log.i(TAG, "stop");
    }

    public void upload() {
        Log.i(TAG, "upload");
    }

    public void recordStartTime() {
        Log.i(TAG, "recordStartTime");
    }

    public boolean isMonitorDurationEnd() {
        return true;
    }
}
