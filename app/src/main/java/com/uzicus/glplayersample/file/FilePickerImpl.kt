package com.uzicus.glplayersample.file

import android.provider.OpenableColumns
import android.content.ContentResolver
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.CompletableDeferred

class FilePickerImpl(componentActivity: ComponentActivity) : FilePicker {

    private var resultDeferred: CompletableDeferred<FileInfo>? = null

    private val openDocumentLauncher = componentActivity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val resultDeferred = resultDeferred ?: return@registerForActivityResult

        if (uri == null) {
            resultDeferred.cancel()
        } else {
            val fileInfo = FileInfo(
                uri = uri.toString(),
                fileName = queryName(componentActivity.contentResolver, uri).orEmpty()
            )
            resultDeferred.complete(fileInfo)
        }
    }

    fun detach() {
        openDocumentLauncher.unregister()
    }

    override suspend fun chooseFile(mimeType: String): FileInfo {
        resultDeferred = CompletableDeferred()
        openDocumentLauncher.launch(arrayOf(mimeType))

        val result = resultDeferred?.await()

        return result ?: throw IllegalStateException("result null")
    }

    private fun queryName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getStringOrNull(nameIndex)
        returnCursor.close()

        return name
    }

}