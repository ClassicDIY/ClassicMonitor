package ca.farrelltonsolar.classic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View {

    public static final int SIZE = 300;
    public static final float TOP = 0.0f;
    public static final float LEFT = 0.0f;
    public static final float RIGHT = 1.0f;
    public static final float BOTTOM = 1.0f;
    public static final float CENTER = 0.5f;
    public static final boolean SHOW_OUTER_SHADOW = true;
    public static final boolean SHOW_OUTER_BORDER = true;
    public static final boolean SHOW_OUTER_RIM = true;
    public static final boolean SHOW_INNER_RIM = true;
    public static final boolean SHOW_NEEDLE = true;
    public static final boolean SHOW_LEDS = false;

    public static final boolean SHOW_SCALE = false;
    public static final boolean SHOW_RANGES = true;
    public static final boolean SHOW_READING = false;
    public static final boolean BiDirectional = false;

    public static final float OUTER_SHADOW_WIDTH = 0.005f;
    public static final float OUTER_BORDER_WIDTH = 0.02f;
    public static final float OUTER_RIM_WIDTH = 0.03f;
    public static final float INNER_RIM_WIDTH = 0.04f;
    public static final float INNER_RIM_BORDER_WIDTH = 0.005f;

    public static final float NEEDLE_WIDTH = 0.035f;
    public static final float NEEDLE_HEIGHT = 0.28f;

    public static final float SCALE_POSITION = 0.025f;
    public static final float SCALE_START_VALUE = 0.0f;
    public static final float SCALE_END_VALUE = 100.0f;
    public static final float SCALE_START_ANGLE = 30.0f;
    public static final int SCALE_DIVISIONS = 10;
    public static final int SCALE_SUBDIVISIONS = 5;

    public static final int[] OUTER_SHADOW_COLORS = {Color.argb(40, 255, 254, 187), Color.argb(20, 255, 247, 219),
            Color.argb(5, 255, 255, 255)};
    public static final float[] OUTER_SHADOW_POS = {0.90f, 0.95f, 0.99f};


    public static final float[] RANGE_VALUES = {0.2f, 0.4f, 0.6f, 0.9f, 1.0f}; // percent of scale
    public static final int[] RANGE_COLORS = {Color.CYAN, Color.YELLOW, Color.GREEN, Color.rgb(255, 200, 0), Color.RED};

    public static final int TEXT_SHADOW_COLOR = Color.argb(250, 0, 0, 0);
    public static final int TEXT_VALUE_COLOR = Color.YELLOW;
    public static final int TEXT_TITLE_COLOR = Color.WHITE;
    public static final int TEXT_UNIT_COLOR = Color.WHITE;
    public static final float TEXT_VALUE_SIZE = 0.3f;
    public static final float TEXT_UNIT_SIZE = 0.1f;
    public static final int TEXT_PRECISION = 1;

    // *--------------------------------------------------------------------- *//
    // Customizable properties
    // *--------------------------------------------------------------------- *//

    private boolean mShowOuterShadow;
    private boolean mShowOuterBorder;
    private boolean mShowOuterRim;
    private boolean mShowInnerRim;
    private boolean mShowScale;
    private boolean mShowRanges;
    private boolean mShowNeedle;
    private boolean mShowLEDs;
    private boolean _showReading;
    private boolean _biDirectional;

    private float mOuterShadowWidth;
    private float mOuterBorderWidth;
    private float mOuterRimWidth;
    private float mInnerRimWidth;
    private float mInnerRimBorderWidth;
    private float mNeedleWidth;
    private float mNeedleHeight;

    private float mScalePosition;
    private float mScaleStartValue;
    private float mScaleEndValue;
    private float mScaleStartAngle;
    //    private float[] mRangeValues;
//
//    private int[] mRangeColors;
    private int mDivisions;
    private int mSubdivisions;

    private RectF mOuterShadowRect;
    private RectF mOuterBorderRect;
    private RectF mOuterRimRect;
    private RectF mInnerRimRect;
    private RectF mInnerRimBorderRect;
    private RectF mFaceRect;
    private RectF mScaleRect;

    private Bitmap mBackground;
    private Paint mBackgroundPaint;
    private Paint mOuterShadowPaint;
    private Paint mOuterBorderPaint;
    private Paint mOuterRimPaint;
    private Paint mInnerRimPaint;
    private Paint mInnerRimBorderLightPaint;
    private Paint mInnerRimBorderDarkPaint;
    private Paint mFacePaint;
    private Paint mFaceBorderPaint;
    private Paint mFaceShadowPaint;
    private Paint[] mRangePaints;
    private Paint mNeedleRightPaint;
    private Paint mNeedleLeftPaint;
    private Paint mNeedleScrewPaint;
    private Paint mNeedleScrewBorderPaint;
    private Paint mReadingValuePaint;
    private Paint mTitlePaint;
    private Paint mTextUnitPaint;
    private Paint mLedOffPaint;
    private Paint mLedOnPaint;
    private Paint _led1Colour;
    private Paint _led2Colour;

    private String mReadingValue;
    private String mReadingUnit;
    private int mTextReadingColor;
    private int mTextTitleColor;
    private int mTextUnitColor;
    private int mTextShadowColor;
    private float mTextReadingSize;
    private float mTextUnitSize;
    private int mTextPrecision;
    private float mTextTitleSize;

    private Path mNeedleRightPath;
    private Path mNeedleLeftPath;

    // *--------------------------------------------------------------------- *//

    private float mScaleRotation;
    private float mDivisionValue;
    private float mSubdivisionValue;
    private float mSubdivisionAngle;

    private float mTargetValue;
    private float mTargetValueNoCaps;
    private float mCurrentValue;

    private float mNeedleVelocity;
    private float mNeedleAcceleration;
    private long mNeedleLastMoved = -1;
    private boolean mNeedleInitialized;
    private String _readingTitle = "";
    private int _zoomCount = 0;

    public GaugeView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            readAttrs(context, attrs, defStyle);
            init();

        }
    }

    public GaugeView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeView(final Context context) {
        this(context, null, 0);
    }

    public float getScaleEnd() {
        return mScaleEndValue;
    }

    public void setScaleEnd(float scale) {
        if (scale > 0 && mScaleEndValue != scale) {
            mScaleEndValue = scale;
            initScale();
            drawGauge();
        }
    }

    public boolean getBiDirectional() {
        return _biDirectional;
    }

    public void setBiDirectional(boolean val) {
        if (_biDirectional != val) {
            _biDirectional = val;
            initScale();
        }
    }

    public void setTitle(String val) {
        _readingTitle = val;
    }

    public void setUnit(String val) {
        mReadingUnit = val;
    }

    public void redrawGauge() {
        drawGauge();
    }

    private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyle, 0);
        mShowOuterShadow = a.getBoolean(R.styleable.GaugeView_showOuterShadow, SHOW_OUTER_SHADOW);
        mShowOuterBorder = a.getBoolean(R.styleable.GaugeView_showOuterBorder, SHOW_OUTER_BORDER);
        mShowOuterRim = a.getBoolean(R.styleable.GaugeView_showOuterRim, SHOW_OUTER_RIM);
        mShowInnerRim = a.getBoolean(R.styleable.GaugeView_showInnerRim, SHOW_INNER_RIM);
        mShowNeedle = a.getBoolean(R.styleable.GaugeView_showNeedle, SHOW_NEEDLE);
        mShowLEDs = a.getBoolean(R.styleable.GaugeView_showLeds, SHOW_LEDS);
        mShowScale = a.getBoolean(R.styleable.GaugeView_showScale, SHOW_SCALE);
        mShowRanges = a.getBoolean(R.styleable.GaugeView_showRanges, SHOW_RANGES);
        _showReading = a.getBoolean(R.styleable.GaugeView_showReading, SHOW_READING);
        _biDirectional = a.getBoolean(R.styleable.GaugeView_biDirectional, BiDirectional);
        _readingTitle = a.getString(R.styleable.GaugeView_readingTitle);

        mOuterShadowWidth = mShowOuterShadow ? a.getFloat(R.styleable.GaugeView_outerShadowWidth, OUTER_SHADOW_WIDTH) : 0.0f;
        mOuterBorderWidth = mShowOuterBorder ? a.getFloat(R.styleable.GaugeView_outerBorderWidth, OUTER_BORDER_WIDTH) : 0.0f;
        mOuterRimWidth = mShowOuterRim ? a.getFloat(R.styleable.GaugeView_outerRimWidth, OUTER_RIM_WIDTH) : 0.0f;
        mInnerRimWidth = mShowInnerRim ? a.getFloat(R.styleable.GaugeView_innerRimWidth, INNER_RIM_WIDTH) : 0.0f;
        mInnerRimBorderWidth = mShowInnerRim ? a.getFloat(R.styleable.GaugeView_innerRimBorderWidth, INNER_RIM_BORDER_WIDTH) : 0.0f;

        mNeedleWidth = a.getFloat(R.styleable.GaugeView_needleWidth, NEEDLE_WIDTH);
        mNeedleHeight = a.getFloat(R.styleable.GaugeView_needleHeight, NEEDLE_HEIGHT);

        mScalePosition = (mShowScale || mShowRanges) ? a.getFloat(R.styleable.GaugeView_scalePosition, SCALE_POSITION) : 0.0f;
        mScaleStartValue = a.getFloat(R.styleable.GaugeView_scaleStartValue, SCALE_START_VALUE);
        mScaleEndValue = a.getFloat(R.styleable.GaugeView_scaleEndValue, SCALE_END_VALUE);
        mScaleStartAngle = a.getFloat(R.styleable.GaugeView_scaleStartAngle, SCALE_START_ANGLE);

        mDivisions = a.getInteger(R.styleable.GaugeView_divisions, SCALE_DIVISIONS);
        mSubdivisions = a.getInteger(R.styleable.GaugeView_subdivisions, SCALE_SUBDIVISIONS);
        mTextShadowColor = a.getColor(R.styleable.GaugeView_textShadowColor, TEXT_SHADOW_COLOR);
