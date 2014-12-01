package ca.farrelltonsolar.uicomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class SolarGauge extends View {

    private static final String TAG = SolarGauge.class.getSimpleName();
    public static final int[] OUTER_SHADOW_COLORS = {Color.argb(40, 255, 254, 187), Color.argb(20, 255, 247, 219),
            Color.argb(5, 255, 255, 255)};
    public static final float[] OUTER_SHADOW_POS = {0.90f, 0.95f, 0.99f};

    private float mScaleStartAngle;
    private float mScaleEndAngle;
    private float mAvailableAngle;
    private Quadrant mFace = Quadrant.Full;

    private float mScaleStartValue;
    private float mScaleEndValue;

    private float mArcRadius;
    private float mMajorTickStepValue;
    private float mMajorTicksLength;
    private float mMinorTicksLength;

    private int mDefaultColor = Color.rgb(180, 180, 180);
    private double mMajorTickPercentOfMax;
    private int mMinorTicksPerDivision;
    private LabelConverter mLabelConverter;
    private String mGaugeTitle;
    private String mReadingUnit;

    private List<ColoredRange> mRanges = new ArrayList<ColoredRange>();

    private int mTitleColor;
    private int mReadingColor;
    private int mTickLabelColor;

    private Paint mNeedleScrewPaint;
    private Paint mNeedleScrewBorderPaint;
    private Paint mBackgroundPaint;
    private Paint mBackgroundInnerPaint;
    private Paint mMaskPaint;
    private Paint mNeedlePaint;
    private Paint mTicksPaint;
    private Paint mTickLabelTextPaint;
    private Paint mColorLinePaint;
    private Paint mLed1Colour;
    private Paint mLed2Colour;
    private Paint mTitleTextPaint;
    private Paint mReadingTextPaint;

    private Paint mTestPaint;

    private boolean mBiDirectional;
    private boolean mShowLEDs;
    private boolean mShowReading;
    private boolean mShowScale;

    private boolean mShowOuterShadow = true;
    private boolean mShowOuterBorder = true;
    private boolean mShowRim = true;

    private int mReadingPrecision;

    private float mTargetValue;
    private float mTargetValueNoCaps;
    private float mCurrentValue;

    private RectF mOuterRect;
    private RectF mScaleArc;
    private RectF mNeedleScrew;
    private RectF mTitlePositionRect;
    private RectF mOuterBorder;
    private RectF mInnerRim;
    private RectF mInnerRimBorder;
    private RectF mBackgroundInnerRing;
    private RectF mBackgroundMaskOval;

    private float mNeedleVelocity;
    private float mNeedleAcceleration;
    private long mNeedleLastMoved = -1;

    private float mDensity;

    private Bitmap mMask;
    private int mWidth;
    private int mHeight;
    private boolean mBSizeChangedComplete = false;

    public SolarGauge(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mDensity = getResources().getDisplayMetrics().density;
        //if (!isInEditMode()) {
            readAttrs(context, attrs, defStyle);
            initDrawingTools();
        //}
        setLabelConverter(new SolarGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

    }

    public SolarGauge(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SolarGauge(final Context context) {
        this(context, null, 0);
    }

    public double getScaleStart() {
        return mScaleStartValue;
    }

    public void setScaleStart(float scaleStart) {
        if (scaleStart < 0 || mBiDirectional)
            scaleStart = 0;
        this.mScaleStartValue = scaleStart;
        if (mScaleEndValue < mScaleStartValue)
            mScaleEndValue = mScaleStartValue+1;
        updateSizes();
        invalidate();
    }

    public double getScaleEnd() {
        return mScaleEndValue;
    }

    public void setScaleEnd(float scaleEnd) {
        if (scaleEnd < 0)
            scaleEnd = 0;
        this.mScaleEndValue = scaleEnd;
        if (mScaleStartValue > mScaleStartValue)
            mScaleStartValue = mScaleEndValue-1;
        updateSizes();
        invalidate();
    }

    public boolean getBiDirectional() {
        return mBiDirectional;
    }

    public void setBiDirectional(boolean val) {
        if (mBiDirectional != val) {
            mBiDirectional = val;
            invalidate();
        }
    }

    public void setLeftLed(final boolean val) {
        mLed1Colour.setColor(val ? Color.GREEN : Color.GRAY);
    }

    public void setRightLed(final boolean val) {
        mLed2Colour.setColor(val ? Color.GREEN : Color.GRAY);
    }

    public double getMajorTickPercentOfMax() {
        return mMajorTickPercentOfMax;
    }

    public void setMajorTickPercentOfMax(double val) {
        if (val <= 0 || val > 100)
            throw new IllegalArgumentException("Bad value specified as a major tick step percent.");
        this.mMajorTickPercentOfMax = val;
        updateSizes();
        invalidate();
    }

    public int getMinorTicks() {
        return mMinorTicksPerDivision;
    }

    public void setMinorTicksPerDivision(int minorTicks) {
        this.mMinorTicksPerDivision = minorTicks;
        invalidate();
    }

    public LabelConverter getLabelConverter() {
        return mLabelConverter;
    }

    public void setLabelConverter(LabelConverter labelConverter) {
        this.mLabelConverter = labelConverter;
        invalidate();
    }

    public void clearColoredRanges() {
        mRanges.clear();
        invalidate();
    }

    public void addColoredRange(double begin, double end, int color) {
        if (begin >= end)
            throw new IllegalArgumentException("Incorrect number range specified!");
        if (begin < 0 || begin > 100)
            throw new IllegalArgumentException("Incorrect number range specified!");
        if (end < 0 || end > 100)
            throw new IllegalArgumentException("Incorrect number range specified!");
        mRanges.add(new ColoredRange(color, begin, end));
        invalidate();
    }

    public void setTargetValue(final float value) {
        if (mShowScale) {
            if (mBiDirectional) {
                if (value < -mScaleEndValue) {
                    mTargetValue = -mScaleEndValue;
                } else if (value > mScaleEndValue) {
                    mTargetValue = mScaleEndValue;
                } else {
                    mTargetValue = value;
                }
            } else {
                if (value < mScaleStartValue) {
                    mTargetValue = mScaleStartValue;
                } else if (value > mScaleEndValue) {
                    mTargetValue = mScaleEndValue;
                } else {
                    mTargetValue = value;
                }
            }
        } else {
            mTargetValue = value;
        }
        mTargetValueNoCaps = value;
//        mNeedleInitialized = true;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //Must be this size
            mWidth = widthSize;
        } else {
            mWidth = getSuggestedMinimumWidth ();
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            mHeight = heightSize;
        } else {
            mHeight = getSuggestedMinimumHeight ();
        }

        if (mHeight >= 0 && mWidth >= 0) {
            mWidth = Math.min(mHeight, mWidth);
            mHeight = mWidth;
        } else if (mWidth >= 0) {
            mHeight = mWidth;
        } else if (mHeight >= 0) {
            mWidth = mHeight;
        } else {
            mWidth = 0;
            mHeight = 0;
        }
        //MUST CALL THIS
        setMeasuredDimension(mWidth, mHeight);
        mWidth = mWidth - getPaddingLeft() - getPaddingRight();
        mHeight = mHeight - getPaddingTop() - getPaddingBottom();

    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        mOuterRect = getScaledRect(1);
        mScaleArc = getScaledRect(0.73f);
        mNeedleScrew = getScaledRect(0.06f);
        mOuterBorder = getScaledRect(0.99f);
        mInnerRim = getScaledRect(0.98f);
        mInnerRimBorder = getScaledRect(0.97f);
        mBackgroundInnerRing = getScaledRect(0.92f);
        mBackgroundMaskOval = getScaledRect(1.15f);
        mTitlePositionRect = getScaledRect(mFace == Quadrant.Quarter? .25f :.37f);

        mArcRadius = mScaleArc.width()/2;
        mBSizeChangedComplete = true;
        updateSizes();
    }

    private void updateSizes() {
        if (mBSizeChangedComplete) {
            mMajorTickStepValue = (float) ((mScaleEndValue - mScaleStartValue) * mMajorTickPercentOfMax / 100);
            mMajorTicksLength = mArcRadius * 0.1f;
            mMinorTicksLength = mMajorTicksLength / 2.5f;

            float desiredWidth = mTitlePositionRect.width();
            setTextSizeForWidth(mTitleTextPaint, desiredWidth * 0.5f, mGaugeTitle); // title is half the size of the reading
            String textValue = valueFloatString(mScaleEndValue);
            String textValueWithUnit = textValue;
            if (!TextUtils.isEmpty(mReadingUnit)) {
                textValueWithUnit += " " + mReadingUnit;
            }
            setTextSizeForWidth(mReadingTextPaint, desiredWidth, textValueWithUnit);
        }
    }

    private void DrawStaticBackground(Canvas canvas) {
        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT);

        // debug
//        RectF outerRing = getScaledRect(1);
//        int w = mWidth + getPaddingLeft() + getPaddingRight();
//        int h = mHeight + getPaddingTop() + getPaddingBottom();
//        canvas.drawRect(0, 0, w, h, mTicksPaint);
//        canvas.drawRect(outerRing.left, outerRing.top, outerRing.right, outerRing.bottom, mTestPaint);
        // end debug

        drawRim(canvas);
        drawBackground(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            return;
        }
        DrawStaticBackground(canvas);

        if (mShowScale) {
            drawTicks(canvas);
        }
        drawText(canvas);
        if (mShowLEDs) {
            drawLEDs(canvas);
        }
        drawNeedle(canvas);
        computeCurrentValue();
    }


    private void drawNeedle(Canvas canvas) {
        float radius = mScaleArc.width()/2.1f;

        if (mCurrentValue > mScaleEndValue)
            mCurrentValue = mScaleEndValue;
        float minimumValue = mBiDirectional ? -mScaleEndValue : mScaleStartValue;
        if (mCurrentValue < minimumValue)
            mCurrentValue = minimumValue;
        float angle = mBiDirectional ?  mScaleStartAngle + mAvailableAngle/2 + mCurrentValue * (mAvailableAngle/2) / mScaleEndValue : mScaleStartAngle + (mCurrentValue * (mAvailableAngle) / mScaleEndValue);
        canvas.drawLine(
                (float) (mScaleArc.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * mNeedleScrew.width() * 0.5f),
                (float) (mScaleArc.centerY() - Math.sin(angle / 180 * Math.PI) * mNeedleScrew.width() * 0.5f),
                (float) (mScaleArc.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius)),
                (float) (mScaleArc.centerY() - Math.sin(angle / 180 * Math.PI) * (radius)),
                mNeedlePaint
        );
        canvas.drawCircle(mNeedleScrew.centerX(), mNeedleScrew.centerY(), mNeedleScrew.width()/2, getDefaultNeedleScrewPaint(mNeedleScrew));
        canvas.drawCircle(mNeedleScrew.centerX(), mNeedleScrew.centerY(), mNeedleScrew.width()/2, getDefaultNeedleScrewBorderPaint());

    }

    private void drawTicks(Canvas canvas) {

        if (mBiDirectional){
            float top = mScaleStartAngle + mAvailableAngle/2 ;
            DrawScaleSegment(canvas, top, mScaleEndAngle, true);
            DrawScaleSegment(canvas, mScaleStartAngle, top, false);
        }
        else {
            DrawScaleSegment(canvas, mScaleStartAngle, mScaleEndAngle, true);
        }
    }

    private void DrawScaleSegment(Canvas canvas, float startAngle, float endAngle, boolean clockwise) {

        float availableAngle = mBiDirectional ? mAvailableAngle/2 : mAvailableAngle;
        float majorTickStepAngle = mMajorTickStepValue / (mScaleEndValue - mScaleStartValue) * availableAngle;
        float minorTickStepAngle = majorTickStepAngle / (1 + mMinorTicksPerDivision);

        double rads = majorTickStepAngle * Math.PI / 180;
        float maxLabelSize = (float)Math.sin(rads) * mArcRadius;
        float maxTextSize = mArcRadius * 0.15f;
        String textValue = mBiDirectional ? "-" + String.valueOf((int) mScaleEndValue) : String.valueOf((int) mScaleEndValue);
        setTextSizeForWidth(mTickLabelTextPaint, maxLabelSize, textValue, maxTextSize);
        double curProgress = 0;
        float currentAngle = clockwise ? startAngle : endAngle;
        while (currentAngle <= endAngle && currentAngle >= startAngle) {
            canvas.drawLine(
                    (float) (mOuterRect.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(mArcRadius - mMajorTicksLength /2)),
                    (float) (mOuterRect.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(mArcRadius - mMajorTicksLength /2)),
                    (float) (mOuterRect.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(mArcRadius + mMajorTicksLength /2)),
                    (float) (mOuterRect.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(mArcRadius + mMajorTicksLength /2)),
                    mTicksPaint
            );
            for (int i=1; i<= mMinorTicksPerDivision; i++) {
                float angle = clockwise ? currentAngle + i* minorTickStepAngle : currentAngle - i* minorTickStepAngle;
                if (angle >= endAngle + minorTickStepAngle /2 || angle <= startAngle - minorTickStepAngle /2) {
                    break;
                }
                canvas.drawLine(
                        (float) (mOuterRect.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * mArcRadius),
                        (float) (mOuterRect.centerY() - Math.sin(angle / 180 * Math.PI) * mArcRadius),
                        (float) (mOuterRect.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (mArcRadius - mMinorTicksLength)),
                        (float) (mOuterRect.centerY() - Math.sin(angle / 180 * Math.PI) * (mArcRadius - mMinorTicksLength)),
                        mTicksPaint
                );
            }
            if (mLabelConverter != null) {
                canvas.save();
                canvas.rotate(180 + currentAngle, mOuterRect.centerX(), mOuterRect.centerY());
                float txtX = mOuterRect.centerX() + mArcRadius + mMajorTicksLength /2 + 8;
                float txtY = mOuterRect.centerY();
                canvas.rotate(+90, txtX, txtY);
                canvas.drawText(mLabelConverter.getLabelFor(curProgress, mScaleEndValue), txtX, txtY, mTickLabelTextPaint);
                canvas.restore();
            }
            if (clockwise) {
                currentAngle += majorTickStepAngle;
                curProgress += mMajorTickStepValue;
            }
            else {
                currentAngle -= majorTickStepAngle;
                curProgress -= mMajorTickStepValue;
            }
        }
        mColorLinePaint.setColor(mDefaultColor);
        canvas.drawArc(mScaleArc, 180 + startAngle, availableAngle, false, mColorLinePaint);
        for (ColoredRange range: mRanges) {
            mColorLinePaint.setColor(range.getColor());
            double start = clockwise ? 180 + startAngle + (range.getBegin() * availableAngle / 100) : 180 + endAngle - (range.getEnd() * availableAngle / 100);
            double sweep = (range.getEnd() - range.getBegin()) * availableAngle / 100;
            canvas.drawArc(mScaleArc, (float) start, (float) sweep, false, mColorLinePaint);
        }
    }

    private void drawRim(final Canvas canvas) {
        if (mShowOuterShadow) {
            canvas.drawOval(mOuterRect, getDefaultOuterShadowPaint(mOuterRect));
        }
        if (mShowOuterBorder) {
            canvas.drawOval(mOuterBorder, getDefaultOuterBorderPaint());
        }
        if (mShowRim) {
            canvas.drawOval(mInnerRim, getDefaultInnerRimPaint(mInnerRim));
            canvas.drawOval(mInnerRim, getDefaultInnerRimBorderLightPaint());
            canvas.drawOval(mInnerRimBorder, getDefaultInnerRimBorderDarkPaint());
        }
    }
    public Paint getDefaultOuterShadowPaint(RectF outer) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(outer.centerX(), outer.centerY(), outer.width() / 2.0f, OUTER_SHADOW_COLORS, OUTER_SHADOW_POS,
                Shader.TileMode.MIRROR));
        return paint;
    }

    private Paint getDefaultOuterBorderPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(245, 0, 0, 0));
        return paint;
    }

    private Paint getDefaultInnerRimPaint(RectF innerRim) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(innerRim.left, innerRim.top, innerRim.left, innerRim.bottom, new int[]{
                Color.argb(255, 68, 73, 80), Color.argb(255, 91, 97, 105), Color.argb(255, 178, 180, 183), Color.argb(255, 188, 188, 190),
                Color.argb(255, 84, 90, 100), Color.argb(255, 137, 137, 137)}, new float[]{0, 0.1f, 0.2f, 0.4f, 0.8f, 1},
                Shader.TileMode.CLAMP));
        return paint;
    }

    private Paint getDefaultInnerRimBorderLightPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(100, 255, 255, 255));
        paint.setStrokeWidth(0.005f);
        return paint;
    }

    private Paint getDefaultInnerRimBorderDarkPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(100, 81, 84, 89));
        paint.setStrokeWidth(0.005f);
        return paint;
    }

    private void drawBackground(Canvas canvas) {
        if (mAvailableAngle > 180) {
//            canvas.drawCircle(mOuterBorder.centerX(), mOuterBorder.centerY(), mOuterBorder.width() / 2, mBackgroundPaint);
//
            canvas.drawCircle(mBackgroundInnerRing.centerX(), mBackgroundInnerRing.centerY(), mBackgroundInnerRing.width() / 2, mBackgroundInnerPaint);

            Bitmap mask = Bitmap.createScaledBitmap(mMask, (int) mBackgroundMaskOval.width(), (int) mBackgroundMaskOval.height(), true);
            canvas.drawBitmap(mask, mBackgroundMaskOval.left, mBackgroundMaskOval.top, mMaskPaint);
        }
        else {
            canvas.drawArc(mOuterRect, 180, 180, true, mBackgroundPaint);
            canvas.drawArc(mBackgroundInnerRing, 180, 180, true, mBackgroundInnerPaint);
            Bitmap mask = Bitmap.createScaledBitmap(mMask, (int) mBackgroundMaskOval.width(), (int) mBackgroundMaskOval.height() / 2, true);
            canvas.drawBitmap(mask, mBackgroundMaskOval.centerX() - mBackgroundMaskOval.width() / 2, mBackgroundMaskOval.centerY() - mBackgroundMaskOval.width() / 2, mMaskPaint);
        }
    }

    private void drawText(final Canvas canvas) {
        if (mGaugeTitle != null && mGaugeTitle.length() > 0) {
            canvas.drawText(mGaugeTitle, mTitlePositionRect.centerX(), mTitlePositionRect.top, mTitleTextPaint);
        }
        if (mShowReading) {
            String textValue = valueFloatString(mTargetValueNoCaps);
            if (!TextUtils.isEmpty(mReadingUnit)) {
                textValue += " " + mReadingUnit;
            }
            canvas.drawText(textValue, mTitlePositionRect.centerX(), mFace==Quadrant.Full ? mTitlePositionRect.bottom : mTitlePositionRect.centerX()*0.9f, mReadingTextPaint);
        }
    }

    private void drawLEDs(final Canvas canvas) {
        float x1 = mOuterRect.centerX() - (mOuterRect.centerX() *.1f);
        float x2 = mOuterRect.centerX() + (mOuterRect.centerX() *.1f);
        float y = mOuterRect.bottom - (mOuterRect.centerY() *.4f);
        float rad = mOuterRect.width() * 0.015f;
        canvas.drawCircle(x1, y, rad, mLed1Colour);
        canvas.drawCircle(x2, y, rad, mLed2Colour);
    }

    private RectF getScaledRect(float factor) {
        final float scale = Math.min(mWidth, mHeight);
        RectF oval = new RectF(0, 0, scale*factor, scale*factor);
        oval.offset((mWidth - oval.width()) / 2 + getPaddingLeft(), (mHeight - oval.height()) / 2 + getPaddingTop());
        return oval;
    }

    private void computeCurrentValue() {
        if (!(Math.abs(mCurrentValue - mTargetValue) > 0.01f)) {
            return;
        }

        if (-1 != mNeedleLastMoved) {
            final float time = (System.currentTimeMillis() - mNeedleLastMoved) / 1000.0f;
            final float direction = Math.signum(mNeedleVelocity);
            if (Math.abs(mNeedleVelocity) < 90.0f) {
                mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
            } else {
                mNeedleAcceleration = 0.0f;
            }

            mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
            mCurrentValue += mNeedleVelocity * time;
            mNeedleVelocity += mNeedleAcceleration * time;

            if ((mTargetValue - mCurrentValue) * direction < 0.01f * direction) {
                mCurrentValue = mTargetValue;
                mNeedleVelocity = 0.0f;
                mNeedleAcceleration = 0.0f;
                mNeedleLastMoved = -1L;
            } else {
                mNeedleLastMoved = System.currentTimeMillis();
            }

            invalidate();

        } else {
            mNeedleLastMoved = System.currentTimeMillis();
            computeCurrentValue();
        }
    }

    private static void setTextSizeForWidth(Paint paint, float desiredWidth, String text, float maxFontSize) {

        final float testTextSize = 48f;
        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();
        desiredTextSize = desiredTextSize > maxFontSize ? maxFontSize : desiredTextSize;
        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    private static void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {

        final float testTextSize = 48f;
        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();
        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    private String valueFloatString(final double value) {
        String fmt = String.format("%%.%df", mReadingPrecision);
        return String.format(fmt, value);
    }

    @SuppressWarnings("NewApi")
    private void initDrawingTools() {
        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        mAvailableAngle = mScaleEndAngle - mScaleStartAngle;
        if (mAvailableAngle > 180) {
            mFace = Quadrant.Full;
        }
        else if (mAvailableAngle <= 90) {
           mFace = Quadrant.Quarter;
        }
        else {
            mFace = Quadrant.Half;
        }

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.rgb(127, 127, 127));

        mTestPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTestPaint.setStyle(Paint.Style.FILL);
        mTestPaint.setColor(Color.GREEN);

        mBackgroundInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundInnerPaint.setStyle(Paint.Style.FILL);
        mBackgroundInnerPaint.setColor(Color.rgb(150, 150, 150));

        mTickLabelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTickLabelTextPaint.setColor(mTickLabelColor);
        mTickLabelTextPaint.setTextAlign(Paint.Align.CENTER);
        mTickLabelTextPaint.setLinearText(true);

        mTitleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitleTextPaint.setColor(mTitleColor);
        mTitleTextPaint.setTextAlign(mFace==Quadrant.Quarter ? Paint.Align.LEFT :Paint.Align.CENTER);

        mReadingTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReadingTextPaint.setColor(mReadingColor);
        mReadingTextPaint.setTextAlign(mFace==Quadrant.Quarter ? Paint.Align.LEFT :Paint.Align.CENTER);

        mMask = BitmapFactory.decodeResource(getResources(), R.drawable.spot_mask);
        mMask = Bitmap.createBitmap(mMask, 0, 0, mFace == Quadrant.Quarter ? mMask.getWidth() /2 : mMask.getWidth(), mFace == Quadrant.Full ? mMask.getHeight()  : mMask.getHeight() /2);

        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskPaint.setDither(true);

        mTicksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTicksPaint.setStrokeWidth(3.0f);
        mTicksPaint.setStyle(Paint.Style.STROKE);
        mTicksPaint.setColor(mDefaultColor);

        mColorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorLinePaint.setStyle(Paint.Style.STROKE);
        mColorLinePaint.setStrokeWidth(5);
        mColorLinePaint.setColor(mDefaultColor);

        mNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNeedlePaint.setStrokeWidth(5);
        mNeedlePaint.setStyle(Paint.Style.STROKE);
        mNeedlePaint.setColor(Color.argb(200, 255, 0, 0));

        mLed1Colour = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLed1Colour.setColor(Color.GRAY);

        mLed2Colour = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLed2Colour.setColor(Color.GRAY);
    }

    private Paint getDefaultNeedleScrewPaint(RectF smallOval) {
        if (mNeedleScrewPaint == null) {
            mNeedleScrewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mNeedleScrewPaint.setShader(new RadialGradient(smallOval.centerX(), smallOval.centerY(), smallOval.width() / 2, new int[]{Color.rgb(171, 171, 171), Color.WHITE}, new float[]{0.05f, 0.9f}, Shader.TileMode.MIRROR));
        }
        return mNeedleScrewPaint;
    }

    private Paint getDefaultNeedleScrewBorderPaint() {
        if (mNeedleScrewBorderPaint == null) {
            mNeedleScrewBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mNeedleScrewBorderPaint.setStyle(Paint.Style.STROKE);
            mNeedleScrewBorderPaint.setColor(Color.argb(100, 81, 84, 89));
            mNeedleScrewBorderPaint.setStrokeWidth(1.0f);
        }
        return mNeedleScrewBorderPaint;
    }


    private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyle, 0);
        mShowOuterShadow = a.getBoolean(R.styleable.GaugeView_showOuterShadow, true);
        mShowOuterBorder = a.getBoolean(R.styleable.GaugeView_showOuterBorder, true);
        mShowRim = a.getBoolean(R.styleable.GaugeView_showRim, true);
        mShowLEDs = a.getBoolean(R.styleable.GaugeView_showLeds, false);
        mBiDirectional = a.getBoolean(R.styleable.GaugeView_biDirectional, false);

        mShowScale = a.getBoolean(R.styleable.GaugeView_showScale, true);
        setScaleStart(a.getInteger(R.styleable.GaugeView_scaleStartValue, 0));
        setScaleEnd(a.getInteger(R.styleable.GaugeView_scaleEndValue, 100));
        mScaleStartAngle = a.getInteger(R.styleable.GaugeView_scaleStartAngle, -70);
        mScaleEndAngle = a.getInteger(R.styleable.GaugeView_scaleEndAngle, 250);
        setMajorTickPercentOfMax(a.getInteger(R.styleable.GaugeView_majorTickPercentOfMax, 10));
        setMinorTicksPerDivision(a.getInteger(R.styleable.GaugeView_minorTicksPerDivision, 2));

        mShowReading = a.getBoolean(R.styleable.GaugeView_showReading, false);
        mReadingUnit =  a.getString(R.styleable.GaugeView_readingUnit);
        mReadingPrecision = a.getInteger(R.styleable.GaugeView_readingPrecision, 1);
        mGaugeTitle = a.getString(R.styleable.GaugeView_gaugeTitle);

        mTitleColor = a.getColor(R.styleable.GaugeView_titleColor, Color.YELLOW);
        mReadingColor = a.getColor(R.styleable.GaugeView_readingColor, Color.YELLOW);
        mTickLabelColor = a.getColor(R.styleable.GaugeView_tickLabelColor, Color.WHITE);


        a.recycle();
    }

    public static interface LabelConverter {
        String getLabelFor(double progress, double maxProgress);
    }

    public static class ColoredRange {

        private int color;
        private double begin;
        private double end;

        public ColoredRange(int color, double begin, double end) {
            this.color = color;
            this.begin = begin;
            this.end = end;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public double getBegin() {
            return begin;
        }

        public void setBegin(double begin) {
            this.begin = begin;
        }

        public double getEnd() {
            return end;
        }

        public void setEnd(double end) {
            this.end = end;
        }
    }

    public enum Quadrant {
        Full,
        Half,
        Quarter;
    }

}
