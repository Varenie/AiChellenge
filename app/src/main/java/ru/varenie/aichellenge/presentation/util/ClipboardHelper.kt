package ru.varenie.aichellenge.presentation.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun copyToClipboard(context: Context, content: String, label: String = "Chat History") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, content)
    clipboard.setPrimaryClip(clip)
}
