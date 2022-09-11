/*
 * Copyright (C) 2022 Acme
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.acme.freeform.util

import android.content.Context
import android.content.pm.ApplicationInfo
import com.acme.freeform.model.App

class Util {
    companion object {
        fun getAppList(context: Context, filterSystemApp: Boolean = false): MutableList<App> {
            val packageManager = context.packageManager
            val appList = mutableListOf<App>()
            val packages = packageManager.getInstalledPackages(0)
            for (i in packages) {
                if (i.packageName == context.packageName) continue
                if (!filterSystemApp) if ((i.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue
                val appIcon = i.applicationInfo.loadIcon(packageManager)
                val appName = i.applicationInfo.loadLabel(packageManager).toString()
                val packageName = i.packageName
                appList.add(App(appName, packageName, appIcon))
            }
            return appList
        }
    }
}