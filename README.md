[中文](https://github.com/moduth/blockcanary/blob/master/README_CN.md)

# BlockCanary [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.moduth/blockcanary/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.moduth/blockcanary)
A transparent ui-block detection library for Android. App only needs one-line-code to setup and provide application context.

# Getting started

```gradle
dependencies {
    // or you can use compile directly if want to enable it in release package and upload log file to server
    // compile 'com.github.moduth:blockcanary:1.0.1'
    debugCompile 'com.github.moduth:blockcanary:1.0.1'
    releaseCompile 'com.github.moduth:blockcanary-no-op:1.0.1'
}
```

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