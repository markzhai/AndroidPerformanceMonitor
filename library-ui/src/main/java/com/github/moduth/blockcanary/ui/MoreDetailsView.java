package com.github.moduth.blockcanary.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author yifan.zhai on 15/9/27.
 */
public final class MoreDetailsView extends View {

    private final Paint mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mFolding = true;

    public MoreDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIconPaint.setStrokeWidth(LeakCanaryUi.dpToPixel(2f, getResources()));
        mIconPaint.setColor(LeakCanaryUi.ROOT_COLOR);
    }


    @Override protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int halfHeight = height / 2;
        int halfWidth = width / 2;

        canvas.drawLine(0, halfHeight, width, halfHeight, mIconPaint);
        if (mFolding) {
            canvas.drawLine(halfWidth, 0, halfWidth, height, mIconPaint);
        }
    }

    public void setFolding(boolean folding) {
        if (folding != this.mFolding) {
            this.mFolding = folding;
            invalidate();
        }
    }
}