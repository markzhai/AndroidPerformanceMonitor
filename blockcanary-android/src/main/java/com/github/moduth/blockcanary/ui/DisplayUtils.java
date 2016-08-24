package com.github.moduth.blockcanary.ui;

import com.github.moduth.blockcanary.BlockCanaryInternals;
import com.github.moduth.blockcanary.internal.BlockInfo;
import com.github.moduth.blockcanary.internal.ProcessUtils;

final class DisplayUtils {

    /**
     * Get key stack string to show as title in ui list.
     */
    public static String keyStackString(BlockInfo blockInfo) {
        String result = "";
        for (String stackEntry : blockInfo.threadStackEntries) {
            if (Character.isLetter(stackEntry.charAt(0))) {
                String[] lines = stackEntry.split(BlockInfo.SEPARATOR);
                for (String line : lines) {
                    String keyStackString = keyStackString(line);
                    if (keyStackString != null) {
                        return keyStackString;
                    }
                }
                return classSimpleName(lines[0]);
            }
        }
        return result;
    }

    /**
     * Class prefix for adapter to fold stack
     */
    public static String getStackFoldPrefix() {
        String prefix = BlockCanaryInternals.getContext().provideStackFoldPrefix();
        if (prefix == null) {
            prefix = ProcessUtils.myProcessName();
        }
        return prefix;
    }

    private static String keyStackString(String line) {
        if (line.startsWith(getStackFoldPrefix())) {
            return classSimpleName(line);
        }
        return null;
    }

    private static String classSimpleName(String className) {
//        int separator = className.lastIndexOf('.');
//        if (separator == -1) {
//            return className;
//        } else {
//            return className.substring(separator + 1);
//        }
        return className.substring(className.indexOf('(') + 1, className.indexOf(')'));
    }
}
