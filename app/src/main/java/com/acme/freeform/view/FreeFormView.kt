/*
 * Copyright (C) 2022 Acme
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.acme.freeform.view

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.View.MeasureSpec.getMode
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.cardview.widget.CardView
import com.acme.freeform.R
import com.acme.freeform.util.FreeFormMotionEvent


@SuppressLint("ClickableViewAccessibility")
class FreeFormView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : CardView(context, attributeSet, defStyleAttr) {
    private val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mDefWindowWidth = mWindowManager.currentWindowMetrics.bounds.width()
    private val mDefWindowHeight = mWindowManager.currentWindowMetrics.bounds.height()
    private val mDefWindowDpi = resources.configuration.densityDpi
    private var mFreeFormScale = 1F
    private val mFreeFormWidth get() = (mDefWindowWidth * 0.7 * mFreeFormScale).toInt()
    private val mFreeFormHeight get() = (mFreeFormWidth / 0.625).toInt()
    private val mFreeFormDpi get() = (mDefWindowDpi * 0.7 * mFreeFormScale).toInt()
    private var mFreeFormPackageName: String? = null

    private val mDisplayManager =
        context.getSystemService(DisplayManager::class.java) as DisplayManager
    private val mVirtualDisplay = mDisplayManager.createVirtualDisplay(
        "acme_freeform_$this",
        mFreeFormWidth,
        mFreeFormHeight,
        mFreeFormDpi,
        null,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or
                DisplayManager.VIRTUAL_DISPLAY_FLAG_TRUSTED
    )

    private val mWindowLayoutParams = WindowManager.LayoutParams()

    private val mRootView = LayoutInflater.from(context).inflate(R.layout.view_freeform, this, true)
    private var mTextureView: TextureView = mRootView.findViewById(R.id.textureView)
    private var mImageView: ImageView = mRootView.findViewById(R.id.imageView)
    private var mTopBar: BarView = mRootView.findViewById(R.id.linearLayout_top_bar_container)
    private var mBottomBar: BarView = mRootView.findViewById(R.id.linearLayout_bottom_bar_container)

    init {
        this.apply {
            radius = 24.dp
            elevation = 8.dp
        }

        mTextureView.layoutParams = LinearLayout.LayoutParams(
            mFreeFormWidth,
            mFreeFormHeight
        )
        mImageView.layoutParams = LinearLayout.LayoutParams(
            mFreeFormWidth,
            mFreeFormHeight
        )

        mTextureView.setOnTouchListener { _, event ->
            FreeFormMotionEvent().injectMotionEvent(
                InputDevice.SOURCE_TOUCHSCREEN,
                event.action,
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                event.x,
                event.y,
                event.pressure,
                mVirtualDisplay.display.displayId
            )
            true
        }
        mTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                surface.release()
                return true
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                mVirtualDisplay.surface = Surface(surface)
            }
        }

        mTopBar.apply {
            var downRawX = 0F
            var downRawY = 0F
            var windowLayoutParamsX = 0
            var windowLayoutParamsY = 0
            setOnTouchBarListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downRawX = event.rawX
                        downRawY = event.rawY
                        windowLayoutParamsX = mWindowLayoutParams.x
                        windowLayoutParamsY = mWindowLayoutParams.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mWindowLayoutParams.x =
                            (windowLayoutParamsX + event.rawX - downRawX).toInt()
                        mWindowLayoutParams.y =
                            (windowLayoutParamsY + event.rawY - downRawY).toInt()
                        mWindowManager.updateViewLayout(this@FreeFormView, mWindowLayoutParams)
                    }
                    MotionEvent.ACTION_UP -> {
                        windowLayoutParamsX = mWindowLayoutParams.x
                        windowLayoutParamsY = mWindowLayoutParams.y
                    }
                }
            }
        }
        mBottomBar.apply {
            var downRawY = 0F
            var scale = 0F
            var freeFormHeight = 0
            var tmpScale = 0F

            setOnTouchBarListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downRawY = event.rawY
                        scale = mFreeFormScale
                        freeFormHeight = mFreeFormHeight
                        mImageView.setImageBitmap(mTextureView.bitmap)
                        mTextureView.visibility = View.GONE
                        mImageView.visibility = View.VISIBLE
                    }
                    MotionEvent.ACTION_MOVE -> {
                        tmpScale = scale - (downRawY - event.rawY) / freeFormHeight
                        mFreeFormScale = tmpScale
                        if (tmpScale <= 0.6F) {
                            mFreeFormScale = 0.6F
                            mWindowLayoutParams.alpha = 0.8F
                        }
                        if (tmpScale >= 1.2F) mFreeFormScale = 1.2F

                        mImageView.layoutParams = LinearLayout.LayoutParams(
                            mFreeFormWidth,
                            mFreeFormHeight
                        )
                        mWindowLayoutParams.apply {
                            width = mFreeFormWidth
                            height = (mFreeFormHeight + 48.dp).toInt()
                        }
                        mWindowManager.updateViewLayout(this@FreeFormView, mWindowLayoutParams)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (tmpScale <= 0.6F || tmpScale >= 1.2F) {
                            if (tmpScale >= 1.2F) {
                                mFreeFormPackageName?.apply {
                                    startApp(this)
                                }
                            }
                            removeFreeForm()
                        } else {
                            mTextureView.visibility = View.VISIBLE
                            mImageView.visibility = View.GONE
                            mTextureView.layoutParams = LinearLayout.LayoutParams(
                                mFreeFormWidth,
                                mFreeFormHeight
                            )
                            mVirtualDisplay.resize(mFreeFormWidth, mFreeFormHeight, mFreeFormDpi)
                            mWindowManager.updateViewLayout(this@FreeFormView, mWindowLayoutParams)
                        }
                    }
                }
            }
        }
    }

    fun addFreeForm(packageName: String) {
        //freeform模式
        val WINDOWING_MODE_FREEFORM = 5

        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
        val freeFormWidth = 1080
        val freeFormHeight = 1728
        val options = ActivityOptions.makeBasic()
        val left = mDefWindowWidth / 2 - freeFormWidth / 2
        val top = mDefWindowHeight / 2 - freeFormHeight / 2
        val right = mDefWindowWidth / 2 + freeFormWidth / 2
        val bottom = mDefWindowHeight / 2 + freeFormHeight / 2
        options.launchBounds = Rect(left, top, right, bottom)
        options.setLaunchWindowingMode(WINDOWING_MODE_FREEFORM)
        context.startActivity(intent, options.toBundle())

//        mFreeFormPackageName = packageName
//        startApp(packageName, mVirtualDisplay.display.displayId)
//
//        mWindowLayoutParams.apply {
//            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            width = mFreeFormWidth
//            height = (mFreeFormHeight + 48.dp).toInt()
//            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
//                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
//                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//            format = PixelFormat.RGBA_8888
//            x = 0
//            y = 0
//        }
//
//        try {
//            mWindowManager.addView(this, mWindowLayoutParams)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    private fun removeFreeForm() {
        mWindowManager.removeViewImmediate(this)
        mVirtualDisplay.surface?.release()
        mVirtualDisplay.release()
        mTextureView.surfaceTexture?.release()
    }

    private fun startApp(packageName: String, displayId: Int = -1) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
        if (displayId == -1) {
            context.startActivity(intent)
        } else {
            val options = ActivityOptions.makeBasic()
                .setLaunchDisplayId(displayId)
            context.startActivity(intent, options.toBundle())
        }
    }
}

val Int.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )