[Chinese README](https://github.com/moduth/blockcanary/blob/master/README_CN.md)

# BlockCanary [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.moduth/blockcanary-android/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.moduth/blockcanary-android)
A transparent ui-block detection library for Android, app only needs one-line-code to setup. Author: [markzhai](https://github.com/markzhai), [Contributors](https://github.com/moduth/blockcanary#contributors)

The naming is to pay respect to the great library [LeakCanary](https://github.com/square/leakcanary), ui-related codes are modified from leakcanary's ui part.

# Getting started

You may choose how to assemble them as you like.

```gradle
dependencies {
    // most often used way, enable notification to notify block event
    compile 'com.github.moduth:blockcanary-android:1.2.1'

    // this way you only enable BlockCanary in debug package
    // debugCompile 'com.github.moduth:blockcanary-android:1.2.1'
    // releaseCompile 'com.github.moduth:blockcanary-no-op:1.2.1'
}
```

PS: As this library uses `getMainLooper().setMessageLogging()`, please check if you also set that in your app. (check related issue https://github.com/moduth/blockcanary/issues/27)

# Usage

Maximum log count is set to 100, you can rewrite it in your app `int.xml`.
```xml
<integer name="block_canary_max_stored_count">100</integer>
```

```java
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        ...
        // Do it on main process
        BlockCanary.install(this, new AppBlockCanaryContext()).start();
    }
}
```

Implement BlockCanaryContext context：
```java
public class AppBlockCanaryContext extends BlockCanaryContext {
    // override to provide context like app qualifier, uid, network type, block threshold, log save path

    // this is default block threshold, you can set it by phone's performance
    @Override
    public int getConfigBlockThreshold() {
        return 500;
    }

    // if set true, notification will be shown, else only write log file
    @Override
    public boolean isNeedDisplay() {
        return BuildConfig.DEBUG;
    }

    // path to save log file
    @Override
    public String getLogPath() {
        return "/blockcanary/performance";
    }
}
```

# How does it work?
Blog in Chinese: [BlockCanary](http://blog.zhaiyifan.cn/2016/01/16/BlockCanaryTransparentPerformanceMonitor/).

Principle flow picture:

![flow](art/flow.png "flow")

1. `BlockCanary.install()` initializes context and internal data structures.
2. `BlockCanary.start()` starts monitor by `Looper.getMainLooper().setMessageLogging(mMainLooperPrinter);`
3. `ThreadStackSampler` and `CpuSampler` start catching thread stack and cpu data.
4. Each time a message dispatch costs time over that set by `BlockCanaryContext.getConfigBlockThreshold`, it triggers a block notify.
5. Write log file with data for analysis.
6. If `BlockCanaryContext.isNeedDisplay` is true, a notification is shown, developer can click and check directly.

# Screenshot

![Block detail](art/shot1.png "detail")
![Block list](art/shot2.png "list")

# Contributors

This library is initially created by [markzhai](https://github.com/markzhai), and is maintained under the organization [moduth](https://github.com/moduth) with [nimengbo](https://github.com/nimengbo) and [zzz40500](https://github.com/zzz40500).

Special thanks to [android-cjj](https://github.com/android-cjj), [Mr.Bao](https://github.com/baoyongzhang), [chiahaolu](https://github.com/chiahaolu) to contribute when this library is still young.

# Change Log

Check [CHANGELOG](https://github.com/moduth/blockcanary/blob/master/CHANGELOG.md)

# Contribute

If you would like to contribute code to BlockCanary you can do so through GitHub by forking the repository and sending a pull request.

# License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