//        if (mShowRanges) {
//
//            final int colorsId = a.getResourceId(R.styleable.GaugeView_rangeColors, 0);
//            readRanges(context.getResources(), colorsId);
//        }

        if (_showReading) {
            final int textValueId = a.getResourceId(R.styleable.GaugeView_textReading, 0);
            final String textValue = a.getString(R.styleable.GaugeView_textReading);
            mReadingValue = (0 < textValueId) ? context.getString(textValueId) : (null != textValue) ? textValue : "";

            final int textUnitId = a.getResourceId(R.styleable.GaugeView_textUnit, 0);
            final String textUnit = a.getString(R.styleable.GaugeView_textUnit);
            mReadingUnit = (0 < textUnitId) ? context.getString(textUnitId) : (null != textUnit) ? textUnit : "";
            mTextReadingColor = a.getColor(R.styleable.GaugeView_textReadingColor, TEXT_VALUE_COLOR);
            mTextUnitColor = a.getColor(R.styleable.GaugeView_textUnitColor, TEXT_UNIT_COLOR);
            mTextShadowColor = a.getColor(R.styleable.GaugeView_textShadowColor, TEXT_SHADOW_COLOR);
            mTextPrecision = a.getInteger(R.styleable.GaugeView_textPrecision, TEXT_PRECISION);
            mTextReadingSize = a.getFloat(R.styleable.GaugeView_textReadingSize, TEXT_VALUE_SIZE);
            mTextTitleSize = a.getFloat(R.styleable.GaugeView_textTitleSize, TEXT_VALUE_SIZE);
            mTextUnitSize = a.getFloat(R.styleable.GaugeView_textUnitSize, TEXT_UNIT_SIZE);
            mTextTitleColor = a.getColor(R.styleable.GaugeView_textTitleColor, TEXT_TITLE_COLOR);
        }

        a.recycle();
    }
