/*
 * Copyright (C) 2022 Acme
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.acme.freeform.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.appcompat.content.res.AppCompatResources
import com.acme.freeform.R

class BarView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {
    private var mBarFlag: View

    init {
        mBarFlag = View(context).apply {
            layoutParams = LayoutParams(36.dp.toInt(), 6.dp.toInt())
            background = TransitionDrawable(
                arrayOf(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.freeform_bar
                    ),
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.freeform_bar_activated
                    )
                )
            )
        }
        this.addView(mBarFlag)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setOnTouchBarListener(l: (v: View, event: MotionEvent) -> Unit) {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    (mBarFlag.background as TransitionDrawable).startTransition(150)
                }
                MotionEvent.ACTION_UP -> {
                    (mBarFlag.background as TransitionDrawable).reverseTransition(150)
                }
            }
            l(v, event)
            true
        }
    }
}