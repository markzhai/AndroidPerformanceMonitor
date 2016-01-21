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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.github.moduth.blockcanary.BlockCanaryContext;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

/**
 * @author markzhai on 15/9/27.
 */
public final class BlockCanaryInternals {
    private static final Executor fileIoExecutor = Executors.newSingleThreadExecutor();

    public static void executeOnFileIoThread(Runnable runnable) {
        fileIoExecutor.execute(runnable);
    }

    public static String getPath() {
        String state = android.os.Environment.getExternalStorageState();
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
            if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
                return android.os.Environment.getExternalStorageDirectory().getPath()
                        + BlockCanaryContext.get().getLogPath();
            }
        }
        return android.os.Environment.getDataDirectory().getAbsolutePath() + BlockCanaryContext.get().getLogPath();
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


    public static void setEnabled(Context context, final Class<?> componentClass,
                                  final boolean enabled) {
        final Context appContext = context.getApplicationContext();
        executeOnFileIoThread(new Runnable() {
            @Override
            public void run() {
                ComponentName component = new ComponentName(appContext, componentClass);
                PackageManager packageManager = appContext.getPackageManager();
                int newState = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
                // Blocks on IPC.
                packageManager.setComponentEnabledSetting(component, newState, DONT_KILL_APP);
            }
        });
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
