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
