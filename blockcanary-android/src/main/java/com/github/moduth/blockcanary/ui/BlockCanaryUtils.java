package com.github.moduth.blockcanary.ui;

import android.text.TextUtils;

import com.github.moduth.blockcanary.BlockCanaryInternals;
import com.github.moduth.blockcanary.internal.BlockInfo;
import com.github.moduth.blockcanary.internal.ProcessUtils;

import java.util.LinkedList;
import java.util.List;

final class BlockCanaryUtils {

    private static final List<String> WHITE_LIST = new LinkedList<>();
    private static final List<String> CONCERN_LIST = new LinkedList<>();

    static {
        WHITE_LIST.addAll(BlockCanaryInternals.getContext().provideWhiteList());

        if (BlockCanaryInternals.getContext().concernPackages() != null) {
            CONCERN_LIST.addAll(BlockCanaryInternals.getContext().concernPackages());
        }
        if (CONCERN_LIST.isEmpty()) {
            CONCERN_LIST.add(ProcessUtils.myProcessName());
        }
    }

    /**
     * Get key stack string to show as title in ui list.
     */
    public static String concernStackString(BlockInfo blockInfo) {
        String result = "";
        for (String stackEntry : blockInfo.threadStackEntries) {
            if (Character.isLetter(stackEntry.charAt(0))) {
                String[] lines = stackEntry.split(BlockInfo.SEPARATOR);
                for (String line : lines) {
                    String keyStackString = concernStackString(line);
                    if (keyStackString != null) {
                        return keyStackString;
                    }
                }
                return classSimpleName(lines[0]);
            }
        }
        return result;
    }

    public static boolean isBlockInfoValid(BlockInfo blockInfo) {
        boolean isValid = !TextUtils.isEmpty(blockInfo.timeStart);
        isValid = isValid && blockInfo.timeCost >= 0;
        return isValid;
    }

    public static boolean isInWhiteList(BlockInfo info) {
        for (String stackEntry : info.threadStackEntries) {
            if (Character.isLetter(stackEntry.charAt(0))) {
                String[] lines = stackEntry.split(BlockInfo.SEPARATOR);
                for (String line : lines) {
                    for (String whiteListEntry : WHITE_LIST) {
                        if (line.startsWith(whiteListEntry)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static List<String> getConcernPackages() {
        return CONCERN_LIST;
    }

    private static String concernStackString(String line) {
        for (String concernPackage : CONCERN_LIST) {
            if (line.startsWith(concernPackage)) {
                return classSimpleName(line);
            }
        }
        return null;
    }

    private static String classSimpleName(String stackLine) {
        int index1 = stackLine.indexOf('(');
        int index2 = stackLine.indexOf(')');
        if (index1 >= 0 && index2 >= 0) {
            return stackLine.substring(index1 + 1, index2);
        }
        return stackLine;
    }
}
