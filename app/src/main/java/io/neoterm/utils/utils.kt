package io.neoterm.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import io.neoterm.R
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.floating.TerminalDialog
import java.io.File
import java.nio.file.Files
import java.text.DecimalFormat

class RangedInt(private val number: Int, private val range: IntRange) {
  fun inc() = (number + 1).takeIf { range.contains(it) } ?: 0
  fun dec() = (number - 1).takeIf { range.contains(it) } ?: range.last
}

fun Long.formatSizeInKB(): String {
  val decimalFormat = DecimalFormat("####.00");
  if (this < 1024) {
    return "$this KB"
  } else if (this < 1024 * 1024) {
    val parsedSize: Float = this / 1024f
    return decimalFormat.format(parsedSize) + " MB"
  } else if (this < 1024 * 1024 * 1024) {
    val parsedSize: Float = this / 1024f / 1024f
    return decimalFormat.format(parsedSize) + " GB"
  } else if (this < 1024L * 1024 * 1024 * 1024) {
    val parsedSize: Float = this / 1024f / 1024f / 1024f
    return decimalFormat.format(parsedSize) + " TB"
  } else {
    return "$this KB"
  }
}

fun Context.extractAssetsDir(dirName: String, extractDir: String) {
  val assets = this.assets
  assets.list(dirName)?.let {
    it.map { File(extractDir, it) }
      .takeWhile { !it.exists() }
      .forEach { file ->
        assets.open("$dirName/${file.name}").use {
          kotlin.runCatching { Files.copy(it, file.toPath()) }
        }
      }
  }
}

fun Context.runApt(
  subCommand: String, vararg extraArgs: String,
  autoClose: Boolean = true, block: (Result<TerminalDialog>) -> Unit
) = TerminalDialog(this)
  .execute(NeoTermPath.APT_BIN_PATH, arrayOf(NeoTermPath.APT_BIN_PATH, subCommand, *extraArgs))
  .imeEnabled(true)
  .onFinish { dialog, session ->
    val exit = session?.exitStatus ?: 1
    if (exit == 0) {
      if (autoClose) dialog.dismiss()
      block(Result.success(dialog))
    } else {
      dialog.setTitle(getString(R.string.error))
      block(Result.failure(RuntimeException()))
    }
  }
  .show("apt $subCommand")

/**
 * Get a file path from a Uri. This will get the the path for Storage Access
 * Framework Documents, as well as the _data field for the MediaStore and
 * other file-based ContentProviders.
 * @param context The context.
 * @param inUri     The Uri to query.
 */
fun Context.getPathOfMediaUri(inUri: Uri?) = inUri?.let {
  when {
    "content".equals(it.scheme, ignoreCase = true) -> getDataColumn(this, it, null, null)
    "file".equals(it.scheme, ignoreCase = true) -> it.path
    DocumentsContract.isDocumentUri(this, it) -> this.getPathOfDocumentUri(it)
    else -> null
  }
}

private fun Context.getPathOfDocumentUri(uri: Uri) = if (isExternalStorageDocument(uri)) {
  val docId = DocumentsContract.getDocumentId(uri)
  val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
  val type = split[0]
  if ("primary".equals(type, ignoreCase = true)) Environment.getExternalStorageDirectory().toString() + "/" + split[1]
  else "/storage/$type/${split[1]}"  // Temporary workaround for non-primary volumes
} else if (isDownloadsDocument(uri)) {
  val id = DocumentsContract.getDocumentId(uri)
  val contentUri = ContentUris.withAppendedId(
    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
  )
  getDataColumn(this, contentUri, null, null)
} else if (isMediaDocument(uri)) {
  val docId = DocumentsContract.getDocumentId(uri)
  val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
  val contentUri = when (split[0]) {
    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    else -> null
  }
  getDataColumn(this, contentUri!!, "_id=?", arrayOf(split[1]))
} else null

/**
 * Get the value of the data column for this Uri
 */
private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?) =
  context.contentResolver.query(uri, arrayOf("_data"), selection, selectionArgs, null)?.use {
    if (it.moveToFirst()) {
      val columnIndex = it.getColumnIndex("_data").takeIf { it != -1 } ?: return@use null
      it.getString(columnIndex)
    } else null
  }

private fun isExternalStorageDocument(uri: Uri) = "com.android.externalstorage.documents" == uri.authority
private fun isDownloadsDocument(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority
private fun isMediaDocument(uri: Uri) = "com.android.providers.media.documents" == uri.authority
