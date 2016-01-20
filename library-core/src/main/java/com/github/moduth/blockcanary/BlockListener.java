package com.github.moduth.blockcanary;

interface BlockListener {
    void onBlockEvent(long realStartTime, long realTimeEnd, long threadTimeStart,
                      long threadTimeEnd);
}
