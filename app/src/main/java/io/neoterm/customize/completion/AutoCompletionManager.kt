package io.neoterm.customize.completion

import android.content.Context
import io.neoterm.customize.completion.provider.PathProvider
import io.neoterm.frontend.completion.CompletionManager

/**
 * @author kiva
 */
object AutoCompletionManager {
    fun init(context: Context) {
        CompletionManager.registerProvider(PathProvider())
    }
}