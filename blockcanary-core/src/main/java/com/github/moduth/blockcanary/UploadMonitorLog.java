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

import android.util.Log;

import com.github.moduth.blockcanary.log.BlockCanaryInternals;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Upload monitor log.
 * <p>
 * Created by markzhai on 2015/9/25.
 */
class UploadMonitorLog {

    private static final String TAG = "UploadMonitorLog";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private UploadMonitorLog() {
        throw new InstantiationError("Must not instantiate this class");
    }

    private static File zipFile() {
        String timeString = Long.toString(System.currentTimeMillis());
        try {
            timeString = FORMAT.format(new Date());
        } catch (Throwable e) {
            Log.e(TAG, "zipFile: ", e);
        }
        File zippedFile = LogWriter.generateTempZipFile("Monitor_looper_" + timeString);
        BlockCanaryCore.getContext().zipLogFile(BlockCanaryInternals.getLogFiles(), zippedFile);
        LogWriter.deleteLogFiles();
        return zippedFile;
    }

    public static void forceZipLogAndUpload() {
        HandlerThread.getWriteLogFileThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                final File file = zipFile();
                if (file.exists()) {
                    BlockCanaryCore.getContext().uploadLogFile(file);
                }
            }
        });
    }
}
