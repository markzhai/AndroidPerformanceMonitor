package com.github.moduth.blockcanary;

/**
 * Created by zzz40500 on 16/1/21.
 */
public class BlockCanaryContextInner {

    private static IBlockCanaryContext mIBlockCanaryContext;


    public static void setIBlockCanaryContext(IBlockCanaryContext IBlockCanaryContext) {
        mIBlockCanaryContext = IBlockCanaryContext;
    }

    public static IBlockCanaryContext get(){

        return  mIBlockCanaryContext;
    }

}
