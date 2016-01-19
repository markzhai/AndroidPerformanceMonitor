**NOT DONE YET, ONLY CODE AND HALFWAY README**

# BlockCanary
BlockCanary是一个Android平台的一个非侵入式的性能监控组件，应用只需要实现一个抽象类，提供一些该组件需要的上下文环境，就可以在平时使用应用的时候检测主线程上的各种卡慢问题，并通过组件提供的各种信息分析出原因并进行修复。

# 功能及原理
见[BlockCanary — 轻松找出Android App界面卡顿元凶](http://blog.zhaiyifan.cn/2016/01/16/BlockCanaryTransparentPerformanceMonitor/).

# 使用方法

在Application中：
```java
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        ...
        // 在主进程初始化调用哈
        BlockCanary.install(this, new AppBlockCanaryContext()).start();
    }
}
```

实现自己的监控上下文：
```java
public class AppBlockCanaryContext extends BlockCanaryContext {
    // 实现各种上下文，包括应用标示符，用户uid，网络类型，卡慢判断阙值，Log保存位置等
}
```

# 引入

```gradle
dependencies {
    // 如果希望在release包也开启监控可以直接用compile
    debugCompile 'com.github.moduth:blockcanary:1.0.0'
    releaseCompile 'com.github.moduth:blockcanary-no-op:1.0.0'
}
```

# Demo工程
**请参考本项目下的demo module，点击三个按钮会触发对应的耗时事件，消息栏则会弹出block的notification，点击可以进去查看详细信息。**
