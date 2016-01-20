# BlockCanary
A transparent ui-block detection library for Android. App only needs one-line-code to setup and provide application context.

# Getting started

```gradle
dependencies {
    // or you can use compile directly if want to enable it in release package and upload log file to server
    // compile 'com.github.moduth:blockcanary:1.0.0'
    debugCompile 'com.github.moduth:blockcanary:1.0.0'
    releaseCompile 'com.github.moduth:blockcanary-no-op:1.0.0'
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

Implement application context：
```java
public class AppBlockCanaryContext extends BlockCanaryContext {
    // override to provide context like app qualifier, uid, network type, block threshold, log save path
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