package com.github.moduth.blockcanary.ui;

import android.content.res.Resources;
import android.graphics.PorterDuffXfermode;
import android.util.DisplayMetrics;
import static android.graphics.PorterDuff.Mode.CLEAR;

/**
 * @author yifan.zhai on 15/9/27.
 */
final class LeakCanaryUi {
    static final int LIGHT_GREY = 0xFFbababa;
    static final int ROOT_COLOR = 0xFF84a6c5;
    static final int LEAK_COLOR = 0xFFb1554e;

    static final PorterDuffXfermode CLEAR_XFER_MODE = new PorterDuffXfermode(CLEAR);

    /**
     * Converts from device independent pixels (dp or dip) to
     * device dependent pixels. This method returns the input
     * multiplied by the display's density. The result is not
     * rounded nor clamped.
     *
     * The value returned by this method is well suited for
     * drawing with the Canvas API but should not be used to
     * set layout dimensions.
     *
     * @param dp The value in dp to convert to pixels
     * @param resources An instances of Resources
     */
    static float dpToPixel(float dp, Resources resources) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return metrics.density * dp;
    }

    private LeakCanaryUi() {
        throw new AssertionError();
    }
}
