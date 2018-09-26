package io.neoterm.setup.connections

import android.content.Context
import android.net.Uri

/**
 * @author kiva
 */

open class LocalFileConnection(context: Context, uri: Uri) : OfflineUriConnection(context, uri)
