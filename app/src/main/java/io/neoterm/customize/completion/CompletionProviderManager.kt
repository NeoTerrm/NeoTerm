package io.neoterm.customize.completion

import android.content.Context
import io.neoterm.customize.completion.provider.FileCompletionProvider
import io.neoterm.customize.completion.provider.ProgramCompletionProvider
import io.neoterm.frontend.completion.CompletionManager

/**
 * @author kiva
 */
object CompletionProviderManager {
    fun init(context: Context) {
        CompletionManager.registerProvider(FileCompletionProvider())
        CompletionManager.registerProvider(ProgramCompletionProvider())
    }
}