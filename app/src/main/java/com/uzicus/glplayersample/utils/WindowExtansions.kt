@file:Suppress("unused")

package com.uzicus.glplayersample.utils

import android.graphics.Rect
import android.view.View
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun WindowInsets.toCompat() = WindowInsetsCompat.toWindowInsetsCompat(this)

val WindowInsetsCompat.imeHeight
    get() = getInsets(WindowInsetsCompat.Type.ime()).bottom
val WindowInsets.imeHeight
    get() = toCompat().imeHeight

val WindowInsetsCompat.navigationBarHeight
    get() = getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
val WindowInsets.navigationBarHeight
    get() = toCompat().navigationBarHeight

val WindowInsetsCompat.keyboardHeight
    get() = if (imeHeight != 0) imeHeight - navigationBarHeight else 0
val WindowInsets.keyboardHeight
    get() = toCompat().keyboardHeight

val WindowInsetsCompat.statusBarHeight
    get() = getInsets(WindowInsetsCompat.Type.statusBars()).top
val WindowInsets.statusBarHeight
    get() = toCompat().statusBarHeight

fun View.doOnApplyWindowInsets(
    block: WindowInsetsCompat.(initialPadding: Rect) -> Unit
) {
    val initialPadding = Rect(paddingLeft, paddingTop, paddingRight, paddingBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        block(insets, initialPadding)
        WindowInsetsCompat.CONSUMED
    }

    requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}