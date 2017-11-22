package com.termux.component.completion

import com.termux.component.completion.provider.FileCompletionProvider
import com.termux.component.completion.provider.ProgramCompletionProvider
import com.termux.frontend.completion.CompletionManager
import com.termux.frontend.component.NeoComponent

/**
 * @author Sam
 */
class CompletionComponent : NeoComponent {
    override fun onServiceInit() {
        CompletionManager.registerProvider(FileCompletionProvider())
        CompletionManager.registerProvider(ProgramCompletionProvider())
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
    }
}
