package ca.farrelltonsolar.uicomponents;

import android.graphics.Color;
import android.widget.BaseAdapter;

public abstract class LabelAdapter extends BaseAdapter {

    public int labelColor() {
        return labelColor;
    }

    public void setlabelColor(int mLabelColor) {
        this.labelColor = mLabelColor;
    }

    private int labelColor = Color.BLACK;


    public enum LabelOrientation {
        HORIZONTAL, VERTICAL
    }
	private double[] mValues;

	void setValues(double[] points) {
		mValues = points;
	}

	@Override
	public int getCount() {
		return mValues.length;
	}

	public Double getItem(int position) {
		return mValues[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}