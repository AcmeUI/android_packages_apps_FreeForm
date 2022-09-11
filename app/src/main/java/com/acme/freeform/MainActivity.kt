/*
 * Copyright (C) 2022 Acme
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.acme.freeform

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.acme.freeform.adapter.AppListAdapter
import com.acme.freeform.util.Util
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(toolbar)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val appListAdapter = AppListAdapter(this)
        appListAdapter.mAppList = Util.getAppList(this)

        recyclerView.apply {
            adapter = appListAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            layoutManager = GridLayoutManager(this.context, 4)
            layoutAnimation = LayoutAnimationController(
                AnimationUtils.loadAnimation(
                    context,
                    androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom
                )
            ).apply {
                order = LayoutAnimationController.ORDER_NORMAL
                delay = 0.3F
            }
        }
    }
}