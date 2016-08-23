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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * No-op context.
 */
public class BlockCanaryContext {

    private static Context sAppContext;
    private static BlockCanaryContext sInstance = null;

    public BlockCanaryContext() {
    }

    public static void init(Context c, BlockCanaryContext g) {
        sAppContext = c;
        sInstance = g;
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

    public String getQualifier() {
        return "Unspecified";
    }

    public String getUid() {
        return "0";
    }

    public String getNetworkType() {
        return "UNKNOWN";
    }

    public int getConfigDuration() {
        return 99999;
    }

    public int getConfigBlockThreshold() {
        return 1000;
    }

    public boolean displayNotification() {
        return false;
    }

    public String getLogPath() {
        return "/blockcanary/";
    }

    public boolean zipLogFile(File[] src, File dest) {
        return false;
    }

    public void uploadLogFile(File zippedFile) {
        throw new UnsupportedOperationException();
    }

    public String getStackFoldPrefix() {
        return null;
    }

    public int getConfigDumpInterval() {
        return getConfigBlockThreshold();
    }

    public List<String> getWhiteList() {
        return null;
    }
}