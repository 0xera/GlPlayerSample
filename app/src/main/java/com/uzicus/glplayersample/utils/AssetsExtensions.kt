package com.uzicus.glplayersample.utils

import android.content.res.AssetManager
import com.google.android.exoplayer2.util.Util
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException

fun AssetManager.loadAsString(assetFileName: String): String {
    var inputStream: InputStream? = null
    return try {
        inputStream = open(assetFileName)
        Util.fromUtf8Bytes(Util.toByteArray(inputStream))
    } catch (e: IOException) {
        throw IllegalStateException(e)
    } finally {
        Util.closeQuietly(inputStream)
    }
}