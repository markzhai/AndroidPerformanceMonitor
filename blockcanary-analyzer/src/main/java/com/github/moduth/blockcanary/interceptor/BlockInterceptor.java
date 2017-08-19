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
import java.util.Collection;

public interface BlockInterceptor {
    void onBlock(Context context, BlockInfo blockInfo);

    int provideBlockThreshold();

    int provideDumpInterval();

    boolean stopWhenDebugging();

    String providePath();


    /**
     * Implement in your project.
     *
     * @return Qualifier which can specify this installation, like version + flavor.
     */
    public String provideQualifier() ;

    /**
     * Implement in your project.
     *
     * @return user id
     */
    public String provideUid()  ;

    /**
     * Network type
     *
     * @return {@link String} like 2G, 3G, 4G, wifi, etc.
     */
    public String provideNetworkType()  ;

    boolean displayNotification();

    int provideMonitorDuration();

    boolean zip(File[] logFiles, File zippedFile);

    void upload(File file);

    boolean deleteFilesInWhiteList();

    boolean filterNonConcernStack();

    Collection<? extends String> concernPackages();

    Collection<? extends String> provideWhiteList();
}
