/**
 * Copyright 2017 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.pdfviewer.util;

import android.app.Activity;
import android.util.Log;
import android.content.pm.ActivityInfo;

import io.legere.pdfiumandroid.util.Size;

public class PageSizeCalculator {

    private final FitPolicy fitPolicy;
    private final Size originalMaxWidthPageSize;
    private final Size originalMaxHeightPageSize;
    private final Size viewSize;
    private Size optimalMaxWidthPageSize;
    private Size optimalMaxHeightPageSize;
    private float widthRatio;
    private float heightRatio;
    private boolean fitEachPage;

    public PageSizeCalculator(FitPolicy fitPolicy, Size originalMaxWidthPageSize, Size originalMaxHeightPageSize,
                              Size viewSize, boolean fitEachPage) {
        this.fitPolicy = fitPolicy;
        this.originalMaxWidthPageSize = originalMaxWidthPageSize;
        this.originalMaxHeightPageSize = originalMaxHeightPageSize;
        this.viewSize = viewSize;
        this.fitEachPage = fitEachPage;
        calculateMaxPages();
    }

    public Size calculate(Size pageSize, boolean showTwoPages, boolean isLandscape) {
        if (pageSize.getWidth() <= 0 || pageSize.getHeight() <= 0) {
            return new Size(0, 0);
        }
        float maxWidth = 0;
        if(showTwoPages && !isLandscape){
            maxWidth = fitEachPage ? viewSize.getWidth()  : (float) pageSize.getWidth() / 2 * widthRatio;
        } else {
            maxWidth = fitEachPage ? viewSize.getWidth() : pageSize.getWidth() * widthRatio;
        }
        float maxHeight = fitEachPage ? viewSize.getHeight() : pageSize.getHeight() * heightRatio;
        switch (fitPolicy) {
            case HEIGHT:
                return fitHeight(pageSize, maxHeight);
            case BOTH:
                return fitBoth(pageSize, maxWidth, maxHeight);
            default:
                return fitWidth(pageSize, maxWidth);
        }
    }

    public Size getOptimalMaxWidthPageSize() {
        return optimalMaxWidthPageSize;
    }

    public Size getOptimalMaxHeightPageSize() {
        return optimalMaxHeightPageSize;
    }

    private void calculateMaxPages() {
        switch (fitPolicy) {
            case HEIGHT:
                optimalMaxHeightPageSize = fitHeight(originalMaxHeightPageSize, viewSize.getHeight());
                heightRatio = (float) optimalMaxHeightPageSize.getHeight() / originalMaxHeightPageSize.getHeight();
                optimalMaxWidthPageSize = fitHeight(originalMaxWidthPageSize, originalMaxWidthPageSize.getHeight() * heightRatio);
                break;
            case BOTH:
                Size localOptimalMaxWidth = fitBoth(originalMaxWidthPageSize, viewSize.getWidth(), viewSize.getHeight());
                float localWidthRatio = (float) localOptimalMaxWidth.getWidth() / originalMaxWidthPageSize.getWidth();
                this.optimalMaxHeightPageSize = fitBoth(originalMaxHeightPageSize, originalMaxHeightPageSize.getWidth() * localWidthRatio,
                        viewSize.getHeight());
                heightRatio = (float) optimalMaxHeightPageSize.getHeight() / originalMaxHeightPageSize.getHeight();
                optimalMaxWidthPageSize = fitBoth(originalMaxWidthPageSize, viewSize.getWidth(), originalMaxWidthPageSize.getHeight() * heightRatio);
                widthRatio = (float) optimalMaxWidthPageSize.getWidth() / originalMaxWidthPageSize.getWidth();
                break;
            default:
                optimalMaxWidthPageSize = fitWidth(originalMaxWidthPageSize, viewSize.getWidth());
                widthRatio = (float) optimalMaxWidthPageSize.getWidth() / originalMaxWidthPageSize.getWidth();
                optimalMaxHeightPageSize = fitWidth(originalMaxHeightPageSize, originalMaxHeightPageSize.getWidth() * widthRatio);
                break;
        }
    }

    private Size fitWidth(Size pageSize, float maxWidth) {
        float w = pageSize.getWidth(), h = pageSize.getHeight();
        float ratio = w / h;
        w = maxWidth;
        h = (float) Math.floor(maxWidth / ratio);
        return new Size(Math.round(w), Math.round(h));
    }

    private Size fitHeight(Size pageSize, float maxHeight) {
        float w = pageSize.getWidth(), h = pageSize.getHeight();
        float ratio = h / w;
        h = maxHeight;
        w = (float) Math.floor(maxHeight / ratio);
        return new Size(Math.round(w), Math.round(h));
    }

    private Size fitBoth(Size pageSize, float maxWidth, float maxHeight) {
        float w = pageSize.getWidth(), h = pageSize.getHeight();
        float ratio = w / h;
        w = maxWidth;
        h = (float) Math.floor(maxWidth / ratio);
        if (h > maxHeight) {
            h = maxHeight;
            w = (float) Math.floor(maxHeight * ratio);
        }
        return new Size(Math.round(w), Math.round(h));
    }

}
