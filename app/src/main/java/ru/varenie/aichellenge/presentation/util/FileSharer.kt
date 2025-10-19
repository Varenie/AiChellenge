package ru.varenie.aichellenge.presentation.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import ru.varenie.aichellenge.BuildConfig
import java.io.File

fun saveAndShareText(
    context: Context,
    fileName: String,
    content: String,
    mimeType: String,
    chooserTitle: String
) {
    val file = File(context.cacheDir, fileName)
    file.writeText(content)

    val uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(intent, chooserTitle)
    context.startActivity(chooser)
}