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

package com.android.systemui.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LockAppUtils {

    static Context ct;
    static String setting = Settings.System.Locked_APP_LIST;


    public LockAppUtils(Context ct) {
        this.ct = ct;
    }

    public static Map<String,Package> parseAppToMap() {

        String appString = Settings.System.getString(ct.getContentResolver(),
                                setting);

        Map<String,Package> map = new HashMap<String,Package>();

        if(TextUtils.isEmpty(appString)) return map;

        final String[] array = TextUtils.split(appString, "\\|");
        for (String item : array) {
            if (TextUtils.isEmpty(item)) {
                continue;
            }
            Package pkg = Package.fromString(item);
            map.put(pkg.name, pkg);
        }
        return map;
    }

    public static void savePackageList(Map<String,Package> map) {

        List<String> settings = new ArrayList<String>();
        for (Package app : map.values()) {
            settings.add(app.toString());
        }
        final String value = TextUtils.join("|", settings);
        Settings.System.putString(ct.getContentResolver(), setting, value);
    }

    public static void removeApp(String packageName, Map<String,Package> map) {
        if (map.remove(packageName) != null) {
            savePackageList(map);
        }
    }

    public static void addApp(String packageName, Map<String,Package> map) {
        Package pkg = map.get(packageName);
        if (pkg == null) {
            pkg = new Package(packageName);
            map.put(packageName, pkg);
            savePackageList(map);
        }
    }

    public static boolean isLockedApp(String packageName, Map<String,Package> map) {
        if (map.get(packageName) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Application class
     */
    public static class Package {
        public String name;
        /**
         * Stores all the application values in one call
         * @param name
         */
        public Package(String name) {
            this.name = name;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name);
            return builder.toString();
        }

        public static Package fromString(String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }

            try {
                Package item = new Package(value);
                return item;
            } catch (NumberFormatException e) {
                return null;
            }
        }

    };

}
