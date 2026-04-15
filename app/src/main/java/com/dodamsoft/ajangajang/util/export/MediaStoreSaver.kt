package com.dodamsoft.ajangajang.util.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object MediaStoreSaver {

    private const val RELATIVE_PATH_IMAGE = "Pictures/아장아장"
    private const val RELATIVE_PATH_PDF = "Download/아장아장"
    private const val ALBUM_DIR = "아장아장"

    /**
     * Saves [bitmap] as PNG to MediaStore (API 29+) or app external files (older).
     */
    fun saveImage(context: Context, bitmap: Bitmap, displayName: String): SavedFile {
        val mimeType = "image/png"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, RELATIVE_PATH_IMAGE)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("MediaStore insert failed")
            runCatching {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } ?: error("Cannot open output stream for $uri")
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }.onFailure { e ->
                runCatching { resolver.delete(uri, null, null) }
                throw e
            }
            SavedFile(uri = uri, mimeType = mimeType, displayName = displayName)
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), ALBUM_DIR)
                .apply { mkdirs() }
            val file = File(dir, displayName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            SavedFile(
                uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file),
                mimeType = mimeType,
                displayName = displayName,
            )
        }
    }

    /**
     * Saves a rendered [PdfDocument] to MediaStore Downloads (API 29+) or app external files (older).
     */
    fun savePdf(context: Context, pdf: PdfDocument, displayName: String): SavedFile {
        val mimeType = "application/pdf"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, RELATIVE_PATH_PDF)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("MediaStore insert failed")
            runCatching {
                resolver.openOutputStream(uri)?.use { out ->
                    pdf.writeTo(out)
                } ?: error("Cannot open output stream for $uri")
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }.onFailure { e ->
                runCatching { resolver.delete(uri, null, null) }
                throw e
            }
            SavedFile(uri = uri, mimeType = mimeType, displayName = displayName)
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), ALBUM_DIR)
                .apply { mkdirs() }
            val file = File(dir, displayName)
            FileOutputStream(file).use { out ->
                pdf.writeTo(out)
            }
            SavedFile(
                uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file),
                mimeType = mimeType,
                displayName = displayName,
            )
        }
    }

    /**
     * Creates a URI for an in-memory bitmap stored in app cache, suitable for SHARE intents.
     * Used when the user taps 공유 without explicitly saving first.
     */
    fun cacheImageForShare(context: Context, bitmap: Bitmap, displayName: String): Uri {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, displayName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
