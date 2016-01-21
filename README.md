[中文](https://github.com/moduth/blockcanary/blob/master/README_CN.md)

# BlockCanary [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.moduth/blockcanary/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.moduth/blockcanary)
A transparent ui-block detection library for Android. App only needs one-line-code to setup and provide application context.

The naming is to pay respect to the great library [LeakCanary](https://github.com/square/leakcanary), ui-related codes are modified from leakcanary's ui part.

# Getting started

```gradle
dependencies {
    // or you can use compile directly if want to enable it in release package and upload log file to server
    // compile 'com.github.moduth:blockcanary:1.0.2'
    debugCompile 'com.github.moduth:blockcanary:1.0.2'
    releaseCompile 'com.github.moduth:blockcanary-no-op:1.0.2'
}
```

PS: As this library uses `getMainLooper().setMessageLogging();`, please check if you also set that in your app.

# Usage

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
See [BlockCanary](http://blog.zhaiyifan.cn/2016/01/16/BlockCanaryTransparentPerformanceMonitor/).

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