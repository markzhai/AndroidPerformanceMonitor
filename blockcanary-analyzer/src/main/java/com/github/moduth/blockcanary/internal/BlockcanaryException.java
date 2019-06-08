package com.github.moduth.blockcanary.internal;

public class BlockcanaryException extends Exception{

    public BlockcanaryException() {
    }

    public BlockcanaryException(String detailMessage) {
        super(detailMessage);
    }

    public BlockcanaryException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BlockcanaryException(Throwable throwable) {
        super(throwable);
    }
}
