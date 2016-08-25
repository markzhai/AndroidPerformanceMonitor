package com.github.moduth.blockcanary.ui;

import java.util.Locale;

/**
 * @author markzhai on 16/8/24
 * @version 1.3.0
 */
public class BlockInfoCorruptException extends Exception {

    public BlockInfoCorruptException(BlockInfoEx blockInfo) {
        this(String.format(Locale.US,
                "BlockInfo (%s) is corrupt.", blockInfo.logFile.getName()));
    }

    public BlockInfoCorruptException(String detailMessage) {
        super(detailMessage);
    }
}
