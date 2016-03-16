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

/**
 * <p>looper线程监控</p>
 * Created by markzhai on 2015/9/25.
 */
public class BlockCanary {

    private static BlockCanary sInstance = null;

    private BlockCanary() {
    }

    /**
     * Install BlockCanary
     * @param context application context
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
        throw new UnsupportedOperationException();
    }

    /**
     * 停止主进程的主线程监控
     */
    public void stop() {
        throw new UnsupportedOperationException();
    }

    /**
     * 上传监控log文件
     */
    public void upload() {
        throw new UnsupportedOperationException();
    }

    /**
     * 记录开启监控的时间到preference，可以在release包收到push通知后调用。
     */
    public void recordStartTime() {
        throw new UnsupportedOperationException();
    }

    /**
     * 是否监控时间结束，根据上次开启的时间(recordStartTime)和getConfigDuration计算出来。
     *
     * @return true则结束
     */
    public boolean isMonitorDurationEnd() {
        return true;
    }
}