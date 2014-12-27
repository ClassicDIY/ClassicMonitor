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
import android.support.v4.app.ListFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Graham on 26/12/2014.
 */
public class InfoFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
        if (cc != null) {
            Pair[] data = new Pair[5];
            data[0] = new Pair("Name:", cc.deviceName());
            data[1] = new Pair("Type:", cc.deviceType().toString());
            data[2] = new Pair("IP Address:", cc.deviceIpAddress());
            data[3] = new Pair("Unit ID:", cc.unitID());
            data[4] = new Pair("Port:", cc.port());
            InfoListAdapter adapter = new InfoListAdapter(inflater.getContext(), data);
            setListAdapter(adapter);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
