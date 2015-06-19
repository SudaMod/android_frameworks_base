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

package com.android.systemui.statusbar.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkTraffic;

public class LockFlow extends TextView {
    
    Context mContext;

    private int KEY_STRING = 1;

    public LockFlow(Context context) {
        this(context, null);
        mContext = context;
    }

    public LockFlow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public LockFlow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mIntentReceiver, filter, null, getHandler());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                NetworkTraffic.mStart = true;
            } else {
                if (NetworkTraffic.mStart && NetworkTraffic.map.size() > 0) {
                    setText(mContext.getString(R.string.lock_flow) + NetworkTraffic.map.get(KEY_STRING).toString());
                    NetworkTraffic.mStart = false;
                }
            }
        }
    };
}
