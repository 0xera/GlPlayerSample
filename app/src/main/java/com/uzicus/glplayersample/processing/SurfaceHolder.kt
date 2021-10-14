package com.uzicus.glplayersample.processing

import android.view.Surface

interface SurfaceHolder {
    fun clearSurface(surface: Surface?)
    fun setSurface(surface: Surface?)
    fun getSurface(): Surface?
    fun release()
}