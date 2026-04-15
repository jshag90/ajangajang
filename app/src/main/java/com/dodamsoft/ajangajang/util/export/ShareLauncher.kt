package com.dodamsoft.ajangajang.util.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat

/**
 * Fires Android's share chooser. The image/PDF attachment is optional —
 * text-only fallback still works.
 */
object ShareLauncher {

    fun shareImage(
        context: Context,
        imageUri: Uri,
        text: String,
        chooserTitle: String = "공유하기",
    ) {
        val intent = ShareCompat.IntentBuilder(context)
            .setType("image/png")
            .setStream(imageUri)
            .setText(text)
            .setChooserTitle(chooserTitle)
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun sharePdf(
        context: Context,
        pdfUri: Uri,
        text: String,
        chooserTitle: String = "공유하기",
    ) {
        val intent = ShareCompat.IntentBuilder(context)
            .setType("application/pdf")
            .setStream(pdfUri)
            .setText(text)
            .setChooserTitle(chooserTitle)
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun shareText(
        context: Context,
        text: String,
        chooserTitle: String = "공유하기",
    ) {
        val intent = ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(text)
            .setChooserTitle(chooserTitle)
            .createChooserIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openSaved(context: Context, uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
