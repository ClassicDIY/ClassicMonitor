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

package com.missouriwindandsolar.classic;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;

/**
* Created by Graham on 03/01/2015.
*/
public class MonthCalendarPagerAdapter extends FragmentPagerAdapter {
    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public MonthCalendarPagerAdapter(FragmentManager fm) {

        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        Log.d(getClass().getName(), String.format("getItem DayLogCalendar position: %d thread is %s", position, Thread.currentThread().getName()));
        return DayLogCalendar.newInstance(position);
    }

    @Override
    public int getCount() {
        return 12;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

}
