/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.widget.ArrayAdapter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ChargeControllers {
    final Object lock = new Object();

    public ChargeControllers() {
        this.devices = new ArrayList<>();
    }

    private List<ChargeController> devices;

    public ChargeController get(int position) {
        synchronized (lock) {
//            if (position >= devices.size()) {
//                position = 0;
//            }
            return devices.get(position);
        }
    }

    public void add(ChargeController cc) {
        synchronized (lock) {
            devices.add(cc);
        }
    }

    public void remove(ChargeController cc) {
        synchronized (lock) {
            devices.remove(cc);
        }
    }

    public int count() {
        synchronized (lock) {
            return devices.size();
        }
    }

    public void clear() {
        synchronized (lock) {
            devices.clear();
        }
    }

    public void load(ArrayAdapter adapter) {
        synchronized (lock) {
            for (ChargeController ct : devices) {
                adapter.add(ct);
            }
        }
    }

    public void load(ArrayList<InetSocketAddress> arr) {
        synchronized (lock) {
            for (ChargeController cc : devices) {
                arr.add(cc.getInetSocketAddress());
            }
        }
    }

}
