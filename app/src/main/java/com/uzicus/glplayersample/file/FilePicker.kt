package com.uzicus.glplayersample.file

interface FilePicker {

    suspend fun chooseFile(mimeType: String = "*/*"): FileInfo

}