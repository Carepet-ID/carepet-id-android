package com.android.carepet.view

import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.widget.EditText

fun EditText.setDrawableEndClickListener(onClick: () -> Unit) {
    setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd: Drawable? = compoundDrawablesRelative[2]
            if (drawableEnd != null && event.rawX >= (right - drawableEnd.bounds.width())) {
                onClick()
                return@setOnTouchListener true
            }
        }
        v.performClick()
        false
    }
}
