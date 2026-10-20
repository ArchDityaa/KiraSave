package com.hakira.kirasave.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

enum class SaveCollection { VIDEO, IMAGE, AUDIO }

object MediaStoreSaver {
    fun save(
        context: Context,
        source: File,
        displayName: String,
        mimeType: String,
        collection: SaveCollection
    ): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(context, source, displayName, mimeType, collection)
        } else {
            saveLegacy(context, source, displayName, mimeType, collection)
        }
    }

    private fun saveWithMediaStore(
        context: Context,
        source: File,
        displayName: String,
        mimeType: String,
        collection: SaveCollection
    ): Uri {
        val resolver = context.contentResolver
        val contentUri = when (collection) {
            SaveCollection.VIDEO -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            SaveCollection.IMAGE -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            SaveCollection.AUDIO -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        val relativePath = when (collection) {
            SaveCollection.VIDEO -> "${Environment.DIRECTORY_MOVIES}/KiraSave"
            SaveCollection.IMAGE -> "${Environment.DIRECTORY_PICTURES}/KiraSave"
            SaveCollection.AUDIO -> "${Environment.DIRECTORY_MUSIC}/KiraSave"
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(contentUri, values)
            ?: throw IllegalStateException("Tidak dapat membuat file di galeri.")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: throw IllegalStateException("Tidak dapat menulis file.")
            val finish = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(uri, finish, null, null)
            return uri
        } catch (t: Throwable) {
            runCatching { resolver.delete(uri, null, null) }
            throw t
        }
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(
        context: Context,
        source: File,
        displayName: String,
        mimeType: String,
        collection: SaveCollection
    ): Uri {
        val directoryName = when (collection) {
            SaveCollection.VIDEO -> Environment.DIRECTORY_MOVIES
            SaveCollection.IMAGE -> Environment.DIRECTORY_PICTURES
            SaveCollection.AUDIO -> Environment.DIRECTORY_MUSIC
        }
        val folder = File(
            Environment.getExternalStoragePublicDirectory(directoryName),
            "KiraSave"
        )
        if (!folder.exists() && !folder.mkdirs()) {
            throw IllegalStateException("Folder penyimpanan tidak dapat dibuat.")
        }
        val destination = uniqueFile(folder, displayName)
        source.inputStream().use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(destination.absolutePath),
            arrayOf(mimeType),
            null
        )
        return Uri.fromFile(destination)
    }

    private fun uniqueFile(folder: File, displayName: String): File {
        val base = displayName.substringBeforeLast('.', displayName)
        val extension = displayName.substringAfterLast('.', "")
        var candidate = File(folder, displayName)
        var index = 1
        while (candidate.exists()) {
            val suffix = if (extension.isBlank()) "" else ".$extension"
            candidate = File(folder, "$base ($index)$suffix")
            index++
        }
        return candidate
    }

    fun delete(context: Context, uriString: String?): Boolean {
        val uri = uriString?.let(Uri::parse) ?: return false
        return try {
            if (uri.scheme == "file") {
                uri.path?.let { File(it).delete() } ?: false
            } else {
                context.contentResolver.delete(uri, null, null) > 0
            }
        } catch (_: Throwable) {
            false
        }
    }

    fun shareableUri(context: Context, uri: Uri): Uri {
        return if (uri.scheme == "file") {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(uri.path ?: return uri)
            )
        } else {
            uri
        }
    }

    fun share(context: Context, uriString: String?, mimeType: String, title: String) {
        val uri = uriString?.let(Uri::parse) ?: return
        val shared = shareableUri(context, uri)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, shared)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun open(context: Context, uriString: String?, mimeType: String) {
        val uri = uriString?.let(Uri::parse) ?: return
        val viewUri = shareableUri(context, uri)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(viewUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
