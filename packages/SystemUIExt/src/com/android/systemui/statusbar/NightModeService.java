/*
 * Copyright (C) 2015 The SudaMod Project
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

package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.graphics.Color;
import android.os.SystemProperties;

import com.android.systemui.SystemUI;

public class NightModeService extends SystemUI {

    static final String TAG = "NightModeService";

    private final Handler mHandler = new Handler();

    private int mNightModeColor;
    private int mNightMode;

    private final Receiver m = new Receiver();
    private int level;
    private int trigger_level;

    private LayoutParams mParams;
    private View view;
    private WindowManager localWindowManager;

    private String trueVersion = SystemProperties.get("ro.modversion");

    public void start() {
        mParams = new WindowManager.LayoutParams();
        ContentObserver obs = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                UpdateSettings();
            }
        };
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.NIGHT_MODE_COLOR),
                false, obs, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.NIGHT_MODE),
                false, obs, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.POWER_SAVE_SETTINGS_TRIGGER_LEVEL),
                false, obs, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL),
                false, obs, UserHandle.USER_ALL);
        UpdateSettings();
        m.init();
    }

    public void ScreenviewInit() {
        localWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mParams.type = LayoutParams.TYPE_SYSTEM_OVERLAY;
        mParams.flags = 280;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.x = 0;
        mParams.y = 0;
        mParams.width = LayoutParams.FILL_PARENT;
        mParams.height = LayoutParams.FILL_PARENT;
        view = new View(mContext);
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
    }

    public void UpdateUI(int v) {
        if (view != null) {
            localWindowManager.removeView(view);
            view = null;
        }
        if (v == 0) return;
        ScreenviewInit();
        switch(v) {
            case 1:
                view.setBackgroundColor(Color.argb(150, 0, 0, 0));
                break;
            case 2:
                view.setBackgroundColor(Color.argb(100, 255, 0, 0));
                break;
            case 3:
                view.setBackgroundColor(Color.argb(60, 255, 255, 0));
                break;
        }
        localWindowManager.addView(view, mParams);
        }

    private void UpdateSettings() {

        mNightModeColor = Settings.Global.getInt(mContext.getContentResolver(),
             Settings.Global.NIGHT_MODE_COLOR, 1);

        mNightMode = Settings.Global.getInt(mContext.getContentResolver(),
             Settings.Global.NIGHT_MODE, 0);

        UpdateUI( trueVersion.startsWith("SM") && mNightMode == 1 ? mNightModeColor : 0);

    }

    private final class Receiver extends BroadcastReceiver {

        public void init() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            mContext.registerReceiver(this, filter, null, mHandler);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
             if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                level = intent.getIntExtra("level", 0);
                trigger_level = Settings.Global.getInt(mContext.getContentResolver(),
                                   Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL, 15);
                Settings.System.putInt(mContext.getContentResolver(),
                       Settings.System.POWER_SAVE_SETTINGS_TRIGGER_LEVEL, level > trigger_level ? 1 : 0);
            }
        }
    };

}
