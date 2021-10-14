package com.uzicus.glplayersample.utils

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.common.base.Charsets
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException

fun AssetManager.loadAsString(assetFileName: String): String {
    var inputStream: InputStream? = null

    return try {
        inputStream = open(assetFileName)
        String(inputStream.readBytes(), Charsets.UTF_8)
    } catch (e: IOException) {
        throw IllegalStateException(e)
    } finally {
        inputStream?.close()
    }
}

fun AssetManager.loadAsBitmap(assetFileName: String): Bitmap {
    var inputStream: InputStream? = null

    return try {
        inputStream = open(assetFileName)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        throw IllegalStateException(e)
    } finally {
        inputStream?.close()
    }
}