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
package com.github.moduth.blockcanary.log;

import com.github.moduth.blockcanary.BlockCanaryContext;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>上报监控日志</p>
 * Created by markzhai on 2015/9/25.
 */
public class UploadMonitorLog {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private static File zipFile() {
        String timeString = System.currentTimeMillis() + "";
        try {
            timeString = FORMAT.format(new Date());
        } catch (Throwable e) {
            // 以防万一
        }
        File zippedFile = LogWriter.generateTempZipFile("Monitor_looper_" + timeString);
        BlockCanaryContext.get().zipLogFile(BlockCanaryInternals.getLogFiles(), zippedFile);
        LogWriter.deleteLogFiles();
        return zippedFile;
    }

    public static void forceZipLogAndUpload() {
        BlockCanaryContext.get().getWriteLogFileThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                final File file = zipFile();
                if (file.exists()) {
                    BlockCanaryContext.get().uploadLogFile(file);
                }
            }
        });
    }
}
