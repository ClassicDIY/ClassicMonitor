package ca.farrelltonsolar.uicomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;


public class SolarGauge extends BaseGauge {


    private Paint mLed1Colour;
    private Paint mLed2Colour;
    private boolean mShowLEDs;


    public SolarGauge(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public SolarGauge(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SolarGauge(final Context context) {
        this(context, null, 0);
    }


    public void setLeftLed(final boolean val) {
        mLed1Colour.setColor(val ? Color.GREEN : Color.GRAY);
    }

    public void setRightLed(final boolean val) {
        mLed2Colour.setColor(val ? Color.GREEN : Color.GRAY);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShowLEDs) {
            drawLEDs(canvas);
        }
    }

    private void drawLEDs(final Canvas canvas) {
        float x1 = mGaugeRect.centerX() - (mGaugeRect.centerX() * .05f);
        float x2 = mGaugeRect.centerX() + (mGaugeRect.centerX() * .05f);
        float y = mGaugeRect.bottom - (mGaugeRect.centerY() * .4f);
        float rad = mGaugeRect.width() * 0.015f;
        canvas.drawCircle(x1, y, rad, mLed1Colour);
        canvas.drawCircle(x2, y, rad, mLed2Colour);
    }

    protected void initDrawingTools() {
        super.initDrawingTools();
        mLed1Colour = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLed1Colour.setColor(Color.GRAY);

        mLed2Colour = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLed2Colour.setColor(Color.GRAY);
    }

    protected void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        super.readAttrs(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gauge, defStyle, 0);
        mShowLEDs = a.getBoolean(R.styleable.Gauge_showLeds, false);
        a.recycle();
    }
}
