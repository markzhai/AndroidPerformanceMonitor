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

import android.content.Context;

import java.io.File;

/**
 * Created by Abner on 16/1/21.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
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
