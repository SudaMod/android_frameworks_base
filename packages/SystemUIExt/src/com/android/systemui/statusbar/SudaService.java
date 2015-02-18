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
import android.content.res.Resources;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.graphics.Color;

import com.android.systemui.SystemUI;

public class SudaService extends SystemUI {

    static final String TAG = "SudaService";

    private final Handler mHandler = new Handler();
    private final Receiver m = new Receiver();

    private int mNightModeColor;
    private int mNightMode;

    private LayoutParams mParams;
    private View view;
    private WindowManager localWindowManager;

    public void start() {
        localWindowManager = (WindowManager) mContext.getSystemService("window");
        mParams = new WindowManager.LayoutParams();
        ContentObserver obs = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                UpdateSettings();
        		m.UpdateUI(mNightMode == 1 ? mNightModeColor : 0);
            }
        };
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.NIGHT_MODE_COLOR),
                false, obs, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.NIGHT_MODE),
                false, obs, UserHandle.USER_ALL);
        UpdateSettings();
        m.init();
        m.UpdateUI(mNightMode == 1 ? mNightModeColor : 0);
    }

    private final class Receiver extends BroadcastReceiver {

        public void init() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            filter.addAction(Intent.ACTION_USER_SWITCHED);
            mContext.registerReceiver(this, filter, null, mHandler);
        }

        public void ScreenviewInit() {
            mParams.type = 2006;
            mParams.flags = 280;
            mParams.format = 1;
            mParams.gravity = 51;
            mParams.x = 0;
            mParams.y = 0;
            mParams.width = -1;
            mParams.height = -1;
            view = new View(mContext);
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
        }

        public void UpdateUI(int v) {
            if (view != null) {
               ((WindowManager)
                 mContext.getSystemService("window")).removeView(view);
            }
            ScreenviewInit();
            switch(v) {
              case 0:
                view.setBackgroundColor(Color.argb(0, 255, 255, 255));
              break;
              case 1:
                view.setBackgroundColor(Color.argb(150, 0, 0, 0));
              break;
              case 2:
                view.setBackgroundColor(Color.argb(100, 255, 0, 0));
              break;
              case 3:
                view.setBackgroundColor(Color.argb(80, 255, 255, 0));
              break;
            }
            localWindowManager.addView(view, mParams);

        }
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateSettings();
        }
    };

    private void UpdateSettings() {

        mNightModeColor = Settings.Global.getInt(mContext.getContentResolver(),
             Settings.Global.NIGHT_MODE_COLOR, 0);

        mNightMode = Settings.Global.getInt(mContext.getContentResolver(),
             Settings.Global.NIGHT_MODE, 0);

    }

}


