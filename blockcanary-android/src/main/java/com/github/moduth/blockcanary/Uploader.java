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

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


final class Uploader {

    private static final String TAG = "Uploader";
    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    private Uploader() {
        throw new InstantiationError("Must not instantiate this class");
    }

    private static File zip() {
        String timeString = Long.toString(System.currentTimeMillis());
        try {
            timeString = FORMAT.format(new Date());
        } catch (Throwable e) {
            Log.e(TAG, "zip: ", e);
        }
        File zippedFile = LogWriter.generateTempZip("BlockCanary-" + timeString);
        BlockCanaryInternals.getContext().zip(BlockCanaryInternals.getLogFiles(), zippedFile);
        LogWriter.deleteAll();
        return zippedFile;
    }

    public static void zipAndUpload() {
        HandlerThreadFactory.getWriteLogThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                final File file = zip();
                if (file.exists()) {
                    BlockCanaryInternals.getContext().upload(file);
                }
            }
        });
    }
}
