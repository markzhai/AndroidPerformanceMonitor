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

import com.github.moduth.blockcanary.internal.BlockInfo;

import java.io.File;
import java.util.List;

/**
 * No-op context.
 */
public class BlockCanaryContext {

    private static Context sApplicationContext;
    private static BlockCanaryContext sInstance = null;

    public BlockCanaryContext() {
    }

    static void init(Context c, BlockCanaryContext g) {
        sApplicationContext = c;
        sInstance = g;
    }

    public static BlockCanaryContext get() {
        if (sInstance == null) {
            throw new RuntimeException("BlockCanaryContext not init");
        } else {
            return sInstance;
        }
    }

    public Context provideContext() {
        return sApplicationContext;
    }

    public String provideQualifier() {
        return "Unspecified";
    }

    public String provideUid() {
        return "0";
    }

    public String provideNetworkType() {
        return "UNKNOWN";
    }

    public int provideMonitorDuration() {
        return 99999;
    }

    public int provideBlockThreshold() {
        return 1000;
    }

    public int provideDumpInterval() {
        return provideBlockThreshold();
    }

    public String providePath() {
        return "/blockcanary/";
    }

    public boolean displayNotification() {
        return false;
    }

    public boolean zip(File[] src, File dest) {
        return false;
    }

    public void upload(File zippedFile) {
        throw new UnsupportedOperationException();
    }

    public List<String> concernPackages() {
        return null;
    }

    public boolean filterNonConcernStack() {
        return false;
    }

    public List<String> provideWhiteList() {
        return null;
    }

    public boolean deleteFilesInWhiteList() {
        return false;
    }

    public void onBlock(Context context, BlockInfo blockInfo) {

    }

    public boolean stopWhenDebugging() {
        return true;
    }
}