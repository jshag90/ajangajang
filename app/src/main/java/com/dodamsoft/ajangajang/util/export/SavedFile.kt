package com.dodamsoft.ajangajang.util.export

import android.net.Uri

data class SavedFile(
    val uri: Uri,
    val mimeType: String,
    val displayName: String,
)
