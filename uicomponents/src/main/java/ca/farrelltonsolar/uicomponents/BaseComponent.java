package ca.farrelltonsolar.uicomponents;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Graham on 05/12/2014.
 */

public class BaseComponent extends View {

    protected static final String TAG = BaseComponent.class.getSimpleName();
    private LabelConverter mLabelConverter;
    protected float mDensity = 1.0f;
    protected int mViewWidth;
    protected int mViewHeight;

    public BaseComponent(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mDensity = getResources().getDisplayMetrics().density;
    }

    public BaseComponent(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseComponent(Context context) {
        this(context, null, 0);
    }

    public LabelConverter getLabelConverter() {
        return mLabelConverter;
    }

    public void setLabelConverter(LabelConverter labelConverter) {
        this.mLabelConverter = labelConverter;
        invalidateAll();
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
            mViewWidth = widthSize;
        } else {
            mViewWidth = getSuggestedMinimumWidth();
        }
        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            mViewHeight = heightSize;
        } else {
            mViewHeight = getSuggestedMinimumHeight();
        }
        //MUST CALL THIS
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    protected void invalidateAll() {

    }

    public static interface LabelConverter {
        String getLabelFor(float value, float min, float max);
    }
}
