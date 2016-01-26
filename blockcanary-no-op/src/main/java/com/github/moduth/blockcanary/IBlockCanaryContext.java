package com.github.moduth.blockcanary;

import android.content.Context;

import java.io.File;

/**
 * Created by markzhai on 16/1/26
 *
 * @author markzhai
 */
public interface IBlockCanaryContext {

    /**
     * Config block threshold
     *
     * @return threshold in mills
     */
    int getConfigBlockThreshold();

    /**
     * If need notification and list ui
     *
     * @return true if need, else if not need.
     */
    boolean isNeedDisplay();

    String getQualifier();

    String getUid();

    String getNetworkType();

    Context getContext();

    String getLogPath();

    boolean zipLogFile(File[] src, File dest);

    void uploadLogFile(File zippedFile);

    /**
     * Config string prefix to determine how to fold stack
     *
     * @return string prefix, null if use process name.
     */
    String getStackFoldPrefix();
}
