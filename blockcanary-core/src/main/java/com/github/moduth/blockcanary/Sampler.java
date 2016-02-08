package com.github.moduth.blockcanary;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Common sampler defines sampling work flow.
 *
 * @author markzhai
 */
public abstract class Sampler {

    static final int DEFAULT_SAMPLE_INTERVAL_MILLIS = 300;

    AtomicBoolean mIsDumping = new AtomicBoolean(false);

    long mSampleIntervalMillis;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doSample();

            // If non-stop, doSample again after mSampleIntervalMillis elapses.
            if (mIsDumping.get()) {
                HandlerThread.getTimerThreadHandler().postDelayed(mRunnable,
                        mSampleIntervalMillis);
            }
        }
    };

    public Sampler(long sampleIntervalMillis) {
        if (0 == sampleIntervalMillis) {
            sampleIntervalMillis = DEFAULT_SAMPLE_INTERVAL_MILLIS;
        }
        mSampleIntervalMillis = sampleIntervalMillis;
    }

    /**
     * start sampling.
     */
    public void start() {
        // exit if dumping
        if (mIsDumping.get()) {
            return;
        }
        mIsDumping.set(true);

        HandlerThread.getTimerThreadHandler().removeCallbacks(mRunnable);
        HandlerThread.getTimerThreadHandler().postDelayed(mRunnable,
                BlockCanaryCore.get().getSampleDelay());
    }

    /**
     * stop sampling
     */
    public void stop() {
        // exit if not dumping
        if (!mIsDumping.get()) {
            return;
        }
        mIsDumping.set(false);
        HandlerThread.getTimerThreadHandler().removeCallbacks(mRunnable);
    }

    abstract void doSample();
}
