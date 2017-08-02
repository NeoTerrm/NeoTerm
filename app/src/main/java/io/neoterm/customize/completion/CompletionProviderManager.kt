package io.neoterm.customize.completion

import io.neoterm.customize.completion.provider.FileCompletionProvider
import io.neoterm.customize.completion.provider.ProgramCompletionProvider
import io.neoterm.frontend.completion.CompletionManager
import io.neoterm.frontend.service.NeoService

/**
 * @author kiva
 */
class CompletionProviderManager : NeoService {
    override fun onServiceInit() {
        CompletionManager.registerProvider(FileCompletionProvider())
        CompletionManager.registerProvider(ProgramCompletionProvider())
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
    }
}