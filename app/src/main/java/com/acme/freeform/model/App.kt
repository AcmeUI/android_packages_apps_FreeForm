/*
 * Copyright (C) 2022 Acme
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.acme.freeform.model

import android.graphics.drawable.Drawable

data class App(
    var appName: String,
    var packageName: String,
    var appIcon: Drawable,
)