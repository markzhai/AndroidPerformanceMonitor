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
 * 使用本库的应用实现该抽象类，提供运行环境给性能监控组件（包括使用配置和app相关的log如用户名和网络环境）
 * <p>
 * Created by markzhai on 2015/9/25.
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
     * 标示符，可以唯一标示该安装版本号，如版本+渠道名+编译平台
     *
     * @return apk唯一标示符
     */
    public String getQualifier() {
        return "Unspecified";
    }

    /**
     * 用户id，方便联系用户和后台查询定位
     *
     * @return 用户id
     */
    public String getUid() {
        return "0";
    }

    /**
     * 网络类型，应用通常都有自己的一套，且较重需要监听网络状态变化，不放在本库中实现
     *
     * @return 2G/3G/4G/wifi等
     */
    public String getNetworkType() {
        return "UNKNOWN";
    }

    /**
     * 卡慢性能监控 持续时长(小时)，开启后到达时长则关闭，配合{@link BlockCanary}的isMonitorDurationEnd
     *
     * @return 监控持续时长（小时）
     */
    public int getConfigDuration() {
        return 99999;
    }

    /**
     * 卡慢性能监控 间隔(毫秒)，超出该间隔判定为卡慢。建议根据机器性能设置不同的数值，如2000/Cpu核数
     *
     * @return 卡慢阙值（毫秒）
     */
    public int getConfigBlockThreshold() {
        return 1000;
    }

    /**
     * 是否需要展示卡慢界面，如仅在Debug包开启
     *
     * @return 是否需要展示卡慢界面
     */
    public boolean isNeedDisplay() {
        return true;
    }

    /**
     * Log文件保存的位置，如"/blockcanary/log"
     *
     * @return Log文件保存的位置
     */
    @Override
    public String getLogPath() {
        return "/blockcanary/performance";
    }

    /**
     * 压缩文件
     *
     * @param src  压缩前的文件
     * @param dest 压缩后的文件
     * @return 压缩是否成功
     */
    @Override
    public boolean zipLogFile(File[] src, File dest) {
        return false;
    }

    /**
     * 上传日志
     *
     * @param zippedFile 压缩后的文件
     */
    @Override
    public void uploadLogFile(File zippedFile) {

    }

    @Override
    public String getStackFoldPrefix() {
        return null;
    }

    @Override
    public int getConfigDumpIntervalMillis() {
        return getConfigBlockThreshold();
    }
}