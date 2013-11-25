/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import com.android.internal.telephony.PhoneStateIntentReceiver;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.telephony.ServiceState;

import com.android.internal.telephony.TelephonyProperties;

public class AirplaneModeEnabler implements Preference.OnPreferenceChangeListener {

    /**
     * Called when someone clicks on the checkbox preference.
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

}