//
//    private void readRanges(final Resources res, final int colorsId) {
//        float[] ranges = RANGE_VALUES;
//        int[] colors = RANGE_COLORS;
//        if (colorsId > 0) {
//            int i = 0;
//            String[] colorsString = res.getStringArray(R.array.rangeColors);
//            List<Integer> colorList = new ArrayList<Integer>();
//            for (i = 0; i < colorsString.length; i++) {
//                colorList.add(Color.parseColor(colorsString[i]));
//            }
//            colors = new int[colorList.size()];
//            i = 0;
//            for (Integer v : colorList) {
//                colors[i++] = (v != null ? v : 0);
//            }
//        }
//        if (ranges.length != colors.length) {
//            throw new IllegalArgumentException(
//                    "The ranges and colors arrays must have the same length.");
//        }
//        //{16.0f, 25.0f, 40.0f, 100.0f};
//
//        final int length = ranges.length;
//        mRangeValues = new float[length];
//        mRangeColors = new int[length];
//
//        SetRange(ranges, colors, length);
//    }
//
//    private void SetRange(float[] ranges, int[] colors, int length) {
//        float range = mScaleEndValue - mScaleStartValue;
//        for (int i = 0; i < length; i++) {
//            float percent =  ranges[i]/100;
//            mRangeValues[i] = range * percent;
//            mRangeColors[i] = colors[i];
//        }
//    }

    private void init() {
        // TODO Why isn't this working with HA layer?
        // The needle is not displayed although the onDraw() is being triggered by invalidate()
        // calls.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        initDrawingRects();
        initDrawingTools();

        // Compute the scale properties
        if (mShowRanges) {
            initScale();
        }
    }

    public void initDrawingRects() {
        // The drawing area is a rectangle of width 1 and height 1,
        // where (0,0) is the top left corner of the canvas.
        // Note that on Canvas X axis points to right, while the Y axis points downwards.
        mOuterShadowRect = new RectF(LEFT, TOP, RIGHT, BOTTOM);

        mOuterBorderRect = new RectF(mOuterShadowRect.left + mOuterShadowWidth, mOuterShadowRect.top + mOuterShadowWidth,
                mOuterShadowRect.right - mOuterShadowWidth, mOuterShadowRect.bottom - mOuterShadowWidth);

        mOuterRimRect = new RectF(mOuterBorderRect.left + mOuterBorderWidth, mOuterBorderRect.top + mOuterBorderWidth,
                mOuterBorderRect.right - mOuterBorderWidth, mOuterBorderRect.bottom - mOuterBorderWidth);

        mInnerRimRect = new RectF(mOuterRimRect.left + mOuterRimWidth, mOuterRimRect.top + mOuterRimWidth, mOuterRimRect.right
                - mOuterRimWidth, mOuterRimRect.bottom - mOuterRimWidth);

        mInnerRimBorderRect = new RectF(mInnerRimRect.left + mInnerRimBorderWidth, mInnerRimRect.top + mInnerRimBorderWidth,
                mInnerRimRect.right - mInnerRimBorderWidth, mInnerRimRect.bottom - mInnerRimBorderWidth);

        mFaceRect = new RectF(mInnerRimRect.left + mInnerRimWidth, mInnerRimRect.top + mInnerRimWidth,
                mInnerRimRect.right - mInnerRimWidth, mInnerRimRect.bottom - mInnerRimWidth);

        mScaleRect = new RectF(mFaceRect.left + mScalePosition, mFaceRect.top + mScalePosition, mFaceRect.right - mScalePosition,
                mFaceRect.bottom - mScalePosition);
    }

    private void initDrawingTools() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setFilterBitmap(true);

        if (mShowOuterShadow) {
            mOuterShadowPaint = getDefaultOuterShadowPaint();
        }
        if (mShowOuterBorder) {
            mOuterBorderPaint = getDefaultOuterBorderPaint();
        }
        if (mShowOuterRim) {
            mOuterRimPaint = getDefaultOuterRimPaint();
        }
        if (mShowInnerRim) {
            mInnerRimPaint = getDefaultInnerRimPaint();
            mInnerRimBorderLightPaint = getDefaultInnerRimBorderLightPaint();
            mInnerRimBorderDarkPaint = getDefaultInnerRimBorderDarkPaint();
        }
        if (mShowRanges) {
            setDefaultScaleRangePaints();
        }
        if (mShowNeedle) {
            setDefaultNeedlePaths();
            mNeedleLeftPaint = getDefaultNeedleLeftPaint();
            mNeedleRightPaint = getDefaultNeedleRightPaint();
            mNeedleScrewPaint = getDefaultNeedleScrewPaint();
            mNeedleScrewBorderPaint = getDefaultNeedleScrewBorderPaint();
        }
        if (_showReading) {
            mReadingValuePaint = getDefaultReadingPaint();
            mTextUnitPaint = getDefaultTextUnitPaint();
        }

        if (mShowLEDs) {
            mLedOffPaint = getDefaultLEDOffPaint();
            mLedOnPaint = getDefaultLEDOnPaint();
            _led1Colour = mLedOffPaint;
            _led2Colour = mLedOffPaint;
        }

        mTitlePaint = getDefaultTitlePaint();
        mFacePaint = getDefaultFacePaint();
        mFaceBorderPaint = getDefaultFaceBorderPaint();
        mFaceShadowPaint = getDefaultFaceShadowPaint();
    }

    public Paint getDefaultOuterShadowPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(CENTER, CENTER, mOuterShadowRect.width() / 2.0f, OUTER_SHADOW_COLORS, OUTER_SHADOW_POS,
                TileMode.MIRROR));
        return paint;
    }

    private Paint getDefaultOuterBorderPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(245, 0, 0, 0));
        return paint;
    }

    public Paint getDefaultOuterRimPaint() {
        // Use a linear gradient to create the 3D effect
        final LinearGradient verticalGradient = new LinearGradient(mOuterRimRect.left, mOuterRimRect.top, mOuterRimRect.left,
                mOuterRimRect.bottom, Color.rgb(255, 255, 255), Color.rgb(84, 90, 100), TileMode.REPEAT);

        // Use a Bitmap shader for the metallic style
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.light_alu);
        final BitmapShader aluminiumTile = new BitmapShader(bitmap, TileMode.REPEAT, TileMode.REPEAT);
        final Matrix matrix = new Matrix();
        matrix.setScale(1.0f / bitmap.getWidth(), 1.0f / bitmap.getHeight());
        aluminiumTile.setLocalMatrix(matrix);

        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new ComposeShader(verticalGradient, aluminiumTile, PorterDuff.Mode.MULTIPLY));
        paint.setFilterBitmap(true);
        return paint;
    }

    private Paint getDefaultInnerRimPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(mInnerRimRect.left, mInnerRimRect.top, mInnerRimRect.left, mInnerRimRect.bottom, new int[]{
                Color.argb(255, 68, 73, 80), Color.argb(255, 91, 97, 105), Color.argb(255, 178, 180, 183), Color.argb(255, 188, 188, 190),
                Color.argb(255, 84, 90, 100), Color.argb(255, 137, 137, 137)}, new float[]{0, 0.1f, 0.2f, 0.4f, 0.8f, 1},
                TileMode.CLAMP));
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

    public Paint getDefaultFacePaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new RadialGradient(0.5f, 0.5f, mFaceRect.width() / 2, new int[]{Color.rgb(50, 132, 206), Color.rgb(36, 89, 162),
                Color.rgb(27, 59, 131)}, new float[]{0.5f, 0.96f, 0.99f}, TileMode.MIRROR));
        return paint;
    }

    public Paint getDefaultFaceBorderPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(100, 81, 84, 89));
        paint.setStrokeWidth(0.005f);
        return paint;
    }

    public Paint getDefaultFaceShadowPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new RadialGradient(0.5f, 0.5f, mFaceRect.width() / 2.0f, new int[]{Color.argb(60, 40, 96, 170),
                Color.argb(80, 15, 34, 98), Color.argb(120, 0, 0, 0), Color.argb(140, 0, 0, 0)},
                new float[]{0.60f, 0.85f, 0.96f, 0.99f}, TileMode.MIRROR));
        return paint;
    }

    public void setDefaultNeedlePaths() {
        final float x = 0.5f, y = 0.5f;
        mNeedleLeftPath = new Path();
        mNeedleLeftPath.moveTo(x, y);
        mNeedleLeftPath.lineTo(x - mNeedleWidth, y);
        mNeedleLeftPath.lineTo(x, y - mNeedleHeight);
        mNeedleLeftPath.lineTo(x, y);
        mNeedleLeftPath.lineTo(x - mNeedleWidth, y);

        mNeedleRightPath = new Path();
        mNeedleRightPath.moveTo(x, y);
        mNeedleRightPath.lineTo(x + mNeedleWidth, y);
        mNeedleRightPath.lineTo(x, y - mNeedleHeight);
        mNeedleRightPath.lineTo(x, y);
        mNeedleRightPath.lineTo(x + mNeedleWidth, y);
    }

    public Paint getDefaultNeedleLeftPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(176, 10, 19));
        return paint;
    }

    public Paint getDefaultNeedleRightPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(252, 18, 30));
        paint.setShadowLayer(0.01f, 0.005f, -0.005f, Color.argb(127, 0, 0, 0));
        return paint;
    }

    public Paint getDefaultNeedleScrewPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new RadialGradient(0.5f, 0.5f, 0.07f, new int[]{Color.rgb(171, 171, 171), Color.WHITE}, new float[]{0.05f,
                0.9f}, TileMode.MIRROR));
        return paint;
    }

    public Paint getDefaultLEDOnPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new RadialGradient(0.5f, 0.5f, 0.07f, new int[]{Color.rgb(171, 171, 171), Color.GREEN}, new float[]{0.05f,
                0.9f}, TileMode.MIRROR));
        return paint;
    }

    public Paint getDefaultLEDOffPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new RadialGradient(0.5f, 0.5f, 0.07f, new int[]{Color.rgb(171, 171, 171), Color.GRAY}, new float[]{0.05f,
                0.9f}, TileMode.MIRROR));
        return paint;
    }

    public Paint getDefaultNeedleScrewBorderPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(100, 81, 84, 89));
        paint.setStrokeWidth(0.005f);
        return paint;
    }

    public void setDefaultScaleRangePaints() {
        final int length = RANGE_COLORS.length;
        mRangePaints = new Paint[length];
        for (int i = 0; i < length; i++) {
            mRangePaints[i] = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            mRangePaints[i].setColor(RANGE_COLORS[i]);
            mRangePaints[i].setStyle(Paint.Style.FILL);
            mRangePaints[i].setStrokeWidth(0.005f);
            mRangePaints[i].setTextSize(0.05f);
            mRangePaints[i].setTypeface(Typeface.SANS_SERIF);
            mRangePaints[i].setTextAlign(Align.CENTER);
            mRangePaints[i].setShadowLayer(0.005f, 0.002f, 0.002f, mTextShadowColor);
        }
    }


    private Paint getDefaultTitlePaint() {
        final Paint paint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setColor(mTextTitleColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0.001f);
        paint.setTextSize(mTextTitleSize);
        paint.setTextAlign(Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setShadowLayer(0.01f, 0.002f, 0.002f, mTextShadowColor);
        return paint;
    }


    public Paint getDefaultReadingPaint() {
        final Paint paint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setColor(mTextReadingColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0.005f);
        paint.setTextSize(mTextReadingSize);
        paint.setTextAlign(Align.CENTER);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setShadowLayer(0.01f, 0.002f, 0.002f, mTextShadowColor);
        return paint;
    }

    public Paint getDefaultTextUnitPaint() {
        final Paint paint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setColor(mTextUnitColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0.005f);
        paint.setTextSize(mTextUnitSize);
        paint.setTextAlign(Align.CENTER);
        paint.setShadowLayer(0.01f, 0.002f, 0.002f, mTextShadowColor);
        return paint;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        final Bundle bundle = (Bundle) state;
        final Parcelable superState = bundle.getParcelable("superState");
        super.onRestoreInstanceState(superState);

        mNeedleInitialized = bundle.getBoolean("needleInitialized");
        mNeedleVelocity = bundle.getFloat("needleVelocity");
        mNeedleAcceleration = bundle.getFloat("needleAcceleration");
        mNeedleLastMoved = bundle.getLong("needleLastMoved");
        mCurrentValue = bundle.getFloat("currentValue");
        mTargetValue = bundle.getFloat("targetValue");
    }

    private void initScale() {

        mDivisionValue = (mScaleEndValue - mScaleStartValue) / mDivisions;
        if (_biDirectional) {
            mDivisionValue *= 2;
            mScaleRotation = 0;
        } else {
            mScaleRotation = (mScaleStartAngle + 180) % 360;
        }
        mSubdivisionValue = mDivisionValue / mSubdivisions;
        mSubdivisionAngle = (360 - 2 * mScaleStartAngle) / (mDivisions * mSubdivisions);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final Bundle state = new Bundle();
        state.putParcelable("superState", superState);
        state.putBoolean("needleInitialized", mNeedleInitialized);
        state.putFloat("needleVelocity", mNeedleVelocity);
        state.putFloat("needleAcceleration", mNeedleAcceleration);
        state.putLong("needleLastMoved", mNeedleLastMoved);
        state.putFloat("currentValue", mCurrentValue);
        state.putFloat("targetValue", mTargetValue);
        return state;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int chosenWidth = chooseDimension(widthMode, widthSize);
        final int chosenHeight = chooseDimension(heightMode, heightSize);
        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    private int chooseDimension(final int mode, final int size) {
        switch (mode) {
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.EXACTLY:
                return size;
            case View.MeasureSpec.UNSPECIFIED:
            default:
                return getDefaultDimension();
        }
    }

    private int getDefaultDimension() {
        return SIZE;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        drawGauge();
    }

    private void drawGauge() {
        if (isInEditMode()) {
            return;
        }
        if (null != mBackground) {
            // Let go of the old background
            mBackground.recycle();
        }
        // Create a new background according to the new width and height
        int h = getHeight();

        if (h == 0)
            return;
        mBackground = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(mBackground);
        final float scale = Math.min(getWidth(), getHeight());
        canvas.scale(scale, scale);
        canvas.translate((scale == getHeight()) ? ((getWidth() - scale) / 2) / scale : 0
                , (scale == getWidth()) ? ((getHeight() - scale) / 2) / scale : 0);

        drawRim(canvas);
        drawFace(canvas);

        if (mShowRanges) {
            drawScale(canvas);
        }

    }


    @Override
    protected void onDraw(final Canvas canvas) {
        drawBackground(canvas);

        final float scale = Math.min(getWidth(), getHeight());
        canvas.scale(scale, scale);
        canvas.translate((scale == getHeight()) ? ((getWidth() - scale) / 2) / scale : 0
                , (scale == getWidth()) ? ((getHeight() - scale) / 2) / scale : 0);

        if (_showReading) {
            drawReading(canvas);
        }

        if (_readingTitle != null && _readingTitle.length() > 0) {
            drawTitle(canvas);
        }
        if (mShowNeedle) {
            drawNeedle(canvas);
        }
        if (mShowLEDs) {
            drawLEDs(canvas);
        }
        computeCurrentValue();
    }

    private void drawBackground(final Canvas canvas) {
        if (null == mBackground) {
            //Logger.log.error("Background not created.");
        } else {
            canvas.drawBitmap(mBackground, 0, 0, mBackgroundPaint);
        }
    }

    private void drawRim(final Canvas canvas) {
        if (mShowOuterShadow) {
            canvas.drawOval(mOuterShadowRect, mOuterShadowPaint);
        }
        if (mShowOuterBorder) {
            canvas.drawOval(mOuterBorderRect, mOuterBorderPaint);
        }
        if (mShowOuterRim) {
            canvas.drawOval(mOuterRimRect, mOuterRimPaint);
        }
        if (mShowInnerRim) {
            canvas.drawOval(mInnerRimRect, mInnerRimPaint);
            canvas.drawOval(mInnerRimRect, mInnerRimBorderLightPaint);
            canvas.drawOval(mInnerRimBorderRect, mInnerRimBorderDarkPaint);
        }
    }

    private void drawFace(final Canvas canvas) {
        // Draw the face gradient
        canvas.drawOval(mFaceRect, mFacePaint);
        // Draw the face border
        canvas.drawOval(mFaceRect, mFaceBorderPaint);
        // Draw the inner face shadow
        canvas.drawOval(mFaceRect, mFaceShadowPaint);
    }

    private void drawReading(final Canvas canvas) {
        String textValue = !TextUtils.isEmpty(mReadingValue) ? mReadingValue : valueFloatString(mTargetValueNoCaps);
        if (!TextUtils.isEmpty(mReadingUnit)) {
            textValue += " " + mReadingUnit;
        }
        final float startX = CENTER;
        final float startY = CENTER + 0.175f;
        canvas.drawText(textValue, startX, startY, mReadingValuePaint);
    }

    private void drawTitle(final Canvas canvas) {
        final float startX = CENTER;
        final float startY = CENTER - 0.175f;
        canvas.drawText(_readingTitle, startX, startY, mTitlePaint);
    }

    private void drawScale(final Canvas canvas) {

        // On canvas, North is 0 degrees, East is 90 degrees, South is 180 etc.
        // We start the scale somewhere South-West so we need to first rotate the canvas.


        float range = mScaleEndValue - mScaleStartValue;
        int scaleDivision = (int) range / mDivisions;
        int totalTicks = mDivisions * mSubdivisions + 1;
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(mScaleRotation, 0.5f, 0.5f);
        if (_biDirectional) {
            totalTicks /= 2;
            totalTicks++;

            for (int i = 0; i < totalTicks; i++) {
                final float y1 = mScaleRect.top;
                final float y2 = y1 + 0.015f; // height of division
                final float y3 = y1 + 0.045f; // height of subdivision

                final float value = getValueForTick(i);
                final Paint paint = getRangePaint(value);
                if (0 == value % scaleDivision) {
                    // Draw a division tick
                    canvas.drawLine(0.5f, y1, 0.5f, y3, paint);
                    // Draw the text 0.15 away from the division tick
                    canvas.drawText(valueString(value), 0.5f, y3 + 0.045f, paint);
                } else {
                    // Draw a subdivision tick
                    canvas.drawLine(0.5f, y1, 0.5f, y2, paint);
                }
                canvas.rotate(mSubdivisionAngle, 0.5f, 0.5f);
            }
            canvas.restore();
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            for (int i = 0; i > -totalTicks; i--) {
                final float y1 = mScaleRect.top;
                final float y2 = y1 + 0.015f; // height of division
                final float y3 = y1 + 0.045f; // height of subdivision

                final float value = getValueForTick(i);
                final Paint paint = getRangePaint(Math.abs(value));
                if (0 == value % scaleDivision) {
                    // Draw a division tick
                    canvas.drawLine(0.5f, y1, 0.5f, y3, paint);
                    // Draw the text 0.15 away from the division tick
                    canvas.drawText(valueString(value), 0.5f, y3 + 0.045f, paint);
                } else {
                    // Draw a subdivision tick
                    canvas.drawLine(0.5f, y1, 0.5f, y2, paint);
                }
                canvas.rotate(-mSubdivisionAngle, 0.5f, 0.5f);
            }
        } else {

            for (int i = 0; i < totalTicks; i++) {
                final float y1 = mScaleRect.top;
                final float y2 = y1 + 0.015f; // height of division
                final float y3 = y1 + 0.045f; // height of subdivision

                final float value = getValueForTick(i);
                final Paint paint = getRangePaint(value);
                if (0 == value % scaleDivision) {
                    // Draw a division tick
                    canvas.drawLine(0.5f, y1, 0.5f, y3, paint);
                    // Draw the text 0.15 away from the division tick
                    canvas.drawText(valueString(value), 0.5f, y3 + 0.045f, paint);
                } else {
                    // Draw a subdivision tick
                    canvas.drawLine(0.5f, y1, 0.5f, y2, paint);
                }
                canvas.rotate(mSubdivisionAngle, 0.5f, 0.5f);
            }
        }
        canvas.restore();
    }

    private String valueString(final float value) {
        return String.format("%d", (int) value);
    }

    private String valueFloatString(final float value) {
        String fmt = String.format("%%.%df", mTextPrecision);
        return String.format(fmt, value);
    }

    private float getValueForTick(final int tick) {
        return tick * (mDivisionValue / mSubdivisions);
    }

    private Paint getRangePaint(final float value) {
        final int length = RANGE_VALUES.length;
        float lowerLimit = -1;
        for (int i = 0; i < length; i++) {
            float upperLimit = mScaleEndValue * RANGE_VALUES[i];
            if (value > lowerLimit && value <= upperLimit) {
                return mRangePaints[i];
            }
            lowerLimit = upperLimit;
        }
        return mRangePaints[RANGE_VALUES.length-1];
    }

    private void drawNeedle(final Canvas canvas) {
        if (mNeedleInitialized) {
            final float angle = getAngleForValue(mCurrentValue);
            // Logger.log.info(String.format("value=%f -> angle=%f", mCurrentValue, angle));

            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.rotate(angle, 0.5f, 0.5f);

            setNeedleShadowPosition(angle);
            canvas.drawPath(mNeedleLeftPath, mNeedleLeftPaint);
            canvas.drawPath(mNeedleRightPath, mNeedleRightPaint);

            canvas.restore();

            // Draw the needle screw and its border
            canvas.drawCircle(0.5f, 0.5f, 0.04f, mNeedleScrewPaint);
            canvas.drawCircle(0.5f, 0.5f, 0.04f, mNeedleScrewBorderPaint);

        }
    }

    private void drawLEDs(final Canvas canvas) {
        canvas.drawCircle(0.463f, 0.83f, 0.02f, _led1Colour);
        canvas.drawCircle(0.527f, 0.83f, 0.02f, _led2Colour);
    }

    private void setNeedleShadowPosition(final float angle) {
        if (angle > 180 && angle < 360) {
            // Move shadow from right to left
            mNeedleRightPaint.setShadowLayer(0, 0, 0, Color.BLACK);
            mNeedleLeftPaint.setShadowLayer(0.01f, -0.005f, 0.005f, Color.argb(127, 0, 0, 0));
        } else {
            // Move shadow from left to right
            mNeedleLeftPaint.setShadowLayer(0, 0, 0, Color.BLACK);
            mNeedleRightPaint.setShadowLayer(0.01f, 0.005f, -0.005f, Color.argb(127, 0, 0, 0));
        }
    }

    private float getAngleForValue(final float value) {
        if (_biDirectional) {
            return (mScaleRotation + (value / mSubdivisionValue) * mSubdivisionAngle) % 360;
        } else {
            return (mScaleRotation + (value / mSubdivisionValue) * mSubdivisionAngle) % 360;
        }
    }

    private void computeCurrentValue() {
        // Logger.log.warn(String.format("velocity=%f, acceleration=%f", mNeedleVelocity,
        // mNeedleAcceleration));

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

    public void setTargetValue(final float value) {
        if (mShowScale || mShowRanges) {
            if (_biDirectional) {
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
        mNeedleInitialized = true;
        invalidate();
    }
    public void setAux1 (final boolean val) {
        _led1Colour = val ? mLedOnPaint : mLedOffPaint;

    }

    public void setAux2 (final boolean val) {
        _led2Colour = val ? mLedOnPaint : mLedOffPaint;
    }
}

