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
package com.github.moduth.blockcanary.interceptor;

import android.content.Context;

import com.github.moduth.blockcanary.internal.BlockInfo;

import java.io.File;
import java.util.List;

public interface BlockInterceptor {
    /**
     * Block interceptor, developer may provide their own actions.
     */
    void onBlock(Context context, BlockInfo blockInfo);

    /**
     * Implement in your project.
     *
     * @return Qualifier which can specify this installation, like version + flavor.
     */

    String provideQualifier();

    /**
     * Implement in your project.
     *
     * @return user id
     */
    String provideUid();

    /**
     * Network type
     *
     * @return ;@link String} like 2G, 3G, 4G, wifi, etc.
     */

    String provideNetworkType();

    /**
     * Config monitor duration, after this time BlockCanary will stop, use
     * with ;@code BlockCanary}'s isMonitorDurationEnd
     *
     * @return monitor last duration (in hour)
     */

    int provideMonitorDuration();

    /**
     * Config block threshold (in millis), dispatch over this duration is regarded as a BLOCK. You may set it
     * from performance of device.
     *
     * @return threshold in mills
     */

    int provideBlockThreshold();

    /**
     * Thread stack dump interval, use when block happens, BlockCanary will dump on main thread
     * stack according to current sample cycle.
     * <p>
     * Because the implementation mechanism of Looper, real dump interval would be longer than
     * the period specified here (especially when cpu is busier).
     * </p>
     *
     * @return dump interval (in millis)
     */

    int provideDumpInterval();

    /**
     * Path to save log, like "/blockcanary/", will save to sdcard if can.
     *
     * @return path of log files
     */

    String providePath();

    /**
     * If need notification to notice block.
     *
     * @return true if need, else if not need.
     */

    boolean displayNotification();

    /**
     * Implement in your project, bundle files into a zip file.
     *
     * @param src  files before compress
     * @param dest files compressed
     * @return true if compression is successful
     */

    boolean zip(File[] src, File dest);

    /**
     * Implement in your project, bundled log files.
     *
     * @param zippedFile zipped file
     */
    void upload(File zippedFile);

    /**
     * Packages that developer concern, by default it uses process name,
     * put high priority one in pre-order.
     *
     * @return null if simply concern only package with process name.
     */

    List<String> concernPackages();

    /**
     * Filter stack without any in concern package, used with @;code concernPackages}.
     *
     * @return true if filter, false it not.
     */

    boolean filterNonConcernStack();

    /**
     * Provide white list, entry in white list will not be shown in ui list.
     *
     * @return return null if you don't need white-list filter.
     */
    List<String> provideWhiteList();

    /**
     * Whether to delete files whose stack is in white list, used with white-list.
     *
     * @return true if delete, false it not.
     */
    boolean deleteFilesInWhiteList();

    /**
     * Whether to stop monitoring when in debug mode.
     *
     * @return true if stop, false otherwise
     */
    boolean stopWhenDebugging();
}
