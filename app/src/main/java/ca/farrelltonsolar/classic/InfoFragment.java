/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.os.Bundle;
import android.app.ListFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

/**
 * Created by Graham on 26/12/2014.
 */
public class InfoFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
        if (cc != null) {
            Pair[] data = new Pair[10];
            data[0] = new Pair<>(getString(R.string.info_model_title), cc.getModel());
            data[1] = new Pair<>(getString(R.string.info_mac_title), cc.getMacAddress());
            data[2] = new Pair<>(getString(R.string.info_unitid_title), String.format("%04x %04x", (cc.unitID() >> 16) & 0xffff , cc.unitID() & 0xffff).toUpperCase());
            if (MonitorApplication.chargeControllers().getConnectionType() == CONNECTION_TYPE.MODBUS ) {
                data[3] = new Pair<>(getString(R.string.info_ipaddress_title), String.format("%s:%s", cc.deviceIpAddress(), cc.port()));
            }
            else {
                data[3] = new Pair<>(getString(R.string.info_name_title), String.format("%s", cc.deviceName()));
            }
            data[4] = new Pair<>(getString(R.string.info_classic_rev_title), cc.getAppVersion());
            data[5] = new Pair<>(getString(R.string.info_network_rev_title),cc.getNetVersion());
            data[6] = new Pair<>(getString(R.string.info_build_date_title),cc.getBuildDate());
            data[7] = new Pair<>(getString(R.string.info_last_voc_title), String.format(Locale.getDefault(), "%1.1f V", cc.getLastVOC()));
            data[8] = new Pair<>(getString(R.string.info_nominal_battery_voltage), String.format(Locale.getDefault(), "%d V", cc.getNominalBatteryVoltage()));
            data[9] = new Pair<>(getString(R.string.MPPTMode), MonitorApplication.getMpptModeText(cc.getMpptMode()));

            InfoListAdapter adapter = new InfoListAdapter(inflater.getContext(), data);
            setListAdapter(adapter);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
