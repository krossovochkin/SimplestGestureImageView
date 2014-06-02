/*
* Copyright 2014 Vasya Drobushkov
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.krossovochkin.simplestgestureimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class SimplestGestureImageView extends ImageView {

    /**
     * Gesture detectors
     */
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    /**
     * Default values
     */
    private static final float DEFAULT_SCALE_MIN = 1.0f;
    private static final float DEFAULT_SCALE_MAX = 1.0f;
    private static final float DEFAULT_SCALE = 1.0f;

    private static final float DEFAULT_SCALE_FOCUS_X = 0.0f;
    private static final float DEFAULT_SCALE_FOCUS_Y = 0.0f;

    private static final float DEFAULT_X = 0.0f;
    private static final float DEFAULT_Y = 0.0f;

    /**
     * Current image position
     */
    private float mX = DEFAULT_X;
    private float mY = DEFAULT_Y;

    /**
     * Scale values
     */
    private float mScaleMax = DEFAULT_SCALE_MAX;
    private float mScaleMin = DEFAULT_SCALE_MIN;
    private float mScaleDefault = DEFAULT_SCALE;

    /**
     * Current scale focus position
     */
    private float mScaleFocusX = DEFAULT_SCALE_FOCUS_X;
    private float mScaleFocusY = DEFAULT_SCALE_FOCUS_Y;
    /**
     * Current scale value
     */
    private float mScaleFactor = mScaleDefault;

    public SimplestGestureImageView(Context context) {
        super(context);

        init(context);
    }

    public SimplestGestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
        initScales(context, attrs);
    }

    public SimplestGestureImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
        initScales(context, attrs);
    }

    /**
     * Inits gesture detectors, and sets touch listener
     * @param context - context instance
     */
    private void init(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetectorListener());
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean res = mScaleGestureDetector.onTouchEvent(motionEvent);
                if (!mScaleGestureDetector.isInProgress()) {
                    res = mGestureDetector.onTouchEvent(motionEvent);
                }
                invalidate();
                return res;
            }
        });
    }

    /**
     * Inits scale values from attributes
     * @param context - context instance
     * @param attrs - attributes
     */
    private void initScales(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SimpleGestureView);

        if (array != null) {
            float scaleMin = array.getFloat(R.styleable.SimpleGestureView_scaleMin, DEFAULT_SCALE_MIN);
            float scaleMax = array.getFloat(R.styleable.SimpleGestureView_scaleMax, DEFAULT_SCALE_MAX);
            float defaultScale = array.getFloat(R.styleable.SimpleGestureView_defaultScale, DEFAULT_SCALE);

            setScales(scaleMin, scaleMax, defaultScale, true);

            array.recycle();
        }
    }

    /**
     * Set scale values
     * @param scaleMin - new value for min scale
     * @param scaleMax - new value for max scale
     * @param defaultScale - new value for default scale
     * @param shouldApplyDefaultScale - if true, than current scale will be set to default, if false, nothing is gonna happen
     * @throws java.lang.IllegalArgumentException if at least one of scales is less than zero, if min scale is greater than max
     * scale, and if default scale is not between (or equal to) min and max values
     */
    public void setScales(float scaleMin, float scaleMax, float defaultScale, boolean shouldApplyDefaultScale) {
        setScales(scaleMin, scaleMax);
        setDefaultScale(defaultScale, shouldApplyDefaultScale);
    }

    /**
     * Set scale values
     * @param scaleMin - new value for max scale
     * @param scaleMax - new value for min scale
     * @throws java.lang.IllegalArgumentException if at least one of scales is less than zero and if min scale is greater than max scale
     */
    public void setScales(float scaleMin, float scaleMax) {
        if (scaleMin < 0) {
            throw new IllegalArgumentException("max scale should be greater than zero");
        }
        if (scaleMax < 0) {
            throw new IllegalArgumentException("min scale should be greater than zero");
        }
        if (scaleMin > scaleMax) {
            throw new IllegalArgumentException("min scale should be less than max scale");
        }

        this.mScaleMax = scaleMax;
        this.mScaleMin = scaleMin;

        if (mScaleMax < mScaleDefault) {
            mScaleDefault = mScaleMax;
        } else if (mScaleMin > mScaleDefault) {
            mScaleDefault = mScaleMin;
        }
    }

    /**
     * Set max scale value
     * @param scaleMax - new value for max scale
     * @throws java.lang.IllegalArgumentException if scale is less than zero and if max scale is less than current min scale
     */
    public void setScaleMax(float scaleMax) {
        if (scaleMax < 0) {
            throw new IllegalArgumentException("scale should be greater than zero");
        }
        if (scaleMax < mScaleMin) {
            throw new IllegalArgumentException("max scale should be greater than min scale. Change min scale first, or use setScales(min, max) method");
        }

        this.mScaleMax = scaleMax;

        if (mScaleMax < mScaleFactor) {
            mScaleFactor = mScaleMax;
        }

        if (mScaleMax < mScaleDefault) {
            mScaleDefault = mScaleMax;
        }
    }

    /**
     * Set min scale value
     * @param scaleMin - new value for min scale
     * @throws java.lang.IllegalArgumentException if scale is less than zero and if min scale is greater than current max scale
     */
    public void setScaleMin(float scaleMin) {
        if (scaleMin < 0) {
            throw new IllegalArgumentException("scale should be greater than zero");
        }
        if (scaleMin > mScaleMax) {
            throw new IllegalArgumentException("min scale should be less than max scale. Change max scale first, or use setScales(min, max) method");
        }

        this.mScaleMin = scaleMin;

        if (mScaleMin > mScaleFactor) {
            mScaleFactor = mScaleMin;
        }

        if (mScaleMin > mScaleDefault) {
            mScaleDefault = mScaleMin;
        }
    }

    /**
     * Set default scale value
     * @param defaultScale - new value for default scale
     * @param shouldApply - if true, then current scale will be set to default, if false, nothing is gonna happen
     * @throws java.lang.IllegalArgumentException if default scale is less than zero and if default scale is not
     * between (or equal to) min and max scales
     */
    public void setDefaultScale(float defaultScale, boolean shouldApply) {
        if(defaultScale < 0) {
            throw new IllegalArgumentException("scale should be greater than zero");
        }
        if(defaultScale < mScaleMin) {
            throw new IllegalArgumentException("default scale should be greater than min scale");
        }
        if(defaultScale > mScaleMax) {
            throw new IllegalArgumentException("default scale should be less than max scale");
        }

        mScaleDefault = defaultScale;

        if(shouldApply) {
            mScaleFactor = mScaleDefault;
        }
    }

    /**
     * Calculates new position after shift. These calculated position values will be used in onDraw method
     * @param x - x-axis shift
     * @param y - y-axis shift
     */
    private void translateBy(float x, float y) {
        mX += -x / mScaleFactor;
        mY += -y / mScaleFactor;
    }

    /**
     * Calculates new scale. New scale value will be between (or equal to) min and max scales values.
     * Calculated scale value will be used in onDraw method
     * @param scaleFactor - scale factor value
     */
    private void scaleBy(float scaleFactor) {
        mScaleFactor *= scaleFactor;
        mScaleFactor = Math.max(mScaleMin, Math.min(mScaleFactor, mScaleMax));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            super.onDraw(canvas);
        } else {
            canvas.save();

            canvas.scale(mScaleFactor, mScaleFactor, mScaleFocusX, mScaleFocusY);
            canvas.translate(mX, mY);
            super.onDraw(canvas);

            canvas.restore();
        }
    }

    /**
     * Scale listener
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFocusX = detector.getFocusX();
            mScaleFocusY = detector.getFocusY();

            scaleBy(detector.getScaleFactor());
            return true;
        }
    }

    /**
     * Move (translate) listener
     */
    private class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            translateBy(distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            return true;
        }
    }
}