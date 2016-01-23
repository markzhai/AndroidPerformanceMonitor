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

import com.github.moduth.blockcanary.BlockCanaryCore;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author markzhai on 15/9/27.
 */
public final class BlockCanaryInternals {

    public static String getPath() {
        String state = android.os.Environment.getExternalStorageState();
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
            if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
                return android.os.Environment.getExternalStorageDirectory().getPath()
                        + BlockCanaryCore.getContext().getLogPath();
            }
        }
        return android.os.Environment.getDataDirectory().getAbsolutePath() + BlockCanaryCore.getContext().getLogPath();
    }

    public static File detectedLeakDirectory() {
        File directory = new File(getPath());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public static File[] getLogFiles() {
        File f = BlockCanaryInternals.detectedLeakDirectory();
        if (f.exists() && f.isDirectory()) {
            return f.listFiles(new BlockLogFileFilter());
        }
        return null;
    }

    private BlockCanaryInternals() {
        throw new AssertionError();
    }

    static class BlockLogFileFilter implements FilenameFilter {

        private String TYPE = ".txt";

        public BlockLogFileFilter() {

        }

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(TYPE);
        }
    }
}
