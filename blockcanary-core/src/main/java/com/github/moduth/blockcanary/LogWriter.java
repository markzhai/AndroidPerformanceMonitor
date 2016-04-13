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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

/**
 * Log writer which runs in standalone thread.
 *
 * @author markzhai on 2015/9/25.
 */
public class LogWriter {

    private static final String TAG = "LogWriter";

    private static final Object SAVE_DELETE_LOCK = new Object();
    private static final SimpleDateFormat FILE_NAME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final long OBSOLETE_DURATION = 2 * 24 * 3600 * 1000L;

    private LogWriter() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Save log to file
     *
     * @param str block log string
     * @return log file path
     */
	public static String saveLooperLog(String str) {
        String path;
        synchronized (SAVE_DELETE_LOCK) {
            path = saveLogToSDCard("looper", str);
        }
        return path;
    }

    /**
     * Delete obsolete log files，see also {@code OBSOLETE_DURATION}
     */
    public static void cleanOldFiles() {
        HandlerThread.getWriteLogFileThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                File[] f = BlockCanaryInternals.getLogFiles();
                if (f != null && f.length > 0) {
                    synchronized (SAVE_DELETE_LOCK) {
                        for (File aF : f) {
                            if (now - aF.lastModified() > OBSOLETE_DURATION) {
                                aF.delete();
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Delete all log files.
     */
    public static void deleteLogFiles() {
        synchronized (SAVE_DELETE_LOCK) {
            try {
                File[] files = BlockCanaryInternals.getLogFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "deleteLogFiles: ", e);
            }
        }
    }

    private static String saveLogToSDCard(String logFileName, String str) {
        String path = "";
        BufferedWriter writer = null;
        try {
            File file = BlockCanaryInternals.detectedBlockDirectory();
            long time = System.currentTimeMillis();
            path = file.getAbsolutePath() + "/" + logFileName + "-" + FILE_NAME_FORMATTER.format(time) + ".txt";
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path, true), "UTF-8");

            writer = new BufferedWriter(out);
            writer.write("\r\n**********************\r\n");
            writer.write(TIME_FORMATTER.format(time) + "(write log time)");
            writer.write("\r\n");
            writer.write("\r\n");
            writer.write(str);
            writer.write("\r\n");
            writer.flush();
            writer.close();
            writer = null;
        } catch (Throwable t) {
            Log.e(TAG, "saveLogToSDCard: ", t);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "saveLogToSDCard: ", e);
            }
        }
        return path;
    }

    public static File generateTempZipFile(String filename) {
        return new File(BlockCanaryInternals.getPath() + "/" + filename + ".log.zip");
    }
}
