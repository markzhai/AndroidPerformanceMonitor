/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.moduth.blockcanary.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public final class MoreDetailsView extends View {

    private final Paint mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mFolding = true;

    public MoreDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIconPaint.setStrokeWidth(BlockCanaryUi.dpToPixel(2f, getResources()));
        mIconPaint.setColor(BlockCanaryUi.ROOT_COLOR);
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