package com.github.moduth.blockcanary;

import android.content.Context;
import android.os.Handler;

import java.io.File;

/**
 * Created by Abner on 16/1/21.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public interface IBlockCanaryContext {


    int getConfigBlockThreshold();

    Handler getWriteLogFileThreadHandler();

    Handler getTimerThreadHandler();

    String getQualifier();

    String getUid();

    String getNetworkType();

    Context getContext();

    String getLogPath();

    boolean zipLogFile(File[] src, File dest);

    void uploadLogFile(File zippedFile);


}
