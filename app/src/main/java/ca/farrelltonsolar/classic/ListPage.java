package ca.farrelltonsolar.classic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ListPage extends Fragment {

    protected RegisterName _register;
    protected String _title;
    protected String _unitOfMeasure;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int resource = R.layout.reading_layout;
        switch (_register) {
            case EnergyToday:
                break;
            case TotalEnergy:
                break;
        }
        View rootView = inflater.inflate(resource, container, false);
        if (rootView != null) {
            TextView view = (TextView) rootView.findViewById(R.id.Unit);
            view.setText(_unitOfMeasure);
            TextView title = (TextView) rootView.findViewById(R.id.Title);
            title.setText(_title);
        }
        return rootView;
    }
    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        TypedArray a = activity.getBaseContext().obtainStyledAttributes(attrs, R.styleable.SolarFragment, 0, 0);
        if (a != null) {
            try {
                _register = RegisterName.valueOf(a.getString(R.styleable.SolarFragment_register));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (a.hasValue(R.styleable.SolarFragment_reading_title)) {
                _title = a.getString(R.styleable.SolarFragment_reading_title);
            }
            if (a.hasValue(R.styleable.SolarFragment_reading_unit)) {
                _unitOfMeasure = a.getString(R.styleable.SolarFragment_reading_unit);
            }

        }
    }

    public void setValue (float val) {
        TextView view = (TextView) this.getView().findViewById(R.id.Value);
        view.setText(String.valueOf(val));
    }

    public void setValue (String val) {
        TextView view = (TextView) this.getView().findViewById(R.id.Value);
        view.setText(val);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.getUserVisibleHint()) {
            RequestGaugeData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            RequestGaugeData();
        }
    }

    private void RequestGaugeData() {
        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusControl", null, MyApplication.getAppContext(), ModbusMaster.class);
        modbusInitIntent.putExtra("Page", Function.Registers.ordinal());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(modbusInitIntent);
    }

}
