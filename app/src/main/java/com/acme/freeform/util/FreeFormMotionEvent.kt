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

package com.acme.freeform.util

import android.hardware.input.InputManager
import android.view.*
import android.view.Display.DEFAULT_DISPLAY
import android.view.Display.INVALID_DISPLAY
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import java.util.*

class FreeFormMotionEvent {
    private val DEFAULT_SIZE = 1.0f
    private val DEFAULT_META_STATE = 0
    private val DEFAULT_BUTTON_STATE = 0
    private val DEFAULT_PRECISION_X = 1.0f
    private val DEFAULT_PRECISION_Y = 1.0f
    private val DEFAULT_DEVICE_ID = 0
    private val DEFAULT_EDGE_FLAGS = 0
    private val DEFAULT_FLAGS = 0

    /**
     * Builds a MotionEvent and injects it into the event stream.
     *
     * @param inputSource the InputDevice.SOURCE_* sending the input event
     * @param action the MotionEvent.ACTION_* for the event
     * @param downTime the value of the ACTION_DOWN event happened
     * @param when the value of SystemClock.uptimeMillis() at which the event happened
     * @param x x coordinate of event
     * @param y y coordinate of event
     * @param pressure pressure of event
     */
    fun injectMotionEvent(
        inputSource: Int, action: Int, downTime: Long, `when`: Long,
        x: Float, y: Float, pressure: Float, displayId: Int
    ) {
        var mDisplayId = displayId

        val pointerCount = 1
        val pointerProperties: Array<PointerProperties?> =
            arrayOfNulls(pointerCount)
        val pointerCoords: Array<PointerCoords?> = arrayOfNulls(pointerCount)
        for (i in 0 until pointerCount) {
            pointerProperties[i] = PointerProperties()
            pointerProperties[i]?.id = i
            pointerProperties[i]?.toolType = MotionEvent.TOOL_TYPE_FINGER
            pointerCoords[i] = PointerCoords()
            pointerCoords[i]?.x = x
            pointerCoords[i]?.y = y
            pointerCoords[i]?.pressure = pressure
            pointerCoords[i]?.size = DEFAULT_SIZE
        }
        if (mDisplayId == INVALID_DISPLAY
            && inputSource and InputDevice.SOURCE_CLASS_POINTER != 0
        ) {
            mDisplayId = DEFAULT_DISPLAY
        }
        val event: MotionEvent = MotionEvent.obtain(
            downTime, `when`, action, pointerCount,
            pointerProperties, pointerCoords, DEFAULT_META_STATE, DEFAULT_BUTTON_STATE,
            DEFAULT_PRECISION_X, DEFAULT_PRECISION_Y, getInputDeviceId(inputSource),
            DEFAULT_EDGE_FLAGS, inputSource, mDisplayId, DEFAULT_FLAGS
        )
        InputManager.getInstance().injectInputEvent(
            event,
            InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH
        )
    }

    private fun getInputDeviceId(inputSource: Int): Int {
        val devIds = InputDevice.getDeviceIds()
        for (devId in devIds) {
            val inputDev = InputDevice.getDevice(devId)
            if (inputDev.supportsSource(inputSource)) {
                return devId
            }
        }
        return DEFAULT_DEVICE_ID
    }
}